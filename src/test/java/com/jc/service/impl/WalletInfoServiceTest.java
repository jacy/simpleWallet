package com.jc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.jc.dao.WalletDao;
import com.jc.domain.Wallet;
import com.jc.exception.BadRequestException;
import com.jc.exception.Errors;

@RunWith(MockitoJUnitRunner.class)
public class WalletInfoServiceTest {
	@InjectMocks
	private WalletInfoServiceImpl service;

	@Mock
	private WalletDao walletDao;
	private Long walletId = 1L;
	private Long walletId2 = 2L;
	private Long walletId3 = 3L;

	private Wallet wallet;
	private Wallet wallet2;
	private Wallet wallet3;

	@Before
	public void setup() {
		wallet = new Wallet(walletId, BigDecimal.TEN, Wallet.ENABLE);
		wallet2 = new Wallet(walletId2, BigDecimal.ZERO, Wallet.ENABLE);
		wallet3 = new Wallet(walletId3, BigDecimal.ONE, Wallet.ENABLE);
	}

	@Test
	public void lockAndVerifyWalletSuccess() {
		Mockito.when(walletDao.lockById(walletId)).thenReturn(wallet);
		assertEquals(wallet, service.lockAndVerifyWallet(walletId));
		Mockito.verify(walletDao).lockById(walletId);
		Mockito.verifyNoMoreInteractions(walletDao);
	}

	@Test
	public void lockAndVerifyComplainsIfWalletNotFound() {
		Mockito.when(walletDao.lockById(walletId)).thenReturn(null);
		try {
			service.lockAndVerifyWallet(walletId);
			fail();
		} catch (BadRequestException e) {
			assertEquals(Errors.WALLET_NOT_FOUND_OR_DISABLE, e.getError());
		}
		Mockito.verify(walletDao).lockById(walletId);
		Mockito.verifyNoMoreInteractions(walletDao);
	}

	@Test
	public void lockAndVerifyComplainsIfWalletIsDisable() {
		wallet.setStatus(Wallet.DISABLE);
		Mockito.when(walletDao.lockById(walletId)).thenReturn(wallet);
		try {
			service.lockAndVerifyWallet(walletId);
			fail();
		} catch (BadRequestException e) {
			assertEquals(Errors.WALLET_NOT_FOUND_OR_DISABLE, e.getError());
		}
		Mockito.verify(walletDao).lockById(walletId);
		Mockito.verifyNoMoreInteractions(walletDao);
	}

	@Test
	public void lockAndVerifyWalletInOrderSuccess() {
		Mockito.when(walletDao.lockById(walletId)).thenReturn(wallet);
		Mockito.when(walletDao.lockById(walletId2)).thenReturn(wallet2);
		Mockito.when(walletDao.lockById(walletId3)).thenReturn(wallet3);
		Map<Long, Wallet> result = service.lockAndVerifyWalletInOrder(walletId2, walletId3, walletId);
		assertEquals(wallet, result.get(walletId));
		assertEquals(wallet2, result.get(walletId2));
		assertEquals(wallet3, result.get(walletId3));
		InOrder inOrder = Mockito.inOrder(walletDao);
		inOrder.verify(walletDao).lockById(walletId);
		inOrder.verify(walletDao).lockById(walletId2);
		inOrder.verify(walletDao).lockById(walletId3);
		Mockito.verifyNoMoreInteractions(walletDao);
	}

	@Test
	public void lockAndVerifyComplainsIfFirstWalletNotFound() {
		Mockito.when(walletDao.lockById(walletId)).thenReturn(null);

		try {
			service.lockAndVerifyWalletInOrder(walletId2, walletId3, walletId);
			fail();
		} catch (BadRequestException e) {
			assertEquals(Errors.WALLET_NOT_FOUND_OR_DISABLE, e.getError());
		}
		InOrder inOrder = Mockito.inOrder(walletDao);
		inOrder.verify(walletDao).lockById(walletId);
		Mockito.verifyNoMoreInteractions(walletDao);
	}

	@Test
	public void lockAndVerifyComplainsIfLastWalletNotFound() {
		Mockito.when(walletDao.lockById(walletId)).thenReturn(wallet);
		Mockito.when(walletDao.lockById(walletId2)).thenReturn(wallet2);
		Mockito.when(walletDao.lockById(walletId3)).thenReturn(null);

		try {
			service.lockAndVerifyWalletInOrder(walletId2, walletId3, walletId);
			fail();
		} catch (BadRequestException e) {
			assertEquals(Errors.WALLET_NOT_FOUND_OR_DISABLE, e.getError());
		}
		InOrder inOrder = Mockito.inOrder(walletDao);
		inOrder.verify(walletDao).lockById(walletId);
		inOrder.verify(walletDao).lockById(walletId2);
		inOrder.verify(walletDao).lockById(walletId3);
		Mockito.verifyNoMoreInteractions(walletDao);
	}
	
	@Test
	public void lockAndVerifyComplainsIfFirstWalletIsDisable() {
		wallet.setStatus(Wallet.DISABLE);
		Mockito.when(walletDao.lockById(walletId)).thenReturn(wallet);

		try {
			service.lockAndVerifyWalletInOrder(walletId2, walletId3, walletId);
			fail();
		} catch (BadRequestException e) {
			assertEquals(Errors.WALLET_NOT_FOUND_OR_DISABLE, e.getError());
		}
		InOrder inOrder = Mockito.inOrder(walletDao);
		inOrder.verify(walletDao).lockById(walletId);
		Mockito.verifyNoMoreInteractions(walletDao);
	}

	@Test
	public void lockAndVerifyComplainsIfLastWalletIsDisable() {
		Mockito.when(walletDao.lockById(walletId)).thenReturn(wallet);
		Mockito.when(walletDao.lockById(walletId2)).thenReturn(wallet2);
		wallet3.setStatus(Wallet.DISABLE);
		Mockito.when(walletDao.lockById(walletId3)).thenReturn(wallet3);

		try {
			service.lockAndVerifyWalletInOrder(walletId2, walletId3, walletId);
			fail();
		} catch (BadRequestException e) {
			assertEquals(Errors.WALLET_NOT_FOUND_OR_DISABLE, e.getError());
		}
		InOrder inOrder = Mockito.inOrder(walletDao);
		inOrder.verify(walletDao).lockById(walletId);
		inOrder.verify(walletDao).lockById(walletId2);
		inOrder.verify(walletDao).lockById(walletId3);
		Mockito.verifyNoMoreInteractions(walletDao);
	}
}
