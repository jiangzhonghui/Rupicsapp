package com.yi4all.rupics.service.enumtype;

public enum RemoteInvokeStatus {

	NEW("新建"),
	ONGOING("进行中"),
	SUCCEED("成功"),
	FAILED("失败");

	private final String displayName;

	RemoteInvokeStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
