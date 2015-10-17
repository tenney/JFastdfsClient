package com.eiviv.fdfs.test;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;
import com.eiviv.fdfs.model.FileInfo;
import com.eiviv.fdfs.model.Result;

public class GetFileInfoTest {
	
	@Test
	public void test() {
		String fileId = "group1/M00/00/00/rBDLZFYWOtSAWybXAAvqH_kipG84617520";
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		
		try {
			Result<FileInfo> result = fastdfsClient.getFileInfo(fileId);
			
			if (!result.isSuccess()) {
				System.out.println(result.getState());
			} else {
				System.out.println(result.getData());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
