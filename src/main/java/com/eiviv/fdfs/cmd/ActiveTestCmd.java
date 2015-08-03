package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.Result;

public class ActiveTestCmd extends AbstractCmd<Boolean> {
	
	@Override
	protected RequestBody getRequestBody() {
		return new RequestBody(Context.FDFS_PROTO_CMD_ACTIVE_TEST);
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
		return 0;
	}
	
	@Override
	protected Result<Boolean> callback(com.eiviv.fdfs.cmd.AbstractCmd.Response response) throws IOException {
		return new Result<Boolean>(response.getCode(), response.isSuccess());
	}
}
