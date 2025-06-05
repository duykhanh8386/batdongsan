package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.request.auth.AuthenticationRequest;
import com.BTL.Springboot.dto.request.auth.IntrospectRequest;
import com.BTL.Springboot.dto.request.auth.LogoutRequest;
import com.BTL.Springboot.dto.response.auth.AuthenticationDto;
import com.BTL.Springboot.dto.response.auth.IntrospectDto;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface AuthenticationService {
    AuthenticationDto authenticate(AuthenticationRequest request);

    IntrospectDto introspect(IntrospectRequest request) throws JOSEException, ParseException;

    void logout(LogoutRequest request) throws ParseException, JOSEException;
}
