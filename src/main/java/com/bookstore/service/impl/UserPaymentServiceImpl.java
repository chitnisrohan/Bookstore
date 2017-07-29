package com.bookstore.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookstore.domain.UserPayment;
import com.bookstore.repository.UserPaymentRepository;
import com.bookstore.service.UserPaymentService;

@Service
public class UserPaymentServiceImpl implements UserPaymentService{
	
	@Autowired
	private UserPaymentRepository userPaymentRepository;

	@Override
	public UserPayment findById(Long id) {
		return userPaymentRepository.findOne(id);
	}

	@Override
	public void removeById(Long creditCardId) {
		userPaymentRepository.delete(creditCardId);
	}

}
