package com.enation.eop.sdk.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.enation.eop.SystemSetting;
import com.enation.eop.sdk.context.EopSetting;
import com.enation.framework.util.StringUtil;

public class StaticResourcesUtil {

	/**
	 * 转换静态资源路径
	 * @param path
	 * @return
	 */
	public static String convertToUrl(String path){

		if(StringUtil.isEmpty(path)){
			return path;
		}
		String static_server_domain = SystemSetting.getStatic_server_domain();
		return path.replaceAll(EopSetting.FILE_STORE_PREFIX, static_server_domain );
	}

	/**
	 * 转换图片的名称
	 * @param filePath  如：http://static.eop.com/user/1/1/attachment/goods/2001010101030.jpg
	 * @param shortName 
	 * @return
	 */
	public static  String getThumbPath(String filePath, String shortName) {
		String pattern = "(.*)([\\.])(.*)";
		String thumbPath = "$1" + shortName + "$2$3";

		Pattern p = Pattern.compile(pattern, 2 | Pattern.DOTALL);
		Matcher m = p.matcher(filePath);
		if (m.find()) {
			String s = m.replaceAll(thumbPath);

			return s;
		}
		return null;
	}
}
