package com.enation.app.javashop.solr.util;

import org.apache.commons.codec.binary.Hex;

public class HexUtil {
	
	/**
	 * 将字符串进行hex
	 * @param str
	 * @return
	 */
	public static String encode(String str){
		try{
			return new Hex().encodeHexString(str.getBytes("UTF-8"));
		}catch(Exception ex){
			return str;
		}
	}
	
	/**
	 * 将hex后的字符串解密
	 * @param encodedStr
	 * @return
	 */
	public static String decode(String encodedStr){
		try{
			return new String(new Hex().decode(encodedStr.getBytes()), "UTF-8");
		}catch(Exception ex){
			return encodedStr;
		}
	}
	
}
