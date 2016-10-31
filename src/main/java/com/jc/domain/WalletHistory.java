package com.jc.domain;

import java.math.BigDecimal;
import java.util.Date;

public class WalletHistory {
	private String id;
	private Long walletId;
	private Long refWalletId;
	private BigDecimal before;
	private BigDecimal after;
	private Date createtime;
	private String ref;
	private BigDecimal amount;
	private WalletHistoryType type;

	public WalletHistory() {
		super();
	}

	public WalletHistory(String id, Long walletId, BigDecimal before, BigDecimal after, Date createtime, String ref, BigDecimal amount, WalletHistoryType type, Long refWalletId) {
		this.id = id;
		this.walletId = walletId;
		this.before = before;
		this.after = after;
		this.createtime = createtime;
		this.ref = ref;
		this.amount = amount;
		this.type = type;
		this.refWalletId = refWalletId;
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

	public Long getRefWalletId() {
		return refWalletId;
	}

	public void setRefWalletId(Long refWalletId) {
		this.refWalletId = refWalletId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((after == null) ? 0 : after.hashCode());
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + ((before == null) ? 0 : before.hashCode());
		result = prime * result + ((createtime == null) ? 0 : createtime.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((ref == null) ? 0 : ref.hashCode());
		result = prime * result + ((refWalletId == null) ? 0 : refWalletId.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((walletId == null) ? 0 : walletId.hashCode());
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
		WalletHistory other = (WalletHistory) obj;
		if (after == null) {
			if (other.after != null)
				return false;
		} else if (after.compareTo(other.after) != 0)
			return false;
		if (amount == null) {
			if (other.amount != null)
				return false;
		} else if (amount.compareTo(other.amount) != 0)
			return false;
		if (before == null) {
			if (other.before != null)
				return false;
		} else if (before.compareTo(other.before) != 0)
			return false;
		if (createtime == null) {
			if (other.createtime != null)
				return false;
		} else if (!createtime.equals(other.createtime))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (ref == null) {
			if (other.ref != null)
				return false;
		} else if (!ref.equals(other.ref))
			return false;
		if (refWalletId == null) {
			if (other.refWalletId != null)
				return false;
		} else if (!refWalletId.equals(other.refWalletId))
			return false;
		if (type != other.type)
			return false;
		if (walletId == null) {
			if (other.walletId != null)
				return false;
		} else if (!walletId.equals(other.walletId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WalletHistory [id=" + id + ", walletId=" + walletId + ", refWalletId=" + refWalletId + ", before=" + before + ", after=" + after + ", createtime=" + createtime + ", ref=" + ref + ", amount=" + amount + ", type=" + type + "]";
	}

}