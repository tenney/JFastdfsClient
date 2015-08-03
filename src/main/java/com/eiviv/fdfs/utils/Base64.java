package com.eiviv.fdfs.utils;

import java.io.IOException;
import java.util.Arrays;

public class Base64 {
	
	private String lineSeparator = System.getProperty("line.separator");
	
	private int lineLength = 72;
	
	private char[] valueToChar = new char[64];
	
	private int[] charToValue = new int[256];
	
	private int[] charToPad = new int[4];
	
	public Base64() {
		this.init('+', '/', '=');
	}
	
	public Base64(char chPlus, char chSplash, char chPad, int lineLength) {
		this.init(chPlus, chSplash, chPad);
		this.lineLength = lineLength;
	}
	
	public Base64(int lineLength) {
		this.lineLength = lineLength;
	}
	
	private void init(char chPlus, char chSplash, char chPad) {
		int index = 0;
		
		for (int i = 'A'; i <= 'Z'; i++) {
			this.valueToChar[index++] = (char) i;
		}
		
		for (int i = 'a'; i <= 'z'; i++) {
			this.valueToChar[index++] = (char) i;
		}
		
		for (int i = '0'; i <= '9'; i++) {
			this.valueToChar[index++] = (char) i;
		}
		
		this.valueToChar[index++] = chPlus;
		this.valueToChar[index++] = chSplash;
		
		for (int i = 0; i < 256; i++) {
			this.charToValue[i] = IGNORE;
		}
		
		for (int i = 0; i < 64; i++) {
			this.charToValue[this.valueToChar[i]] = i;
		}
		
		this.charToValue[chPad] = PAD;
		
		Arrays.fill(this.charToPad, chPad);
	}
	
	public String encode(byte[] b) throws IOException {
		int outputLength = ((b.length + 2) / 3) * 4;
		
		if (lineLength != 0) {
			int lines = (outputLength + lineLength - 1) / lineLength - 1;
			
			if (lines > 0) {
				outputLength += lines * lineSeparator.length();
			}
		}
		
		StringBuffer sb = new StringBuffer(outputLength);
		
		int linePos = 0;
		
		int len = (b.length / 3) * 3;
		int leftover = b.length - len;
		for (int i = 0; i < len; i += 3) {
			linePos += 4;
			
			if (linePos > lineLength) {
				if (lineLength != 0) {
					sb.append(lineSeparator);
				}
				
				linePos = 4;
			}
			
			int combined = b[i + 0] & 0xff;
			combined <<= 8;
			combined |= b[i + 1] & 0xff;
			combined <<= 8;
			combined |= b[i + 2] & 0xff;
			
			int c3 = combined & 0x3f;
			combined >>>= 6;
			int c2 = combined & 0x3f;
			combined >>>= 6;
			int c1 = combined & 0x3f;
			combined >>>= 6;
			int c0 = combined & 0x3f;
			
			sb.append(valueToChar[c0]);
			sb.append(valueToChar[c1]);
			sb.append(valueToChar[c2]);
			sb.append(valueToChar[c3]);
		}
		
		switch (leftover) {
		case 0:
		default:
			break;
		case 1:
			linePos += 4;
			
			if (linePos > lineLength) {
				
				if (lineLength != 0) {
					sb.append(lineSeparator);
				}
				
				linePos = 4;
			}
			
			sb.append(encode(new byte[] { b[len], 0, 0 }).substring(0, 2));
			sb.append("==");
			break;
		
		case 2:
			linePos += 4;
			
			if (linePos > lineLength) {
				
				if (lineLength != 0) {
					sb.append(lineSeparator);
				}
				
				linePos = 4;
			}
			
			sb.append(encode(new byte[] { b[len], b[len + 1], 0 }).substring(0, 3));
			sb.append("=");
			break;
		
		}
		
		if (outputLength != sb.length()) {
			System.out.println("oops: minor program flaw: output length mis-estimated");
			System.out.println("estimate:" + outputLength);
			System.out.println("actual:" + sb.length());
		}
		
		return sb.toString();
	}
	
	/**
	 * decode a well-formed complete Base64 string back into an array of bytes.
	 * It must have an even multiple of 4 data characters (not counting \n),
	 * padded out with = as needed.
	 */
	public byte[] decodeAuto(String s) {
		int nRemain = s.length() % 4;
		
		if (nRemain == 0) {
			return this.decode(s);
		}
		
		return this.decode(s + new String(this.charToPad, 0, 4 - nRemain));
	}
	
	/**
	 * decode a well-formed complete Base64 string back into an array of bytes.
	 * It must have an even multiple of 4 data characters (not counting \n),
	 * padded out with = as needed.
	 */
	public byte[] decode(String s) {
		byte[] b = new byte[(s.length() / 4) * 3];
		
		int cycle = 0;
		int combined = 0;
		int j = 0;
		int len = s.length();
		int dummies = 0;
		
		for (int i = 0; i < len; i++) {
			
			int c = s.charAt(i);
			int value = (c <= 255) ? charToValue[c] : IGNORE;
			
			switch (value) {
			case IGNORE:
				break;
			case PAD:
				value = 0;
				dummies++;
			default:
				switch (cycle) {
				case 0:
					combined = value;
					cycle = 1;
					break;
				case 1:
					combined <<= 6;
					combined |= value;
					cycle = 2;
					break;
				case 2:
					combined <<= 6;
					combined |= value;
					cycle = 3;
					break;
				case 3:
					combined <<= 6;
					combined |= value;
					b[j + 2] = (byte) combined;
					combined >>>= 8;
					b[j + 1] = (byte) combined;
					combined >>>= 8;
					b[j] = (byte) combined;
					j += 3;
					cycle = 0;
					break;
				}
				break;
			}
		}
		
		if (cycle != 0) {
			throw new ArrayIndexOutOfBoundsException("Input to decode not an even multiple of 4 characters; pad with =.");
		}
		
		j -= dummies;
		
		if (b.length != j) {
			byte[] b2 = new byte[j];
			System.arraycopy(b, 0, b2, 0, j);
			b = b2;
		}
		
		return b;
	}
	
	/**
	 * determines how long the lines are that are generated by encode.
	 * Ignored by decode.
	 * 
	 * @param length 0 means no newlines inserted. Must be a multiple of 4.
	 */
	public void setLineLength(int length) {
		this.lineLength = (length / 4) * 4;
	}
	
	/**
	 * How lines are separated.
	 * Ignored by decode.
	 * 
	 * @param lineSeparator may be "" but not null.
	 *            Usually contains only a combination of chars \n and \r.
	 *            Could be any chars not in set A-Z a-z 0-9 + /.
	 */
	public void setLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
	}
	
	/**
	 * Marker value for chars we just ignore, e.g. \n \r high ascii
	 */
	static final int IGNORE = -1;
	
	/**
	 * Marker for = trailing pad
	 */
	static final int PAD = -2;
}
