package com.jc.util;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

public class BigDecimalUtilTest {

	@Test
	public void testAddShouldRoundDown() {
		BigDecimal sum = BigDecimalUtil.add(new BigDecimal(99.998D), new BigDecimal(100.001));
		Assert.assertEquals(new BigDecimal("199.99"), sum);
		
		sum = BigDecimalUtil.add(new BigDecimal(99.998D), new BigDecimal(-10.56));
		Assert.assertEquals(new BigDecimal("89.43"), sum);
	}
}
