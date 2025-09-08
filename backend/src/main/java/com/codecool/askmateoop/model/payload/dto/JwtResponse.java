package com.codecool.askmateoop.model.payload.dto;

import com.codecool.askmateoop.model.entities.Role;

import java.util.Set;

public record JwtResponse(String jwt, String username, Set<Role> roles) {
}
