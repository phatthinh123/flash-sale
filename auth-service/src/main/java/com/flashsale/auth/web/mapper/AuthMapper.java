package com.flashsale.auth.web.mapper;

import com.flashsale.auth.business.port.AuthPort;
import com.flashsale.auth.web.dto.LoginRequest;
import com.flashsale.auth.web.dto.RegisterRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    AuthPort.RegisterCommand to(RegisterRequest request);

    AuthPort.LoginCommand to(LoginRequest request);
}
