package com.eiviv.fdfs.test;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;
import com.eiviv.fdfs.model.Result;

public class UpdateTest {
	
	@Test
	public void testUploadMeta() throws Exception {
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		URL fileUrl = this.getClass().getResource("/Koala.jpg");
		File file = new File(fileUrl.getPath());
		HashMap<String, String> meta = new HashMap<String, String>();
		meta.put("size", "200x200");
		
		Result<String> uploadResult = fastdfsClient.upload(file, null, meta);
		
		if (!uploadResult.isSuccess()) {
			System.out.println(uploadResult.getMessage());
			return;
		}
		
		String fileId = uploadResult.getData();
		
		System.out.println("fileId:" + fileId);
		
		meta.put("size", "300x300");
		meta.put("nickname", "nickname");
		fastdfsClient.setMeta(fileId, meta);
		
		Result<HashMap<String, String>> metaInfoResult = fastdfsClient.getMeta(fileId);
		
		if (!metaInfoResult.isSuccess()) {
			System.out.println(metaInfoResult.getMessage());
			return;
		}
		
		for (Map.Entry<String, String> entry : metaInfoResult.getData().entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
		
		fastdfsClient.close();
	}
}
