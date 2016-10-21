package com.jc.domain;

import java.math.BigDecimal;

public class Wallet {
	private Long id;
	private BigDecimal balance;
	private int status;

	public static final int ENABLE = 1;

	public Wallet(Long id, BigDecimal balance, int status) {
		super();
		this.id = id;
		this.balance = balance;
		this.status = status;
	}

	public boolean isEnable() {
		return ENABLE == status;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
