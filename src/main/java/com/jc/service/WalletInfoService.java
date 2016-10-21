package com.jc.service;

import java.util.Map;

import com.jc.domain.Wallet;

public interface WalletInfoService {
	Map<Long, Wallet> lockAndVerifyWalletInOrder(Long... walletIds);

	Wallet lockAndVerifyWallet(Long walletId);
}
