package com.enation.app.shop.mobile.service;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.enation.app.shop.mobile.common.CrmConstant;
import com.enation.app.shop.mobile.utils.CrmParams;
import com.enation.eop.sdk.context.EopSetting;
import com.enation.framework.util.JsonUtil;
import com.google.gson.Gson;

@Service
public class CrmPointManager {
	 final static  int  P_TYPE_ADD = 1; //增加积分
	 final static  int  P_TYPE_SUB = 2; //减少积分
	 
	 final static String add_memo = "退还积分";
	 final static String sub_memo = "购买商品";
	 
	
	public String CrmPointRest(String params, String requestUrl) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);	
		headers.add("Accept", MediaType.APPLICATION_JSON.toString());
		HttpEntity<String> formEntity = new HttpEntity<String>(params, headers);
		return restTemplate.postForObject(requestUrl, formEntity, String.class);
	}
	
	//登录接口
	public static String buildLoginParams(String password,String mobile){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("p_pwd", password);
		params.put("p_mobile",mobile);
		CrmParams crm = new CrmParams();
		crm.setParams(params);
		return new Gson().toJson(crm);
	}
	
	//查询会员积分
	public int queryMemberPoint(String ticket) throws Exception {	
		String requestUrl = EopSetting.CRMURL + CrmConstant.crmApiNO.CRM_QUERY_POINT;
//		String requestUrl = "http://43.254.53.219:9080/RestAPI/8014";
		String result = CrmPointRest(CrmParams.buildCrmParams(ticket), requestUrl);
		int cmtotjf;;
		if (JsonUtil.parseJson(result).get("code").getAsInt() == 0) {
			cmtotjf = JsonUtil.parseJson(result).getAsJsonObject("data").get("cmtotjf").getAsInt();
		} else {
			throw new Exception("request fail");
		}
		return cmtotjf;
	}
	
	//新增会员积分
	public boolean addMemberPoint(String ticket,Integer point,String orderNo){
		boolean flag = false;
		String requestUrl = EopSetting.CRMURL + CrmConstant.crmApiNO.CRM_MODIFY_POINT;
//		String requestUrl = "http://43.254.53.219:9080/RestAPI/8006";
		String result = CrmPointRest(buildPointParamByTicket(orderNo + "_" + System.currentTimeMillis(), P_TYPE_ADD, point, ticket,add_memo),requestUrl);
		if (JsonUtil.parseJson(result).get("code").getAsInt() == 0) {
			String o_rest = JsonUtil.parseJson(result).getAsJsonObject("data").get("o_ret").getAsString();
			flag = Integer.valueOf(o_rest.trim()) == 0; 
		}
		return flag;
	}
	
	
	//减去会员积分
	public boolean subMemberPointByMember(String cmmemberId,Integer point,String orderNo){
		boolean flag = false;
		String requestUrl = EopSetting.CRMURL + CrmConstant.crmApiNO.CRM_MODIFY_POINT;
//		String requestUrl = "http://43.254.53.219:9080/RestAPI/8006";
		String result = CrmPointRest(buildPointParamByMember(orderNo + "_" + System.currentTimeMillis(), P_TYPE_SUB, point, cmmemberId,sub_memo),requestUrl);
		if (JsonUtil.parseJson(result).get("code").getAsInt() == 0) {
			String o_rest = JsonUtil.parseJson(result).getAsJsonObject("data").get("o_ret").getAsString();
			flag = Integer.valueOf(o_rest.trim())== 0; 
		}
		return flag;
	}
	
	
	//新增会员积分
	public boolean addMemberPointByMember(String cmmemberId,Integer point,String orderNo){
		boolean flag = false;
		String requestUrl = EopSetting.CRMURL + CrmConstant.crmApiNO.CRM_MODIFY_POINT;
//		String requestUrl = "http://43.254.53.219:9080/RestAPI/8006";
		String result = CrmPointRest(buildPointParamByMember(orderNo +"_" + System.currentTimeMillis(), P_TYPE_ADD, point, cmmemberId,add_memo),requestUrl);
		if (JsonUtil.parseJson(result).get("code").getAsInt() == 0) {
			String o_rest = JsonUtil.parseJson(result).getAsJsonObject("data").get("o_ret").getAsString();
			flag = Integer.valueOf(o_rest.trim()) == 0; 
		}
		return flag;
	}
	
	
	//减去会员积分
	public boolean subMemberPointByTicket(String ticket,Integer point,String orderNo){
		boolean flag = false;
		String requestUrl = EopSetting.CRMURL + CrmConstant.crmApiNO.CRM_MODIFY_POINT;
//		String requestUrl = "http://43.254.53.219:9080/RestAPI/8006";
		String result = CrmPointRest(buildPointParamByTicket(orderNo + "_" + System.currentTimeMillis(), P_TYPE_SUB, point, ticket,sub_memo),requestUrl);
		System.out.println(result);
		if (JsonUtil.parseJson(result).get("code").getAsInt() == 0) {
			String o_rest = JsonUtil.parseJson(result).getAsJsonObject("data").get("o_ret").getAsString();
			flag = Integer.valueOf(o_rest.trim())== 0; 
		}
		return flag;
	}
	
	
	public static String generateP_Key(String orderno) {
		return DigestUtils.md5Hex("K" + orderno + "ey").toUpperCase();
	}

	
	public static String buildPointParamByTicket(String orderNo, int P_TYPE, Integer pointNum, String ticket,String memo) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p_no", orderNo);
		params.put("ticket", ticket);
		params.put("p_type", P_TYPE);
		params.put("p_jf", pointNum);
		params.put("p_memo",memo);
		params.put("p_key", generateP_Key(orderNo));
		CrmParams crm = new CrmParams();
		crm.setParams(params);
		return new Gson().toJson(crm);
	}
	
	public static String buildPointParamByMember(String orderNo, int P_TYPE, Integer pointNum, String cmmemberId,String memo) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p_no", orderNo);
		params.put("cmmemberId", cmmemberId);
		params.put("p_type", P_TYPE);
		params.put("p_jf", pointNum);
		params.put("p_memo", memo);
		params.put("p_key", generateP_Key(orderNo));
		CrmParams crm = new CrmParams();
		crm.setParams(params);
		return new Gson().toJson(crm);
	}
	
//	public static void main(String[] args) throws Exception{
//		int point = new CrmPointManager().queryMemberPoint("00000255772888_APP");
//		System.out.println(point);
//		new CrmPointManager().subMemberPointByTicket("00000255772888_APP", 100, "114125341231231_" + System.currentTimeMillis());
//		int point1 = new CrmPointManager().queryMemberPoint("00000255772888_APP");
//		System.out.println(point1);
//		
//	}

		
	public static void main(String[] args) throws Exception{
//		String url = "http://test.bestbond.cn/bkb/interface/getCustomerCoupon.mvc";
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
//        headers.setContentType(type);
//        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
//        HttpEntity<String> formEntity = new HttpEntity<String>("18621916251", headers);
//        String s = restTemplate.postForObject(url, formEntity, String.class);
//        System.out.println(s);
		
		
	}
	
	
}
