package com.swl.booking.system.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swl.booking.system.exception.GlobalExceptionHandler;
import com.swl.booking.system.request.ApiRequest;
import com.swl.booking.system.request.user.UserLoginRequest;
import com.swl.booking.system.request.user.UserRegisterRequest;
import com.swl.booking.system.request.user.UserUpdateRequest;
import com.swl.booking.system.response.user.UserLoginResponse;
import com.swl.booking.system.response.user.UserProfileResponse;
import com.swl.booking.system.security.JwtTokenProvider;
import com.swl.booking.system.security.UserPrincipal;
import com.swl.booking.system.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;

    @Mock
    private UserPrincipal userPrincipal;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ApiRequest<UserRegisterRequest> registerApiRequest;
    private ApiRequest<UserLoginRequest> loginApiRequest;
    private ApiRequest<UserUpdateRequest> updateApiRequest;
    private UserLoginResponse userLoginResponse;
    private UserProfileResponse userProfileResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // Setup test data for user registration
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setAddress("Test Address");
        
        registerApiRequest = new ApiRequest<>();
        registerApiRequest.setData(registerRequest);

        // Setup test data for user login
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
        
        loginApiRequest = new ApiRequest<>();
        loginApiRequest.setData(loginRequest);

        // Setup test data for user update
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setPassword("newpassword123");
        
        updateApiRequest = new ApiRequest<>();
        updateApiRequest.setData(updateRequest);

        // Setup response data
        userLoginResponse = new UserLoginResponse();
        userLoginResponse.setId(1L);
        userLoginResponse.setName("Test User");
        userLoginResponse.setEmail("test@example.com");
        userLoginResponse.setToken("jwt-token");

        userProfileResponse = new UserProfileResponse();
        userProfileResponse.setId(1L);
        userProfileResponse.setName("Test User");
        userProfileResponse.setEmail("test@example.com");
        userProfileResponse.setAddress("Test Address");
        userProfileResponse.setRegistrationDate("2024-01-01");
    }

    @Test
    void registerUser_Success() throws Exception {
        // Given
        doNothing().when(userService).registerUser(any(UserRegisterRequest.class));

        // When
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerApiRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Success"))
                .andExpect(jsonPath("$.message").value("Registration successful."));

        // Then
        verify(userService).registerUser(any(UserRegisterRequest.class));
    }

    @Test
    void loginUser_Success() throws Exception {
        // Given
        when(userService.authenticateAndGenerateToken(any(UserLoginRequest.class))).thenReturn(userLoginResponse);
        when(jwtTokenProvider.generateToken(eq("test@example.com"))).thenReturn("jwt-token");

        // When
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginApiRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Success"))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Test User"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));

        // Then
        verify(userService).authenticateAndGenerateToken(any(UserLoginRequest.class));
        verify(jwtTokenProvider).generateToken(eq("test@example.com"));
    }

    @Test
    void updateUserProfile_Success() throws Exception {
        // Given
        doNothing().when(userService).updateUserProfile(any(UserUpdateRequest.class));

        // When
        mockMvc.perform(post("/api/auth/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateApiRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Success"))
                .andExpect(jsonPath("$.message").value("Password reset is successful"));

        // Then
        verify(userService).updateUserProfile(any(UserUpdateRequest.class));
    }

    @Test
    void myProfile_Success() throws Exception {
        // Given
        when(userService.getUser()).thenReturn(userProfileResponse);

        // When
        mockMvc.perform(post("/api/auth/myProfile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Success"))
                .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Test User"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.address").value("Test Address"))
                .andExpect(jsonPath("$.data.registrationDate").value("2024-01-01"));

        // Then
        verify(userService).getUser();
    }

    @Test
    void registerUser_ThrowsException_WhenUserAlreadyExists() throws Exception {
        // Given
        doThrow(new RuntimeException("User with this email already exists."))
                .when(userService).registerUser(any(UserRegisterRequest.class));

        // When & Then
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerApiRequest)))
                .andExpect(status().isInternalServerError());

        verify(userService).registerUser(any(UserRegisterRequest.class));
    }

    @Test
    void loginUser_ThrowsException_WhenInvalidCredentials() throws Exception {
        // Given
        when(userService.authenticateAndGenerateToken(any(UserLoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginApiRequest)))
                .andExpect(status().isInternalServerError());

        verify(userService).authenticateAndGenerateToken(any(UserLoginRequest.class));
    }
}