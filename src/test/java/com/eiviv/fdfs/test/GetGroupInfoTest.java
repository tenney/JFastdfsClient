package com.eiviv.fdfs.test;

import java.util.ArrayList;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;
import com.eiviv.fdfs.model.GroupInfo;

public class GetGroupInfoTest {
	
	@Test
	public void test() {
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		
		try {
			ArrayList<GroupInfo> groupInfos = fastdfsClient.getGroupInfos();
			
			for (GroupInfo groupInfo : groupInfos) {
				System.out.println("groupName:" + groupInfo.getGroupName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
