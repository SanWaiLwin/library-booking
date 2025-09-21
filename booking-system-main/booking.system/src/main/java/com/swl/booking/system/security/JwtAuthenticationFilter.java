package com.swl.booking.system.security;

import java.io.IOException;
 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtTokenProvider tokenProvider;

	@Autowired
	private CustomUserDetailsService customUserDetailsService; 

	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String jwt = getJwtFromRequest(request);
			if (request.getRequestURI().contains("/auth/")) // validate api for only auth contains url
			{
				if (!StringUtils.hasText(jwt)) {
					// auth key missing
					response.setStatus(HttpStatus.BAD_REQUEST.value());
					response.getWriter().write("JWT token missing.");
					return;
				}

				String name = tokenProvider.getUsernameFromToken(jwt);
				if (!tokenProvider.validateToken(jwt) || name == null) {
					// invalid token
					response.setStatus(HttpStatus.BAD_REQUEST.value());
					response.getWriter().write("Invalid JWT.");
					return;
				}
				
				boolean isexpire = JWTSingleton.getInstance().checkJWTexist(name, jwt);
				if(isexpire) {
					response.setStatus(HttpStatus.BAD_REQUEST.value());
					response.getWriter().write("Invalid JWT.");
					return;
				}

				UserDetails userDetails = customUserDetailsService.loadUserByUsername(name);
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());

				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
			
		} catch (Exception ex) {
			logger.error("Could not set user authentication in security context", ex);
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.getWriter().write("Invalid JWT.");
			return;
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * Get JWT From Request
	 * 
	 * @param request
	 * @return
	 */
	private String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7, bearerToken.length());
		}
		return null;
	}
}