package com.oggo.planmaker.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.oggo.planmaker.model.User;

@Mapper
public interface UserMapper {

	void join(User user);
	
	User login(User user);
	
	User firstCheck(User user);
	
	void googleJoin(User user);


	@Select("SELECT COUNT(*) > 0 FROM tb_user WHERE user_id = #{userId}")
	boolean existsByUserId(String userId);

	@Select("SELECT COUNT(*) > 0 FROM tb_user WHERE user_email = #{userEmail}")
	boolean existsByUserEmail(String userEmail);
}
