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
		String fileId = "group1/M00/00/0F/rBDLZFXA0q-AdzsdAAyPUlOp7Oo2830.gz";
		System.out.println("fileId:" + fileId);
		
		String t = "ABCD";
		byte[] tb = t.getBytes(Context.CHARSET);
		
		fastdfsClient.appendFile(fileId, tb);
		
		fastdfsClient.close();
	}
	
}
