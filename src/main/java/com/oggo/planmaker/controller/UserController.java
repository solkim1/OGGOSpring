package com.oggo.planmaker.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
//		System.out.println(user.getUserId());
//		System.out.println(user.getUserEmail());
//		System.out.println(user.getUserPw());
		mapper.join(user);
	}
	@PostMapping(value = "/login")
	public ResponseEntity<?> login(@RequestBody User user) {
	    // 입력 검증
	    if (user == null || user.getUserPw() == null) {
	        return ResponseEntity.badRequest().body("Invalid input.");
	    }

	    try {
	        User loginUser = mapper.login(user);

	        // 사용자 정보 확인
	        if (loginUser != null) {
	            System.out.println(loginUser.getUserId());
	            System.out.println(loginUser.getUserEmail());
	            return ResponseEntity.ok(loginUser);
	        } else {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
	        }
	    } catch (Exception e) {
	        // 예외 처리
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing your request.");
	    }
	}

	

	@GetMapping(value = "/checkId")
	public HashMap<String, Object> checkId(@RequestParam String userId) {
		System.out.println("아이디체크");
		boolean available = !mapper.existsByUserId(userId);
		HashMap<String, Object> response = new HashMap<>();
		response.put("available", available);
		return response;
	}

	@GetMapping(value = "/checkEmail")
	public HashMap<String, Object> checkEmail(@RequestParam String userEmail) {
		System.out.println("이메일체크");
		boolean available = !mapper.existsByUserEmail(userEmail);
		HashMap<String, Object> response = new HashMap<>();
		response.put("available", available);
		return response;
	}

}
