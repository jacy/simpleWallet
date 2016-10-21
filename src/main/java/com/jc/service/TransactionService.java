package com.jc.service;

import java.math.BigDecimal;

import com.jc.domain.Wallet;
import com.jc.domain.WalletHistory;
import com.jc.domain.WalletHistoryType;

public interface TransactionService {
	void transfer(Long fromWalletId, Long toWalletId, BigDecimal amount, String reference);

	WalletHistory changeBalance(Wallet wallet, BigDecimal amount, String reference, WalletHistoryType type, String description);
}
