package com.oggo.planmaker.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.oggo.planmaker.model.User;

@Mapper
public interface UserMapper {

	void join(User user);

	User login(User user);

	User googleLogin(User user);

	User firstCheck(User user);

	void googleJoin(User user);

	// 아이디중복체크
	@Select("SELECT COUNT(*) > 0 FROM tb_user WHERE user_id = #{userId}")
	boolean existsByUserId(String userId);

	// 이메일중복체크
	@Select("SELECT COUNT(*) > 0 FROM tb_user WHERE user_email = #{userEmail}")
	boolean existsByUserEmail(String userEmail);

	int editProfile(User user);
	
	User getUserById(User user);
	
	void deleteId(@Param("userId")String userId);

}
