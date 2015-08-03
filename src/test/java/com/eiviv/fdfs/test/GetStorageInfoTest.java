package com.eiviv.fdfs.test;

import java.util.ArrayList;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;
import com.eiviv.fdfs.model.GroupInfo;
import com.eiviv.fdfs.model.StorageInfo;

public class GetStorageInfoTest {
	
	@Test
	public void test() {
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		
		try {
			ArrayList<GroupInfo> groupInfos = fastdfsClient.getGroupInfos();
			ArrayList<StorageInfo> storageInfos = null;
			String groupName = null;
			
			for (GroupInfo groupInfo : groupInfos) {
				groupName = groupInfo.getGroupName();
				storageInfos = fastdfsClient.getStorageInfos(groupName);
				
				for (StorageInfo storageInfo : storageInfos) {
					System.out.println("freeMD:" + storageInfo.getFreeMB());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
