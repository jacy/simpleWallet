package com.jc.dao;

import java.util.Optional;

import com.jc.domain.WalletHistory;
import com.jc.domain.WalletHistoryType;

public interface WalletHistoryDao {

	Optional<WalletHistory> getByRefAndType(String reference, WalletHistoryType fundin);

	void insert(WalletHistory history);

}
