package de.bitrecycling.springsaml.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authentication")
public class AuthenticationController {
	
	@GetMapping("/status/isLoggedIn")
	public Boolean checkStatus(){
		return SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
	}
}
