package com.yi4all.rupics.service.enumtype;

public enum RemoteRequestAction {

	GET("GET"),
	POST("POST");

	private final String displayName;

	RemoteRequestAction(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
