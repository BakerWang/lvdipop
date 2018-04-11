package com.enation.app.service.front.api;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.base.core.model.Member;
import com.enation.app.service.core.servicegoods.model.ServiceGoods;
import com.enation.app.service.core.servicegoods.service.IServiceGoodsManager;
import com.enation.eop.sdk.context.EopSetting;
import com.enation.eop.sdk.context.UserConext;
import com.enation.framework.action.GridController;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.JsonMessageUtil;
import com.enation.framework.util.JsonResultUtil;

/**
 * 
 * @ClassName: ServiceGoodsApiController
 * @Description: 服务商品API
 * @author: liuyulei
 * @date: 2016年9月14日 上午7:21:39
 */
@Controller
@RequestMapping("/api/shop/servicegoods")
public class ServiceGoodsApiController {

	@Autowired
	private IServiceGoodsManager serviceGoodsManager;

	@Autowired
	private IDaoSupport daoSupport;

	private Logger logger = Logger.getLogger(getClass());

	/**
	 * 
	 * @Title: getServiceGoodsInfo
	 * @Description: TODO 获取卡密列表
	 * @return 返回卡密列表信息
	 * @throws ParseException
	 * @return: List @author： liuyulei
	 * @date：2016年9月14日 上午7:21:06
	 */
	@ResponseBody
	@RequestMapping(value = "/get-service-code", produces = MediaType.APPLICATION_JSON_VALUE)
	public List getServiceGoodsInfo() throws ParseException {
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		Integer itemid = Integer.parseInt(request.getParameter("itemid"));
		List<ServiceGoods> sGoodsList = serviceGoodsManager.getCodeByParam(itemid);
		return sGoodsList;
	}

	/**
	 * 
	 * @Title: checkCode
	 * @Description: TODO 服务码校验
	 * @param code
	 *            服务码
	 * @return 返回校验后的信息
	 * @return: JsonResult @author： liuyulei
	 * @date：2016年9月14日 上午7:19:51
	 */
	@ResponseBody
	@RequestMapping("/check-code")
	public JsonResult checkCode(String code,String self_store) {

		try {
			Map map = serviceGoodsManager.checkCode(code.trim());

			if (map == null ) { // 判断验证码是否存在
				return JsonResultUtil.getErrorJson("验证码不存在!");
			}
			
			if("b2b2c".equals(EopSetting.PRODUCT)){   //如果是多店，则要验证是否为自营
				if(!this.isCurrcentStore(code, self_store)){//自营店铺验证卡密
					return JsonResultUtil.getErrorJson("验证码不属于本店铺！");
				}
			}
			
			if (map.get("status") != null && ((Integer) map.get("status")) == 1) {// 判断验证码是否消费
				return JsonResultUtil.getErrorJson("已消费！");
			} else if (map.get("valid_time") != null && Integer.parseInt(map.get("valid_time") + "") == 0) {// 判断是否超过有效期
				return JsonResultUtil.getErrorJson("超过有效期，该验证码已失效！");
			} else if (map.get("status") != null && ((Integer) map.get("status")) == 2) {//定时程序使用后判断此状态    超过有效期
				return JsonResultUtil.getErrorJson("超过有效期！");
			} else if (map.get("status") != null && ((Integer) map.get("status")) == 2) {
				return JsonResultUtil.getErrorJson("卡密已经失效！");
			}else{
				return JsonResultUtil.getSuccessJson("success");
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("error");
		}
	}

	/**
	 * 
	 * @Title: used
	 * @Description: TODO 根据验证码 修改验证码状态
	 * @param code
	 *            验证码
	 * @return 返回修改后的验证码信息
	 * @return: Map @author： liuyulei
	 * @date：2016年9月14日 上午7:18:32
	 */
	@ResponseBody
	@RequestMapping("/service-goods-used")
	public Map used(String code) throws Exception {
		Map map = serviceGoodsManager.used(code, 1); // 修改状态 1 已使用 并返回该条数据信息
		if (map == null) { // 如果为null,则为状态修改失败。
			map = new HashMap<Object, Object>();
			map.put("checkCode", 0);
			map.put("message", "消费失败！");
			return map;
		} else {
			map.put("checkCode", 1);
			map.put("message", "消费成功！");
			map.put("enable_time",
					DateUtil.toString(Long.valueOf((Integer) map.get("enable_time")), "yyyy-MM-dd HH:mm:ss"));
			map.put("create_time",
					DateUtil.toString(Long.valueOf((Integer) map.get("create_time")), "yyyy-MM-dd HH:mm:ss"));
			return map;
		}
	}

	/**
	 * 
	 * @Title: seeCode
	 * @Description: TODO 修改卡密查看状态
	 * @param itemid
	 *            订单项id
	 * @return
	 * @throws Exception
	 * @return: String @author： liuyulei
	 * @date：2016年9月21日 上午10:43:21
	 */
	@ResponseBody
	@RequestMapping("/service-goods-seecode")
	public String seeCode(Integer itemid) throws Exception {
		try {
			this.serviceGoodsManager.seeCodeEdit(itemid);
			return JsonMessageUtil.getSuccessJson("成功！");
		} catch (Exception e) {
			this.logger.error("修改卡密查看状态失败！");
			return JsonMessageUtil.getErrorJson("修改卡密查看状态失败！");
		}
	}

	
	/**
	 * 
	 * @Title: isCurrcentStore
	 * @Description: TODO 判断 验证码 是否属于当前登录人的店铺
	 * @param code 验证码
	 * @param self_store 是否为平台自营
	 * @return
	 * @return: boolean true 是     false 不是
	 */
	private boolean isCurrcentStore(String code,String self_store) {
		String store_str = this.getStoreId().toString();
		if("b2b2c".equals(EopSetting.PRODUCT)){
			store_str = "yes".equals(self_store)?"1":this.getStoreId().toString(); // 店铺id
		}
		
		//根据卡密   获取卡密所属店铺
		Map map = this.serviceGoodsManager.checkCode(code.trim());
		Integer store_id = (Integer) map.get("store_id");
		
		return store_str.equals(store_id.toString());
	}
	
	/**
	 * 
	 * @Title: getServiceGoodsSeeCode
	 * @Description: TODO 获取卡密查看状态
	 * @return
	 * @throws ParseException
	 * @return: List @author： liuyulei
	 * @date：2016年9月21日 上午10:46:54
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value = "/get-service-code-look-status", produces = MediaType.APPLICATION_JSON_VALUE)
	public Object getServiceGoodsSeeCode() throws ParseException {
		try {
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			Integer itemid = Integer.parseInt(request.getParameter("itemid"));
			List<ServiceGoods> sGoodsList = this.serviceGoodsManager.getCodeByParam(itemid);
			Integer status = 0;
			if(sGoodsList == null){
				return status;
			}
			for (ServiceGoods serviceGoods : sGoodsList) {
				status = serviceGoods.getLook_status() == 1 ? 1 : 0;
			}
			return status;
		} catch (NumberFormatException e) {

			e.printStackTrace();
			return 2;
		}
	}

	/**
	 * 
	 * @Title: getCodeByOrderid
	 * @Description: TODO 根据订单id获取卡密列表信息
	 * @param orderid
	 * @return
	 * @throws Exception
	 * @return: String @author： liuyulei
	 * @date：2016年9月21日 下午2:01:48
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/get-service-goods-seecode-status")
	public Object getCodeByOrderid(Integer orderid) throws Exception {
		try {
			List<Map> sGoodsList = this.serviceGoodsManager.getCodeLsitByOrderId(orderid);
			Integer status = 0;
			if (sGoodsList != null) {
				for (Map map : sGoodsList) {
					status = (Integer) map.get("look_status");
				}
			}
			return status;
		} catch (Exception e) {
			this.logger.error("根据订单id获取卡密状态出错！");
			e.printStackTrace();
			return 2;
		}
	}

	/**
	 * 
	 * @Title: isCurrcentStore
	 * @Description: TODO 判断 验证码 是否属于当前登录人的店铺
	 * @param code
	 *            验证码
	 * @return
	 * @return: boolean true 是 false 不是
	 */
	private boolean isCurrcentStore(String code) {
		String storeId_str = code.substring(0, this.getStoreId().toString().length()); // 从验证码中获取店铺ID
		String store_str = this.getStoreId().toString(); // 店铺id
		return store_str.equals(storeId_str);
	}

	/**
	 * 
	 * @Title: getStoreId
	 * @Description: TODO 后台校验,获取店铺id
	 * @return 店铺id
	 * @return: Integer @author： liuyulei
	 * @date：2016年9月14日 上午7:17:46
	 */
	private Integer getStoreId() {
		Member member = UserConext.getCurrentMember();
		StringBuffer sql = new StringBuffer();
		sql.append("select s.store_id from es_store s where s.member_id = ? ");
		Integer id = this.daoSupport.queryForInt(sql.toString(), member.getMember_id());
		return id;
	}

}
