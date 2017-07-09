package com.bookstore.controller;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bookstore.domain.security.PasswordResetToken;

@Controller
public class HomeController {

	@RequestMapping("/")
	public String index() {
		return "index";
	}	
	
	@RequestMapping("/login")
	public String login(Model model) {
		model.addAttribute("classActiveLogin", true);
		return "myAccount";
	}
	
	@RequestMapping("/forgetPassword")
	public String forgetPassword(
			Locale locale, 
			@RequestParam("token") String token,
			Model model) {
		
		PasswordResetToken passToken = userService.getPasswordResetToken(token);
		model.addAttribute("classActiveForgetPassword", true);
		return "myAccount";
	}
	
	@RequestMapping("/newUser")
	public String newUser(Model model) {
		model.addAttribute("classActiveNewAccount", true);
		return "myAccount";
	}
}
