package com.swl.booking.system.service.impl;

import java.util.Optional;

import com.swl.booking.system.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.swl.booking.system.entity.User;
import com.swl.booking.system.exception.AlreadyExitException;
import com.swl.booking.system.repository.UserRepository;
import com.swl.booking.system.request.user.UserLoginRequest;
import com.swl.booking.system.request.user.UserRegisterRequest;
import com.swl.booking.system.request.user.UserUpdateRequest;
import com.swl.booking.system.response.user.UserLoginResponse;
import com.swl.booking.system.response.user.UserProfileResponse;
import com.swl.booking.system.security.UserPrincipal;
import com.swl.booking.system.util.CommonConstant;
import com.swl.booking.system.util.CommonUtil;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void registerUser(UserRegisterRequest req) {
		Optional<User> existingUser = userRepository.findByEmail(req.getEmail());
		if (existingUser.isPresent()) {
			throw new AlreadyExitException("User with this phone already exists.");
		}

		User user = prepareUserFromReq(req);
		user = userRepository.save(user);
		sendVerifyEmail(user);
	}

	private boolean sendVerifyEmail(User user) {
		return true;
	}

	private User prepareUserFromReq(UserRegisterRequest req) {
		User entity = new User();
		entity.setName(req.getName());
		entity.setEmail(req.getEmail());
		entity.setPassword(passwordEncoder.encode(req.getPassword()));
		entity.setAddress(req.getAddress());
		return entity;
	}

	@Override
	public UserLoginResponse authenticateAndGenerateToken(UserLoginRequest req) {
		Optional<User> user = userRepository.findByEmail(req.getEmail());
		if (user.isEmpty() || !passwordEncoder.matches(req.getPassword(), user.get().getPassword())) {
			throw new AlreadyExitException("Invalid credentials for email: " + req.getEmail());
		}
		return prepareUserLoginResponse(user.get());
	}

	private UserLoginResponse prepareUserLoginResponse(User user) {
		UserLoginResponse resp = new UserLoginResponse();
		resp.setId(user.getId());
		resp.setName(user.getName());
		resp.setEmail(user.getEmail());
		return resp;
	}

	@Override
	public UserProfileResponse getUser() {
		UserPrincipal userData = CommonUtil.getUserPrincipalFromAuthentication();
		Optional<User> user = userRepository.findById(userData.getId());
		if (user.isEmpty()) {
			throw new AlreadyExitException("Invalid credentials for phone no: " + userData.getId());
		}
		return prepareDataForProfile(user.get());
	}

	private UserProfileResponse prepareDataForProfile(User user) {
		UserProfileResponse resp = new UserProfileResponse();
		resp.setId(user.getId());
		resp.setName(user.getName());
		resp.setEmail(user.getEmail());
		resp.setAddress(user.getAddress());
		resp.setRegistrationDate(CommonUtil.dateToString(CommonConstant.STD_DATE_FORMAT, user.getRegistrationDate()));
		return resp;
	}

	@Override
	public void updateUserProfile(UserUpdateRequest req) {
		UserPrincipal userData = CommonUtil.getUserPrincipalFromAuthentication();
		User user = userRepository.findById(userData.getId()).get();
		user.setPassword(passwordEncoder.encode(req.getPassword()));
		userRepository.save(user);
	}

	@Override
	public User findById(Long id) {
		Optional<User> userOpt = userRepository.findById(id);
		if (userOpt.isEmpty()) {
			throw new AlreadyExitException("Invalid credentials for id: " + id);
		}
		return userOpt.get();
	}
}
