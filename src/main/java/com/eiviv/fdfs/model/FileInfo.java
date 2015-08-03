package com.eiviv.fdfs.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("serial")
public class FileInfo implements Serializable {
	
	protected String ip;
	protected long fileSize;
	protected Date ceateTime;
	protected int crc32;
	
	public FileInfo(long fileSize, int ceateTime, int crc32, String ip) {
		this.fileSize = fileSize;
		this.ceateTime = new Date(ceateTime * 1000L);
		this.crc32 = crc32;
		this.ip = ip;
	}
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public long getFileSize() {
		return fileSize;
	}
	
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	
	public Date getCeateTime() {
		return ceateTime;
	}
	
	public void setCeateTime(Date ceateTime) {
		this.ceateTime = ceateTime;
	}
	
	public int getCrc32() {
		return crc32;
	}
	
	public void setCrc32(int crc32) {
		this.crc32 = crc32;
	}
	
	@Override
	public String toString() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return "ip = " + this.ip + ", " + "fileSize = " + this.fileSize + ", " + "ceateTime = " + df.format(this.ceateTime) + ", " + "crc32 = "
				+ this.crc32;
	}
}
