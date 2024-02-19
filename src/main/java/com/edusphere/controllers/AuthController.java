package com.edusphere.controllers;

import com.edusphere.utils.JwtUtil;
import com.edusphere.vos.LoginRequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@Tag(name = "Auth Controller", description = "APIs for managing authentication")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;


    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Authenticate User", description = "Authenticate a user and generate a JWT token")
    @PostMapping
    public ResponseEntity<String> authenticateUser(
            @Parameter(description = "Login Request Object") @RequestBody LoginRequestVO loginRequestVO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestVO.getUsername(),
                            loginRequestVO.getPassword()
                    )
            );

            // Generate JWT
            String token = jwtUtil.generateToken(authentication);
            return ResponseEntity.ok(token); // Return the token
        } catch (AuthenticationException ex) {
            return ResponseEntity.badRequest().body("Invalid username/password");
        }
    }
}
