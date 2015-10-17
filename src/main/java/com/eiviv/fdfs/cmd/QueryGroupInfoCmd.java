package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.GroupInfo;
import com.eiviv.fdfs.model.Result;

public class QueryGroupInfoCmd extends AbstractCmd<ArrayList<GroupInfo>> {
	
	@Override
	protected RequestContext getRequestContext() {
		return new RequestContext(Context.TRACKER_PROTO_CMD_SERVER_LIST_GROUP);
	}
	
	@Override
	protected OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
	}
	
	@Override
	protected long getLongOfFixedResponseEntity() {
		return -1;
	}
	
	@Override
	protected Result<ArrayList<GroupInfo>> callback(ResponseContext responseContext) {
		Result<ArrayList<GroupInfo>> result = new Result<ArrayList<GroupInfo>>(responseContext.getCode());
		
		if (!responseContext.isSuccess()) {
			return result;
		}
		
		byte[] data = responseContext.getData();
		int dataLength = data.length;
		
		if (dataLength % GroupInfo.BYTE_SIZE != 0) {
			result.setCode(Context.ERR_NO_ECORRECT);
			return result;
		}
		
		ArrayList<GroupInfo> groupInfos = new ArrayList<GroupInfo>();
		int offset = 0;
		
		while (offset < dataLength) {
			GroupInfo groupInfo = new GroupInfo(data, offset);
			groupInfos.add(groupInfo);
			offset += GroupInfo.BYTE_SIZE;
		}
		
		result.setData(groupInfos);
		
		return result;
	}
	
}
