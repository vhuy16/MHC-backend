package com.topick.superapp.mhc.auth.service;

import com.topick.superapp.mhc.ApiResponse;
import com.topick.superapp.mhc.auth.dto.AuthResponse;
import com.topick.superapp.mhc.auth.dto.DoctorRegisterRequest;
import com.topick.superapp.mhc.auth.dto.LoginRequest;
import com.topick.superapp.mhc.auth.dto.RegisterRequest;
import com.topick.superapp.mhc.auth.repository.DoctoRepository;
import com.topick.superapp.mhc.auth.repository.PatientRepository;
import com.topick.superapp.mhc.auth.repository.UserRepository;
import com.topick.superapp.mhc.enums.Role;
import com.topick.superapp.mhc.model.Doctor;
import com.topick.superapp.mhc.model.Patient;
import com.topick.superapp.mhc.model.User;
import com.topick.superapp.mhc.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service

@Transactional
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private DoctoRepository  doctoRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    public ApiResponse refreshToken(String refreshToken) {
        if(refreshToken==null || refreshToken.isEmpty()){
            return null;
        }
        var userId = jwtUtil.parseAccessToken(refreshToken).getSubject();
        if(!jwtUtil.validateToken(refreshToken)){
            return new ApiResponse("Refresh Token không hợp lệ", false, null);
        }

        String storedToken = redisTemplate.opsForValue().get("refresh:"+userId);
        if(!refreshToken.equals(storedToken)){
            return new ApiResponse("Refresh Token không hợp lệ", false, null);
        }
        Optional<User> user = userRepository.findById(UUID.fromString(userId));
        if(!user.isPresent()){
            return new ApiResponse("User khong ton tai", false, null);
        }

        String accessToken = jwtUtil.generateAccessToken(user.get());
        return new ApiResponse("Refresh thành công", true, accessToken);
    }

    public ApiResponse login(LoginRequest loginRequest) {
        Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());

        if(!user.isPresent()){
            return new  ApiResponse(
                   "Email ko tồn tại", false, null
            );
        } else if(!bCryptPasswordEncoder.matches(loginRequest.getPassword(), user.get().getPasswordHash())){
            return new  ApiResponse(
                    "Sai mật khẩu", false, null
            );
        }
      String accessToken = jwtUtil.generateAccessToken(user.get());
        String refreshToken = jwtUtil.generateRefreshToken(user.get());
        redisTemplate.opsForValue().set(
                "refresh:" + user.get().getId().toString(),
                refreshToken,
                7,
                TimeUnit.DAYS
        );
        return new ApiResponse(
                "Đăng nhập thành công", true, new AuthResponse(accessToken, refreshToken)
        );
    }

    public Boolean logout() {
        var userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      boolean result =   redisTemplate.delete("refresh:"+userId.toString());
        return result;
    }
    public ApiResponse registerDoctor (DoctorRegisterRequest doctorRegisterRequest) {
        String inviteKey = "invite:" + doctorRegisterRequest.getInviteCode();
        Boolean result = redisTemplate.hasKey(inviteKey);
        if(!Boolean.TRUE.equals(result)){
            return new ApiResponse(
                    "Invite code không hợp lệ", false, null
            );
        }
        redisTemplate.delete(inviteKey);
        if(userRepository.findByEmail(doctorRegisterRequest.getEmail()).isPresent()){
            return new  ApiResponse(
                    "Tài khoản đã tồn tại", false, null
            );
        }
        User user = User.builder()
                .email(doctorRegisterRequest.getEmail())
                .passwordHash(bCryptPasswordEncoder.encode(doctorRegisterRequest.getPassword()))
                .role(Role.DOCTOR)
                .createdAt(LocalDateTime.now())
                .verified(false)
                .build();
        userRepository.save(user);
        Doctor doctor = Doctor.builder()
                .specialty(doctorRegisterRequest.getSpecialty())
                .licenseNumber(String.valueOf(doctorRegisterRequest.getLicenseNumber()))
                .pricePerSession(doctorRegisterRequest.getPricePerSession())
                .users(user)
                .fullName(doctorRegisterRequest.getFullName())
                .build();
        doctoRepository.save(doctor);
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        redisTemplate.opsForValue().set(
                "refresh:" + user.getId().toString(),
                refreshToken,
                7,
                TimeUnit.DAYS
        );
        return new ApiResponse(
                "Đăng kí thành công", true, new AuthResponse(accessToken, refreshToken)
        );
    }

    public ApiResponse registerUser(RegisterRequest registerRequest) {
        if(userRepository.findByEmail(registerRequest.getEmail()).isPresent()){
            return new  ApiResponse(
                    "Tài khoản đã tồn tại", false, null
            );
        }
        User user = User.builder()
                .email(registerRequest.getEmail())
                .passwordHash(bCryptPasswordEncoder.encode(registerRequest.getPassword()))
                .role(Role.PATIENT)
                .createdAt(LocalDateTime.now())
                .verified(false)
                .build();
        userRepository.save(user);
        Patient patient = Patient.builder()

                .users(user)
                .fullName(registerRequest.getFullName())
                .build();
        patientRepository.save(patient);
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        redisTemplate.opsForValue().set(
                "refresh:" + user.getId().toString(),
                refreshToken,
                7,
                TimeUnit.DAYS
        );
        return new ApiResponse(
                "Đăng kí thành công", true, new AuthResponse(accessToken, refreshToken)
        );
    }


}
