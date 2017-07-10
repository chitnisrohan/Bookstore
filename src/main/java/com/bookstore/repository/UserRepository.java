package com.bookstore.repository;

import org.springframework.data.repository.CrudRepository;

import com.bookstore.domain.User;

//  CrudRepository - has many methods that we can directly use.(google it). and doesnt have findByUsername() method.
//  so we added this method so that spring boot will realize this method and knows we pass username and it will return user for us.
public interface UserRepository extends CrudRepository<User, Long> {
	User findByUsername(String username);
	User findByEmail(String email);
}
