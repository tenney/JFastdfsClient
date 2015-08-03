package com.eiviv.fdfs.model;

import java.io.Serializable;
import java.util.Date;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.utils.ByteUtils;

@SuppressWarnings("serial")
public class StorageInfo implements Serializable {
	
	public final static int BYTE_SIZE = 612;
	
	protected byte status;
	protected String id;
	protected String ipAddr;
	protected String domainName; // http domain name
	protected String srcIpAddr;
	protected String version;
	protected Date joinTime; // storage join timestamp (create timestamp)
	protected Date upTime; // storage service started timestamp
	protected long totalMB; // total disk storage in MB
	protected long freeMB; // free disk storage in MB
	
	protected int uploadPriority; // upload priority
	protected int storePathCount; // store base path count of each storage
	protected int subdirCountPerPath;
	protected int currentWritePath; // current write path index
	protected int storagePort;
	protected int storageHttpPort; // storage http server port
	
	protected long totalUploadCount;
	protected long successUploadCount;
	protected long totalAppendCount;
	protected long successAppendCount;
	protected long totalModifyCount;
	protected long successModifyCount;
	protected long totalTruncateCount;
	protected long successTruncateCount;
	protected long totalSetMetaCount;
	protected long successSetMetaCount;
	protected long totalDeleteCount;
	protected long successDeleteCount;
	protected long totalDownloadCount;
	protected long successDownloadCount;
	protected long totalGetMetaCount;
	protected long successGetMetaCount;
	protected long totalCreateLinkCount;
	protected long successCreateLinkCount;
	protected long totalDeleteLinkCount;
	protected long successDeleteLinkCount;
	protected long totalUploadBytes;
	protected long successUploadBytes;
	protected long totalAppendBytes;
	protected long successAppendBytes;
	protected long totalModifyBytes;
	protected long successModifyBytes;
	protected long totalDownloadloadBytes;
	protected long successDownloadloadBytes;
	protected long totalSyncInBytes;
	protected long successSyncInBytes;
	protected long totalSyncOutBytes;
	protected long successSyncOutBytes;
	protected long totalFileOpenCount;
	protected long successFileOpenCount;
	protected long totalFileReadCount;
	protected long successFileReadCount;
	protected long totalFileWriteCount;
	protected long successFileWriteCount;
	
	protected Date lastSourceUpdate;
	protected Date lastSyncUpdate;
	protected Date lastSyncedTimestamp;
	protected Date lastHeartBeatTime;
	protected boolean ifTrunkServer;
	
	public StorageInfo(byte[] data, int offset) {
		this.status = data[offset];
		offset += 1;
		this.id = new String(data, offset, Context.FDFS_STORAGE_ID_MAX_SIZE).trim();
		offset += Context.FDFS_STORAGE_ID_MAX_SIZE;
		this.ipAddr = new String(data, offset, Context.FDFS_IPADDR_SIZE).trim();
		offset += Context.FDFS_IPADDR_SIZE;
		this.domainName = new String(data, offset, Context.FDFS_DOMAIN_NAME_MAX_SIZE).trim();
		offset += Context.FDFS_DOMAIN_NAME_MAX_SIZE;
		this.srcIpAddr = new String(data, offset, Context.FDFS_IPADDR_SIZE).trim();
		offset += Context.FDFS_IPADDR_SIZE;
		this.version = new String(data, offset, Context.FDFS_VERSION_SIZE).trim();
		offset += Context.FDFS_VERSION_SIZE;
		this.joinTime = new Date(ByteUtils.bytes2long(data, offset) * 1000);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.upTime = new Date(ByteUtils.bytes2long(data, offset) * 1000);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalMB = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.freeMB = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.uploadPriority = (int) ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.storePathCount = (int) ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.subdirCountPerPath = (int) ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.currentWritePath = (int) ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.storagePort = (int) ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.storageHttpPort = (int) ByteUtils.bytes2long(data, offset);
		
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalUploadCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successUploadCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalAppendCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successAppendCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalModifyCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successModifyCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalTruncateCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successTruncateCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalSetMetaCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successSetMetaCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalDeleteCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successDeleteCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalDownloadCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successDownloadCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalGetMetaCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successGetMetaCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalCreateLinkCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successCreateLinkCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalDeleteLinkCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successDeleteLinkCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalUploadBytes = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successUploadBytes = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalAppendBytes = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successAppendBytes = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalModifyBytes = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successModifyBytes = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalDownloadloadBytes = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successDownloadloadBytes = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalSyncInBytes = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successSyncInBytes = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalSyncOutBytes = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successSyncOutBytes = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalFileOpenCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successFileOpenCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalFileReadCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successFileReadCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.totalFileWriteCount = ByteUtils.bytes2long(data, offset);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.successFileWriteCount = ByteUtils.bytes2long(data, offset);
		
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.lastSourceUpdate = new Date(ByteUtils.bytes2long(data, offset) * 1000);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.lastSyncUpdate = new Date(ByteUtils.bytes2long(data, offset) * 1000);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.lastSyncedTimestamp = new Date(ByteUtils.bytes2long(data, offset) * 1000);
		offset += Context.FDFS_PROTO_PKG_LEN_SIZE;
		this.lastHeartBeatTime = new Date(ByteUtils.bytes2long(data, offset) * 1000);
		
		this.ifTrunkServer = (data[offset] != 0);
	}
	
	public byte getStatus() {
		return status;
	}
	
	public void setStatus(byte status) {
		this.status = status;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getIpAddr() {
		return ipAddr;
	}
	
	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}
	
	public String getSrcIpAddr() {
		return srcIpAddr;
	}
	
	public void setSrcIpAddr(String srcIpAddr) {
		this.srcIpAddr = srcIpAddr;
	}
	
	public String getDomainName() {
		return domainName;
	}
	
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
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
	
	public int getUploadPriority() {
		return uploadPriority;
	}
	
	public void setUploadPriority(int uploadPriority) {
		this.uploadPriority = uploadPriority;
	}
	
	public Date getJoinTime() {
		return joinTime;
	}
	
	public void setJoinTime(Date joinTime) {
		this.joinTime = joinTime;
	}
	
	public Date getUpTime() {
		return upTime;
	}
	
	public void setUpTime(Date upTime) {
		this.upTime = upTime;
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
	
	public int getCurrentWritePath() {
		return currentWritePath;
	}
	
	public void setCurrentWritePath(int currentWritePath) {
		this.currentWritePath = currentWritePath;
	}
	
	public long getTotalUploadCount() {
		return totalUploadCount;
	}
	
	public void setTotalUploadCount(long totalUploadCount) {
		this.totalUploadCount = totalUploadCount;
	}
	
	public long getSuccessUploadCount() {
		return successUploadCount;
	}
	
	public void setSuccessUploadCount(long successUploadCount) {
		this.successUploadCount = successUploadCount;
	}
	
	public long getTotalAppendCount() {
		return totalAppendCount;
	}
	
	public void setTotalAppendCount(long totalAppendCount) {
		this.totalAppendCount = totalAppendCount;
	}
	
	public long getSuccessAppendCount() {
		return successAppendCount;
	}
	
	public void setSuccessAppendCount(long successAppendCount) {
		this.successAppendCount = successAppendCount;
	}
	
	public long getTotalModifyCount() {
		return totalModifyCount;
	}
	
	public void setTotalModifyCount(long totalModifyCount) {
		this.totalModifyCount = totalModifyCount;
	}
	
	public long getSuccessModifyCount() {
		return successModifyCount;
	}
	
	public void setSuccessModifyCount(long successModifyCount) {
		this.successModifyCount = successModifyCount;
	}
	
	public long getTotalTruncateCount() {
		return totalTruncateCount;
	}
	
	public void setTotalTruncateCount(long totalTruncateCount) {
		this.totalTruncateCount = totalTruncateCount;
	}
	
	public long getSuccessTruncateCount() {
		return successTruncateCount;
	}
	
	public void setSuccessTruncateCount(long successTruncateCount) {
		this.successTruncateCount = successTruncateCount;
	}
	
	public long getTotalSetMetaCount() {
		return totalSetMetaCount;
	}
	
	public void setTotalSetMetaCount(long totalSetMetaCount) {
		this.totalSetMetaCount = totalSetMetaCount;
	}
	
	public long getSuccessSetMetaCount() {
		return successSetMetaCount;
	}
	
	public void setSuccessSetMetaCount(long successSetMetaCount) {
		this.successSetMetaCount = successSetMetaCount;
	}
	
	public long getTotalDeleteCount() {
		return totalDeleteCount;
	}
	
	public void setTotalDeleteCount(long totalDeleteCount) {
		this.totalDeleteCount = totalDeleteCount;
	}
	
	public long getSuccessDeleteCount() {
		return successDeleteCount;
	}
	
	public void setSuccessDeleteCount(long successDeleteCount) {
		this.successDeleteCount = successDeleteCount;
	}
	
	public long getTotalDownloadCount() {
		return totalDownloadCount;
	}
	
	public void setTotalDownloadCount(long totalDownloadCount) {
		this.totalDownloadCount = totalDownloadCount;
	}
	
	public long getSuccessDownloadCount() {
		return successDownloadCount;
	}
	
	public void setSuccessDownloadCount(long successDownloadCount) {
		this.successDownloadCount = successDownloadCount;
	}
	
	public long getTotalGetMetaCount() {
		return totalGetMetaCount;
	}
	
	public void setTotalGetMetaCount(long totalGetMetaCount) {
		this.totalGetMetaCount = totalGetMetaCount;
	}
	
	public long getSuccessGetMetaCount() {
		return successGetMetaCount;
	}
	
	public void setSuccessGetMetaCount(long successGetMetaCount) {
		this.successGetMetaCount = successGetMetaCount;
	}
	
	public long getTotalCreateLinkCount() {
		return totalCreateLinkCount;
	}
	
	public void setTotalCreateLinkCount(long totalCreateLinkCount) {
		this.totalCreateLinkCount = totalCreateLinkCount;
	}
	
	public long getSuccessCreateLinkCount() {
		return successCreateLinkCount;
	}
	
	public void setSuccessCreateLinkCount(long successCreateLinkCount) {
		this.successCreateLinkCount = successCreateLinkCount;
	}
	
	public long getTotalDeleteLinkCount() {
		return totalDeleteLinkCount;
	}
	
	public void setTotalDeleteLinkCount(long totalDeleteLinkCount) {
		this.totalDeleteLinkCount = totalDeleteLinkCount;
	}
	
	public long getSuccessDeleteLinkCount() {
		return successDeleteLinkCount;
	}
	
	public void setSuccessDeleteLinkCount(long successDeleteLinkCount) {
		this.successDeleteLinkCount = successDeleteLinkCount;
	}
	
	public long getTotalUploadBytes() {
		return totalUploadBytes;
	}
	
	public void setTotalUploadBytes(long totalUploadBytes) {
		this.totalUploadBytes = totalUploadBytes;
	}
	
	public long getSuccessUploadBytes() {
		return successUploadBytes;
	}
	
	public void setSuccessUploadBytes(long successUploadBytes) {
		this.successUploadBytes = successUploadBytes;
	}
	
	public long getTotalAppendBytes() {
		return totalAppendBytes;
	}
	
	public void setTotalAppendBytes(long totalAppendBytes) {
		this.totalAppendBytes = totalAppendBytes;
	}
	
	public long getSuccessAppendBytes() {
		return successAppendBytes;
	}
	
	public void setSuccessAppendBytes(long successAppendBytes) {
		this.successAppendBytes = successAppendBytes;
	}
	
	public long getTotalModifyBytes() {
		return totalModifyBytes;
	}
	
	public void setTotalModifyBytes(long totalModifyBytes) {
		this.totalModifyBytes = totalModifyBytes;
	}
	
	public long getSuccessModifyBytes() {
		return successModifyBytes;
	}
	
	public void setSuccessModifyBytes(long successModifyBytes) {
		this.successModifyBytes = successModifyBytes;
	}
	
	public long getTotalDownloadloadBytes() {
		return totalDownloadloadBytes;
	}
	
	public void setTotalDownloadloadBytes(long totalDownloadloadBytes) {
		this.totalDownloadloadBytes = totalDownloadloadBytes;
	}
	
	public long getSuccessDownloadloadBytes() {
		return successDownloadloadBytes;
	}
	
	public void setSuccessDownloadloadBytes(long successDownloadloadBytes) {
		this.successDownloadloadBytes = successDownloadloadBytes;
	}
	
	public long getTotalSyncInBytes() {
		return totalSyncInBytes;
	}
	
	public void setTotalSyncInBytes(long totalSyncInBytes) {
		this.totalSyncInBytes = totalSyncInBytes;
	}
	
	public long getSuccessSyncInBytes() {
		return successSyncInBytes;
	}
	
	public void setSuccessSyncInBytes(long successSyncInBytes) {
		this.successSyncInBytes = successSyncInBytes;
	}
	
	public long getTotalSyncOutBytes() {
		return totalSyncOutBytes;
	}
	
	public void setTotalSyncOutBytes(long totalSyncOutBytes) {
		this.totalSyncOutBytes = totalSyncOutBytes;
	}
	
	public long getSuccessSyncOutBytes() {
		return successSyncOutBytes;
	}
	
	public void setSuccessSyncOutBytes(long successSyncOutBytes) {
		this.successSyncOutBytes = successSyncOutBytes;
	}
	
	public long getTotalFileOpenCount() {
		return totalFileOpenCount;
	}
	
	public void setTotalFileOpenCount(long totalFileOpenCount) {
		this.totalFileOpenCount = totalFileOpenCount;
	}
	
	public long getSuccessFileOpenCount() {
		return successFileOpenCount;
	}
	
	public void setSuccessFileOpenCount(long successFileOpenCount) {
		this.successFileOpenCount = successFileOpenCount;
	}
	
	public long getTotalFileReadCount() {
		return totalFileReadCount;
	}
	
	public void setTotalFileReadCount(long totalFileReadCount) {
		this.totalFileReadCount = totalFileReadCount;
	}
	
	public long getSuccessFileReadCount() {
		return successFileReadCount;
	}
	
	public void setSuccessFileReadCount(long successFileReadCount) {
		this.successFileReadCount = successFileReadCount;
	}
	
	public long getTotalFileWriteCount() {
		return totalFileWriteCount;
	}
	
	public void setTotalFileWriteCount(long totalFileWriteCount) {
		this.totalFileWriteCount = totalFileWriteCount;
	}
	
	public long getSuccessFileWriteCount() {
		return successFileWriteCount;
	}
	
	public void setSuccessFileWriteCount(long successFileWriteCount) {
		this.successFileWriteCount = successFileWriteCount;
	}
	
	public Date getLastSourceUpdate() {
		return lastSourceUpdate;
	}
	
	public void setLastSourceUpdate(Date lastSourceUpdate) {
		this.lastSourceUpdate = lastSourceUpdate;
	}
	
	public Date getLastSyncUpdate() {
		return lastSyncUpdate;
	}
	
	public void setLastSyncUpdate(Date lastSyncUpdate) {
		this.lastSyncUpdate = lastSyncUpdate;
	}
	
	public Date getLastSyncedTimestamp() {
		return lastSyncedTimestamp;
	}
	
	public void setLastSyncedTimestamp(Date lastSyncedTimestamp) {
		this.lastSyncedTimestamp = lastSyncedTimestamp;
	}
	
	public Date getLastHeartBeatTime() {
		return lastHeartBeatTime;
	}
	
	public void setLastHeartBeatTime(Date lastHeartBeatTime) {
		this.lastHeartBeatTime = lastHeartBeatTime;
	}
	
	public boolean isIfTrunkServer() {
		return ifTrunkServer;
	}
	
	public void setIfTrunkServer(boolean ifTrunkServer) {
		this.ifTrunkServer = ifTrunkServer;
	}
	
}
