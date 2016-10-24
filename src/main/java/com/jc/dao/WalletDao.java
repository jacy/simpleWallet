package com.jc.dao;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.jc.domain.Wallet;

public interface WalletDao {
	@Select("SELECT * FROM wallet WHERE id = #{id} FOR UPDATE")
	Wallet lockById(Long id);

	@Update("UPDATE wallet SET balance=#{after} where id=#{id}")
	void updateBalance(@Param("id") Long id, @Param("after") BigDecimal after);

}