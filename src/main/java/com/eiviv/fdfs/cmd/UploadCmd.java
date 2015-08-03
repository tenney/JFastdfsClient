package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.utils.ByteUtils;

public class UploadCmd extends AbstractCmd<String> {
	
	private File file;
	private String extName;
	private byte storePathIndex;
	
	/**
	 * 实例化
	 * 
	 * @param file 文件
	 * @param extName 扩展名
	 * @param storePathIndex 上传路径
	 */
	public UploadCmd(File file, String extName, byte storePathIndex) {
		this.file = file;
		this.extName = extName;
		this.storePathIndex = storePathIndex;
	}
	
	@Override
	protected com.eiviv.fdfs.cmd.AbstractCmd.RequestBody getRequestBody() {
		byte[] body = new byte[15];
		Arrays.fill(body, (byte) 0);
		body[0] = storePathIndex;
		byte[] fileSizeByte = ByteUtils.long2bytes(file.length());
		byte[] fileExtNameByte = getFileExtNameByte(extName);
		int fileExtNameByteLen = fileExtNameByte.length;
		
		if (fileExtNameByteLen > Context.FDFS_FILE_EXT_NAME_MAX_LEN) {
			fileExtNameByteLen = Context.FDFS_FILE_EXT_NAME_MAX_LEN;
		}
		
		System.arraycopy(fileSizeByte, 0, body, 1, fileSizeByte.length);
		System.arraycopy(fileExtNameByte, 0, body, fileSizeByte.length + 1, fileExtNameByteLen);
		
		return new RequestBody(Context.STORAGE_PROTO_CMD_UPLOAD_FILE, body, file);
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
	
	/**
	 * 获取文件扩展名
	 * 
	 * @param fileName
	 * @return
	 */
	private byte[] getFileExtNameByte(String fileName) {
		String fileExtName = null;
		int nPos = fileName.lastIndexOf('.');
		
		if (nPos > 0 && fileName.length() - nPos <= Context.FDFS_FILE_EXT_NAME_MAX_LEN + 1) {
			fileExtName = fileName.substring(nPos + 1);
			
			if (fileExtName != null && fileExtName.length() > 0) {
				return fileExtName.getBytes(Context.CHARSET);
			} else {
				return new byte[0];
			}
		} else {
			return fileName.getBytes(Context.CHARSET);
		}
	}
	
}
