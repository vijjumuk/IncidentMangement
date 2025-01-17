package com.homedepot.di.xd.efs.exception;

public class ReportException extends Exception {

	private static final long serialVersionUID = 9087673904055914989L;

	public ReportException() {
		super();
	}

	public ReportException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ReportException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReportException(String message) {
		super(message);
	}

	public ReportException(Throwable cause) {
		super(cause);
	}

}
