package com.eiviv.fdfs.test;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import com.eiviv.fdfs.client.FastdfsClient;
import com.eiviv.fdfs.client.FastdfsClientFactory;

public class UploadTest {
	
	public static class UploaderRunner implements Runnable {
		
		private FastdfsClient fastdfsClient;
		private File file;
		private int i = 1;
		
		public UploaderRunner(FastdfsClient fastdfsClient, File file) {
			this.fastdfsClient = fastdfsClient;
			this.file = file;
		}
		
		@Override
		public void run() {
			
			while (true) {
				
				if (i >= 2) {
					break;
				}
				
				i++;
				
				try {
					String fileId = fastdfsClient.upload("group", file);
					
					System.out.println(Thread.currentThread().getName() + " - " + i + " upload file, resv fileId" + fileId);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
		}
	}
	
	@Test
	public void test() {
		FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
		URL fileUrl = this.getClass().getResource("/Koala.jpg");
		File file = new File(fileUrl.getPath());
		
		UploaderRunner runer = new UploaderRunner(fastdfsClient, file);
		
		for (int i = 0; i < 10; i++) {
			new Thread(runer).run();
		}
	}
}
