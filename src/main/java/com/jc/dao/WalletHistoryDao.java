package com.jc.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.jc.domain.WalletHistory;
import com.jc.domain.WalletHistoryType;

public interface WalletHistoryDao {

	@Select("SELECT * FROM wallet_history WHERE type= #{type} and ref=#{ref}")
	WalletHistory getByRefAndType(@Param("ref") String ref, @Param("type") WalletHistoryType type);

	@Insert("INSERT INTO wallet_history(id,wallet_id,before,after,amount,ref,createtime, type, ref_wallet_id) " 
	+ "VALUES(#{id},#{walletId},#{before},#{after},#{amount},#{ref},#{createtime},#{type},#{refWalletId})")
	void insert(WalletHistory history);
}