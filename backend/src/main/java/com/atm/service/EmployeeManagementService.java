package com.atm.service;

import com.atm.common.PageResponse;
import com.atm.dto.request.EmployeeCreateRequest;
import com.atm.dto.request.EmployeeUpdateRequest;
import com.atm.dto.response.EmployeeResponse;
import org.springframework.data.domain.Pageable;

public interface EmployeeManagementService {

    PageResponse<EmployeeResponse> list(Pageable pageable);

    EmployeeResponse get(Long employeeId);

    EmployeeResponse create(EmployeeCreateRequest request, String creatorUsername);

    EmployeeResponse update(Long employeeId, EmployeeUpdateRequest request, String editorUsername);

    EmployeeResponse disable(Long employeeId, String editorUsername);

    EmployeeResponse enable(Long employeeId, String editorUsername);
}
