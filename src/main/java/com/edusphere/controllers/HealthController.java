package com.edusphere.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Tag(name = "HealthController Controller", description = "Endpoint to get health information")
@SecurityRequirement(name = "Bearer Authentication")
public class HealthController {

    @GetMapping
    public String health() {
        return "OK";
    }
}
