package com.swl.booking.system.security; 

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
 
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
 
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swl.booking.system.entity.User;

public class UserPrincipal implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2354990332385454650L;

	private Long id;

	private String email;

	private String orgNo;

	private boolean isSuperAdmin;

	@JsonIgnore
	private String password;

	private Collection<? extends GrantedAuthority> authorities;

	public UserPrincipal(Long id, String email, String password, String orgNo, boolean isSuperAdmin, Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.email = email;
		this.password = password;
		this.orgNo = orgNo;
		this.isSuperAdmin = isSuperAdmin;
		this.authorities = authorities;
	} 
	
	public static UserPrincipal create(User user) {
		List<GrantedAuthority> authorities = new ArrayList<>();
		if(user.isVerified()){
//			for(UserRole r : UserRole.values()) {
//				GrantedAuthority auth = new SimpleGrantedAuthority(r.toString());
//				authorities.add(auth);
//			}
		} else {
			GrantedAuthority auth = new SimpleGrantedAuthority(user.getName().toString());
			authorities.add(auth);
		}
		return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), user.getAddress(), user.isVerified(), authorities);
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UserPrincipal that = (UserPrincipal) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {

		return Objects.hash(id);
	}

	@Override
	public String getUsername() {
		return email;
	}
	
	public Long getId() {
		return id;
	}
	
	public boolean isSuperAdmin() {
		return isSuperAdmin;
	}

}
