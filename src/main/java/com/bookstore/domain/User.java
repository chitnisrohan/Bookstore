package com.bookstore.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.bookstore.domain.security.Authority;
import com.bookstore.domain.security.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class User implements UserDetails{
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id", nullable = false, updatable = false)
	private long id;
	private String username;
	private String password;
	private String firstName;
	private String lastName;
	
	@Column(name="email", nullable = false, updatable = false)
	private String email;
	private String phone;
	private boolean enabled=true;
	
	@OneToOne(cascade=CascadeType.ALL, mappedBy="user")
	private ShoppingCart shoppingCart;

	public ShoppingCart getShoppingCart() {
		return shoppingCart;
	}
	public void setShoppingCart(ShoppingCart shoppingCart) {
		this.shoppingCart = shoppingCart;
	}

	@OneToMany(cascade=CascadeType.ALL, mappedBy="user")
	private List<UserShipping> userShippingList;

	@OneToMany(cascade=CascadeType.ALL, mappedBy="user")
	private List<UserPayment> userPaymentList;
	

//	If the relationship is bidirectional, the mappedBy element must be used to specify the relationship field or property of the entity that is the owner of the relationship.
//  The OneToMany annotation may be used within an embeddable class contained within an entity class to specify a relationship to a collection of entities. If the relationship is bidirectional, the mappedBy element must be used to specify the relationship field or property of the entity that is the owner of the relationship.
//	cascadeType ALL means - whether operation is propogated to chain of this mapping. here, Mapping between users and roles. i.e. If we work on this user object and if userRoles property changes, then changes will be propogated to userRoles DB as well. and ALL means all operations will be propogated
	@OneToMany(mappedBy="user", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
//	to break loop in case of serialization and deserialization
	@JsonIgnore
	private Set<UserRole> userRoles = new HashSet<>();
	
	
	
	public List<UserShipping> getUserShippingList() {
		return userShippingList;
	}
	public void setUserShippingList(List<UserShipping> userShippingList) {
		this.userShippingList = userShippingList;
	}
	public List<UserPayment> getUserPaymentList() {
		return userPaymentList;
	}
	public void setUserPaymentList(List<UserPayment> userPaymentList) {
		this.userPaymentList = userPaymentList;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public Set<UserRole> getUserRoles() {
		return userRoles;
	}
	public void setUserRoles(Set<UserRole> userRoles) {
		this.userRoles = userRoles;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> authorities = new HashSet<>();
		userRoles.forEach(ur -> authorities.add(new Authority(ur.getRole().getName())));
		return authorities;
	}
	
	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}

}
