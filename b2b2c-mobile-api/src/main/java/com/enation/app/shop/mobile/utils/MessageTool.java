package com.enation.app.shop.mobile.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import org.apache.commons.codec.digest.DigestUtils;

import com.enation.eop.sdk.context.EopSetting;

public class MessageTool {
	public static String sendMessage(String content, String receiver) {
		BufferedReader reader = null;
		String result = null;
		StringBuffer sbf = new StringBuffer();
		try {
			URL url = new URL(EopSetting.MESSAGE_HTTPURL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			StringBuffer params = new StringBuffer();
			String timestamp = String.valueOf(new Date().getTime());
			params.append("timestamp").append("=").append(new Date().getTime());
			params.append("&content").append("=").append(URLEncoder.encode(content, "UTF-8").toString());
			params.append("&receiver").append("=").append(receiver);
			params.append("&token").append("=").append(DigestUtils.md5Hex(EopSetting.MESSAGE_TOKENKEY + timestamp));
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.writeBytes(params.toString());
			out.flush();
			out.close();
			connection.connect();

			InputStream is = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String strRead = null;
			while ((strRead = reader.readLine()) != null) {
				sbf.append(strRead);
				sbf.append("\r\n");
			}
			reader.close();
			result = sbf.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
