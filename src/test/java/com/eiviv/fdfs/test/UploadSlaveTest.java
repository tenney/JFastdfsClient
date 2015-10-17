package com.eiviv.fdfs.test;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;
import com.eiviv.fdfs.model.Result;

public class UploadSlaveTest {
	
	@Test
	public void testUploadSlave() throws Exception {
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		URL fileUrl = this.getClass().getResource("/Koala.jpg");
		File file = new File(fileUrl.getPath());
		Result<String> uploadResult = fastdfsClient.upload("group1", file);
		
		if (!uploadResult.isSuccess()) {
			System.out.println(uploadResult.getState());
			return;
		}
		
		String fileId = uploadResult.getData();
		
		System.out.println("fileId:" + fileId);
		
		Result<String> uploadSlaveResult = fastdfsClient.uploadSlave(file, fileId, "_200x200", "jpg");
		
		if (!uploadSlaveResult.isSuccess()) {
			System.out.println(uploadSlaveResult.getState());
			return;
		}
		
		System.out.println(uploadSlaveResult.getData());
		
		fastdfsClient.close();
	}
	
}
