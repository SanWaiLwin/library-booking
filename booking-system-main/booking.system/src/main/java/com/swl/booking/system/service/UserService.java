package com.swl.booking.system.service;

import com.swl.booking.system.entity.User;
import com.swl.booking.system.request.user.UserLoginRequest; 
import com.swl.booking.system.request.user.UserRegisterRequest;
import com.swl.booking.system.request.user.UserUpdateRequest;
import com.swl.booking.system.response.user.UserLoginResponse;
import com.swl.booking.system.response.user.UserProfileResponse;

public interface UserService {   

	void registerUser(UserRegisterRequest req);

	UserLoginResponse authenticateAndGenerateToken(UserLoginRequest req); 

	void updateUserProfile(UserUpdateRequest req);

	User findById(Long id);

	UserProfileResponse getUser(); 
}
