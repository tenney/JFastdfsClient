package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.exception.FastdfsClientException;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.utils.ByteUtils;

public abstract class AbstractCmd<T extends Serializable> implements Cmd<T> {
	
	/**
	 * socket write
	 * 
	 * @param socket
	 * @throws FastdfsClientException
	 */
	public final void request(Socket socket) throws IOException {
		OutputStream sockectOutputStream = socket.getOutputStream();
		RequestContext reqCon = getRequestContext();
		
		if (reqCon == null) {
			return;
		}
		
		byte[] params = reqCon.getRequestParams();
		
		if (params == null) {
			params = new byte[0];
		}
		
		byte[] reqEntity = new byte[Context.FDFS_PROTO_PKG_LEN_SIZE + 2 + params.length];
		
		Arrays.fill(reqEntity, (byte) 0);
		
		InputStream inputStream = reqCon.getInputStream();
		byte[] multipartByte = reqCon.getMultipartByte();
		byte[] reqConLenByte = null;
		
		if (inputStream != null || multipartByte != null) {
			reqConLenByte = ByteUtils.long2bytes(params.length + reqCon.getMultipartSize());
		} else {
			reqConLenByte = ByteUtils.long2bytes(params.length);
		}
		
		System.arraycopy(reqConLenByte, 0, reqEntity, 0, reqConLenByte.length);
		System.arraycopy(params, 0, reqEntity, Context.FDFS_PROTO_PKG_LEN_SIZE + 2, params.length);
		
		reqEntity[Context.PROTO_HEADER_CMD_INDEX] = reqCon.getRequestCmdCode();
		reqEntity[Context.PROTO_HEADER_STATUS_INDEX] = (byte) 0;
		
		sockectOutputStream.write(reqEntity);
		
		if (multipartByte != null) {
			sockectOutputStream.write(multipartByte, 0, multipartByte.length);
			return;
		}
		
		if (inputStream == null) {
			return;
		}
		
		byte[] readBuff = new byte[256 * 1024];
		int readLen = 0;
		
		try {
			while ((readLen = inputStream.read(readBuff)) != -1) {
				sockectOutputStream.write(readBuff, 0, readLen);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				inputStream = null;
			}
		}
	}
	
	/**
	 * socket read
	 * 
	 * @param socket
	 * @return result
	 * @throws FastdfsClientException
	 */
	private Result<T> receive(Socket socket) throws IOException {
		OutputStream writer = getOutputStream();
		
		if (writer == null) {
			return callback(new ResponseContext(Context.SUCCESS_CODE));
		}
		
		InputStream sockectInputStream = socket.getInputStream();
		
		byte[] resEntity = new byte[Context.FDFS_PROTO_PKG_LEN_SIZE + 2];
		int resEntityLen = sockectInputStream.read(resEntity);
		
		if (resEntityLen != resEntity.length) {
			throw new IOException("recv package size " + resEntityLen + " != " + resEntity.length);
		}
		
		byte resCmdCode = Context.STORAGE_PROTO_CMD_RESP;
		byte recvCmdCode = resEntity[Context.PROTO_HEADER_CMD_INDEX];
		
		if (recvCmdCode != resCmdCode) {
			throw new IOException("recv cmd: " + recvCmdCode + " is not correct, expect cmd: " + resCmdCode);
		}
		
		byte recvStatus = resEntity[Context.PROTO_HEADER_STATUS_INDEX];
		
		if (recvStatus != Context.SUCCESS_CODE) {
			return callback(new ResponseContext(recvStatus));
		}
		
		long resEntity2L = ByteUtils.bytes2long(resEntity, 0);
		
		if (resEntity2L < 0) {
			throw new IOException("recv body length: " + resEntity2L + " < 0!");
		}
		
		long fixedResEntityLen = getLongOfFixedResponseEntity();
		
		if (fixedResEntityLen >= 0 && resEntity2L != fixedResEntityLen) {
			throw new IOException("recv body length: " + resEntity2L + " is not correct, expect length: " + fixedResEntityLen);
		}
		
		int totalBytes = 0;
		int remainBytes = (int) resEntity2L;
		byte[] buff = new byte[remainBytes];
		
		if (Context.STORAGE_PROTO_CMD_DOWNLOAD_FILE == getRequestContext().getRequestCmdCode()) {
			buff = new byte[256 * 1024];
		}
		
		try {
			while (totalBytes < resEntity2L) {
				int len = remainBytes;
				
				if (len > buff.length) {
					len = buff.length;
				}
				
				if ((resEntityLen = sockectInputStream.read(buff, 0, len)) < 0) {
					break;
				}
				
				writer.write(buff, 0, resEntityLen);
				
				totalBytes += resEntityLen;
				remainBytes -= resEntityLen;
			}
			
			if (totalBytes != resEntity2L) {
				throw new IOException("recv package size " + totalBytes + " != " + resEntity2L);
			}
			
			if (writer instanceof ByteArrayOutputStream) {
				return callback(new ResponseContext(Context.SUCCESS_CODE, ((ByteArrayOutputStream) writer).toByteArray()));
			}
			
			return callback(new ResponseContext(Context.SUCCESS_CODE, null));
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				writer = null;
			}
		}
	}
	
	@Override
	public final Result<T> exec(Socket socket) throws IOException {
		request(socket);
		return receive(socket);
	}
	
	protected static final class RequestContext {
		
		private byte requestCmdCode;
		private byte[] requestParams;
		private byte[] multipartByte;
		private InputStream inputStream;
		private long multipartSize;
		
		public RequestContext(byte requestCmdCode) {
			this.requestCmdCode = requestCmdCode;
		}
		
		public RequestContext(byte requestCmdCode, byte[] requestParams) {
			this(requestCmdCode);
			this.requestParams = requestParams;
		}
		
		public RequestContext(byte requestCmdCode, byte[] requestParams, InputStream inputStream, long multipartSize) {
			this(requestCmdCode, requestParams);
			this.inputStream = inputStream;
			this.multipartSize = multipartSize;
		}
		
		public RequestContext(byte requestCmdCode, byte[] requestParams, byte[] multipartByte) {
			this(requestCmdCode, requestParams);
			this.multipartByte = multipartByte;
			this.multipartSize = multipartByte.length;
		}
		
		public byte getRequestCmdCode() {
			return requestCmdCode;
		}
		
		public void setRequestCmdCode(byte requestCmdCode) {
			this.requestCmdCode = requestCmdCode;
		}
		
		public byte[] getRequestParams() {
			return requestParams;
		}
		
		public void setRequestParams(byte[] requestParams) {
			this.requestParams = requestParams;
		}
		
		public byte[] getMultipartByte() {
			return multipartByte;
		}
		
		public void setMultipartByte(byte[] multipartByte) {
			this.multipartByte = multipartByte;
		}
		
		public InputStream getInputStream() {
			return inputStream;
		}
		
		public void setInputStream(InputStream inputStream) {
			this.inputStream = inputStream;
		}
		
		public long getMultipartSize() {
			return multipartSize;
		}
		
		public void setMultipartSize(long multipartSize) {
			this.multipartSize = multipartSize;
		}
		
	}
	
	protected static final class ResponseContext {
		
		private int code;
		private byte[] data;
		
		public ResponseContext(int code) {
			this.code = code;
		}
		
		public ResponseContext(int code, byte[] data) {
			this(code);
			this.data = data;
		}
		
		public boolean isSuccess() {
			return this.code == Context.SUCCESS_CODE;
		}
		
		public int getCode() {
			return this.code;
		}
		
		public void setCode(int code) {
			this.code = code;
		}
		
		public byte[] getData() {
			return this.data;
		}
		
		public void setData(byte[] data) {
			this.data = data;
		}
		
	}
	
	/**
	 * 获取请求 body
	 * 
	 * @return
	 */
	protected abstract RequestContext getRequestContext();
	
	/**
	 * 定向输出
	 * 
	 * @return
	 */
	protected abstract OutputStream getOutputStream();
	
	/**
	 * 获取respone body 固定长度
	 * 
	 * @return
	 */
	protected abstract long getLongOfFixedResponseEntity();
	
	/**
	 * 处理完成回调方法
	 * 
	 * @param response
	 * @return
	 * @throws IOException
	 */
	protected abstract Result<T> callback(ResponseContext responseContext);
	
}
