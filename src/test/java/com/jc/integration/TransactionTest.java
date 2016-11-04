package com.jc.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
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
	private static final int TOTAL_THREAD_COUNTS = 2000;
	private static final int PARALLEL_THREAD_COUNTS = 1000;
	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(TOTAL_THREAD_COUNTS);
	private Function<Throwable, Void> failOnException = e -> {
		fail();
		return null;
	};

	@Test
	public void testTransactionWillBeRockbackIfSameReferenceExists() {
		assertBalance(15L, "100000.00");
		assertBalance(16L, "100000.00");
		String existingFundInRef = "ref1";
		assertNotNull(historyDao.getByRefAndType(existingFundInRef, WalletHistoryType.FUNDIN));
		assertNull(historyDao.getByRefAndType(existingFundInRef, WalletHistoryType.FUNDOUT));
		try {
			transactionService.transfer(15L, 16L, BigDecimal.ONE, existingFundInRef);
			fail();
		} catch (BadRequestException e) {
			assertEquals(Errors.TRANSACTION_EXISTS, e.getError());
		}
		assertBalance(15L, "100000.00");
		assertBalance(16L, "100000.00");
		assertNotNull(historyDao.getByRefAndType(existingFundInRef, WalletHistoryType.FUNDIN));
		assertNull(historyDao.getByRefAndType(existingFundInRef, WalletHistoryType.FUNDOUT));
	}

	@Test
	public void tesDuplicateTransferRequestsOnlyOneWillSucceed() {
		assertBalance(13L, "100000.00");
		assertBalance(14L, "100000.00");
		final String ref = "same-ref";
		final AtomicInteger errorCount = new AtomicInteger();
		parallelRunning(i -> () -> transactionService.transfer(13L, 14L, BigDecimal.TEN, ref), e -> {
			assertTrue(e.getCause() instanceof BadRequestException);
			assertEquals(Errors.TRANSACTION_EXISTS, ((BadRequestException) e.getCause()).getError());
			errorCount.incrementAndGet();
			return null;
		});
		assertEquals(PARALLEL_THREAD_COUNTS - 1, errorCount.get());
		assertBalance(13L, "99990.00");
		assertBalance(14L, "100010.00");
		assertFundInFundOutExist(ref);
	}

	@Test
	public void testNoDeadLockIfUserASendMoneyToUserBAtTheSameTime() {
		assertBalance(3L, "100000.00");
		assertBalance(4L, "100000.00");
		final String prefix = "deadLockSameUserTest3To4-";
		parallelRunning(i -> () -> transactionService.transfer(3L, 4L, BigDecimal.TEN, prefix + i), failOnException);
		assertBalance(3L, "90000.00");
		assertBalance(4L, "110000.00");
		parallelAssert(i -> assertFundInFundOutExist(prefix + i));
	}

	@Test
	public void testNoDeadLockIfUserASendMoneyToUserBAndUserCAtTheSameTime() {
		assertBalance(5L, "100000.00");
		assertBalance(6L, "100000.00");
		assertBalance(7L, "100000.00");
		final String prefix1 = "deadLockDiffUserTest5To6-";
		final String prefix2 = "deadLockDiffUserTest5To7-";
		parallelRunning(i -> () -> {
			transactionService.transfer(5L, 6L, BigDecimal.TEN, prefix1 + i);
			transactionService.transfer(5L, 7L, BigDecimal.TEN, prefix2 + i);
		}, failOnException);
		assertBalance(5L, "80000.00");
		assertBalance(6L, "110000.00");
		assertBalance(7L, "110000.00");
		parallelAssert(i -> {
			assertFundInFundOutExist(prefix1 + i);
			assertFundInFundOutExist(prefix2 + i);
		});
	}

	@Test
	public void testNoDeadLockIfUserAReceivesMoneyFromUserBAndUserCAtTheSameTime() {
		assertBalance(8L, "100000.00");
		assertBalance(9L, "100000.00");
		assertBalance(10L, "100000.00");
		final String prefix1 = "deadLockDiffUserTest8To10--";
		final String prefix2 = "deadLockDiffUserTest9To10-";
		parallelRunning(i -> () -> {
			transactionService.transfer(8L, 10L, BigDecimal.TEN, prefix1 + i);
			transactionService.transfer(9L, 10L, BigDecimal.TEN, prefix2 + i);
		}, failOnException);
		assertBalance(8L, "90000.00");
		assertBalance(9L, "90000.00");
		assertBalance(10L, "120000.00");
		parallelAssert(i -> {
			assertFundInFundOutExist(prefix1 + i);
			assertFundInFundOutExist(prefix2 + i);
		});
	}

	@Test
	public void testNoDeadLockIf2UsersTransferMoneyToEachOtherAtTheSameTime() {
		assertBalance(11L, "100000.00");
		assertBalance(12L, "100000.00");
		final String prefix1 = "deadLockDiffUserTest11To12--";
		final String prefix2 = "deadLockDiffUserTest12To11-";
		parallelRunning(i -> () -> {
			transactionService.transfer(11L, 12L, BigDecimal.TEN, prefix1 + i);
			transactionService.transfer(12L, 11L, BigDecimal.ONE, prefix2 + i);
		}, failOnException);
		assertBalance(11L, "91000.00");
		assertBalance(12L, "109000.00");
		parallelAssert(i -> {
			assertFundInFundOutExist(prefix1 + i);
			assertFundInFundOutExist(prefix2 + i);
		});
	}

	private void parallelAssert(Consumer<Integer> assertion) {
		IntStream.rangeClosed(1, PARALLEL_THREAD_COUNTS).forEach(i -> assertion.accept(i));
	}

	private void parallelRunning(Function<Integer, Runnable> jobBuilder, Function<Throwable, Void> exceptionCallback) {
		CompletableFuture<?>[] futures = IntStream.rangeClosed(1, PARALLEL_THREAD_COUNTS).mapToObj(i -> CompletableFuture.runAsync(jobBuilder.apply(i), EXECUTOR).exceptionally(exceptionCallback)).toArray(CompletableFuture[]::new);
		CompletableFuture.allOf(futures).join();
	}

	private void assertFundInFundOutExist(String ref) {
		assertNotNull(historyDao.getByRefAndType(ref, WalletHistoryType.FUNDIN));
		assertNotNull(historyDao.getByRefAndType(ref, WalletHistoryType.FUNDOUT));
	}
	
	private void assertBalance(Long walletId, String balance) {
		assertEquals(new BigDecimal(balance), walletDao.getBalance(walletId));
	}
}