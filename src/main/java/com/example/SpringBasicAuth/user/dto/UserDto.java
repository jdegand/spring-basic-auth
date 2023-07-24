package com.example.SpringBasicAuth.user.dto;

public record UserDto(
    Integer id,
    String username,
    String roles,
    boolean enabled
){}
