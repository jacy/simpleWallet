package com.jc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.jc.dao.WalletDao;
import com.jc.dao.WalletHistoryDao;
import com.jc.domain.Wallet;
import com.jc.domain.WalletHistory;
import com.jc.domain.WalletHistoryType;
import com.jc.exception.BadRequestException;
import com.jc.exception.Errors;
import com.jc.exception.InvalidParameterException;
import com.jc.service.WalletInfoService;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTest {
	@InjectMocks
	private TransactionServiceImpl service;

	private TransactionServiceImpl spyService;

	@Mock
	private WalletDao walletDao;
	@Mock
	private WalletInfoService infoService;
	@Mock
	private WalletHistoryDao historyDao;

	private Long walletId = 1L;
	private Long walletId2 = 2L;

	private Wallet wallet;
	private Wallet wallet2;

	private String ref = UUID.randomUUID().toString();

	@Before
	public void setup() {
		wallet = new Wallet(walletId, BigDecimal.TEN, Wallet.ENABLE);
		wallet2 = new Wallet(walletId2, BigDecimal.ZERO, Wallet.ENABLE);

		spyService = Mockito.spy(service);
	}

	@Test
	public void testChangeBalanceComplainsIfAfterIsNegative() {
		try {
			service.changeBalance(wallet, BigDecimal.valueOf(-10.01), ref, WalletHistoryType.FUNDOUT, walletId2);
			fail();
		} catch (BadRequestException e) {
			assertEquals(Errors.INSUFFICIENT_BALANCE, e.getError());
		}
		Mockito.verifyZeroInteractions(walletDao, historyDao, infoService);
	}

	@Test
	public void testChangeBalanceComplainsIfTransactionExists() {
		Mockito.when(historyDao.getByRefAndType(ref, WalletHistoryType.FUNDOUT)).thenReturn(new WalletHistory());
		try {
			service.changeBalance(wallet, BigDecimal.valueOf(-9.99), ref, WalletHistoryType.FUNDOUT, walletId2);
			fail();
		} catch (BadRequestException e) {
			assertEquals(Errors.TRANSACTION_EXISTS, e.getError());
		}
		Mockito.verify(historyDao).getByRefAndType(ref, WalletHistoryType.FUNDOUT);
		Mockito.verifyZeroInteractions(walletDao, infoService);
		Mockito.verifyNoMoreInteractions(historyDao);
	}

	@Test
	public void testChangeBalanceSuccessIfAmountIsNegative() {
		Mockito.when(historyDao.getByRefAndType(ref, WalletHistoryType.FUNDOUT)).thenReturn(null);
		BigDecimal amount = BigDecimal.valueOf(-9.99);
		WalletHistory history = service.changeBalance(wallet, amount, ref, WalletHistoryType.FUNDOUT, walletId2);
		InOrder inOrder = Mockito.inOrder(walletDao, historyDao, infoService);
		inOrder.verify(historyDao).getByRefAndType(ref, WalletHistoryType.FUNDOUT);
		inOrder.verify(historyDao).insert(Mockito.any(WalletHistory.class));
		inOrder.verify(walletDao).updateBalance(walletId, new BigDecimal("0.01"));
		assertEquals(BigDecimal.TEN, history.getBefore());
		assertEquals(new BigDecimal("0.01"), history.getAfter());
		assertEquals(new BigDecimal("-9.99"), history.getAmount());
		assertEquals(ref, history.getRef());
		assertNotNull(history.getCreatetime());
		assertEquals(walletId2, history.getRefWalletId());
		assertNotNull(history.getId());
		assertEquals(WalletHistoryType.FUNDOUT, history.getType());
		assertEquals(walletId, history.getWalletId());
		Mockito.verifyNoMoreInteractions(walletDao, historyDao, infoService);
	}

	@Test
	public void testChangeBalanceSuccessIfAmountIsPositive() {
		Mockito.when(historyDao.getByRefAndType(ref, WalletHistoryType.FUNDIN)).thenReturn(null);
		BigDecimal amount = BigDecimal.valueOf(9.99);
		WalletHistory history = service.changeBalance(wallet, amount, ref, WalletHistoryType.FUNDIN, walletId2);
		InOrder inOrder = Mockito.inOrder(walletDao, historyDao, infoService);
		inOrder.verify(historyDao).getByRefAndType(ref, WalletHistoryType.FUNDIN);
		inOrder.verify(historyDao).insert(Mockito.any(WalletHistory.class));
		inOrder.verify(walletDao).updateBalance(walletId, new BigDecimal("19.99"));
		assertEquals(BigDecimal.TEN, history.getBefore());
		assertEquals(new BigDecimal("19.99"), history.getAfter());
		assertEquals(new BigDecimal("9.99"), history.getAmount());
		assertEquals(ref, history.getRef());
		assertNotNull(history.getCreatetime());
		assertEquals(walletId2, history.getRefWalletId());
		assertNotNull(history.getId());
		assertEquals(WalletHistoryType.FUNDIN, history.getType());
		assertEquals(walletId, history.getWalletId());
		Mockito.verifyNoMoreInteractions(walletDao, historyDao, infoService);
	}

	@Test
	public void testTransferComplainsIfFromWalletIdIsNull() {
		try {
			service.transfer(null, null, null, null);
			fail();
		} catch (InvalidParameterException e) {
			assertEquals("fromWalletId should not be null", e.getMessage());
		}
		Mockito.verifyZeroInteractions(walletDao, historyDao, infoService);
	}

	@Test
	public void testTransferComplainsIfToWalletIdIsNull() {
		try {
			service.transfer(walletId, null, null, null);
			fail();
		} catch (InvalidParameterException e) {
			assertEquals("toWalletId should not be null", e.getMessage());
		}
		Mockito.verifyZeroInteractions(walletDao, historyDao, infoService);
	}

	@Test
	public void testTransferComplainsIfAmountIsNull() {
		try {
			service.transfer(walletId, walletId2, null, null);
			fail();
		} catch (InvalidParameterException e) {
			assertEquals("amount should not be null", e.getMessage());
		}
		Mockito.verifyZeroInteractions(walletDao, historyDao, infoService);
	}

	@Test
	public void testTransferComplainsIfReferenceIsNull() {
		try {
			service.transfer(walletId, walletId2, BigDecimal.ONE, null);
			fail();
		} catch (InvalidParameterException e) {
			assertEquals("reference should not be null", e.getMessage());
		}
		Mockito.verifyZeroInteractions(walletDao, historyDao, infoService);
	}

	@Test
	public void testTransferComplainsIfFromWalletIdIsTheSameAsToWalletId() {
		try {
			service.transfer(walletId, walletId, BigDecimal.ONE, ref);
			fail();
		} catch (InvalidParameterException e) {
			assertEquals("fromWalletId and toWalletId should not be the same", e.getMessage());
		}
		Mockito.verifyZeroInteractions(walletDao, historyDao, infoService);
	}

	@Test
	public void testTransferComplainsIfAmountIsNegative() {
		try {
			service.transfer(walletId, walletId2, BigDecimal.valueOf(-0.001), ref);
			fail();
		} catch (InvalidParameterException e) {
			assertEquals("invalid amount", e.getMessage());
		}
		Mockito.verifyZeroInteractions(walletDao, historyDao, infoService);
	}

	@Test
	public void testTransferSuccess() {
		Map<Long, Wallet> wallets = new HashMap<Long, Wallet>();
		wallets.put(walletId, wallet);
		wallets.put(walletId2, wallet2);
		Mockito.doReturn(wallets).when(infoService).lockAndVerifyWalletInOrder(walletId, walletId2);
		Mockito.doReturn(new WalletHistory()).when(spyService).changeBalance(wallet, new BigDecimal("-10.00"), ref, WalletHistoryType.FUNDOUT, walletId2);
		Mockito.doReturn(new WalletHistory()).when(spyService).changeBalance(wallet2, new BigDecimal("10.00"), ref, WalletHistoryType.FUNDIN, walletId);
		spyService.transfer(walletId, walletId2, new BigDecimal(10.001), ref);
		InOrder inOrder = Mockito.inOrder(spyService, walletDao, historyDao, infoService);
		inOrder.verify(infoService).lockAndVerifyWalletInOrder(walletId, walletId2);
		inOrder.verify(spyService).changeBalance(wallet, new BigDecimal("-10.00"), ref, WalletHistoryType.FUNDOUT, walletId2);
		inOrder.verify(spyService).changeBalance(wallet2, new BigDecimal("10.00"), ref, WalletHistoryType.FUNDIN, walletId);
		Mockito.verifyNoMoreInteractions(walletDao, historyDao, infoService);
	}
}
