package com.enation.app.shop.core.order.model;

import java.util.HashMap;
import java.util.Map;

import com.enation.app.shop.core.util.SHA1Utils;
import com.google.gson.Gson;

public class CrmParams {
	private String source;
	private String sign;
	private String timestamp;
	private Map<String,Object> params;
	
	public CrmParams(){
		this.source = "POP";
		String timeStamp = String.valueOf(System.currentTimeMillis());
		this.timestamp = timeStamp;
		this.sign = SHA1Utils.getsign(timeStamp);
	}
	
	public static String buildCrmParams(String ticketNo){
		Map<String,Object> tickMap = new HashMap<String,Object>();
		tickMap.put("ticket", ticketNo);
		CrmParams crm = new CrmParams();
		crm.setParams(tickMap);
		return new Gson().toJson(crm);
	}
	
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public Map<String, Object> getParams() {
		return params;
	}
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	
	
	
}
