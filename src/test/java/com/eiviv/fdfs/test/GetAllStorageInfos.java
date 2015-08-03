package com.eiviv.fdfs.test;

import java.util.ArrayList;
import java.util.Map;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;
import com.eiviv.fdfs.model.StorageInfo;

public class GetAllStorageInfos {
	
	@Test
	public void test() {
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		
		try {
			Map<String, ArrayList<StorageInfo>> groupStorageInfos = fastdfsClient.getAllStorageInfo();
			
			for (Map.Entry<String, ArrayList<StorageInfo>> entry : groupStorageInfos.entrySet()) {
				System.out.println("Group: " + entry.getKey());
				
				for (StorageInfo storageInfo : entry.getValue()) {
					System.out.println("storage ip: " + storageInfo.getIpAddr());
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
