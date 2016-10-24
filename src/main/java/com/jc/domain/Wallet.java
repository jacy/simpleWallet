package com.jc.domain;

import java.math.BigDecimal;

public class Wallet {
	private Long id;
	private BigDecimal balance;
	private int status;

	public static final int DISABLE = 0;
	public static final int ENABLE = 1;

	public Wallet(Long id, BigDecimal balance, int status) {
		this.id = id;
		this.balance = balance;
		this.status = status;
	}

	public Wallet() {
		super();
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((balance == null) ? 0 : balance.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + status;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Wallet other = (Wallet) obj;
		if (balance == null) {
			if (other.balance != null)
				return false;
		} else if (balance.compareTo(other.balance) != 0)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (status != other.status)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Wallet [id=" + id + ", balance=" + balance + ", status=" + status + "]";
	}

}
