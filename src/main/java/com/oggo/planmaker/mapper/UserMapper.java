package com.oggo.planmaker.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.oggo.planmaker.model.User;

@Mapper
public interface UserMapper {
	
	void join(User user);
	
	@Select("SELECT COUNT(*) > 0 FROM tb_user WHERE user_id = #{userId}")
    boolean existsByUserId(String userId);	
}
