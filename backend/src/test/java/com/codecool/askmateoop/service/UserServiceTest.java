package com.codecool.askmateoop.service;

import com.codecool.askmateoop.errorhandler.custom_exceptions.EmailAlreadyInUseException;
import com.codecool.askmateoop.errorhandler.custom_exceptions.NotAllowedOperationException;
import com.codecool.askmateoop.errorhandler.custom_exceptions.UsernameAlreadyExistsException;
import com.codecool.askmateoop.model.entities.Role;
import com.codecool.askmateoop.model.entities.UserEntity;
import com.codecool.askmateoop.model.payload.dto.JwtResponse;
import com.codecool.askmateoop.model.payload.dto.user.LoginRequestDTO;
import com.codecool.askmateoop.model.payload.dto.user.NewUserDTO;
import com.codecool.askmateoop.model.payload.dto.user.PointsDTO;
import com.codecool.askmateoop.repository.UserRepository;
import com.codecool.askmateoop.security.jwt.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserService userService;

    @Test
    public void createUserWithValidCredentialsThenSaveUser() {
        NewUserDTO dto = new NewUserDTO("testuser", "test@test.com", "123456");
        when(userRepository.existsByUsername(dto.username())).thenReturn(false);
        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(passwordEncoder.encode(dto.password())).thenReturn("encodedPw");

        userService.createUser(dto);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());

        UserEntity savedUser = captor.getValue();
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@test.com", savedUser.getEmail());
        assertEquals("encodedPw", savedUser.getPassword());
        assertTrue(savedUser.getRoles().contains(Role.ROLE_USER));
    }

    @Test
    public void createUserWithUsernameAlreadyInUseThenThrowUserNameAlreadyInUseException() {
        NewUserDTO dto = new NewUserDTO("testuser", "test@test.com", "123456");
        when(userRepository.existsByUsername(dto.username())).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> userService.createUser(dto));
    }

    @Test
    public void createUserWithEmailAlreadyExistsThenThrowsEmailAlreadyExistsException() {
        NewUserDTO dto = new NewUserDTO("testuser", "test@test.com", "123456");
        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThrows(EmailAlreadyInUseException.class, () -> userService.createUser(dto));
    }


    @Test
    void loginUserWithValidCredentialsThenReturnJwtToken() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "password");

        User userDetails = new User(
                "testuser",
                "password",
                Set.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("fake-jwt-token");

        ResponseEntity<?> response = userService.loginUser(loginRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(JwtResponse.class);
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertThat(jwtResponse.jwt()).isEqualTo("fake-jwt-token");
        assertThat(jwtResponse.username()).isEqualTo("testuser");
        assertThat(jwtResponse.roles()).containsExactlyInAnyOrder(Role.ROLE_USER);
    }

    @Test
    void testLoginUserWithBadCredentialsThenThrowBadCredentialsException() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("wronguser", "wrongpass");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        assertThatThrownBy(() -> userService.loginUser(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    public void getUserIdWhenUserExists() {
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        user.setId(1);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        assertEquals(1, userService.getUserId("testuser"));
    }

    @Test
    public void getUserIdWhenUserDoesNotExistThenThrowsNoSuchUserException() {
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        user.setId(1);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.getUserId("testuser"));
    }

    @Test
    public void getReliabilityLevelWithValidUserIdThenReturnReliabilityLevel() {
        UserEntity userEntity = new UserEntity();
        userEntity.setReliabilityPoints(3);
        userEntity.setId(1);
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));
        int result = userService.getReliabilityLevel(userEntity.getId());

        assertEquals(3, result);
    }

    @Test
    public void getReliabilityLevelWithInvalidUserIdThenThrowsNoSuchElementException() {
        UserEntity userEntity = new UserEntity();
        userEntity.setReliabilityPoints(3);
        userEntity.setId(1);
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.getReliabilityLevel(userEntity.getId()));
    }

    @Test
    public void addNewPointsWithValidUserId() {
        PointsDTO dto = new PointsDTO(1, 5);
        UserEntity user = new UserEntity();
        user.setReliabilityPoints(3);
        when(userRepository.findById(dto.userId())).thenReturn(Optional.of(user));
        userService.addNewPoints(dto);

        verify(userRepository, times(1)).save(user);
        assertEquals(8, user.getReliabilityPoints());
    }

    @Test
    public void addNewPointsWithInvalidUserId() {
        PointsDTO dto = new PointsDTO(1, 5);
        when(userRepository.findById(dto.userId())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.addNewPoints(dto));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    public void deleteUser_withValidUserId_deletesUser() {
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1);
        userEntity.setUsername("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(userEntity));

        userService.deleteUser(1);

        verify(userRepository, times(1)).delete(userEntity);
    }

    @Test
    public void deleteUser_withDifferentId_throwsNotAllowedException() {
        User springUser = new User("testUser", "password", new ArrayList<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1);
        userEntity.setUsername("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(userEntity));

        assertThrows(NotAllowedOperationException.class, () -> userService.deleteUser(2));
        verify(userRepository, never()).delete(any());
    }

    @Test
    public void deleteUser_userNotFound_throwsNoSuchElementException() {
        User springUser = new User("ghostUser", "password", new ArrayList<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("ghostUser")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.deleteUser(1));
        verify(userRepository, never()).delete(any());
    }

    @Test
    public void makeUserModWithValidUserIdMakeAUserModerator() {
        UserEntity user = new UserEntity();
        user.setRoles(EnumSet.of(Role.ROLE_USER));
        user.setId(1);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.makeUserMod(user.getId());

        verify(userRepository, times(1)).save(user);
        assertEquals(user.getRoles(), EnumSet.of(Role.ROLE_USER, Role.ROLE_MODERATOR));
    }

    @Test
    public void makeUserModWithInvalidUserIdThrowsNoSuchElementException() {
        UserEntity user = new UserEntity();
        user.setRoles(EnumSet.of(Role.ROLE_USER));
        user.setId(1);
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.makeUserMod(user.getId()));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    public void makeModUserWithValidUserIdMakeModeratorUser() {
        UserEntity user = new UserEntity();
        user.setRoles(EnumSet.of(Role.ROLE_USER, Role.ROLE_MODERATOR));
        user.setId(1);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.makeModUser(user.getId());

        verify(userRepository, times(1)).save(user);
        assertEquals(user.getRoles(), EnumSet.of(Role.ROLE_USER));
    }

    @Test
    public void makeModUserWithInvalidUserIdThrowsNoSuchElementException() {
        UserEntity user = new UserEntity();
        user.setRoles(EnumSet.of(Role.ROLE_USER, Role.ROLE_MODERATOR));
        user.setId(1);
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.makeModUser(user.getId()));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    public void deleteAnyUserWithValidUserId_deletesUser() {
        UserEntity user = new UserEntity();
        user.setId(1);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.deleteAnyUser(user.getId());

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    public void deleteAnyUserWithInvalidUserIdThrowsNoSuchElementException() {
        UserEntity user = new UserEntity();
        user.setId(1);
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.deleteAnyUser(user.getId()));
        verify(userRepository, never()).delete(any(UserEntity.class));
    }
}
