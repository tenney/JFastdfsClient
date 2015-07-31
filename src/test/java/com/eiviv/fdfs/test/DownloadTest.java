package com.eiviv.fdfs.test;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;

public class DownloadTest {
	
	@Test
	public void test() {
		String fileId = "group1/M00/00/0F/rBDLZFW7y86ASgSBAAvqH_kipG8289.jpg";
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		File file = new File("E:/test.jpg");
		
		try {
			FileOutputStream os = new FileOutputStream(file);
			boolean flag = fastdfsClient.download(fileId, os);
			
			System.out.println(flag);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
