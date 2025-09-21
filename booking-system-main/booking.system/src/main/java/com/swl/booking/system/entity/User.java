package com.swl.booking.system.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1958156138226982290L;

	@Column(name = "name")
	private String name; 
	
	@Column(name = "email", unique = true)
	private String email;

	@Column(name = "password")
	private String password;

	@Column(name = "address")
	private String address;

	@Column(name = "isVerified")
	private boolean isVerified = false;

	@Column(name = "registrationDate")
	private Date registrationDate = new Date();

}
