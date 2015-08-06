package com.eiviv.fdfs.exception;

@SuppressWarnings("serial")
public class FastdfsClientException extends Exception {
	
	public FastdfsClientException() {
		super();
	}
	
	public FastdfsClientException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public FastdfsClientException(String message) {
		super(message);
	}
	
	public FastdfsClientException(Throwable cause) {
		super(cause);
	}
	
}
