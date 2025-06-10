package com.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo")
@Tag(name = "Demo", description = "Demo API endpoints with Basic Authentication")
@SecurityRequirement(name = "basicAuth")
public class DemoController {

    @GetMapping("/hello")
    @Operation(summary = "Get hello message", description = "Returns a simple hello message")
    public String hello() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return "Hello from Demo API! You are logged in as: " + auth.getName();
    }

    @GetMapping("/hello/{name}")
    @Operation(summary = "Get personalized hello message", description = "Returns a personalized hello message")
    public String helloWithName(
            @Parameter(description = "Name of the person to greet")
            @PathVariable String name) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return "Hello, " + name + "! Request made by: " + auth.getName();
    }

    @PostMapping("/echo")
    @Operation(summary = "Echo message", description = "Returns the same message that was sent")
    public String echo(
            @Parameter(description = "Message to echo back")
            @RequestBody String message) {
        return "Echo: " + message;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin only endpoint", description = "Only accessible by admin users")
    public String adminOnly() {
        return "This is an admin-only endpoint!";
    }

    @GetMapping("/user-info")
    @Operation(summary = "Get current user info", description = "Returns information about the authenticated user")
    public String getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return "Username: " + auth.getName() +
                ", Authorities: " + auth.getAuthorities().toString();
    }
}