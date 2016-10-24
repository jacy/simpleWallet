package com.jc.util;

import java.math.BigDecimal;

public class BigDecimalUtil {
	private static final int TWO_DIGITS = 2;

	public static BigDecimal add(BigDecimal v1, BigDecimal v2) {
		return roundDown(v1.add(v2));
	}

	public static BigDecimal roundDown(BigDecimal num) {
		if (num == null) {
			return num;
		}
		return num.setScale(TWO_DIGITS, BigDecimal.ROUND_DOWN);
	}
}
