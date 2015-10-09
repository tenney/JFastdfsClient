package com.eiviv.fdfs.cmd;

import java.io.OutputStream;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.Result;

public class CloseCmd extends AbstractCmd<Boolean> {
	
	@Override
	protected RequestContext getRequestContext() {
		return new RequestContext(Context.FDFS_PROTO_CMD_QUIT);
	}
	
	@Override
	protected OutputStream getOutputStream() {
		return null;
	}
	
	@Override
	protected long getLongOfFixedResponseEntity() {
		return 0;
	}
	
	@Override
	protected Result<Boolean> callback(ResponseContext responseContext) {
		return new Result<Boolean>(responseContext.getCode(), responseContext.isSuccess());
	}
	
}
