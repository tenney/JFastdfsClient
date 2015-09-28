package com.eiviv.fdfs.test;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;
import com.eiviv.fdfs.model.FileInfo;
import com.eiviv.fdfs.model.Result;

public class GetFileInfoTest {
	
	@Test
	public void test() {
		String fileId = "group1/M00/00/0F/rBDLZFW_vNuARX-FAAvqH_kipG8429.jpg";
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		
		try {
			Result<FileInfo> result = fastdfsClient.getFileInfo(fileId);
			
			if (!result.isSuccess()) {
				System.out.println(result.getMessage());
			} else {
				System.out.println(result.getData().getFileSize());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
