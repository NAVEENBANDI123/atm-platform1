package com.atm.security;

import com.atm.entity.User;
import com.atm.entity.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private final String userType;
    private final Set<GrantedAuthority> authorities;

    private UserPrincipal(Long id, String username, String passwordHash, boolean enabled,
                          boolean accountNonLocked, String userType, Set<GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
        this.userType = userType;
        this.authorities = authorities;
    }

    public static UserPrincipal from(User user) {
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toSet());
        boolean enabled = user.isEnabled() && user.getStatus() == UserStatus.ACTIVE;
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                enabled,
                !user.isAccountLocked(),
                user.getUserType() == null ? null : user.getUserType().name(),
                authorities);
    }

    public Long getId() {
        return id;
    }

    public String getUserType() {
        return userType;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
