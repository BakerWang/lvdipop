package com.enation.app.shop.core.order.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.enation.app.shop.core.member.service.impl.MemberManager;
import com.enation.app.shop.core.order.model.CrmParams;
import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.model.Refund;
import com.enation.app.shop.core.order.model.SellBackStatus;
import com.enation.app.shop.core.order.plugin.order.OrderPluginBundle;
import com.enation.app.shop.core.order.service.IOrderManager;
import com.enation.app.shop.core.order.service.IRefundManager;
import com.enation.app.shop.core.order.service.ISellBackManager;
import com.enation.eop.resource.model.AdminUser;
import com.enation.eop.sdk.context.EopSetting;
import com.enation.eop.sdk.context.UserConext;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.JsonUtil;
import com.google.gson.Gson;

/**
 * 退款单管理类
 * @author Kanon
 *
 */
@Service("refundManager")
public class RefundManager implements IRefundManager {
	
	@Autowired
	private IDaoSupport daoSupport;
	
	@Autowired
	private OrderPluginBundle orderPluginBundle;
	
	@Autowired
	private ISellBackManager sellBackManager;
	
	@Autowired
	private IOrderManager orderManager;
	
	@Autowired
	private MemberManager memberManager;
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.order.service.IRefundManager#addRefund(com.enation.app.shop.core.order.model.Refund)
	 */
	@Override
	public void addRefund(Refund refund) {
		daoSupport.insert("es_refund", refund);
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.order.service.IRefundManager#editRefund(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public void editRefund(Integer id, Integer status,Double refund_money) {
		AdminUser adminUser = UserConext.getCurrentAdminUser();
		/**
		 * 修改退款单状态，修改退款单金额
		 */
		daoSupport.execute("update es_refund set status=?,refund_user=?,refund_time=?,refund_money=? where id=?", status,adminUser.getUsername(),DateUtil.getDateline(),refund_money,id);
		
		/**
		 * 修改售后申请状态
		 */
		Refund refund=this.getRefund(id);
		daoSupport.execute("update es_sellback_list set tradestatus=? where id=?",SellBackStatus.refund.getValue(),refund.getSellback_id());
		
		/**
		 * 记录订单日志
		 */
		orderManager.addLog(refund.getOrder_id(), "已退款，金额：" + refund_money);
		
		/**
		 * 记录维权订单日志
		 */
		sellBackManager.saveLog(refund.getSellback_id(), "已退款，金额："+refund_money);
		
		/**
		 * 激发退款事件 add by jianghongyan 
		 * 激发退款结算后时间
		 */
		Order order=orderManager.get(refund.getOrder_id());
		orderPluginBundle.onRefund(order, refund);//
//		orderPluginBundle.afterReturnOrderConfirm(refund.getOrder_id(), -refund_money);
		//最后需要退还会员积分
		String cmmemid = memberManager.queryCmmemid(order.getMember_id());
		if (order.getConsumepoint() > 0) {
			try {
				boolean flag = addMemberPoint(cmmemid, order.getConsumepoint(), order.getSn());
				if (flag) {
					orderManager.addLog(refund.getOrder_id(), "已退还积分：" + order.getConsumepoint());
				} else {
					orderManager.addLog(refund.getOrder_id(), "退还积分失败：" + order.getConsumepoint());
				}
			} catch (Exception e) {
				orderManager.addLog(refund.getOrder_id(), "退还积分失败：" + e.getMessage());
			}
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.order.service.IRefundManager#getRefund(java.lang.Integer)
	 */
	@Override
	public Refund getRefund(Integer id) {
		return (Refund) daoSupport.queryForObject("select * from es_refund where id=?", Refund.class, id);
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.order.service.IRefundManager#refundList(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public Page refundList(Integer page, Integer pageSize) {

		return daoSupport.queryForPage("select * from es_refund order by create_time desc",page, pageSize);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.order.service.IRefundManager#getRefundBySellbackId(java.lang.Integer)
	 */
	@Override
	public Refund getRefundBySellbackId(Integer id) {
		
		return (Refund) daoSupport.queryForObject("select * from es_refund where sellback_id=?", Refund.class, id);
	}
	
	
	//新增会员积分
	public boolean addMemberPoint(String cmmemberId,Integer point,String orderNo){
		boolean flag = false;
		String requestUrl = EopSetting.CRMURL + 8006;
		String result = CrmPointRest(buildPointParamByMember(orderNo +"_" + System.currentTimeMillis(), 1, point, cmmemberId,"退还积分"),requestUrl);
		if (JsonUtil.parseJson(result).get("code").getAsInt() == 0) {
			String o_rest = JsonUtil.parseJson(result).getAsJsonObject("data").get("o_ret").getAsString();
			flag = Integer.valueOf(o_rest.trim()) == 0; 
		}
		return flag;
	}
	
	public String CrmPointRest(String params, String requestUrl) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		headers.add("Accept", MediaType.APPLICATION_JSON.toString());
		HttpEntity<String> formEntity = new HttpEntity<String>(params, headers);
		return restTemplate.postForObject(requestUrl, formEntity, String.class);
	}

	
	public static String generateP_Key(String orderno) {
		return DigestUtils.md5Hex("K" + orderno + "ey").toUpperCase();
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

}
