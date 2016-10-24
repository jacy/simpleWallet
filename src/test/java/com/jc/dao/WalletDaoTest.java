package com.jc.dao;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.jc.domain.Wallet;

public class WalletDaoTest extends BaseMapperTestFramwork {
	@Autowired
	private WalletDao walletDao;

	@Test
	public void testLockById() {
		assertEquals(new Wallet(1L, BigDecimal.valueOf(100.23), 1), walletDao.lockById(1L));
		assertEquals(new Wallet(2L, BigDecimal.valueOf(10000), 0), walletDao.lockById(2L));
		assertNull(walletDao.lockById(-1L));
	}
	
	@Test
	public void testUpdateBalance() {
		assertEquals(new Wallet(1L, BigDecimal.valueOf(100.23), 1), walletDao.lockById(1L));
		walletDao.updateBalance(1L, BigDecimal.valueOf(9999.999));
		assertEquals(new Wallet(1L, BigDecimal.valueOf(10000.00), 1), walletDao.lockById(1L));
		walletDao.updateBalance(1L, BigDecimal.valueOf(88.99));
		assertEquals(new Wallet(1L, BigDecimal.valueOf(88.99), 1), walletDao.lockById(1L));
	}

}
