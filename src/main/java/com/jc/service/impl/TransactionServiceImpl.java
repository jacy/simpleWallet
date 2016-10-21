package com.jc.service.impl;

import static com.jc.exception.Errors.INSUFFICIENT_BALANCE;
import static com.jc.exception.Errors.TRANSACTION_EXISTS;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.jc.dao.WalletDao;
import com.jc.dao.WalletHistoryDao;
import com.jc.domain.Wallet;
import com.jc.domain.WalletHistory;
import com.jc.domain.WalletHistoryType;
import com.jc.exception.BadRequestException;
import com.jc.service.TransactionService;
import com.jc.service.WalletInfoService;
import com.jc.util.BigDecimalUtil;

public class TransactionServiceImpl implements TransactionService {
	private WalletInfoService infoService;
	private WalletHistoryDao historyDao;
	private WalletDao walletDao;

	@Override
	public void transfer(Long fromWalletId, Long toWalletId, BigDecimal amount, String reference) {
		validateRequest(fromWalletId, toWalletId, amount, reference);
		Map<Long, Wallet> wallets = infoService.lockAndVerifyWalletInOrder(fromWalletId, toWalletId);
		changeBalance(wallets.get(fromWalletId), amount.negate(), reference, WalletHistoryType.FUNDOUT, "Send " + amount + " to " + toWalletId);
		changeBalance(wallets.get(toWalletId), amount, reference, WalletHistoryType.FUNDIN, "Receive " + amount + " from " + fromWalletId);
	}

	private void validateRequest(Long fromWalletId, Long toWalletId, BigDecimal amount, String reference) {
		assert fromWalletId != null : "fromWalletId should not be null";
		assert toWalletId != null : "toWalletId should not be null";
		assert !fromWalletId.equals(toWalletId) : "fromWalletId and toWalletId should not be the same";
		assert reference != null : "reference should not be null";
		assert isNegative(amount) : "invalid amount";
	}

	private boolean isNegative(BigDecimal amount) {
		return amount.compareTo(BigDecimal.ZERO) < 0;
	}

	private BigDecimal caculateBalance(BigDecimal amount, BigDecimal balance) {
		BigDecimal after = BigDecimalUtil.add(balance, amount);
		if (isNegative(after)) {
			throw new BadRequestException(INSUFFICIENT_BALANCE);
		}
		return after;
	}

	@Override
	public WalletHistory changeBalance(Wallet wallet, BigDecimal amount, String reference, WalletHistoryType type, String description) {
		BigDecimal after = caculateBalance(amount, wallet.getBalance());
		historyDao.getByRefAndType(reference, type).ifPresent(h -> {
			throw new BadRequestException(TRANSACTION_EXISTS);
		});
		WalletHistory history = new WalletHistory(UUID.randomUUID().toString(), wallet.getId(), wallet.getBalance(), after, new Date(), reference, amount, type, description);
		historyDao.insert(history);
		walletDao.updateBalance(wallet.getId(), after);
		return history;
	}

}