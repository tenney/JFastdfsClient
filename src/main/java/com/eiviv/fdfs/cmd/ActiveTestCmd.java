package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.exception.FastdfsClientException;
import com.eiviv.fdfs.model.Result;

public class ActiveTestCmd extends AbstractCmd<Boolean> {
	
	@Override
	protected RequestContext getRequestContext() {
		return new RequestContext(Context.FDFS_PROTO_CMD_ACTIVE_TEST);
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
}
