package com.feuca.facturacion.service;

import com.feuca.facturacion.dto.request.Auth.LoginRequest;
import com.feuca.facturacion.dto.response.Auth.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
