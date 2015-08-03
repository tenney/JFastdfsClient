package com.eiviv.fdfs.client;

public class AbstractClient {
	
	/**
	 * 拆分 fileId
	 * 
	 * @param fileId "group/remoteFileName"
	 * @return 拆分成数组, [0] groupName, [1] remoteFileName
	 */
	protected String[] splitFileId(String fileId) {
		int pos = fileId.indexOf("/");
		
		if ((pos <= 0) || (pos == fileId.length() - 1)) {
			return null;
		}
		
		String[] results = new String[2];
		results[0] = fileId.substring(0, pos);
		results[1] = fileId.substring(pos + 1);
		
		return results;
	}
	
}
