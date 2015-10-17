package com.eiviv.fdfs.test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;
import com.eiviv.fdfs.model.Result;

public class UpdateTest {
	
	@Test
	public void test() throws Exception {
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
//		URL fileUrl = this.getClass().getResource("/Koala.jpg");
		//group1/M00/00/00/wKgAClYhTJqABSnxAAVCu85XxGU964.jpg
		File file = new File("/Users/tenney/Pictures/Untitled00.jpg");//group1/M00/00/00/wKgAClYhJfeAAwOqAAVCu85XxGU562.jpg
//		HashMap<String, String> meta = new HashMap<String, String>();
//		meta.put("size", "200x200");
		
		Result<String> uploadResult = fastdfsClient.upload("group1", file, "jpg");
//		Result<String> uploadResult = fastdfsClient.upload("group1", file, null, meta);
		
		if (!uploadResult.isSuccess()) {
			System.out.println(uploadResult.getState());
			return;
		}
		
		String fileId = uploadResult.getData();
		
		System.out.println("fileId:" + fileId);
		
//		meta.put("size", "300x300");
//		meta.put("nickname", "nickname");
//		fastdfsClient.setMeta(fileId, meta);
		
		Result<HashMap<String, String>> metaInfoResult = fastdfsClient.getMeta(fileId);
		
		if (!metaInfoResult.isSuccess()) {
			System.out.println(metaInfoResult.getState());
			return;
		}
		
		for (Map.Entry<String, String> entry : metaInfoResult.getData().entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
		
		fastdfsClient.close();
	}
}
