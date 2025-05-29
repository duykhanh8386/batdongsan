package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.response.user.UserAccountDto;

import java.util.List;

public interface UserAccountService {
    List<UserAccountDto> findAll();

    UserAccountDto getById(Integer id);

    UserAccountDto getMyInfo();
}
