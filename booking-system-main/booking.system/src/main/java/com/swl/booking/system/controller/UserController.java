package com.swl.booking.system.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swl.booking.system.exception.RdpException;
import com.swl.booking.system.request.ApiRequest;
import com.swl.booking.system.request.user.UserLoginRequest; 
import com.swl.booking.system.request.user.UserRegisterRequest;
import com.swl.booking.system.request.user.UserUpdateRequest;
import com.swl.booking.system.response.ApiResponse;
import com.swl.booking.system.response.user.UserLoginResponse;
import com.swl.booking.system.response.user.UserProfileResponse;
import com.swl.booking.system.security.JwtTokenProvider;
import com.swl.booking.system.service.UserService;
import com.swl.booking.system.util.CommonConstant;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api")
public class UserController {

	private final UserService userService;

	private final JwtTokenProvider jwtTokenProvider;

	public UserController(UserService userService, JwtTokenProvider jwtTokenProvider) {
		this.userService = userService;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@PostMapping("/register")
	public ApiResponse<Void> registerUser(@Valid @RequestBody ApiRequest<UserRegisterRequest> apiRequest)
			throws RdpException {
		UserRegisterRequest req = apiRequest.getData();
		userService.registerUser(req);
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_SUCCESS, "Registration successful.");
	}

	@PostMapping("/login")
	public ApiResponse<UserLoginResponse> loginUser(@Valid @RequestBody ApiRequest<UserLoginRequest> apiRequest) {
		UserLoginRequest req = apiRequest.getData();
		UserLoginResponse resp = userService.authenticateAndGenerateToken(req);
		String token = jwtTokenProvider.generateToken(resp.getEmail());
		resp.setToken(token);
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_SUCCESS, "Login successful", resp);
	}

	@PostMapping("/auth/resetPassword")
	public ApiResponse<String> updateUserProfile(@Valid @RequestBody ApiRequest<UserUpdateRequest> apiRequest) {
		UserUpdateRequest req = apiRequest.getData();
		userService.updateUserProfile(req);
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_SUCCESS, "Password reset is successful");
	}

	@PostMapping("/auth/myProfile")
	public ApiResponse<UserProfileResponse> myProfile() { 
		UserProfileResponse resp = userService.getUser();
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_SUCCESS, "Users retrieved successfully", resp);
	}
}
