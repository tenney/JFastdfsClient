package com.eiviv.fdfs.client;

public class AbstractClient {
	
	protected String[] splitFileId(String fileid) {
		int pos = fileid.indexOf("/");
		
		if ((pos <= 0) || (pos == fileid.length() - 1)) {
			return null;
		}
		
		String[] results = new String[2];
		results[0] = fileid.substring(0, pos);
		results[1] = fileid.substring(pos + 1);
		
		return results;
	}
	
}
