package de.bitrecycling.springsaml.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class PublicHelloWorldController {

	@GetMapping("/")
	public String hello(){
		return "helloapp.html";
	}
	
	@ResponseBody
	@GetMapping(path = "/serverhello", produces = "application/json")
	public String serverHello(){
		return "{\"hello\":\"hello world!\"}";
	}
	
}
