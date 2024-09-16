package com.krishna.domain;

import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * The Class UserModel.
 */
@Entity
public class UserModel {

	/** The user id. */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "userId")
	private Long userId;

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/** The email. */
	@Column(name = "email")
	private String email;

	/** The emp name. */
	@Column(name = "empName")
	private String empName;

	/** The roles. */
	@Column(name = "roles")
	private ArrayList<String> roles;

	@Column(name="grade")
	private String grade;
	
	@Column(name="rank")
	private long rank;
	
	/**
	 * Instantiates a new user model.
	 */
	public UserModel() {

	}

	/**
	 * Instantiates a new user model.
	 *
	 * @param email the email
	 * @param empName the emp name
	 * @param roles the roles
	 */
	public UserModel(String email, String empName, ArrayList<String> roles) {
		super();
		this.email = email;
		this.empName = empName;
		this.roles = roles;
	}

	/**
	 * Gets the user id.
	 *
	 * @return the user id
	 */
	public long getUserId() {
		return userId;
	}

	/**
	 * Sets the user id.
	 *
	 * @param userId the new user id
	 */
	public void setUserId(long userId) {
		this.userId = userId;
	}

	/**
	 * Gets the email.
	 *
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the email.
	 *
	 * @param email the new email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Gets the emp name.
	 *
	 * @return the emp name
	 */
	public String getEmpName() {
		return empName;
	}

	/**
	 * Sets the emp name.
	 *
	 * @param empName the new emp name
	 */
	public void setEmpName(String empName) {
		this.empName = empName;
	}

	/**
	 * Gets the roles.
	 *
	 * @return the roles
	 */
	public ArrayList<String> getRoles() {
		return roles;
	}

	/**
	 * Sets the roles.
	 *
	 * @param roles the new roles
	 */
	public void setRoles(ArrayList<String> roles) {
		this.roles = roles;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public long getRank() {
		return rank;
	}

	public void setRank(long rank) {
		this.rank = rank;
	}

}
