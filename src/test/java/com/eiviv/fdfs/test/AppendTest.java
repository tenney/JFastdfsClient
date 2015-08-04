package com.eiviv.fdfs.test;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;
import com.eiviv.fdfs.context.Context;

public class AppendTest {
	
	@Test
	public void test() throws Exception {
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		URL fileUrl = this.getClass().getResource("/Test.txt");
		File file = new File(fileUrl.getPath());
		String fileId = fastdfsClient.upload(file);
		System.out.println("fileId:" + fileId);
		
		byte[] tb = "ABCD".getBytes(Context.CHARSET);
		
		boolean result = fastdfsClient.appendFile(fileId, tb);
		
		System.out.println(result);
		
		fastdfsClient.close();
	}
	
}
