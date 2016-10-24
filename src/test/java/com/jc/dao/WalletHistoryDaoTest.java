package com.jc.dao;

import static com.jc.util.DateFormatUtil.parse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import com.jc.domain.WalletHistory;
import com.jc.domain.WalletHistoryType;

public class WalletHistoryDaoTest extends BaseMapperTestFramwork {
	@Autowired
	private WalletHistoryDao walletHistoryDao;

	@Test
	public void testGetByRefAndType() {
		WalletHistory historyFromDB = walletHistoryDao.getByRefAndType("ref1", WalletHistoryType.FUNDIN);
		assertEquals(new WalletHistory("id1", 1L, BigDecimal.ZERO, BigDecimal.valueOf(100.23), parse("2016-10-24 20:30:40"), "ref1", BigDecimal.valueOf(100.23), WalletHistoryType.FUNDIN, null), historyFromDB);
		historyFromDB = walletHistoryDao.getByRefAndType("ref2", WalletHistoryType.FUNDIN);
		assertEquals(new WalletHistory("id2", 2L, BigDecimal.ZERO, BigDecimal.valueOf(10000), parse("2016-10-25 20:30:40"), "ref2", BigDecimal.valueOf(10000), WalletHistoryType.FUNDIN, null), historyFromDB);
		assertNull(walletHistoryDao.getByRefAndType("ref1", WalletHistoryType.FUNDOUT));
	}

	@Test
	public void testInsert() {
		String ref = UUID.randomUUID().toString();
		assertNull(walletHistoryDao.getByRefAndType(ref, WalletHistoryType.FUNDOUT));
		Date date = new Date();
		WalletHistory history = new WalletHistory("testinsert1", 1L, BigDecimal.ZERO, BigDecimal.TEN, date, ref, BigDecimal.TEN, WalletHistoryType.FUNDIN, "testDescription");
		walletHistoryDao.insert(history);
		WalletHistory historyFromDB = walletHistoryDao.getByRefAndType(ref, WalletHistoryType.FUNDIN);
		assertEquals(history, historyFromDB);
		try {
			walletHistoryDao.insert(history);
			fail();
		} catch (DuplicateKeyException e) {
			assertTrue(e.getMessage().contains("unique constraint "));
		}
	}
}
