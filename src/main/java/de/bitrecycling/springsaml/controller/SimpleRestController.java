package de.bitrecycling.springsaml.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleRestController {
	
	@GetMapping(produces = "application/json")
	public String hello(){
		return "{\"hello\":\"hello world!\"}";
	}
	
}
