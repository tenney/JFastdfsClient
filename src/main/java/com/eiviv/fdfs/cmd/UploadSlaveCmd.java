package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.utils.ByteUtils;

public class UploadSlaveCmd extends AbstractCmd<String> {
	
	private File file;
	private String masterfilename;
	private String prefix;
	private String ext;
	
	public UploadSlaveCmd(File file, String masterfilename, String prefix, String ext) {
		this.file = file;
		this.masterfilename = masterfilename;
		this.prefix = prefix;
		this.ext = ext;
	}
	
	@Override
	protected com.eiviv.fdfs.cmd.AbstractCmd.RequestBody getRequestBody() {
		byte[] masterfileNameLenByte = ByteUtils.long2bytes(masterfilename.length());
		byte[] fileSizeLenByte = ByteUtils.long2bytes(file.length());
		byte[] prefixByte = prefix.getBytes(Context.CHARSET);
		byte[] fileExtNameByte = getFileExtNameByte(ext);
		int fileExtNameByteLen = fileExtNameByte.length;
		
		if (fileExtNameByteLen > Context.FDFS_FILE_EXT_NAME_MAX_LEN) {
			fileExtNameByteLen = Context.FDFS_FILE_EXT_NAME_MAX_LEN;
		}
		
		byte[] masterfilenameBytes = masterfilename.getBytes(Context.CHARSET);
		
		byte[] body = new byte[2 * Context.FDFS_PROTO_PKG_LEN_SIZE + Context.FDFS_FILE_PREFIX_MAX_LEN + Context.FDFS_FILE_EXT_NAME_MAX_LEN
				+ masterfilenameBytes.length];
		
		Arrays.fill(body, (byte) 0);
		
		System.arraycopy(masterfileNameLenByte, 0, body, 0, masterfileNameLenByte.length);
		System.arraycopy(fileSizeLenByte, 0, body, Context.FDFS_PROTO_PKG_LEN_SIZE, fileSizeLenByte.length);
		System.arraycopy(prefixByte, 0, body, 2 * Context.FDFS_PROTO_PKG_LEN_SIZE, prefixByte.length);
		System.arraycopy(fileExtNameByte, 0, body, 2 * Context.FDFS_PROTO_PKG_LEN_SIZE + Context.FDFS_FILE_PREFIX_MAX_LEN, fileExtNameByteLen);
		System.arraycopy(masterfilenameBytes, 0, body, 2 * Context.FDFS_PROTO_PKG_LEN_SIZE + Context.FDFS_FILE_PREFIX_MAX_LEN
				+ Context.FDFS_FILE_EXT_NAME_MAX_LEN, masterfilenameBytes.length);
		
		return new RequestBody(Context.STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE, body, file);
	}
	
	@Override
	protected OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
	}
	
	@Override
	protected byte getResponseCmdCode() {
		return Context.STORAGE_PROTO_CMD_RESP;
	}
	
	@Override
	protected long getFixedBodyLength() {
		return -1;
	}
	
	@Override
	protected Result<String> callback(com.eiviv.fdfs.cmd.AbstractCmd.Response response) throws IOException {
		Result<String> result = new Result<String>(response.getCode());
		
		if (!response.isSuccess()) {
			result.setMessage("Error");
			return result;
		}
		
		byte[] data = response.getData();
		String group = new String(data, 0, Context.FDFS_GROUP_NAME_MAX_LEN).trim();
		String remoteFileName = new String(data, Context.FDFS_GROUP_NAME_MAX_LEN, data.length - Context.FDFS_GROUP_NAME_MAX_LEN);
		result.setData(group + "/" + remoteFileName);
		
		return result;
	}
	
	private byte[] getFileExtNameByte(String extName) {
		
		if (extName != null && extName.length() > 0) {
			return extName.getBytes(Context.CHARSET);
		}
		
		return new byte[0];
	}
	
}
