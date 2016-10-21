package com.jc.service.impl;

import static com.jc.exception.Errors.WALLET_NOT_FOUND_OR_DISABLE;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jc.dao.WalletDao;
import com.jc.domain.Wallet;
import com.jc.exception.BadRequestException;
import com.jc.service.WalletInfoService;

public class WalletInfoServiceImpl implements WalletInfoService {
	private WalletDao walletDao;
	@Override
	public Map<Long, Wallet> lockAndVerifyWalletInOrder(Long... walletIds) {
		return Stream.of(walletIds).sorted(Long::compare)
				.collect(Collectors.toMap(id -> id, id -> lockAndVerifyWallet(id)));
	}

	@Override
	public Wallet lockAndVerifyWallet(Long walletId) {
		return walletDao.lockById(walletId).filter(Wallet::isEnable)
				.orElseThrow(() -> new BadRequestException(WALLET_NOT_FOUND_OR_DISABLE));
	}
}
