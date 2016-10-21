package com.jc.domain;

import java.math.BigDecimal;
import java.util.Date;

public class WalletHistory {
	private String id;
	private Long walletId;
	private BigDecimal before;
	private BigDecimal after;
	private Date createtime;
	private String ref;
	private BigDecimal amount;
	private WalletHistoryType type;
	private String description;
	

	public WalletHistory(String id, Long walletId, BigDecimal before, BigDecimal after, Date createtime, String ref, BigDecimal amount, WalletHistoryType type, String description) {
		this.id = id;
		this.walletId = walletId;
		this.before = before;
		this.after = after;
		this.createtime = createtime;
		this.ref = ref;
		this.amount = amount;
		this.type = type;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getWalletId() {
		return walletId;
	}

	public void setWalletId(Long walletId) {
		this.walletId = walletId;
	}

	public BigDecimal getBefore() {
		return before;
	}

	public void setBefore(BigDecimal before) {
		this.before = before;
	}

	public BigDecimal getAfter() {
		return after;
	}

	public void setAfter(BigDecimal after) {
		this.after = after;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public WalletHistoryType getType() {
		return type;
	}

	public void setType(WalletHistoryType type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}