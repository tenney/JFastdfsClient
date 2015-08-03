package com.eiviv.fdfs.utils;

public class ByteUtils {
	
	/**
	 * 将long类型数据转化为 byte 数组
	 * 
	 * @param l long参数
	 * @return byte数组
	 */
	public static byte[] long2bytes(long l) {
		byte[] bytes = new byte[8];
		
		bytes[0] = (byte) ((l >> 56) & 0xFF);
		bytes[1] = (byte) ((l >> 48) & 0xFF);
		bytes[2] = (byte) ((l >> 40) & 0xFF);
		bytes[3] = (byte) ((l >> 32) & 0xFF);
		bytes[4] = (byte) ((l >> 24) & 0xFF);
		bytes[5] = (byte) ((l >> 16) & 0xFF);
		bytes[6] = (byte) ((l >> 8) & 0xFF);
		bytes[7] = (byte) (l & 0xFF);
		
		return bytes;
	}
	
	/**
	 * 将byte数组转换为long类型数据
	 * 
	 * @param bytes btype数组
	 * @param offset 位置
	 * @return long类型数据
	 */
	public static long bytes2long(byte[] bytes, int offset) {
		return (((long) (bytes[offset] >= 0 ? bytes[offset] : 256 + bytes[offset])) << 56)
				| (((long) (bytes[offset + 1] >= 0 ? bytes[offset + 1] : 256 + bytes[offset + 1])) << 48)
				| (((long) (bytes[offset + 2] >= 0 ? bytes[offset + 2] : 256 + bytes[offset + 2])) << 40)
				| (((long) (bytes[offset + 3] >= 0 ? bytes[offset + 3] : 256 + bytes[offset + 3])) << 32)
				| (((long) (bytes[offset + 4] >= 0 ? bytes[offset + 4] : 256 + bytes[offset + 4])) << 24)
				| (((long) (bytes[offset + 5] >= 0 ? bytes[offset + 5] : 256 + bytes[offset + 5])) << 16)
				| (((long) (bytes[offset + 6] >= 0 ? bytes[offset + 6] : 256 + bytes[offset + 6])) << 8)
				| (bytes[offset + 7] >= 0 ? bytes[offset + 7] : 256 + bytes[offset + 7]);
	}
	
	/**
	 * 将byte数组转换为int
	 * 
	 * @param bs
	 * @param offset
	 * @return
	 */
	public static int bytes2int(byte[] bs, int offset) {
		return ((bs[offset] >= 0 ? bs[offset] : 256 + bs[offset]) << 24) | ((bs[offset + 1] >= 0 ? bs[offset + 1] : 256 + bs[offset + 1]) << 16)
				| ((bs[offset + 2] >= 0 ? bs[offset + 2] : 256 + bs[offset + 2]) << 8)
				| (bs[offset + 3] >= 0 ? bs[offset + 3] : 256 + bs[offset + 3]);
	}
	
	/**
	 * 将byte数组转换为ip
	 * 
	 * @param bs
	 * @param offset
	 * @return
	 */
	public static String bytes2ip(byte[] bs, int offset) {
		
		if (bs[0] == 0 || bs[3] == 0) {
			return "";
		}
		
		StringBuilder sbResult = new StringBuilder(16);
		
		int n;
		
		for (int i = offset; i < offset + 4; i++) {
			n = (bs[i] >= 0) ? bs[i] : 256 + bs[i];
			
			if (sbResult.length() > 0) {
				sbResult.append(".");
			}
			
			sbResult.append(String.valueOf(n));
		}
		
		return sbResult.toString();
	}
}
