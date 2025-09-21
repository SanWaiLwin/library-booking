package com.swl.booking.system.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JWTSingleton {

	private static JWTSingleton jwtSingleton;
	private static Map<Long, String> expirejwtMap;

	private JWTSingleton() {
		if (jwtSingleton != null) {
			throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
		}
	}

	public static JWTSingleton getInstance() {
		// Double check locking pattern
		if (jwtSingleton == null) { // Check for the first time
			synchronized (JWTSingleton.class) { // Check for the second time.
				// if there is no instance available... create new one
				if (jwtSingleton == null) {
					jwtSingleton = new JWTSingleton();
				}
			}
		}
		return jwtSingleton;
	}

	private void checkMaps() {
		if (expirejwtMap == null) {
			expirejwtMap = new HashMap<>();
		}
	}

	public boolean checkJWTexist(Long id, String jwt) {
		checkMaps();
		if (!Objects.isNull(id) && expirejwtMap.containsKey(id)) {
			return true;
		} else
			return false;
	}

	public boolean checkJWTexist(String name, String jwt) {
		checkMaps();
		if (!Objects.isNull(name) && expirejwtMap.containsKey(name)) {
			return true;
		}
		return false;
	}

	public void addMap(Long id, String jwt) {
		checkMaps();
		expirejwtMap.put(id, jwt);
	}
}
