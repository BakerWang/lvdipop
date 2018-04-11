package com.enation.app.shop.mobile.utils;

import javax.servlet.http.HttpServletRequest;
import com.enation.framework.context.webcontext.ThreadContextHolder;

public class CommonRequest {
	public final static String MEMBER_PARAM_ATTRIBUITE = "member_id";
	public final static String CRM_TICKET = "ticket";

	public static Integer getMemberID() {
		Integer member_id = null;
		try {
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			member_id = (Integer) request.getAttribute(MEMBER_PARAM_ATTRIBUITE);
		} catch (Exception e) {
		}
		return member_id;
	}
	
	public static String getReqTicket() {
		String ticket = null;
		try {
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			ticket =  (String) request.getAttribute(CRM_TICKET);
		} catch (Exception e) {
		}
		return ticket;
	}

}
