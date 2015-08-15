package com.eiviv.fdfs.test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;

public class UpdateTest {
	
	@Test
	public void testUploadMeta() throws Exception {
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
//		URL fileUrl = this.getClass().getResource("/Koala.jpg");
		File file = new File("/Users/tenney/Desktop/aa.jpg");
		HashMap<String, String> meta = new HashMap<String, String>();
		meta.put("size", "200x200");
		
		String fileId = fastdfsClient.upload("group1", file, "jpg", meta);
		System.out.println("fileId:" + fileId);
		
		meta.put("size", "300x300");
		meta.put("nickname", "nickname");
		fastdfsClient.setMeta(fileId, meta);
		
		Map<String, String> a = fastdfsClient.getMeta(fileId);
		
		for (Map.Entry<String, String> entry : a.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
		
		fastdfsClient.close();
	}
}
