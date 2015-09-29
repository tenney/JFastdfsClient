package com.eiviv.fdfs.test;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;
import com.eiviv.fdfs.model.Result;

public class DeleteTest {
	
	@Test
	public void delete() throws Exception {
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		URL fileUrl = this.getClass().getResource("/Koala.jpg");
		File file = new File(fileUrl.getPath());
		Result<String> uploadResult = fastdfsClient.upload(file);
		
		if (!uploadResult.isSuccess()) {
			System.out.println(uploadResult.getState());
			return;
		}
		
		String fileId = uploadResult.getData();
		
		System.out.println("fileId:" + fileId);
		
		Result<Boolean> deleteResult = fastdfsClient.delete(fileId);
		System.out.println(deleteResult.getData());
		
		fastdfsClient.close();
	}
}
