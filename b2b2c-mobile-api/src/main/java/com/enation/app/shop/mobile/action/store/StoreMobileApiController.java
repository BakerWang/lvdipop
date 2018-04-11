package com.enation.app.shop.mobile.action.store;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.jasypt.commons.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.b2b2c.core.goods.service.IStoreGoodsManager;
import com.enation.app.b2b2c.core.goods.service.IStoreGoodsTagManager;
import com.enation.app.b2b2c.core.member.model.MemberCollect;
import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreCollectManager;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.b2b2c.core.store.model.Store;
import com.enation.app.b2b2c.core.store.service.IStoreManager;
import com.enation.app.base.core.model.Member;
import com.enation.app.shop.mobile.service.impl.ApiStoreManager;
import com.enation.app.shop.mobile.utils.CommonRequest;
import com.enation.eop.SystemSetting;
import com.enation.eop.sdk.context.UserConext;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.Page;
import com.enation.framework.util.JsonResultUtil;

/**
 * 商家api
 * 
 * @author wanghongjun
 * @version v1.1 2015-08-31
 * @since v1.0
 */
@SuppressWarnings("serial")
@Controller
@Scope("prototype")
@RequestMapping("/api/mobile/store")
public class StoreMobileApiController {
	protected final Logger logger = Logger.getLogger(StoreMobileApiController.class);
	
	
	@Autowired
	private IStoreManager storeManager;
	@Autowired
	private IStoreMemberManager storeMemberManager;
	@Autowired
	private IStoreGoodsTagManager storeGoodsTagManager;
	@Autowired
	private IStoreGoodsManager storeGoodsManager;
	@Autowired
	private IStoreCollectManager storeCollectManager;
	
	@Autowired
	private ApiStoreManager apiStoreManager;

	/**
	 * 通过商家ID，获得商家详细，
	 * store_id 商家ID
	 * type     商家类型
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/store-intro",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult storeIntro(HttpServletRequest request ) {
		try {
			Map<String,Object> data = new HashMap<String,Object>();
			Store store = storeManager.getStore(NumberUtils.toInt(request.getParameter("storeid"), 0));
			Integer memberId = CommonRequest.getMemberID();
			StoreMember member   = storeMemberManager.getMember(memberId);
			if(member == null) {
				data.put("isCollect", false);
			} else {
				data.put("isCollect", storeCollectManager.isCollect(member.getMember_id(), store.getStore_id()));
			}
			data.put("store", store);
			return JsonResultUtil.getObjectJson(data);
		} catch (RuntimeException e) {
			this.logger.error("获取商家详细出错", e);
			return JsonResultUtil.getErrorJson(e.getMessage());

		}
	}
	
	/**
	 * 需要修改图片
	 * 根据标签获取店铺商品列表
	 * num  显示数量
	 * storeid  商家ID
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@RequestMapping(value="/store-goods-list",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult storeGoodsList(Integer storeid,Integer pageNo,Integer pageSize) {
		JsonResult jsonResult=new JsonResult();
		HttpServletRequest request=ThreadContextHolder.getHttpRequest();
		String mark = request.getParameter("mark");
		Integer member_id = CommonRequest.getMemberID();
		try {
			if(storeid==null || storeid==0){
				StoreMember storeMember = storeMemberManager.getMember(member_id);
				storeid = storeMember.getStore_id();
			}
			if( pageSize==null||pageSize==0) pageSize=SystemSetting.getBackend_pagesize();
			Map map = new HashMap();
			map.put("mark", mark);
			map.put("storeid", storeid);
			String page = request.getParameter("pageNo");
			page = (page == null || page.equals("")) ? "1" : page;
			Page webpage=new Page();
			//查询标签商品列表
			webpage = storeGoodsTagManager.getGoodsList(map, Integer.parseInt(page), pageSize);
			for (Map resultMap : (List<Map>) webpage.getResult()) {
				resultMap.put("thumbnail",StaticResourcesUtil.convertToUrl((String)resultMap.get("thumbnail")));
				resultMap.put("small",StaticResourcesUtil.convertToUrl((String)resultMap.get("small")));
				resultMap.put("big",StaticResourcesUtil.convertToUrl((String)resultMap.get("big")));
			}
			//获取总记录数
			Long totalCount = webpage.getTotalCount();
			webpage.setCurrentPageNo(pageNo);
			if(totalCount==0){
				jsonResult.setResult(1);
				jsonResult.setData("[]");
				return jsonResult;
			}else{
				return JsonResultUtil.getObjectJson(webpage);
			}
		} catch (RuntimeException e) {
			this.logger.error("获取商品列表出错", e);
			return JsonResultUtil.getErrorJson(e.getMessage());

		}
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@RequestMapping(value="/store-goods-all",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult storeGoodsAll(Integer storeid, String pageNo, String pageSize) {
		JsonResult jsonResult = new JsonResult();
		try {
			Map map = new HashMap();
			map.put("store_id", storeid);
			map.put("market_enable", 1);//添加marker_enable 为上架
			pageNo = (pageNo == null || pageNo.equals("")) ? "1" : pageNo;
			if (StringUtils.isEmpty(pageSize)) {
				pageSize = "10";
			}
			Page webpage = new Page();
			webpage = storeGoodsManager.storeGoodsList(Integer.valueOf(pageNo), Integer.valueOf(pageSize), map);
			for (Map resultMap : (List<Map>) webpage.getResult()) {
				resultMap.put("thumbnail",StaticResourcesUtil.convertToUrl((String)resultMap.get("thumbnail")));
				resultMap.put("small",StaticResourcesUtil.convertToUrl((String)resultMap.get("small")));
				resultMap.put("big",StaticResourcesUtil.convertToUrl((String)resultMap.get("big")));
				resultMap.put("original",StaticResourcesUtil.convertToUrl((String)resultMap.get("original")));
			}
			// 获取总记录数
			Long totalCount = webpage.getTotalCount();
			webpage.setCurrentPageNo(Long.valueOf(pageNo));
			if (totalCount == 0) {
				jsonResult.setResult(1);
				jsonResult.setData("[]");
				return jsonResult;
			} else {
				return JsonResultUtil.getObjectJson(webpage);
			}
		} catch (RuntimeException e) {
			this.logger.error("获取商品列表出错", e);
			return JsonResultUtil.getErrorJson(e.getMessage());
		}
	}
	
	
	/**
	 * 添加收藏店铺
	 * @param member 店铺会员,StoreMember
	 * @param store_id 店铺Id,Integer
	 * @param collect	收藏店铺,MemberCollect
	 * result 	为1表示调用成功0表示失败 
	 * @return 返回json串
	 */
	@ResponseBody
	@RequestMapping(value="/add-collect",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult addCollect(Integer store_id){
		try {
			Integer member_id = CommonRequest.getMemberID();
			//SSO 
			StoreMember member = storeMemberManager.getMember(member_id);
			if (member!=null) {
				if(member.getStore_id()!=null && member.getStore_id().equals(store_id)){
					return JsonResultUtil.getErrorJson("不能收藏自己的店铺！");
				}
				MemberCollect collect = new MemberCollect();
				collect.setMember_id(member.getMember_id());
				collect.setStore_id(store_id);
				this.storeCollectManager.addCollect(collect);
				this.storeManager.addcollectNum(store_id);
				return JsonResultUtil.getSuccessJson("收藏成功!");
			} else {
				return JsonResultUtil.getErrorJson("请登录！收藏失败！");
			}
		} catch (RuntimeException e) {
			this.logger.error("收藏店铺出错", e);
			return JsonResultUtil.getErrorJson(e.getMessage() + "");
		}
	}
	
	/**
	 * 删除收藏店铺
	 * @param celloct_id 收藏店铺Id,Integer
	 * @param store_id 店铺Id,Integer
	 * @return 返回json串
	 * result 	为1表示调用成功0表示失败
	 */
	@ResponseBody
	@RequestMapping(value="/del",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult del(Integer store_id){
		try {
			Integer member_id = CommonRequest.getMemberID();
			StoreMember member = storeMemberManager.getMember(member_id);
			if(member != null){
				this.storeCollectManager.delCollect(store_id,member.getMember_id());
				this.storeManager.reduceCollectNum(store_id);
				return JsonResultUtil.getSuccessJson("取消收藏成功！");
			}else{
				return JsonResultUtil.getErrorJson("请登录！取消收藏失败！");
			}
		} catch (Exception e) {
			this.logger.error("删除店铺收藏出错", e);
			return JsonResultUtil.getErrorJson("取消收藏失败，请重试！");
		}
	}
	
	/**
	 * 获取商铺列表
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value="/store-list",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult storeList() {
		try {
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			String pageNo = request.getParameter("page");
			int pageSize=10;
			pageNo = (pageNo == null || pageNo.equals("")) ? "1" : pageNo;
			
			Page page = storeManager.store_list(new HashMap(),1, Integer.parseInt(pageNo), pageSize);
			Collections.sort((List)page.getResult(), new Comparator<Map>() {
				public int compare(Map o1, Map o2) {
					return (Double) o1.get("distance") < (Double) o2.get("distance")
							? ((Double) o1.get("distance") == (Double) o2.get("distance") ? 0 : -1) : 1;
				}
			});
			
			return JsonResultUtil.getObjectJson(page);
		} catch(RuntimeException e) {
			this.logger.error("获取店铺列表出错", e);
			return JsonResultUtil.getErrorJson("获取列表失败，请重试！");
		}
	}
	
	/**
	 * 获取收藏的店铺列表
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/store-collect-list",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult storeCollectList() {
		try {
			Integer member_id = CommonRequest.getMemberID();
			StoreMember member = storeMemberManager.getMember(member_id);
			if (member != null) {
				HttpServletRequest request = ThreadContextHolder.getHttpRequest();
				String pageNo = request.getParameter("page");
				String pageSize = request.getParameter("pageSize");
				if (StringUtils.isEmpty(pageSize)) {
					pageSize = "10";
				}
				pageNo = (pageNo == null || pageNo.equals("")) ? "1" : pageNo;
				Page page = storeCollectManager.getList(member.getMember_id(), Integer.parseInt(pageNo),
						Integer.valueOf(pageSize));
				List list = (List) page.getResult();
				for (int i = 0; i < list.size(); i++) {
					Map map = (Map) list.get(i);
					map.put("store_logo", StaticResourcesUtil.convertToUrl(map.get("store_logo").toString()));
				}
				page.setCurrentPageNo(Long.valueOf(pageNo));
				return JsonResultUtil.getObjectJson(page);
			} else {
				return JsonResultUtil.getErrorJson("请登录！获取店铺收藏列表失败！");
			}
		} catch (RuntimeException e) {
			this.logger.error("获取店铺收藏列表出错", e);
			return JsonResultUtil.getErrorJson("获取店铺收藏列表失败，请重试！");
		}
	}
	
	
	/**
	 * 
	 * @Title: getstoreListByStoreName 
	 * @Description: 根据店铺名称查询   模糊查询店铺
	 * @param store_name		[String]  	店铺名称         非必填项
	 * @param city_name			[Stirng]  	城市名称		非必填项
	 * @param longitude			[Double]  	经度			非必填项
	 * @param latitude			[Double]  	纬度			非必填项
	 * @param sclassify_id		[Integer] 	分类	ID		非必填项
	 * @param page				[Integer] 	页码		        非必填项
	 * @param pageSize			[Integer] 	每页显示条数		非必填项
	 * @return
	 * @return: JsonResult
	 * @author： liuyulei
	 * @date：2016年10月9日 下午5:04:27
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@RequestMapping(value="/search-store-list",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult getstoreListByStoreName(String store_name,String city_name,
			Double longitude,Double latitude,Integer sclassify_id,Integer store_recommend) {
		try {
			Map params = new HashMap();
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			String pageNo = request.getParameter("page");
			String pageSize = request.getParameter("pageSize");
			params.put("store_name", store_name);
			params.put("city_name", city_name);  //城市名称
			params.put("longitude", longitude);
			params.put("latitude", latitude);        
			params.put("sclassify_id", sclassify_id);      
			params.put("store_recommend", store_recommend);
			pageNo = (pageNo == null || pageNo.equals("")) ? "1" : pageNo;
			pageSize = (pageSize == null || pageSize.equals("")) ? "10" : pageSize;
			//查询出店铺数据
			Page page = this.apiStoreManager.storeList(params,1, Integer.parseInt(pageNo), Integer.parseInt(pageSize));
			return JsonResultUtil.getObjectJson(page);
		} catch(RuntimeException e) {
			e.printStackTrace();
			this.logger.error("获取店铺列表出错", e);
			return JsonResultUtil.getErrorJson("获取列表失败，请重试！");
		}
	}
	
	/**
	 * 
	 * @Title: getStoreInfo 
	 * @Description:   根据店铺id  查询店铺详细信息
	 * @param store_id  店铺ID
	 * @return  
	 * 		result	[Integer]	返回结果   0：失败     1：成功	
	 * 		message [String]	提示信息
	 * 		data	[Object]	店铺实体	
	 * @return: JsonResult
	 * @author： liuyulei
	 * @date：2016年10月10日 下午12:45:10
	 */
	@ResponseBody
	@RequestMapping(value="/get-store-info",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult getStoreInfo(Integer store_id){
		Integer member_id = CommonRequest.getMemberID();
		boolean isCollect  = false;
		isCollect =  this.storeCollectManager.isCollect(member_id, store_id);
		try {
			if(store_id == null ){
				this.logger.error("查询店铺详细，参数：store_id为空！");
				return JsonResultUtil.getErrorJson("查询店铺详细，参数参数：store_id为空！");
			}
			Map map = this.apiStoreManager.getStore(store_id);
			map.put("isCollect", isCollect);
			return JsonResultUtil.getObjectJson(map);
		} catch(RuntimeException e) {
			this.logger.error("获取店铺详细出错", e);
			return JsonResultUtil.getErrorJson("获取店铺详细失败，请重试！");
		}
	}
	
}
