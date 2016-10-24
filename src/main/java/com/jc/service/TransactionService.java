package com.jc.service;

import java.math.BigDecimal;

public interface TransactionService {
	void transfer(Long fromWalletId, Long toWalletId, BigDecimal amount, String reference);
}
