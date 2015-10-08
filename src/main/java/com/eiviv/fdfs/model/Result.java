package com.eiviv.fdfs.model;

import java.io.Serializable;

import com.eiviv.fdfs.context.Context;

public class Result<T extends Serializable> {
	
	private int code;
	private String state;
	private T data;
	
	public Result(int code) {
		this.code = code;
		this.setState(code);
	}
	
	public Result(int code, T data) {
		this(code);
		this.data = data;
	}
	
	public boolean isSuccess() {
		return this.code == Context.SUCCESS_CODE;
	}
	
	public int getCode() {
		return code;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	
	public String getState() {
		return state;
	}
	
	public void setState(int code) {
		String state = "";
		
		switch (code) {
		case Context.SUCCESS_CODE:
			state = "成功";
			break;
		case Context.ERR_NO_ENOENT:
			state = "未找到指定访问点";
			break;
		case Context.ERR_NO_EIO:
			state = "IO异常";
			break;
		case Context.ERR_NO_EBUSY:
			state = "系统繁忙";
			break;
		case Context.ERR_NO_EINVAL:
			state = "无效访问";
			break;
		case Context.ERR_NO_ENOSPC:
			state = "剩余空间不足";
			break;
		case Context.ECONNREFUSED:
			state = "拒绝访问";
			break;
		case Context.ERR_NO_EALREADY:
			state = "已在运行中";
			break;
		default:
			break;
		}
		
		this.state = state;
	}
	
	public T getData() {
		return data;
	}
	
	public void setData(T data) {
		this.data = data;
	}
	
}
