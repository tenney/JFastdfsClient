package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.exception.FastdfsClientException;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.utils.ByteUtils;

public class UpdateMetaDataCmd extends AbstractCmd<Boolean> {
	
	private String group;
	private String fileName;
	private Map<String, String> metaData;
	
	/**
	 * 实例化
	 * 
	 * @param group 组名
	 * @param fileName 文件名
	 * @param metaData 云信息
	 */
	public UpdateMetaDataCmd(String group, String fileName, Map<String, String> metaData) {
		this.group = group;
		this.fileName = fileName;
		this.metaData = metaData;
	}
	
	@Override
	protected RequestContext getRequestContext() {
		byte[] groupByte = group.getBytes(Context.CHARSET);
		byte[] fileNameByte = fileName.getBytes(Context.CHARSET);
		byte[] fileNameSizeByte = ByteUtils.long2bytes(fileNameByte.length);
		byte[] metaDataByte = metaDataToStr(metaData).getBytes(Context.CHARSET);
		byte[] metaDataSizeByte = ByteUtils.long2bytes(metaDataByte.length);
		byte[] params = new byte[2 * Context.FDFS_PROTO_PKG_LEN_SIZE + 1 + Context.FDFS_GROUP_NAME_MAX_LEN + fileNameByte.length
				+ metaDataByte.length];
		
		int groupLen = groupByte.length;
		
		if (groupLen > Context.FDFS_GROUP_NAME_MAX_LEN) {
			groupLen = Context.FDFS_GROUP_NAME_MAX_LEN;
		}
		
		Arrays.fill(params, (byte) 0);
		
		int pos = 0;
		System.arraycopy(fileNameSizeByte, 0, params, pos, fileNameSizeByte.length);
		pos += Context.FDFS_PROTO_PKG_LEN_SIZE;
		System.arraycopy(metaDataSizeByte, 0, params, pos, metaDataSizeByte.length);
		pos += Context.FDFS_PROTO_PKG_LEN_SIZE;
		params[pos] = Context.STORAGE_SET_METADATA_FLAG_OVERWRITE;
		pos += 1;
		System.arraycopy(groupByte, 0, params, pos, groupLen);
		pos += Context.FDFS_GROUP_NAME_MAX_LEN;
		System.arraycopy(fileNameByte, 0, params, pos, fileNameByte.length);
		pos += fileNameByte.length;
		System.arraycopy(metaDataByte, 0, params, pos, metaDataByte.length);
		
		return new RequestContext(Context.STORAGE_PROTO_CMD_SET_METADATA, params);
	}
	
	@Override
	protected OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
	}
	
	@Override
	protected long getLongOfFixedResponseEntity() {
		return 0;
	}
	
	@Override
	protected Result<Boolean> callback(ResponseContext responseContext) throws FastdfsClientException {
		return new Result<Boolean>(responseContext.getCode(), responseContext.isSuccess());
	}
	
	/**
	 * 生成 meta str
	 * 
	 * @param metaData
	 * @return
	 */
	private String metaDataToStr(Map<String, String> metaData) {
		StringBuffer sb = new StringBuffer();
		
		for (String key : metaData.keySet()) {
			sb.append(Context.FDFS_RECORD_SEPERATOR);
			sb.append(key);
			sb.append(Context.FDFS_FIELD_SEPERATOR);
			sb.append(metaData.get(key));
		}
		
		return sb.toString().substring(Context.FDFS_RECORD_SEPERATOR.length());
	}
	
}
