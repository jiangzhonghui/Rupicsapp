package com.yi4all.rupics;

public interface MyTask {
	public void setMyProgress(final int value);

	public void setMyTitle(final int value, final String... other);

	public boolean isCancelled();
}
