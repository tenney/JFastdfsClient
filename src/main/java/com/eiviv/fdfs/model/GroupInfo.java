package com.eiviv.fdfs.model;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.utils.ByteUtils;

public class GroupInfo {
	
	public final static int BYTE_SIZE = 105;
	
	private String groupName; // name of this group
	private long totalMB; // total disk storage in MB
	private long freeMB; // free disk space in MB
	private long trunkFreeMB; // trunk free space in MB
	private int storageCount; // storage server count
	private int storagePort; // storage server port
	private int storageHttpPort; // storage server HTTP port
	private int activeCount; // active storage server count
	private int currentWriteServer; // current storage server index to upload file
	private int storePathCount; // store base path count of each storage server
	private int subdirCountPerPath; // sub dir count per store path
	private int currentTrunkFileId; // current trunk file id
	
	public GroupInfo(byte[] data, int offset) {
		this.groupName = new String(data, offset, Context.FDFS_GROUP_NAME_MAX_LEN + 1).trim();
		offset += Context.FDFS_GROUP_NAME_MAX_LEN + 1;
		this.totalMB = ByteUtils.bytes2long(data, offset);
		offset += 8;
		this.freeMB = ByteUtils.bytes2long(data, offset);
		offset += 8;
		this.trunkFreeMB = ByteUtils.bytes2long(data, offset);
		offset += 8;
		this.storageCount = (int) ByteUtils.bytes2long(data, offset);
		offset += 8;
		this.storagePort = (int) ByteUtils.bytes2long(data, offset);
		offset += 8;
		this.storageHttpPort = (int) ByteUtils.bytes2long(data, offset);
		offset += 8;
		this.activeCount = (int) ByteUtils.bytes2long(data, offset);
		offset += 8;
		this.currentWriteServer = (int) ByteUtils.bytes2long(data, offset);
		offset += 8;
		this.storePathCount = (int) ByteUtils.bytes2long(data, offset);
		offset += 8;
		this.subdirCountPerPath = (int) ByteUtils.bytes2long(data, offset);
		offset += 8;
		this.currentTrunkFileId = (int) ByteUtils.bytes2long(data, offset);
	}
	
	public String getGroupName() {
		return groupName;
	}
	
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public long getTotalMB() {
		return totalMB;
	}
	
	public void setTotalMB(long totalMB) {
		this.totalMB = totalMB;
	}
	
	public long getFreeMB() {
		return freeMB;
	}
	
	public void setFreeMB(long freeMB) {
		this.freeMB = freeMB;
	}
	
	public long getTrunkFreeMB() {
		return trunkFreeMB;
	}
	
	public void setTrunkFreeMB(long trunkFreeMB) {
		this.trunkFreeMB = trunkFreeMB;
	}
	
	public int getStorageCount() {
		return storageCount;
	}
	
	public void setStorageCount(int storageCount) {
		this.storageCount = storageCount;
	}
	
	public int getStoragePort() {
		return storagePort;
	}
	
	public void setStoragePort(int storagePort) {
		this.storagePort = storagePort;
	}
	
	public int getStorageHttpPort() {
		return storageHttpPort;
	}
	
	public void setStorageHttpPort(int storageHttpPort) {
		this.storageHttpPort = storageHttpPort;
	}
	
	public int getActiveCount() {
		return activeCount;
	}
	
	public void setActiveCount(int activeCount) {
		this.activeCount = activeCount;
	}
	
	public int getCurrentWriteServer() {
		return currentWriteServer;
	}
	
	public void setCurrentWriteServer(int currentWriteServer) {
		this.currentWriteServer = currentWriteServer;
	}
	
	public int getStorePathCount() {
		return storePathCount;
	}
	
	public void setStorePathCount(int storePathCount) {
		this.storePathCount = storePathCount;
	}
	
	public int getSubdirCountPerPath() {
		return subdirCountPerPath;
	}
	
	public void setSubdirCountPerPath(int subdirCountPerPath) {
		this.subdirCountPerPath = subdirCountPerPath;
	}
	
	public int getCurrentTrunkFileId() {
		return currentTrunkFileId;
	}
	
	public void setCurrentTrunkFileId(int currentTrunkFileId) {
		this.currentTrunkFileId = currentTrunkFileId;
	}
	
}
