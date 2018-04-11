package com.enation.app.shop.mobile.payment.wechatpay.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.JsonUtil;
import com.tenpay.util.MD5Util;

public class PayCommonUtil {  
    //随机字符串生成  
	public static String getRandomString(int length) {
		String base = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}    
    
    //请求xml组装  
	public static String getRequestXml(SortedMap<String, Object> parameters) {
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		Set es = parameters.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			if ("attach".equalsIgnoreCase(key) || "body".equalsIgnoreCase(key) || "sign".equalsIgnoreCase(key)) {
				sb.append("<" + key + ">" + "<![CDATA[" + value + "]]></" + key + ">");
			} else {
				sb.append("<" + key + ">" + value + "</" + key + ">");
			}
		}
		sb.append("</xml>");
		return sb.toString();
	}  
      
	public static String createSign(String characterEncoding, SortedMap<String, Object> parameters,String api_key) {
		StringBuffer sb = new StringBuffer();
		Set es = parameters.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			Object v = entry.getValue();
			if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + api_key);
		String sign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toUpperCase();
		return sign;
	}  
      //请求方法  
	public static String httpsRequest(String requestUrl, String requestMethod, String outputStr) {
		try {
			
			URL url = new URL(requestUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			System.out.println("openConnection  ************");

			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			conn.setRequestMethod(requestMethod);
			conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
			// 当outputStr不为null时向输出流写数据
			if (null != outputStr) {
				OutputStream outputStream = conn.getOutputStream();
				// 注意编码格式
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}
			// 从输入流读取返回内容
			InputStream inputStream = conn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String str = null;
			StringBuffer buffer = new StringBuffer();
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			inputStream = null;
			conn.disconnect();
			return buffer.toString();
		} catch (ConnectException ce) {
			System.out.println("connection timeout：{}" + ce);
		} catch (Exception e) {
			System.out.println("https exception：{}" + e);
		}
		return null;
	}  
      
	public static Map doXMLParse(String strxml) throws JDOMException, IOException {
		strxml = strxml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");
		if (null == strxml || "".equals(strxml)) {
			return null;
		}
		Map m = new HashMap();
		InputStream in = new ByteArrayInputStream(strxml.getBytes("UTF-8"));
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(in);
		Element root = doc.getRootElement();
		List list = root.getChildren();
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Element e = (Element) it.next();
			String k = e.getName();
			String v = "";
			List children = e.getChildren();
			if (children.isEmpty()) {
				v = e.getTextNormalize();
			} else {
				v = getChildrenText(children);
			}
			m.put(k, v);
		}
		in.close();
		return m;
	}  
              
	public static String getChildrenText(List children) {
		StringBuffer sb = new StringBuffer();
		if (!children.isEmpty()) {
			Iterator it = children.iterator();
			while (it.hasNext()) {
				Element e = (Element) it.next();
				String name = e.getName();
				String value = e.getTextNormalize();
				List list = e.getChildren();
				sb.append("<" + name + ">");
				if (!list.isEmpty()) {
					sb.append(getChildrenText(list));
				}
				sb.append(value);
				sb.append("</" + name + ">");
			}
		}
		return sb.toString();
	}  
      
      public static Map<String, String> weixinPrePay(String sn,BigDecimal totalAmount,  
			String description, String appid, String mch_id, String notify_url,String api_key) {
		SortedMap<String, Object> parameterMap = new TreeMap<String, Object>();
		parameterMap.put("appid", appid);
		parameterMap.put("mch_id", mch_id);
		parameterMap.put("nonce_str", PayCommonUtil.getRandomString(32));
		parameterMap.put("body",
				StringUtils.abbreviate(description.replaceAll("[^0-9a-zA-Z\\u4e00-\\u9fa5 ]", ""), 600));
		parameterMap.put("out_trade_no", sn);
		parameterMap.put("fee_type", "CNY");
		BigDecimal total = totalAmount.multiply(new BigDecimal(100));
		java.text.DecimalFormat df = new java.text.DecimalFormat("0");
		parameterMap.put("total_fee", df.format(total));
		parameterMap.put("notify_url", notify_url);
		parameterMap.put("trade_type", "APP");
		String sign = PayCommonUtil.createSign("UTF-8", parameterMap,api_key);
		parameterMap.put("sign", sign);
		String requestXML = PayCommonUtil.getRequestXml(parameterMap);
		String result = PayCommonUtil.httpsRequest("https://api.mch.weixin.qq.com/pay/unifiedorder", "POST",
				requestXML);
		Map<String, String> map = null;
		try {
			map = PayCommonUtil.doXMLParse(result);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}  
      
      /**
       * 生成预支付订单
       * @param sn
       * @param totalAmount
       * @param description
       * @return
       */
	public static String ceratePrePay(String sn,BigDecimal totalAmount,  
			String description, String appid, String mch_id, String notify_url,String api_key) {
		Map<String, String> map = weixinPrePay(sn,totalAmount,description,appid,mch_id,notify_url,api_key);
		SortedMap<String, Object> parameterMap = new TreeMap<String, Object>();
		parameterMap.put("appid", appid);
		parameterMap.put("partnerid", mch_id);
		parameterMap.put("prepayid", map.get("prepay_id"));
		parameterMap.put("package", "Sign=WXPay");
		parameterMap.put("noncestr", PayCommonUtil.getRandomString(32));
		parameterMap.put("timestamp", DateUtil.getDateline());
		String sign = PayCommonUtil.createSign("UTF-8", parameterMap,api_key);
		parameterMap.put("sign", sign);
		return JsonUtil.MapToJson(parameterMap);
	}  
	
       
}  