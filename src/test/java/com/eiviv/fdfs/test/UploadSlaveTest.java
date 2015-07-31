package com.eiviv.fdfs.test;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;

public class UploadSlaveTest {
	
	@Test
	public void testUploadSlave() throws Exception {
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		URL fileUrl = this.getClass().getResource("/Koala.jpg");
		File file = new File(fileUrl.getPath());
		String fileId = fastdfsClient.upload(file);
		System.out.println("fileId:" + fileId);
		
		String result = fastdfsClient.uploadSlave(file, fileId, "_200x200", "jpg");
		System.out.println(result);
		fastdfsClient.close();
	}
	
}
