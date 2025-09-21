package com.swl.booking.system.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.swl.booking.system.security.UserPrincipal;

public class CommonUtil {

	public static String dateToString(String format, Date date) {
		if (date == null) {
			return "";
		}
		if (!validString(format)) {
			format = CommonConstant.STD_DATE_TIME_FORMAT;
		}
		return new SimpleDateFormat(format).format(date);
	}

	public static boolean validString(String value) {
		return value != null && !value.trim().isEmpty();
	}

	public static UserPrincipal getUserPrincipalFromAuthentication() {
		UserPrincipal userPrincipal = null;
		Authentication auth = (Authentication) SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			Object p = auth.getPrincipal();
			if (p instanceof UserPrincipal) {
				userPrincipal = (UserPrincipal) p;
			}
		}
		return userPrincipal;
	}
}
