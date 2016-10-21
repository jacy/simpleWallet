package com.jc.dao;

import java.math.BigDecimal;
import java.util.Optional;

import com.jc.domain.Wallet;

public interface WalletDao {

	Optional<Wallet> lockById(Long walletId);

	void updateBalance(Long id, BigDecimal after);

}