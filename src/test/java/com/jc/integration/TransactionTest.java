package com.jc.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jc.config.AppConfig;
import com.jc.dao.WalletDao;
import com.jc.dao.WalletHistoryDao;
import com.jc.domain.WalletHistoryType;
import com.jc.exception.BadRequestException;
import com.jc.exception.Errors;
import com.jc.service.TransactionService;

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
	public void testTransactionWillRockballIfCatchException() {
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
		final String prefix = "deadLockSameUserTest3To4-";
		try {
			CompletableFuture<?>[] futures = IntStream.rangeClosed(1, 1000).mapToObj(i -> CompletableFuture.runAsync(
					() -> transactionService.transfer(3L, 4L, BigDecimal.TEN, prefix + i)
			, EXECUTE_SERVICE)).toArray(CompletableFuture[]::new);
			CompletableFuture.allOf(futures).join();
		} catch (Exception e) {
			fail();
		}
		assertEquals(new BigDecimal("90000.00"), walletDao.getBalance(3L));
		assertEquals(new BigDecimal("110000.00"), walletDao.getBalance(4L));
		IntStream.rangeClosed(1, 1000).forEach(i -> assertFundInFundOutWithRef(prefix + i));
	}

	private void assertFundInFundOutWithRef(String ref) {
		assertNotNull(historyDao.getByRefAndType(ref, WalletHistoryType.FUNDIN));
		assertNotNull(historyDao.getByRefAndType(ref, WalletHistoryType.FUNDOUT));
	}

	@Test
	public void testNoDeadLockIfUserTransferToDiffWalletConcurrently() {
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(5L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(6L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(7L));
		final String prefix1 = "deadLockDiffUserTest5To6-";
		final String prefix2 = "deadLockDiffUserTest5To7-";
		try {
			CompletableFuture<?>[] futures = IntStream.rangeClosed(1, 1000).mapToObj(i -> CompletableFuture.runAsync(() -> {
				transactionService.transfer(5L, 6L, BigDecimal.TEN, prefix1 + i);
				transactionService.transfer(5L, 7L, BigDecimal.TEN, prefix2 + i);
			}, EXECUTE_SERVICE)).toArray(CompletableFuture[]::new);
			CompletableFuture.allOf(futures).join();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(new BigDecimal("80000.00"), walletDao.getBalance(5L));
		assertEquals(new BigDecimal("110000.00"), walletDao.getBalance(6L));
		assertEquals(new BigDecimal("110000.00"), walletDao.getBalance(7L));
		IntStream.rangeClosed(1, 1000).forEach(i -> {
			assertFundInFundOutWithRef(prefix1 + i);
			assertFundInFundOutWithRef(prefix2 + i);
		});
	}

	@Test
	public void testNoDeadLockIfDiffUserTransferToSameWalletConcurrently() {
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(8L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(9L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(10L));
		final String prefix1 = "deadLockDiffUserTest8To10--";
		final String prefix2 = "deadLockDiffUserTest9To10-";
		try {
			CompletableFuture<?>[] futures = IntStream.rangeClosed(1, 1000).mapToObj(i -> CompletableFuture.runAsync(() -> {
				transactionService.transfer(8L, 10L, BigDecimal.TEN, prefix1 + i);
				transactionService.transfer(9L, 10L, BigDecimal.TEN, prefix2 + i);
			}, EXECUTE_SERVICE)).toArray(CompletableFuture[]::new);
			CompletableFuture.allOf(futures).join();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(new BigDecimal("90000.00"), walletDao.getBalance(8L));
		assertEquals(new BigDecimal("90000.00"), walletDao.getBalance(9L));
		assertEquals(new BigDecimal("120000.00"), walletDao.getBalance(10L));
		IntStream.rangeClosed(1, 1000).forEach(i -> {
			assertFundInFundOutWithRef(prefix1 + i);
			assertFundInFundOutWithRef(prefix2 + i);
		});
	}

	@Test
	public void testNoDeadLockIf2UsersTransferToEachOtherConcurrently() {
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(11L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(12L));
		final String prefix1 = "deadLockDiffUserTest11To12--";
		final String prefix2 = "deadLockDiffUserTest12To11-";
		try {
			CompletableFuture<?>[] futures = IntStream.rangeClosed(1, 1000).mapToObj(i -> CompletableFuture.runAsync(() -> {
				transactionService.transfer(11L, 12L, BigDecimal.TEN, prefix1 + i);
				transactionService.transfer(12L, 11L, BigDecimal.ONE, prefix2 + i);
			}, EXECUTE_SERVICE)).toArray(CompletableFuture[]::new);
			CompletableFuture.allOf(futures).join();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(new BigDecimal("91000.00"), walletDao.getBalance(11L));
		assertEquals(new BigDecimal("109000.00"), walletDao.getBalance(12L));
		IntStream.rangeClosed(1, 1000).forEach(i -> {
			assertFundInFundOutWithRef(prefix1 + i);
			assertFundInFundOutWithRef(prefix2 + i);
		});
	}
}