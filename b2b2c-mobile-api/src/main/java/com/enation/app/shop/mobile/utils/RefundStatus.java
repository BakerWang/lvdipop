package com.enation.app.shop.mobile.utils;


public enum RefundStatus {
	waitapprove("等待审核", 0), approve("审核通过", 1), notapprove("审核不通过", 2), waitpay("等待平台退款", 3), refused("已拒绝",
			4), refunded("已退款", 6);

	private String name;
	private int value;

	private RefundStatus(String _name, int _value) {
		this.name = _name;
		this.value = _value;
	}

	public static String valueOf(int status) {
		RefundStatus[] statusList = values();
		for (RefundStatus refundStatus : statusList) {
			if (refundStatus.getValue() == status) {
				return refundStatus.getName();
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}
	

}
