package com.yourmicroworld.auth;

import com.yourmicroworld.user.AppUser;
import com.yourmicroworld.user.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public AuthService(UserRepository userRepository, JwtService jwtService) { this.userRepository = userRepository; this.jwtService = jwtService; }

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByUsername(request.username())) throw new IllegalArgumentException("用户名已被使用");
        AppUser user = userRepository.save(new AppUser(request.username(), passwordEncoder.encode(request.password())));
        return responseOf(user);
    }
    public AuthResponse login(AuthRequest request) {
        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) throw new IllegalArgumentException("用户名或密码错误");
        return responseOf(user);
    }
    private AuthResponse responseOf(AppUser user) { return new AuthResponse(user.getId(), user.getUsername(), user.getRole(), jwtService.createToken(user)); }
}
