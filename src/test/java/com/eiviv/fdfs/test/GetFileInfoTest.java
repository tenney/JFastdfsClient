package com.eiviv.fdfs.test;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;
import com.eiviv.fdfs.model.FileInfo;

public class GetFileInfoTest {
	
	@Test
	public void test() {
		String fileId = "group1/M00/00/0F/rBDLZFW_vNuARX-FAAvqH_kipG8429.jpg";
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		
		try {
			FileInfo fileInfo = fastdfsClient.getFileInfo(fileId);
			System.out.println(fileInfo.getFileSize());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
