package com.bookstore.controller;

import java.util.List;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.bookstore.domain.Book;
import com.bookstore.domain.User;
import com.bookstore.domain.UserBilling;
import com.bookstore.domain.UserPayment;
import com.bookstore.domain.UserShipping;
import com.bookstore.domain.security.PasswordResetToken;
import com.bookstore.domain.security.Role;
import com.bookstore.domain.security.UserRole;
import com.bookstore.service.BookService;
import com.bookstore.service.UserPaymentService;
import com.bookstore.service.UserService;
import com.bookstore.service.UserShippingService;
import com.bookstore.service.impl.UserSecurityService;
import com.bookstore.utility.MailConstructor;
import com.bookstore.utility.SecurityUtility;
import com.bookstore.utility.USConstants;

@Controller
public class HomeController {	
	
	@Autowired
	private UserShippingService userShippingService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserSecurityService userSecurityService;
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private MailConstructor mailConstructor;
	
	@Autowired
	private BookService bookService;
	
	@Autowired
	private UserPaymentService userPaymentService;

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
			HttpServletRequest request,
			@ModelAttribute("email") String userEmail,
			Model model
			) {
		
		model.addAttribute("classActiveForgetPassword", true);
		User user = userService.findByEmail(userEmail);
		if(user == null) {
			model.addAttribute("emailNotExists",true);
			return "myAccount";
		}
		
		userService.save(user);
		
		String password = SecurityUtility.randomPassword();
		
		String encryptedPassword = SecurityUtility.passwordEncoder().encode(password);
		user.setPassword(encryptedPassword);
		
		String token = UUID.randomUUID().toString();
		userService.createPasswordResetTokenForUser(user, token);
		
		String appUrl = "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
		
		SimpleMailMessage email = mailConstructor.constructResetTokenEmail(appUrl, request.getLocale(), token, user, password);
		
		mailSender.send(email);
		
		model.addAttribute("forgetPasswordEmailSent", "true");
		
		return "myAccount";
	}
	
	@PostMapping("/setDefaultPayment")
	public String setDefaultPayment (
			@ModelAttribute("defaultUserPaymentId") Long defaultUserPaymentId, Principal principal, Model model
			) {
		User user = userService.findByUsername(principal.getName());
		userService.setUserDefaultPayment(defaultUserPaymentId, user);
		
		model.addAttribute("user", user);
		
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveBilling", true);
		model.addAttribute("listOfShippingAddresses", true);
		
		model.addAttribute("userPaymentList", user.getUserPaymentList());
		model.addAttribute("userShippingList", user.getUserShippingList());
		
		return "myProfile";
	}
	
	@RequestMapping("/removeCreditCard")
	public String removeCreditCard(
			@RequestParam("id") Long creditCardId, Principal principal, Model model
			) {
		User user = userService.findByUsername(principal.getName());
		UserPayment userPayment = userPaymentService.findById(creditCardId);
		
		
//		protecting authorized url access. e.g. Any user can request to update credit card by accessing 
//		url "/updateCreditCard?id=1".
//		Person should edit his own credit card
		if(user.getId() != userPayment.getUser().getId()) {
			return "badRequestPage";
		} else {
			model.addAttribute("user", user);

			userPaymentService.removeById(creditCardId);
			
			model.addAttribute("listOfCreditCards", true);
			model.addAttribute("classActiveBilling", true);
			model.addAttribute("listOfShippingAddresses", true);
			
			model.addAttribute("userPaymentList", user.getUserPaymentList());
			model.addAttribute("userShippingList", user.getUserShippingList());
//			model.addAttribute("orderList", user.getOrderList());
			
			return "myProfile";
		}
	}
	
	@RequestMapping("/updateCreditCard")
	public String updateCreditCard(
			@ModelAttribute("id") Long creditCardId, Principal principal, Model model
			) {
		User user = userService.findByUsername(principal.getName());
		UserPayment userPayment = userPaymentService.findById(creditCardId);
		
		
//		protecting authorized url access. e.g. Any user can request to update credit card by accessing 
//		url "/updateCreditCard?id=1".
//		Person should edit his own credit card
		if(user.getId() != userPayment.getUser().getId()) {
			return "badRequestPage";
		} else {
			model.addAttribute("user", user);
			UserBilling userBilling = userPayment.getUserBilling();
			model.addAttribute("userPayment", userPayment);
			model.addAttribute("userBilling", userBilling);
			
			List<String> stateList = USConstants.listOfUSStatesCode;
			Collections.sort(stateList);
			model.addAttribute("stateList", stateList);
			
			model.addAttribute("addNewCreditCard", true);
			model.addAttribute("classActiveBilling", true);
			model.addAttribute("listOfShippingAddresses", true);
			
			model.addAttribute("userPaymentList", user.getUserPaymentList());
			model.addAttribute("userShippingList", user.getUserShippingList());
//			model.addAttribute("orderList", user.getOrderList());
			
			return "myProfile";
		}
	}

	@RequestMapping(value="/newUser", method = RequestMethod.POST)
	public String newUserPost(
			HttpServletRequest request,
			@ModelAttribute("email") String userEmail,
			@ModelAttribute("username") String username,
			Model model
			) throws Exception{
		model.addAttribute("classActiveNewAccount", true);
		model.addAttribute("email", userEmail);
		model.addAttribute("username", username);
		
		if (userService.findByUsername(username) != null) {
			model.addAttribute("usernameExists", true);
			
			return "myAccount";
		}
		
		if (userService.findByEmail(userEmail) != null) {
			model.addAttribute("emailExists", true);
			
			return "myAccount";
		}
		
		User user = new User();
		user.setUsername(username);
		user.setEmail(userEmail);
		
		String password = SecurityUtility.randomPassword();
		
		String encryptedPassword = SecurityUtility.passwordEncoder().encode(password);
		user.setPassword(encryptedPassword);
		
		Role role = new Role();
		role.setRoleId(1);
		role.setName("ROLE_USER");
		Set<UserRole> userRoles = new HashSet<>();
		userRoles.add(new UserRole(user, role));
		userService.createUser(user, userRoles);
		
		String token = UUID.randomUUID().toString();
		userService.createPasswordResetTokenForUser(user, token);
		
		String appUrl = "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
		
		SimpleMailMessage email = mailConstructor.constructResetTokenEmail(appUrl, request.getLocale(), token, user, password);
		
		mailSender.send(email);
		
		model.addAttribute("emailSent", "true");
		
		return "myAccount";
	}	
	
//	User fills his information to create new account. and clicks create new account button. now we mail user security token.
//	user opens his email and clicks on the link to validate his email-id. now we come to this path (/newUser) and we are going to
//	retrieve token from the link using @RequestParam. then we do all validations as per below function, we return to myProfile page
	@RequestMapping("/newUser")
	public String newUser(
			Model model,
			Locale locale, 
			@RequestParam("token") String token) {
		
		PasswordResetToken passToken = userService.getPasswordResetToken(token);
		if (passToken == null) {
			String message = "Invalid Token";
			model.addAttribute("message",message);
			return "redirect:/badRequest";
		}
		
//		gets the current user from the token
		User user = passToken.getUser();
		String username = user.getUsername();
		
//		Below code sets the authentication environment and sets login session for that user
//		makes sure current login session is for that user
		UserDetails userDetails = userSecurityService.loadUserByUsername(username);		
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		model.addAttribute("user", user);
		model.addAttribute("classActiveEdit", true);
		return "myProfile";
	}
	
	@RequestMapping("/bookshelf")
	public String bookShelf(Model model, Principal principal) {
		List<Book> bookList = bookService.findAll();
		if(principal != null) {
			String username = principal.getName();
			User user = userService.findByUsername(username);
			model.addAttribute("user",user);
		}
		model.addAttribute("bookList", bookList);
		return "bookShelf";
	}
	
	@RequestMapping("/bookDetail")
	public String bookDetail(
			@PathParam("id") Long id, Model model, Principal principal
			) {
		
		if(principal != null) {
			String username = principal.getName();
			User user = userService.findByUsername(username);
			model.addAttribute("user",user);
		}
		
		Book book = bookService.findById(id);
		model.addAttribute("book", book);
		
		List<Integer> qtyList = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
		
		model.addAttribute("qtyList", qtyList);
		model.addAttribute("qty", 1);
		
		return "bookDetails";
		
	}
	
	@RequestMapping("/listOfCreditCards") 
	public String listOfCreditCards(Model model, Principal principal, HttpServletRequest request) {
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user",user);
		model.addAttribute("userPaymentList",user.getUserPaymentList());
		model.addAttribute("userShippingList",user.getUserShippingList());
//		model.addAttribute("orderList",user.orderList());
		
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveBilling", true);
		model.addAttribute("listOfShippingAddresses", true);
		
		return "myProfile";
	}
	
	@RequestMapping("/listOfShippingAddresses") 
	public String listOfShippingAddresses(Model model, Principal principal, HttpServletRequest request) {
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user",user);
		model.addAttribute("userPaymentList",user.getUserPaymentList());
		model.addAttribute("userShippingList",user.getUserShippingList());
//		model.addAttribute("orderList",user.orderList());

		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveShipping", true);
		model.addAttribute("listOfShippingAddresses", true);
		return "myProfile";
	}
	
	@PostMapping("/addNewCreditCard") 
	public String addNewCreditCard(
			@ModelAttribute("userBilling") UserBilling userBilling,
			@ModelAttribute("userPayment") UserPayment userPayment,
			Principal principal, Model model
			) {
		User user = userService.findByUsername(principal.getName());
		userService.updateUserBilling(userBilling, userPayment, user);
		
		model.addAttribute("user", user);
		model.addAttribute("userPaymentList", user.getUserPaymentList());
		model.addAttribute("userShippingList", user.getUserShippingList());
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveBilling", true);
		model.addAttribute("listOfShippingAddresses", true);
		
		
		return "myProfile";
	}
	
	@GetMapping("/addNewCreditCard")
	public String addNewCreditCard(Model model, Principal principal) {
		
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user",user);
		
		model.addAttribute("addNewCreditCard", true);
		model.addAttribute("classActiveBilling", true);
		model.addAttribute("listOfShippingAddresses", true);
		
		
		UserBilling userBilling = new UserBilling();
		UserPayment userPayment = new UserPayment();
		
		model.addAttribute("userPayment", userPayment);
		model.addAttribute("userBilling", userBilling);
		
		List<String> stateList = USConstants.listOfUSStatesCode;
		Collections.sort(stateList);
		model.addAttribute("stateList", stateList);
		
		model.addAttribute("userPaymentList",user.getUserPaymentList());
		model.addAttribute("userShippingList",user.getUserShippingList());
//		model.addAttribute("orderList",user.orderList());
		
		return "myProfile";
		
	}
	
	@RequestMapping("/updateUserShipping")
	public String updateUserShipping(
			@ModelAttribute("id") Long shippingAddressId, Principal principal, Model model
			) {
		User user = userService.findByUsername(principal.getName());
		UserShipping userShipping = userShippingService.findById(shippingAddressId);
		
		if(user.getId() != userShipping.getUser().getId()) {
			return "badRequestPage";
		} else {
			model.addAttribute("user", user);
			
			model.addAttribute("userShipping", userShipping);
			
			List<String> stateList = USConstants.listOfUSStatesCode;
			Collections.sort(stateList);
			model.addAttribute("stateList", stateList);
			
			model.addAttribute("addNewShippingAddress", true);
			model.addAttribute("classActiveShipping", true);
			model.addAttribute("listOfCreditCards", true);
			
			model.addAttribute("userPaymentList", user.getUserPaymentList());
			model.addAttribute("userShippingList", user.getUserShippingList());
//			model.addAttribute("orderList", user.getOrderList());
			
			return "myProfile";
		}
	}
	
	@RequestMapping("/removeUserShipping")
	public String removeUserShipping(
			@ModelAttribute("id") Long userShippingId, Principal principal, Model model
			){
		User user = userService.findByUsername(principal.getName());
		UserShipping userShipping = userShippingService.findById(userShippingId);
		
		if(user.getId() != userShipping.getUser().getId()) {
			return "badRequestPage";
		} else {
			model.addAttribute("user", user);
			
			userShippingService.removeById(userShippingId);
			
			model.addAttribute("listOfShippingAddresses", true);
			model.addAttribute("classActiveShipping", true);
			
			model.addAttribute("userPaymentList", user.getUserPaymentList());
			model.addAttribute("userShippingList", user.getUserShippingList());
//			model.addAttribute("orderList", user.getOrderList());
			
			return "myProfile";
		}
	}
	
	@RequestMapping(value="/setDefaultShippingAddress", method=RequestMethod.POST)
	public String setDefaultShippingAddress(
			@ModelAttribute("defaultShippingAddressId") Long defaultShippingId, Principal principal, Model model
			) {
		User user = userService.findByUsername(principal.getName());
		userService.setUserDefaultShipping(defaultShippingId, user);
		
		model.addAttribute("user", user);
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveShipping", true);
		model.addAttribute("listOfShippingAddresses", true);
		
		model.addAttribute("userPaymentList", user.getUserPaymentList());
		model.addAttribute("userShippingList", user.getUserShippingList());
//		model.addAttribute("orderList", user.getOrderList());
		
		return "myProfile";
	}
	
	@PostMapping("/addNewShippingAddress")
	public String addNewShippingAddress(
			@ModelAttribute("userShipping") UserShipping userShipping,
			Principal principal, Model model			
			) {
		
		User user = userService.findByUsername(principal.getName());
		userService.updateUserShipping(userShipping, user);
		
		model.addAttribute("user", user);
		model.addAttribute("userPaymentList", user.getUserPaymentList());
		model.addAttribute("userShippingList", user.getUserShippingList());
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveShipping", true);
		model.addAttribute("listOfShippingAddresses", true);
		
		return "myProfile";
	}
	
	@RequestMapping("/addNewShippingAddress")
	public String addNewShippingAddress(Model model, Principal principal) {
		
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user",user);
		
		model.addAttribute("addNewShippingAddress", true);
		model.addAttribute("classActiveShipping", true);
		
		UserShipping userShipping = new UserShipping();		
		model.addAttribute("userShipping", userShipping);
		
		List<String> stateList = USConstants.listOfUSStatesCode;
		Collections.sort(stateList);
		model.addAttribute("stateList", stateList);
		
		model.addAttribute("userPaymentList",user.getUserPaymentList());
		model.addAttribute("userShippingList",user.getUserShippingList());
//		model.addAttribute("orderList",user.orderList());
		
		return "myProfile";
		
	}

	
	@RequestMapping("/myProfile") 
	public String myProfile(Model model, Principal principal) {
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user",user);
		model.addAttribute("userPaymentList",user.getUserPaymentList());
		model.addAttribute("userShippingList",user.getUserShippingList());
		// model.addAttribute("orderList",user.getOrderList());
		
		UserShipping userShipping = new UserShipping();
		model.addAttribute("userShipping", userShipping);
		
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("listOfShippingAddresses", true);
		
		List<String> stateList = USConstants.listOfUSStatesCode;
		Collections.sort(stateList);
		model.addAttribute("stateList", stateList);
		model.addAttribute("classActiveEdit", true);
		
		return "myProfile";
		
	}
}
