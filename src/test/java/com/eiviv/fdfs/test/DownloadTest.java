package com.eiviv.fdfs.test;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;
import com.eiviv.fdfs.model.Result;

public class DownloadTest {
	
	@Test
	public void test() {
		String fileId = "group1/M00/00/00/rBDLZFYWNlGAEHhfAAvqH_kipG87746242";
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		File file = new File("E:/test.jpg");
		
		try {
			FileOutputStream os = new FileOutputStream(file);
			Result<Boolean> downloadResult = fastdfsClient.download(fileId, os);
			
			System.out.println(downloadResult.getData());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
