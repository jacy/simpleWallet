package com.jc.service.impl;

import static com.jc.domain.WalletHistoryType.FUNDIN;
import static com.jc.domain.WalletHistoryType.FUNDOUT;
import static com.jc.exception.Errors.INSUFFICIENT_BALANCE;
import static com.jc.exception.Errors.TRANSACTION_EXISTS;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.jc.dao.WalletDao;
import com.jc.dao.WalletHistoryDao;
import com.jc.domain.Wallet;
import com.jc.domain.WalletHistory;
import com.jc.domain.WalletHistoryType;
import com.jc.exception.BadRequestException;
import com.jc.exception.InvalidParameterException;
import com.jc.service.TransactionService;
import com.jc.service.WalletInfoService;
import com.jc.util.BigDecimalUtil;

@Service("transactionService")
@Transactional(isolation = Isolation.READ_COMMITTED)
public class TransactionServiceImpl implements TransactionService {
	@Autowired
	private WalletInfoService infoService;
	@Autowired
	private WalletHistoryDao historyDao;
	@Autowired
	private WalletDao walletDao;

	@Override
	public void transfer(Long fromWalletId, Long toWalletId, BigDecimal amount, String reference) {
		validateRequest(fromWalletId, toWalletId, amount, reference);
		Map<Long, Wallet> wallets = infoService.lockAndVerifyWalletInOrder(fromWalletId, toWalletId);
		amount = BigDecimalUtil.roundDown(amount);
		changeBalance(wallets.get(fromWalletId), amount.negate(), reference, FUNDOUT, toWalletId);
		changeBalance(wallets.get(toWalletId), amount, reference, FUNDIN, fromWalletId);
	}

	public WalletHistory changeBalance(Wallet wallet, BigDecimal amount, String reference, WalletHistoryType type, Long refWalletId) {
		BigDecimal after = caculateAndVerifyBalance(amount, wallet.getBalance());
		Optional.ofNullable(historyDao.getByRefAndType(reference, type)).ifPresent(h -> {throw new BadRequestException(TRANSACTION_EXISTS);});
		WalletHistory history = new WalletHistory(UUID.randomUUID().toString(), wallet.getId(), wallet.getBalance(), after, new Date(), reference, amount, type, refWalletId);
		historyDao.insert(history);
		walletDao.updateBalance(wallet.getId(), after);
		return history;
	}

	private void validateRequest(Long fromWalletId, Long toWalletId, BigDecimal amount, String reference) {
		assertion(fromWalletId != null, "fromWalletId should not be null");
		assertion(toWalletId != null, "toWalletId should not be null");
		assertion(amount != null, "amount should not be null");
		assertion(reference != null, "reference should not be null");
		assertion(!fromWalletId.equals(toWalletId), "fromWalletId and toWalletId should not be the same");
		assertion(isPositive(amount), "invalid amount");
	}

	private boolean isPositive(BigDecimal amount) {
		return amount.compareTo(BigDecimal.ZERO) >= 0;
	}

	private BigDecimal caculateAndVerifyBalance(BigDecimal amount, BigDecimal balance) {
		BigDecimal after = BigDecimalUtil.add(balance, amount);
		if (!isPositive(after)) {
			throw new BadRequestException(INSUFFICIENT_BALANCE);
		}
		return after;
	}

	private void assertion(boolean expression, String msg) {
		if (!expression) {
			throw new InvalidParameterException(msg);
		}
	}
}