package com.enation.app.shop.mobile;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import com.enation.app.base.core.model.Member;
import com.enation.app.base.core.service.IMemberManager;
import com.enation.app.shop.mobile.common.CrmConstant;
import com.enation.app.shop.mobile.utils.CommonRequest;
import com.enation.app.shop.mobile.utils.CrmParams;
import com.enation.eop.sdk.context.EopSetting;
import com.enation.framework.util.JsonUtil;

public class APPInterceptor extends HandlerInterceptorAdapter {
	private static Logger logger = Logger.getLogger(APPInterceptor.class);
	@Autowired
	IMemberManager memberManager;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		PrintWriter out = null;
		try {
			String url = request.getRequestURL().toString();
			if (url.matches(".*/mobile/.*") && !url.contains("order-attention")) {
				String ticket = request.getParameter("ticket");
				if (StringUtils.isEmpty(ticket)) {
					out = response.getWriter();
					response.setStatus(200);
					response.setContentType("application/json;charset=utf-8");
					out.append("{\"result\":0,\"message\":\"ticket is null\",\"data\":\"\"}");
					return false;
				}
				String params = CrmParams.buildCrmParams(ticket);
				String result = checkCrmResult(params, EopSetting.CRMURL + CrmConstant.crmApiNO.CRM_GET_USE);
				// 请求成功
				Member member = new Member();

				if (JsonUtil.parseJson(result).get("code").getAsInt() == 0) {
					String cmmemid = JsonUtil.parseJson(result).getAsJsonObject("data").get("cmmemid").getAsString();
					member = memberManager.memberByCmmemid(cmmemid);
				} else {
					out = response.getWriter();
					response.setStatus(200);
					response.setContentType("application/json;charset=utf-8");
					out.append("{\"result\":9909,\"message\":\"request sso failed\",\"data\":\"\"}");
					return false;
				}
				if (member != null && member.getMember_id() != null) {
					request.setAttribute(CommonRequest.MEMBER_PARAM_ATTRIBUITE, member.getMember_id());
					request.setAttribute(CommonRequest.CRM_TICKET, ticket);
				} else {
					// 新增一个逻辑,如果本地没查到会员,则去接口拉一次同步,再查询一下
					String userResult = checkCrmResult(params,
							EopSetting.CRMURL + CrmConstant.crmApiNO.CRM_USER_DETAIL);
					if (JsonUtil.parseJson(result).get("code").getAsInt() == 0) { // 如果成功
						// 解析信息插入数据库
						Map<String, Object> map = JsonUtil.toMap(JsonUtil.parseJson(result).get("data").toString());
						member.setUname(map.get("cmname") + "");
						member.setEmail(map.get("cmemail") + "");
						member.setName(map.get("cmname") + "");
						member.setNickname(map.get("cmname") + "");
						member.setMobile(map.get("cmmobile") + "");
						member.setCmmemid(map.get("cmmemid") + "");
						member.setCmcustid(map.get("cmcustid") + "");

						memberManager.add(member);
						member = memberManager.memberByCmmemid(member.getCmmemid());

						request.setAttribute(CommonRequest.MEMBER_PARAM_ATTRIBUITE, member.getMember_id());
						request.setAttribute(CommonRequest.CRM_TICKET, ticket);
					} else {
						out = response.getWriter();
						response.setStatus(200);
						response.setContentType("application/json;charset=utf-8");
						out.append("{\"result\":9909,\"message\":\"member data is null\",\"data\":\"\"}");
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			out = response.getWriter();
			response.setStatus(200);
			response.setContentType("application/json;charset=utf-8");
			out.append("{\"result\":0,\"message\":\"request error\",\"data\":\"\"}");
		} finally {
			if (out != null) {
				out.close();
			}
		}
		
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		logger.info(request);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {

	}

	public String checkCrmResult(String params, String requestUrl) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		headers.add("Accept", MediaType.APPLICATION_JSON.toString());
		HttpEntity<String> formEntity = new HttpEntity<String>(params, headers);
		return restTemplate.postForObject(requestUrl, formEntity, String.class);
	}
	

}
