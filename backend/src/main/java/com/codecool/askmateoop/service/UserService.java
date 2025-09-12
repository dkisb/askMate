package com.codecool.askmateoop.service;

import com.codecool.askmateoop.errorhandler.custom_exceptions.NotAllowedOperationException;
import com.codecool.askmateoop.errorhandler.custom_exceptions.EmailAlreadyInUseException;
import com.codecool.askmateoop.errorhandler.custom_exceptions.UsernameAlreadyExistsException;
import com.codecool.askmateoop.model.payload.dto.JwtResponse;
import com.codecool.askmateoop.model.payload.dto.user.LoginRequestDTO;
import com.codecool.askmateoop.model.payload.dto.user.NewUserDTO;
import com.codecool.askmateoop.model.payload.dto.user.PointsDTO;
import com.codecool.askmateoop.model.entities.Role;
import com.codecool.askmateoop.model.entities.UserEntity;
import com.codecool.askmateoop.repository.UserRepository;
import com.codecool.askmateoop.security.jwt.JwtUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.EnumSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public void createUser(NewUserDTO request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException(request.email());
        }
        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(EnumSet.of(Role.ROLE_USER));
        userRepository.save(user);
    }

    public ResponseEntity<?> loginUser(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        User userDetails = (User) authentication.getPrincipal();
        Set<Role> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(Role::valueOf)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), roles));
    }

    public int getUserId(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found with username: " + username))
                .getId();
    }

    public int getReliabilityLevel (int userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found with userId: " + userId));
        return user.getReliabilityPoints();
    }

    public void addNewPoints (PointsDTO pointsDTO) {
        UserEntity user = userRepository.findById(pointsDTO.userId()).orElseThrow(() -> new NoSuchElementException("User not found with userId: " + pointsDTO.userId()));
        user.setReliabilityPoints(user.getReliabilityPoints() + pointsDTO.points());
        userRepository.save(user);
    }

    public void deleteUser(int id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity currentUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new NoSuchElementException("User not found"));
        if (currentUser.getId() != id) {
            throw new NotAllowedOperationException("You are not allowed to delete this answer");
        }
        userRepository.delete(currentUser);
    }

    public void makeUserMod(int id) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
        user.setRoles(EnumSet.of(Role.ROLE_USER, Role.ROLE_MODERATOR));
        userRepository.save(user);
    }

    public void makeModUser(int id) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
        user.setRoles(EnumSet.of(Role.ROLE_USER));
        userRepository.save(user);
    }

    public void deleteAnyUser(int id) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
        userRepository.delete(user);
    }
}
