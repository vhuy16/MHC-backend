package com.topick.superapp.mhc.auth.controller;

import com.topick.superapp.mhc.ApiResponse;
import com.topick.superapp.mhc.auth.dto.*;
import com.topick.superapp.mhc.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {
    @Autowired
    AuthService authService;
    @PostMapping("/login")

    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest loginRequest) {
        ApiResponse response = authService.login(loginRequest);
        if(response.isSuccess()){    return ResponseEntity.ok(response);}
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/register")

    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest registerRequest) {
        ApiResponse response = authService.registerUser(registerRequest);
        if(response.isSuccess()){
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }


    @PostMapping("/doctor/register")

    public ResponseEntity<ApiResponse> registerDoctor(@RequestBody DoctorRegisterRequest  doctorRegisterRequest) {
        ApiResponse response = authService.registerDoctor(doctorRegisterRequest);
        if(response.isSuccess()){
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/logout")

    public ResponseEntity<Boolean> logout() {
        boolean response = authService.logout();
        if(response){
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
    @PostMapping("/refresh")

    public ResponseEntity<ApiResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        ApiResponse response = authService.refreshToken(refreshTokenRequest.getRefreshToken());
        if(response.isSuccess()){
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}
