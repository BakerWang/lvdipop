package com.enation.app.service.core.servicegoods.action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.base.core.model.Member;
import com.enation.app.service.core.servicegoods.service.IServiceGoodsManager;
import com.enation.app.shop.core.goods.service.IBrandManager;
import com.enation.eop.sdk.context.EopSetting;
import com.enation.eop.sdk.context.UserConext;
import com.enation.framework.action.GridController;
import com.enation.framework.action.GridJsonResult;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;

/**
 * 
 * @ClassName: ServiceGoodsController
 * @Description: 服务商品验证
 * @author: liuyulei
 * @date: 2016年9月14日 上午9:51:25
 * @since:v61
 * @version:v1.0
 */
@Scope("prototype")
@Controller
@RequestMapping("/shop/admin/service-goods")
public class ServiceGoodsController extends GridController {

	@Autowired
	private IServiceGoodsManager serviceGoodsManager;
	
	@Autowired
	protected IBrandManager brandManager;
	
	@Autowired
	private IStoreMemberManager storeMemberManager;
	
	@Autowired
	private IDaoSupport daoSupport;

	/**
	 * 
	 * @Title: getServiceGoodCheckCodesHtml 
	 * @Description: TODO   跳转到服务商品列表页面
	 * @return
	 * @return: ModelAndView
	 * @author： liuyulei
	 * @date：2016年9月20日 上午10:23:55
	 */
	@RequestMapping("/get-service-goods-list")
	public ModelAndView getServiceGoodListHtml() {
		ModelAndView view = getGridModelAndView();
		view.setViewName("/shop/admin/goods/service_goods_list");
		return view;
	}
	
	/**
	 * 服务码校验
	 * 
	 * @param code
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/check-code")
	public JsonResult checkCode(String code,String self_store) {
		Map map = null;
		
		try {
			map = serviceGoodsManager.checkCode(code.trim());
			
			if (map == null ) {//如果为null   
				return JsonResultUtil.getErrorJson("验证码不存在!");
			}
			
			if("b2b2c".equals(EopSetting.PRODUCT)){   //如果是多店，则要验证是否为自营
				if(!this.isCurrcentStore(code, self_store)){//自营店铺验证卡密
					return JsonResultUtil.getErrorJson("验证码不属于本店铺！");
				}
			}
			
			
			if (map.get("status") != null && ((Integer) map.get("status")) == 1) {//如果状态为  1
				return JsonResultUtil.getErrorJson("已消费！");
			} else if (map.get("valid_time") != null && Integer.parseInt(map.get("valid_time") + "") == 0) {//超过有效期
				return JsonResultUtil.getErrorJson("超过有效期，该验证码已失效！");
			}else if(map.get("status") != null && ((Integer) map.get("status")) == 2){
				return JsonResultUtil.getErrorJson("超过有效期！");
			}else if(map.get("status") != null && ((Integer) map.get("status")) == 3){
				return JsonResultUtil.getErrorJson("卡密已经失效！");
			}else{
				return JsonResultUtil.getSuccessJson("success");
			}

		} catch (Exception e) {
			this.logger.error("获取验证码信息失败!", e);
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("error");
		}

		
	}

	/**
	 * 使用服务码
	 * 
	 * @param code
	 * @return Map
	 */
	@ResponseBody
	@RequestMapping("/service-goods-used")
	public Map used(String code) {
		// System.out.println("使用:"+code);
		Map map = null;
		try {
			map = serviceGoodsManager.used(code, 1); // 修改验证码状态
		} catch (Exception e) {
			this.logger.error("修改验证码状态失败!", e);
			e.printStackTrace();
		}

		if (map == null) {	//map如果为null
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
	 * @Title: getServiceGoodCheckCodesHtml 
	 * @Description: TODO  跳转服务码校验页面
	 * @return
	 * @return: ModelAndView
	 * @author： liuyulei
	 * @date：2016年9月14日 下午2:58:30
	 */
	@RequestMapping("/get-servicegoods-checkcode-html")
	public ModelAndView getServiceGoodCheckCodesHtml() {
		ModelAndView view = new ModelAndView();
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String self_store = request.getParameter("self_store")==null?"no":request.getParameter("self_store");
		view.addObject("self_store", self_store);
		view.setViewName("/shop/admin/order/order_check");
		return view;

	}
	
	/**
	 * 
	 * @Title: getServiceGoodCheckCodesHtml 
	 * @Description: TODO  跳转服务商品卡密列表页面
	 * @return
	 * @return: ModelAndView
	 * @author： liuyulei
	 * @date：2016年9月14日 下午2:58:30
	 */
	@RequestMapping("/service-goods-list")
	public ModelAndView getServiceGoodsList() {
		ModelAndView view= getGridModelAndView();
		view.addObject("optype","no");
		view.setViewName("/b2b2c/admin/goods/servicegoods_list");
		return view;

	}
	
	
	/**
	 * 
	 * @Title: listJson 
	 * @Description: 服务商品卡密列表
	 * @param stype 搜索类型,Integer
	 * @param keyword 搜索关键字,String
	 * @param catid 商品分类Id,Integer
	 * @param name 商品名称,String
	 * @param sn 商品编号,String 
	 * @return
	 * @return: GridJsonResult
	 * @author： liuyulei
	 * @date：2016年9月18日 下午5:12:35
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@RequestMapping(value="/list-json")
	public GridJsonResult listJson(Integer catid,Integer stype,String keyword,String name,String sn) {

		Map goodsMap = new HashMap();
		if(stype!=null){
			if(stype==0){
				goodsMap.put("stype", stype);
				goodsMap.put("keyword", keyword);
			}else if(stype==1){
				goodsMap.put("stype", stype);
				goodsMap.put("name", name);
				goodsMap.put("sn", sn);
				goodsMap.put("catid", catid);
			}
		}
		
		webpage =this.serviceGoodsManager.searchServiceGoods(goodsMap, this.getPage(), this.getPageSize(), null,this.getSort(),this.getOrder());
		
		return JsonResultUtil.getGridJson(webpage);
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
		StoreMember storemember = this.storeMemberManager.getStoreMember();
		String store_str = "";
		if("b2b2c".equals(EopSetting.PRODUCT)){
			store_str = "yes".equals(self_store)?"1":storemember.getStore_id()+""; // 店铺id
		}
		//根据卡密   获取卡密所属店铺
		Map map = this.serviceGoodsManager.checkCode(code.trim());
		Integer store_id = (Integer) map.get("store_id");
		
		return store_str.equals(store_id.toString());
	}
}
