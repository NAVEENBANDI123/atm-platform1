package com.atm.service.impl;

import com.atm.audit.AuditService;
import com.atm.common.PageResponse;
import com.atm.dto.request.EmployeeCreateRequest;
import com.atm.dto.request.EmployeeUpdateRequest;
import com.atm.dto.response.EmployeeResponse;
import com.atm.entity.EmployeeProfile;
import com.atm.entity.Role;
import com.atm.entity.RoleName;
import com.atm.entity.User;
import com.atm.entity.UserStatus;
import com.atm.entity.UserType;
import com.atm.exception.BadRequestException;
import com.atm.exception.DuplicateResourceException;
import com.atm.exception.ResourceNotFoundException;
import com.atm.mapper.DomainMapper;
import com.atm.repository.EmployeeProfileRepository;
import com.atm.repository.RoleRepository;
import com.atm.repository.UserRepository;
import com.atm.service.EmailService;
import com.atm.service.EmployeeManagementService;
import com.atm.util.IdentifierGenerators;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeManagementServiceImpl implements EmployeeManagementService {

    private static final Set<RoleName> EMPLOYEE_ROLES = Set.of(
            RoleName.ROLE_SUPER_ADMIN,
            RoleName.ROLE_ACCOUNTANT,
            RoleName.ROLE_CASHIER,
            RoleName.ROLE_LOAN_OFFICER,
            RoleName.ROLE_CARD_OFFICER);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdentifierGenerators identifiers;
    private final AuditService auditService;
    private final EmailService emailService;
    private final DomainMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> list(Pageable pageable) {
        return PageResponse.from(userRepository.findByUserType(UserType.EMPLOYEE, pageable)
                .map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse get(Long employeeId) {
        User u = mustEmployee(employeeId);
        return toResponse(u);
    }

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeCreateRequest req, String creatorUsername) {
        if (!EMPLOYEE_ROLES.contains(req.role())) {
            throw new BadRequestException("Role " + req.role() + " is not a valid employee role");
        }
        if (userRepository.existsByUsername(req.username())) {
            throw new DuplicateResourceException("Username already taken");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (userRepository.existsByMobile(req.mobile())) {
            throw new DuplicateResourceException("Mobile already registered");
        }
        Role role = roleRepository.findByName(req.role())
                .orElseThrow(() -> new IllegalStateException("Role missing: " + req.role()));
        User creator = userRepository.findByUsername(creatorUsername).orElse(null);

        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .mobile(req.mobile())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .enabled(true)
                .accountLocked(false)
                .userType(UserType.EMPLOYEE)
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        user = userRepository.save(user);

        EmployeeProfile profile = EmployeeProfile.builder()
                .user(user)
                .employeeCode(identifiers.nextEmployeeCode())
                .designation(req.designation())
                .department(req.department())
                .createdByAdminId(creator == null ? null : creator.getId())
                .build();
        profileRepository.save(profile);

        auditService.recordWithValues("CREATE_EMPLOYEE", "USER", String.valueOf(user.getId()),
                "Created employee " + user.getUsername() + " with role " + req.role(),
                null, req.role().name());

        emailService.send(user.getEmail(),
                "Your employee account has been created",
                "employee-welcome",
                Map.of(
                        "name", req.fullName(),
                        "username", req.username(),
                        "role", req.role().name(),
                        "employeeCode", profile.getEmployeeCode()));
        return toResponse(user);
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long employeeId, EmployeeUpdateRequest req, String editor) {
        User u = mustEmployee(employeeId);
        String before = describe(u);

        if (req.fullName() != null) u.setFullName(req.fullName());
        if (req.email() != null && !req.email().equalsIgnoreCase(u.getEmail())) {
            if (userRepository.existsByEmail(req.email())) {
                throw new DuplicateResourceException("Email already registered");
            }
            u.setEmail(req.email());
        }
        if (req.mobile() != null && !req.mobile().equals(u.getMobile())) {
            if (userRepository.existsByMobile(req.mobile())) {
                throw new DuplicateResourceException("Mobile already registered");
            }
            u.setMobile(req.mobile());
        }
        if (req.role() != null) {
            if (!EMPLOYEE_ROLES.contains(req.role())) {
                throw new BadRequestException("Role " + req.role() + " is not a valid employee role");
            }
            Role role = roleRepository.findByName(req.role())
                    .orElseThrow(() -> new IllegalStateException("Role missing: " + req.role()));
            u.setRoles(new HashSet<>(Set.of(role)));
        }
        userRepository.save(u);

        EmployeeProfile profile = profileRepository.findByUserId(u.getId())
                .orElseGet(() -> EmployeeProfile.builder()
                        .user(u)
                        .employeeCode(identifiers.nextEmployeeCode())
                        .build());
        if (req.designation() != null) profile.setDesignation(req.designation());
        if (req.department() != null) profile.setDepartment(req.department());
        profileRepository.save(profile);

        auditService.recordWithValues("UPDATE_EMPLOYEE", "USER", String.valueOf(u.getId()),
                "Updated by " + editor, before, describe(u));

        return toResponse(u);
    }

    @Override
    @Transactional
    public EmployeeResponse disable(Long employeeId, String editor) {
        User u = mustEmployee(employeeId);
        String before = u.getStatus().name();
        u.setStatus(UserStatus.DISABLED);
        u.setEnabled(false);
        userRepository.save(u);
        auditService.recordWithValues("DISABLE_EMPLOYEE", "USER", String.valueOf(u.getId()),
                "Disabled by " + editor, before, "DISABLED");
        return toResponse(u);
    }

    @Override
    @Transactional
    public EmployeeResponse enable(Long employeeId, String editor) {
        User u = mustEmployee(employeeId);
        String before = u.getStatus().name();
        u.setStatus(UserStatus.ACTIVE);
        u.setEnabled(true);
        userRepository.save(u);
        auditService.recordWithValues("ENABLE_EMPLOYEE", "USER", String.valueOf(u.getId()),
                "Enabled by " + editor, before, "ACTIVE");
        return toResponse(u);
    }

    // -----------------------------------------------------------------

    private User mustEmployee(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Employee", "id", id));
        if (u.getUserType() != UserType.EMPLOYEE) {
            throw new BadRequestException("User " + u.getUsername() + " is not an employee");
        }
        return u;
    }

    private EmployeeResponse toResponse(User user) {
        EmployeeProfile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        return mapper.toEmployeeResponse(user, profile);
    }

    private static String describe(User u) {
        return "username=" + u.getUsername()
                + ";email=" + u.getEmail()
                + ";mobile=" + u.getMobile()
                + ";role=" + u.getRoles().stream().map(r -> r.getName().name()).findFirst().orElse("?")
                + ";status=" + u.getStatus();
    }
}
