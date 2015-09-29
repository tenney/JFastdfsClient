package com.eiviv.fdfs.test;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;
import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.Result;

public class AppendTest {
	
	@Test
	public void test() throws Exception {
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		URL fileUrl = this.getClass().getResource("/Test.txt");
		File file = new File(fileUrl.getPath());
		Result<String> uploadResult = fastdfsClient.upload(file);
		
		if (!uploadResult.isSuccess()) {
			System.out.println(uploadResult.getState());
			return;
		}
		
		String fileId = uploadResult.getData();
		
		System.out.println("fileId:" + fileId);
		
		byte[] tb = "ABCD".getBytes(Context.CHARSET);
		
		Result<Boolean> appendResult = fastdfsClient.append(fileId, tb);
		
		System.out.println(appendResult.getData());
		
		fastdfsClient.close();
	}
	
}
