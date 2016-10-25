package com.jc.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jc.dao.WalletDao;
import com.jc.dao.WalletHistoryDao;
import com.jc.domain.WalletHistoryType;
import com.jc.exception.BadRequestException;
import com.jc.exception.Errors;
import com.jc.service.TransactionService;
import com.js.config.AppConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppConfig.class })
@Rollback(false)
public class TransactionTest {
	@Autowired
	private WalletDao walletDao;
	@Autowired
	private WalletHistoryDao historyDao;
	@Autowired
	private TransactionService transactionService;
	private static final ExecutorService EXECUTE_SERVICE = Executors.newFixedThreadPool(2000);

	@Test
	public void testTransactionWillRockballIfTransactionExists() {
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(3L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(4L));
		assertNotNull(historyDao.getByRefAndType("ref1", WalletHistoryType.FUNDIN));
		assertNull(historyDao.getByRefAndType("ref1", WalletHistoryType.FUNDOUT));
		try {
			transactionService.transfer(3L, 4L, BigDecimal.ONE, "ref1");
			fail();
		} catch (BadRequestException e) {
			assertEquals(Errors.TRANSACTION_EXISTS, e.getError());
		}
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(3L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(4L));
		assertNotNull(historyDao.getByRefAndType("ref1", WalletHistoryType.FUNDIN));
		assertNull(historyDao.getByRefAndType("ref1", WalletHistoryType.FUNDOUT));
	}

	@Test
	public void testNoDeadLockIfUserTransferToSameWalletConcurrently() {
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(3L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(4L));
		List<Future<?>> fs = new ArrayList<Future<?>>();
		try {
			for (int i = 0; i < 1000; i++) {
				final String ref = "deadLockSameUserTest3To4-" + i;
				fs.add(EXECUTE_SERVICE.submit(() -> transactionService.transfer(3L, 4L, BigDecimal.TEN, ref)));
			}
			for (Future<?> future : fs) {
				future.get();
			}
		} catch (Exception e) {
			fail();
		}
		assertEquals(new BigDecimal("90000.00"), walletDao.getBalance(3L));
		assertEquals(new BigDecimal("110000.00"), walletDao.getBalance(4L));
		for (int i = 0; i < 1000; i++) {
			assertNotNull(historyDao.getByRefAndType("deadLockSameUserTest3To4-" + i, WalletHistoryType.FUNDIN));
			assertNotNull(historyDao.getByRefAndType("deadLockSameUserTest3To4-" + i, WalletHistoryType.FUNDOUT));
		}
	}

	@Test
	public void testNoDeadLockIfUserTransferToDiffWalletConcurrently() {
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(5L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(6L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(7L));
		List<Future<?>> fs = new ArrayList<Future<?>>();
		try {
			for (int i = 0; i < 1000; i++) {
				final String ref1 = "deadLockDiffUserTest5To6-" + i;
				final String ref2 = "deadLockDiffUserTest5To7-" + i;
				fs.add(EXECUTE_SERVICE.submit(() -> transactionService.transfer(5L, 6L, BigDecimal.TEN, ref1)));
				fs.add(EXECUTE_SERVICE.submit(() -> transactionService.transfer(5L, 7L, BigDecimal.TEN, ref2)));
			}
			for (Future<?> future : fs) {
				future.get();
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(new BigDecimal("80000.00"), walletDao.getBalance(5L));
		assertEquals(new BigDecimal("110000.00"), walletDao.getBalance(6L));
		assertEquals(new BigDecimal("110000.00"), walletDao.getBalance(7L));
		for (int i = 0; i < 1000; i++) {
			assertNotNull(historyDao.getByRefAndType("deadLockDiffUserTest5To6-" + i, WalletHistoryType.FUNDIN));
			assertNotNull(historyDao.getByRefAndType("deadLockDiffUserTest5To6-" + i, WalletHistoryType.FUNDOUT));
			assertNotNull(historyDao.getByRefAndType("deadLockDiffUserTest5To7-" + i, WalletHistoryType.FUNDIN));
			assertNotNull(historyDao.getByRefAndType("deadLockDiffUserTest5To7-" + i, WalletHistoryType.FUNDOUT));
		}
	}

	@Test
	public void testNoDeadLockIfDiffUserTransferToSameWalletConcurrently() {
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(8L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(9L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(10L));
		List<Future<?>> fs = new ArrayList<Future<?>>();
		try {
			for (int i = 0; i < 1000; i++) {
				final String ref1 = "deadLockDiffUserTest8To10-" + i;
				final String ref2 = "deadLockDiffUserTest9To10-" + i;
				fs.add(EXECUTE_SERVICE.submit(() -> transactionService.transfer(8L, 10L, BigDecimal.TEN, ref1)));
				fs.add(EXECUTE_SERVICE.submit(() -> transactionService.transfer(9L, 10L, BigDecimal.TEN, ref2)));
			}
			for (Future<?> future : fs) {
				future.get();
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(new BigDecimal("90000.00"), walletDao.getBalance(8L));
		assertEquals(new BigDecimal("90000.00"), walletDao.getBalance(9L));
		assertEquals(new BigDecimal("120000.00"), walletDao.getBalance(10L));
		for (int i = 0; i < 1000; i++) {
			assertNotNull(historyDao.getByRefAndType("deadLockDiffUserTest8To10-" + i, WalletHistoryType.FUNDIN));
			assertNotNull(historyDao.getByRefAndType("deadLockDiffUserTest8To10-" + i, WalletHistoryType.FUNDOUT));
			assertNotNull(historyDao.getByRefAndType("deadLockDiffUserTest9To10-" + i, WalletHistoryType.FUNDIN));
			assertNotNull(historyDao.getByRefAndType("deadLockDiffUserTest9To10-" + i, WalletHistoryType.FUNDOUT));
		}
	}

	@Test
	public void testNoDeadLockIf2UsersTransferToEachOtherAtTheSameTime() {
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(11L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(12L));
		List<Future<?>> fs = new ArrayList<Future<?>>();
		try {
			for (int i = 0; i < 1000; i++) {
				final String ref1 = "deadLockDiffUserTest11To12-" + i;
				final String ref2 = "deadLockDiffUserTest12To11-" + i;
				fs.add(EXECUTE_SERVICE.submit(() -> transactionService.transfer(11L, 12L, BigDecimal.TEN, ref1)));
				fs.add(EXECUTE_SERVICE.submit(() -> transactionService.transfer(12L, 11L, BigDecimal.ONE, ref2)));
			}
			for (Future<?> future : fs) {
				future.get();
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(new BigDecimal("91000.00"), walletDao.getBalance(11L));
		assertEquals(new BigDecimal("109000.00"), walletDao.getBalance(12L));
		for (int i = 0; i < 1000; i++) {
			assertNotNull(historyDao.getByRefAndType("deadLockDiffUserTest11To12-" + i, WalletHistoryType.FUNDIN));
			assertNotNull(historyDao.getByRefAndType("deadLockDiffUserTest11To12-" + i, WalletHistoryType.FUNDOUT));
			assertNotNull(historyDao.getByRefAndType("deadLockDiffUserTest12To11-" + i, WalletHistoryType.FUNDIN));
			assertNotNull(historyDao.getByRefAndType("deadLockDiffUserTest12To11-" + i, WalletHistoryType.FUNDOUT));
		}
	}
}
