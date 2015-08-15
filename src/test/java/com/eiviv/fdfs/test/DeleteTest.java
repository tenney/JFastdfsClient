package com.eiviv.fdfs.test;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;

public class DeleteTest {
	
	@Test
	public void delete() throws Exception {
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		URL fileUrl = this.getClass().getResource("/Koala.jpg");
		File file = new File(fileUrl.getPath());
		String fileId = fastdfsClient.upload("group1",file);
		System.out.println("fileId:" + fileId);
		
		Boolean flag = fastdfsClient.delete(fileId);
		System.out.println(flag);
		
		fastdfsClient.close();
	}
}
