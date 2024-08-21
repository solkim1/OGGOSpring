package com.oggo.planmaker.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oggo.planmaker.mapper.UserMapper;
import com.oggo.planmaker.model.User;

@RestController
@RequestMapping(value = "/user")
public class UserController {

	@Autowired
	UserMapper mapper;
	
	@PostMapping(value = "/join")
	public void join(@RequestBody User user) {
		System.out.println(user.getUserId());
		System.out.println(user.getUserEmail());
		System.out.println(user.getUserPw());
		mapper.join(user);
	}
	@GetMapping(value = "checkId")
	public HashMap<String,Object> checkId(@RequestParam String userId){
		System.out.println("아이디체크");
		boolean available = !mapper.existsByUserId(userId);
        HashMap<String, Object> response = new HashMap<>();
        response.put("available", available);
		return response;
		
	}
}
