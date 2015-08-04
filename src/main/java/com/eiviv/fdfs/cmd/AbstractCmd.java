package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.utils.ByteUtils;

public abstract class AbstractCmd<T extends Serializable> implements Cmd<T> {
	
	/**
	 * socket write
	 * 
	 * @param socket socket
	 * @throws IOException
	 */
	public final void request(Socket socket) throws IOException {
		OutputStream os = socket.getOutputStream();
		RequestBody requestBody = getRequestBody();
		
		if (requestBody == null) {
			new IllegalArgumentException("requestBody can not be null");
		}
		
		byte[] body = requestBody.getBody();
		
		if (body == null) {
			body = new byte[0];
		}
		
		byte[] header = new byte[Context.FDFS_PROTO_PKG_LEN_SIZE + 2 + body.length];
		
		Arrays.fill(header, (byte) 0);
		
		InputStream is = requestBody.getInputStream();
		byte[] fileByte = requestBody.getFileByte();
		byte[] bodyLenByte = null;
		
		if (is != null || fileByte != null) {
			bodyLenByte = ByteUtils.long2bytes(body.length + requestBody.getSize());
		} else {
			bodyLenByte = ByteUtils.long2bytes(body.length);
		}
		
		System.arraycopy(bodyLenByte, 0, header, 0, bodyLenByte.length);
		System.arraycopy(body, 0, header, Context.FDFS_PROTO_PKG_LEN_SIZE + 2, body.length);
		
		header[Context.PROTO_HEADER_CMD_INDEX] = requestBody.getRequestCmdCode();
		header[Context.PROTO_HEADER_STATUS_INDEX] = (byte) 0;
		
		os.write(header);
		
		if (is != null) {
			byte[] readBuff = new byte[256 * 1024];
			int readLen = 0;
			
			try {
				while ((readLen = is.read(readBuff)) != -1) {
					os.write(readBuff, 0, readLen);
				}
			} catch (IOException e) {
				throw e;
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						throw e;
					} finally {
						is = null;
					}
				}
			}
		} else if (fileByte != null) {
			os.write(fileByte, 0, fileByte.length);
		}
	}
	
	@Override
	public final Result<T> exec(Socket socket) throws IOException {
		request(socket);
		
		OutputStream writer = getOutputStream();
		
		if (writer == null) {
			return callback(new Response(Context.SUCCESS_CODE, null));
		}
		
		try {
			byte[] header = new byte[Context.FDFS_PROTO_PKG_LEN_SIZE + 2];
			InputStream sockectInputStream = socket.getInputStream();
			int headerLen = sockectInputStream.read(header);
			
			if (headerLen != header.length) {
				throw new IOException("recv package size " + headerLen + " != " + header.length);
			}
			
			byte resCmdCode = getResponseCmdCode();
			
			if (header[Context.PROTO_HEADER_CMD_INDEX] != resCmdCode) {
				throw new IOException("recv cmd: " + header[Context.PROTO_HEADER_CMD_INDEX] + " is not correct, expect cmd: " + resCmdCode);
			}
			
			if (header[Context.PROTO_HEADER_STATUS_INDEX] != Context.SUCCESS_CODE) {
				return callback(new Response(Context.PROTO_HEADER_STATUS_INDEX, null));
			}
			
			long bodyLen = ByteUtils.bytes2long(header, 0);
			
			if (bodyLen < 0) {
				throw new IOException("recv body length: " + bodyLen + " < 0!");
			}
			
			long fixedBodyLen = getFixedBodyLength();
			
			if (fixedBodyLen >= 0 && bodyLen != fixedBodyLen) {
				throw new IOException("recv body length: " + bodyLen + " is not correct, expect length: " + fixedBodyLen);
			}
			
			byte[] buff = new byte[2 * 1024];
			int totalBytes = 0;
			int remainBytes = (int) bodyLen;
			
			while (totalBytes < bodyLen) {
				int len = remainBytes;
				
				if (len > buff.length) {
					len = buff.length;
				}
				
				if ((headerLen = sockectInputStream.read(buff, 0, len)) < 0) {
					break;
				}
				
				writer.write(buff, 0, headerLen);
				totalBytes += headerLen;
				remainBytes -= headerLen;
			}
			
			if (totalBytes != bodyLen) {
				throw new IOException("recv package size " + totalBytes + " != " + bodyLen);
			}
			
			if (writer instanceof ByteArrayOutputStream) {
				return callback(new Response(Context.SUCCESS_CODE, ((ByteArrayOutputStream) writer).toByteArray()));
			}
			
			return callback(new Response(Context.SUCCESS_CODE, null));
		} catch (IOException e) {
			throw e;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					throw e;
				} finally {
					writer = null;
				}
			}
		}
	}
	
	protected static final class RequestBody {
		
		private byte requestCmdCode;
		private byte[] body;
		private byte[] fileByte;
		private InputStream inputStream;
		private long size;
		
		public RequestBody(byte requestCmdCode) {
			this.requestCmdCode = requestCmdCode;
		}
		
		public RequestBody(byte requestCmdCode, byte[] body) {
			this(requestCmdCode);
			this.body = body;
		}
		
		public RequestBody(byte requestCmdCode, byte[] body, InputStream inputStream, long size) {
			this(requestCmdCode, body);
			this.inputStream = inputStream;
			this.size = size;
		}
		
		public RequestBody(byte requestCmdCode, byte[] body, byte[] fileByte) {
			this(requestCmdCode, body);
			this.fileByte = fileByte;
			this.size = fileByte.length;
		}
		
		public byte getRequestCmdCode() {
			return requestCmdCode;
		}
		
		public void setRequestCmdCode(byte requestCmdCode) {
			this.requestCmdCode = requestCmdCode;
		}
		
		public byte[] getBody() {
			return body;
		}
		
		public void setBody(byte[] body) {
			this.body = body;
		}
		
		public byte[] getFileByte() {
			return fileByte;
		}
		
		public void setFileByte(byte[] fileByte) {
			this.fileByte = fileByte;
		}
		
		public InputStream getInputStream() {
			return inputStream;
		}
		
		public void setInputStream(InputStream inputStream) {
			this.inputStream = inputStream;
		}
		
		public long getSize() {
			return size;
		}
		
		public void setSize(long size) {
			this.size = size;
		}
		
	}
	
	protected static final class Response {
		
		private int code;
		private byte[] data;
		
		public Response(int code, byte[] data) {
			this.code = code;
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
	protected abstract RequestBody getRequestBody();
	
	/**
	 * 定向输出
	 * 
	 * @return
	 */
	protected abstract OutputStream getOutputStream();
	
	/**
	 * 获取 response cmd
	 * 
	 * @return
	 */
	protected abstract byte getResponseCmdCode();
	
	/**
	 * 获取respone body 固定长度
	 * 
	 * @return
	 */
	protected abstract long getFixedBodyLength();
	
	/**
	 * 处理完成回调方法
	 * 
	 * @param response
	 * @return
	 * @throws IOException
	 */
	protected abstract Result<T> callback(Response response) throws IOException;
	
}
