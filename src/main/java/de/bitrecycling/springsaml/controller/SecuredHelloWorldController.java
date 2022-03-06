package de.bitrecycling.springsaml.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/secure")
public class SecuredHelloWorldController {


	@GetMapping()
	public String get(){
		return "/";
	}
	
	@GetMapping("/")
	public String hello(){
		return "/helloapp.html";
	}
	
	@ResponseBody
	@GetMapping(path = "/serverhello",produces = "application/json")
	public String serverhello(){
		return "{\"secure_hello\":\"hello secure world!\"}";
	}
	
	
}
