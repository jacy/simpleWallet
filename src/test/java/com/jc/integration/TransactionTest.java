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
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(15L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(16L));
		assertNotNull(historyDao.getByRefAndType("ref1", WalletHistoryType.FUNDIN));
		assertNull(historyDao.getByRefAndType("ref1", WalletHistoryType.FUNDOUT));
		try {
			transactionService.transfer(15L, 16L, BigDecimal.ONE, "ref1");
			fail();
		} catch (BadRequestException e) {
			assertEquals(Errors.TRANSACTION_EXISTS, e.getError());
		}
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(15L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(16L));
		assertNotNull(historyDao.getByRefAndType("ref1", WalletHistoryType.FUNDIN));
		assertNull(historyDao.getByRefAndType("ref1", WalletHistoryType.FUNDOUT));
	}

	@Test
	public void tesDuplicateTransferRequestsOnlyOneWillSucceed() {
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(13L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(14L));
		final String ref = "same-ref";
		final AtomicInteger errorCount = new AtomicInteger();
		parallelRunning(i -> () -> transactionService.transfer(13L, 14L, BigDecimal.TEN, ref), e -> {
			assertTrue(e.getCause() instanceof BadRequestException);
			assertEquals(Errors.TRANSACTION_EXISTS, ((BadRequestException) e.getCause()).getError());
			errorCount.incrementAndGet();
			return null;
		});
		assertEquals(999, errorCount.get());
		assertEquals(new BigDecimal("99990.00"), walletDao.getBalance(13L));
		assertEquals(new BigDecimal("100010.00"), walletDao.getBalance(14L));
		assertFundInFundOutExist(ref);
	}

	@Test
	public void testNoDeadLockIfUserASendMoneyToUserBAtTheSameTime() {
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(3L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(4L));
		final String prefix = "deadLockSameUserTest3To4-";
		parallelRunning(i -> () -> transactionService.transfer(3L, 4L, BigDecimal.TEN, prefix + i), failOnException);
		assertEquals(new BigDecimal("90000.00"), walletDao.getBalance(3L));
		assertEquals(new BigDecimal("110000.00"), walletDao.getBalance(4L));
		parallelAssert(i -> assertFundInFundOutExist(prefix + i));
	}
	
	@Test
	public void testNoDeadLockIfUserASendMoneyToUserBAndUserCAtTheSameTime() {
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(5L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(6L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(7L));
		final String prefix1 = "deadLockDiffUserTest5To6-";
		final String prefix2 = "deadLockDiffUserTest5To7-";
		parallelRunning(i -> () -> {
			transactionService.transfer(5L, 6L, BigDecimal.TEN, prefix1 + i);
			transactionService.transfer(5L, 7L, BigDecimal.TEN, prefix2 + i);
		}, failOnException);
		assertEquals(new BigDecimal("80000.00"), walletDao.getBalance(5L));
		assertEquals(new BigDecimal("110000.00"), walletDao.getBalance(6L));
		assertEquals(new BigDecimal("110000.00"), walletDao.getBalance(7L));
		parallelAssert(i -> {
			assertFundInFundOutExist(prefix1 + i);
			assertFundInFundOutExist(prefix2 + i);
		});
	}

	@Test
	public void testNoDeadLockIfUserAReceivesMoneyFromUserBAndUserCAtTheSameTime() {
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(8L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(9L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(10L));
		final String prefix1 = "deadLockDiffUserTest8To10--";
		final String prefix2 = "deadLockDiffUserTest9To10-";
		parallelRunning(i -> () -> {
			transactionService.transfer(8L, 10L, BigDecimal.TEN, prefix1 + i);
			transactionService.transfer(9L, 10L, BigDecimal.TEN, prefix2 + i);
		}, failOnException);
		assertEquals(new BigDecimal("90000.00"), walletDao.getBalance(8L));
		assertEquals(new BigDecimal("90000.00"), walletDao.getBalance(9L));
		assertEquals(new BigDecimal("120000.00"), walletDao.getBalance(10L));
		parallelAssert(i -> {
			assertFundInFundOutExist(prefix1 + i);
			assertFundInFundOutExist(prefix2 + i);
		});
	}

	@Test
	public void testNoDeadLockIf2UsersTransferMoneyToEachOtherAtTheSameTime() {
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(11L));
		assertEquals(new BigDecimal("100000.00"), walletDao.getBalance(12L));
		final String prefix1 = "deadLockDiffUserTest11To12--";
		final String prefix2 = "deadLockDiffUserTest12To11-";
		parallelRunning(i -> () -> {
			transactionService.transfer(11L, 12L, BigDecimal.TEN, prefix1 + i);
			transactionService.transfer(12L, 11L, BigDecimal.ONE, prefix2 + i);
		}, failOnException);
		assertEquals(new BigDecimal("91000.00"), walletDao.getBalance(11L));
		assertEquals(new BigDecimal("109000.00"), walletDao.getBalance(12L));
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
}