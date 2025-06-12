package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.dto.response.user.UserAccountDto;
import com.BTL.Springboot.entity.UserAccount;
import com.BTL.Springboot.mapper.UserAccountMapper;
import com.BTL.Springboot.repository.UserAccountRepository;
import com.BTL.Springboot.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserAccountServiceImpl implements UserAccountService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserAccountMapper mapper;

    // Phân quyền
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DIRECTOR') or hasRole('DEPUTY_DIRECTOR')")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DIRECTOR', 'DEPUTY_DIRECTOR')")
    @Override
    public List<UserAccountDto> findAll() {
        log.debug("Fetching all user accounts for role ADMIN");
        return userAccountRepository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public UserAccountDto getById(Integer id) {
        UserAccount userAccount = userAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tồn tại UserAccount có Id: " + id));
        return mapper.toDto(userAccount);
    }

    @Override
    public UserAccountDto getMyInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        UserAccount userAccount = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tồn tại: " + username));

        return mapper.toDto(userAccount);
    }
}
