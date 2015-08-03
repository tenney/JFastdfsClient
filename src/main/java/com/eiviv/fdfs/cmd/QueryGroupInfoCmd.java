package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.GroupInfo;
import com.eiviv.fdfs.model.Result;

public class QueryGroupInfoCmd extends AbstractCmd<ArrayList<GroupInfo>> {
	
	@Override
	protected com.eiviv.fdfs.cmd.AbstractCmd.RequestBody getRequestBody() {
		return new RequestBody(Context.TRACKER_PROTO_CMD_SERVER_LIST_GROUP);
	}
	
	@Override
	protected OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
	}
	
	@Override
	protected byte getResponseCmdCode() {
		return Context.TRACKER_PROTO_CMD_RESP;
	}
	
	@Override
	protected long getFixedBodyLength() {
		return -1;
	}
	
	@Override
	protected com.eiviv.fdfs.model.Result<ArrayList<GroupInfo>> callback(com.eiviv.fdfs.cmd.AbstractCmd.Response response) throws IOException {
		
		if (!response.isSuccess()) {
			return new Result<ArrayList<GroupInfo>>(response.getCode(), "Error");
		}
		
		byte[] data = response.getData();
		int dataLength = data.length;
		
		if (dataLength % GroupInfo.BYTE_SIZE != 0) {
			throw new IOException("recv body length: " + data.length + " is not correct");
		}
		
		ArrayList<GroupInfo> groupInfos = new ArrayList<GroupInfo>();
		int offset = 0;
		
		while (offset < dataLength) {
			GroupInfo groupInfo = new GroupInfo(data, offset);
			groupInfos.add(groupInfo);
			offset += GroupInfo.BYTE_SIZE;
		}
		
		return new Result<ArrayList<GroupInfo>>(response.getCode(), groupInfos);
	}
	
}
