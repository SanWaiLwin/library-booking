package com.swl.booking.system.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.swl.booking.system.entity.User;
import com.swl.booking.system.exception.AlreadyExitException;
import com.swl.booking.system.repository.UserRepository;
import com.swl.booking.system.request.user.UserLoginRequest;
import com.swl.booking.system.request.user.UserRegisterRequest;
import com.swl.booking.system.request.user.UserUpdateRequest;
import com.swl.booking.system.response.user.UserLoginResponse;
import com.swl.booking.system.response.user.UserProfileResponse;
import com.swl.booking.system.security.UserPrincipal;
import com.swl.booking.system.util.CommonUtil;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserRegisterRequest userRegisterRequest;
    private UserLoginRequest userLoginRequest;
    private UserUpdateRequest userUpdateRequest;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword123");
        testUser.setAddress("Test Address");
        testUser.setVerified(false);
        testUser.setRegistrationDate(new Date());
        testUser.setCreatedTime(new Date());
        testUser.setUpdatedTime(new Date());

        // Setup request objects
        userRegisterRequest = new UserRegisterRequest();
        userRegisterRequest.setName("Test User");
        userRegisterRequest.setEmail("test@example.com");
        userRegisterRequest.setPassword("password123");
        userRegisterRequest.setAddress("Test Address");

        userLoginRequest = new UserLoginRequest();
        userLoginRequest.setEmail("test@example.com");
        userLoginRequest.setPassword("password123");

        userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setPassword("newPassword123");

        // Setup UserPrincipal
        userPrincipal = new UserPrincipal(1L, "test@example.com", "encodedPassword123", null, false, null);
    }

    @Test
    void registerUser_Success() {
        // Given
        when(userRepository.findByEmail(userRegisterRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userRegisterRequest.getPassword())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        assertDoesNotThrow(() -> userService.registerUser(userRegisterRequest));

        // Then
        verify(userRepository).findByEmail(userRegisterRequest.getEmail());
        verify(passwordEncoder).encode(userRegisterRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ThrowsException_WhenUserAlreadyExists() {
        // Given
        when(userRepository.findByEmail(userRegisterRequest.getEmail())).thenReturn(Optional.of(testUser));

        // When & Then
        AlreadyExitException exception = assertThrows(AlreadyExitException.class, 
            () -> userService.registerUser(userRegisterRequest));
        
        assertEquals("User with this phone already exists.", exception.getMessage());
        verify(userRepository).findByEmail(userRegisterRequest.getEmail());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticateAndGenerateToken_Success() {
        // Given
        when(userRepository.findByEmail(userLoginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(userLoginRequest.getPassword(), testUser.getPassword())).thenReturn(true);

        // When
        UserLoginResponse response = userService.authenticateAndGenerateToken(userLoginRequest);

        // Then
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getName(), response.getName());
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(userRepository).findByEmail(userLoginRequest.getEmail());
        verify(passwordEncoder).matches(userLoginRequest.getPassword(), testUser.getPassword());
    }

    @Test
    void authenticateAndGenerateToken_ThrowsException_WhenUserNotFound() {
        // Given
        when(userRepository.findByEmail(userLoginRequest.getEmail())).thenReturn(Optional.empty());

        // When & Then
        AlreadyExitException exception = assertThrows(AlreadyExitException.class,
            () -> userService.authenticateAndGenerateToken(userLoginRequest));
        
        assertEquals("Invalid credentials for email: " + userLoginRequest.getEmail(), exception.getMessage());
        verify(userRepository).findByEmail(userLoginRequest.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void authenticateAndGenerateToken_ThrowsException_WhenInvalidPassword() {
        // Given
        when(userRepository.findByEmail(userLoginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(userLoginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        // When & Then
        AlreadyExitException exception = assertThrows(AlreadyExitException.class,
            () -> userService.authenticateAndGenerateToken(userLoginRequest));
        
        assertEquals("Invalid credentials for email: " + userLoginRequest.getEmail(), exception.getMessage());
        verify(userRepository).findByEmail(userLoginRequest.getEmail());
        verify(passwordEncoder).matches(userLoginRequest.getPassword(), testUser.getPassword());
    }

    @Test
    void updateUserProfile_Success() {
        // Given
        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getUserPrincipalFromAuthentication).thenReturn(userPrincipal);
            when(userRepository.findById(userPrincipal.getId())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode(userUpdateRequest.getPassword())).thenReturn("newEncodedPassword123");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            assertDoesNotThrow(() -> userService.updateUserProfile(userUpdateRequest));

            // Then
            verify(userRepository).findById(userPrincipal.getId());
            verify(passwordEncoder).encode(userUpdateRequest.getPassword());
            verify(userRepository).save(testUser);
        }
    }

    @Test
    void getUser_Success() {
        // Given
        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getUserPrincipalFromAuthentication).thenReturn(userPrincipal);
            mockedCommonUtil.when(() -> CommonUtil.dateToString(any(), any())).thenReturn("2024-01-01");
            when(userRepository.findById(userPrincipal.getId())).thenReturn(Optional.of(testUser));

            // When
            UserProfileResponse response = userService.getUser();

            // Then
            assertNotNull(response);
            assertEquals(testUser.getId(), response.getId());
            assertEquals(testUser.getName(), response.getName());
            assertEquals(testUser.getEmail(), response.getEmail());
            assertEquals(testUser.getAddress(), response.getAddress());
            assertEquals("2024-01-01", response.getRegistrationDate());
            verify(userRepository).findById(userPrincipal.getId());
        }
    }

    @Test
    void getUser_ThrowsException_WhenUserNotFound() {
        // Given
        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getUserPrincipalFromAuthentication).thenReturn(userPrincipal);
            when(userRepository.findById(userPrincipal.getId())).thenReturn(Optional.empty());

            // When & Then
            AlreadyExitException exception = assertThrows(AlreadyExitException.class,
                () -> userService.getUser());
            
            assertEquals("Invalid credentials for phone no: " + userPrincipal.getId(), exception.getMessage());
            verify(userRepository).findById(userPrincipal.getId());
        }
    }

    @Test
    void findById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).findById(1L);
    }

    @Test
    void findById_ThrowsException_WhenUserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        AlreadyExitException exception = assertThrows(AlreadyExitException.class,
            () -> userService.findById(1L));
        
        assertEquals("Invalid credentials for id: 1", exception.getMessage());
        verify(userRepository).findById(1L);
    }
}