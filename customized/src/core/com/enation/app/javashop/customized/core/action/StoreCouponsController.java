package com.enation.app.javashop.customized.core.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.enation.app.b2b2c.core.store.model.Store;
import com.enation.app.b2b2c.core.store.model.StoreCoupons;
import com.enation.app.b2b2c.core.store.service.IStoreManager;
import com.enation.app.base.core.model.Member;
import com.enation.app.base.core.service.IMemberManager;
import com.enation.app.base.core.service.IRegionsManager;
import com.enation.app.javashop.customized.core.service.IMemberCouponsManager;
import com.enation.app.javashop.customized.core.service.IStoreCouponsManager;
import com.enation.app.shop.component.bonus.model.MemberCoupons;
import com.enation.app.shop.core.member.service.IMemberLvManager;
import com.enation.framework.action.GridController;
import com.enation.framework.action.GridJsonResult;
import com.enation.framework.action.JsonResult;
import com.enation.framework.database.Page;
import com.enation.framework.util.CurrencyUtil;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;

/**
 * 自营店铺优惠券管理类
 * @author DMRain
 * @date 2016-10-8
 * @since v61
 * @version 1.0
 */
@SuppressWarnings({ "rawtypes", "unchecked", "static-access" })
@Scope("prototype")
@Controller 
@RequestMapping("/b2b2c/admin/self-coupons")
public class StoreCouponsController extends GridController{

	@Autowired
	private IStoreCouponsManager storeCouponsManager;
	
	@Autowired
	private IMemberCouponsManager memberCouponsManager;
	
	@Autowired
	private IStoreManager storeManager;
	
	@Autowired
	private IMemberManager memberManager;
	
	@Autowired
	private IRegionsManager regionsManager;
	
	@Autowired
	private IMemberLvManager memberLvManager;
	
	/**
	 * 跳转至优惠券分页列表
	 * @return
	 */
	@RequestMapping(value="/list")
	public ModelAndView list(){
		ModelAndView view = this.getGridModelAndView();
		view.addObject("provinceList", this.regionsManager.listProvince());
		view.setViewName("/b2b2c/admin/coupons/self-coupons-list");
		return view;
	}
	
	/**
	 * 获取优惠券分页列表json
	 * @param stype 搜索类型
	 * @param coupons_name 优惠券名称
	 * @param coupons_type 优惠券类型
	 * @param status 优惠券状态
	 * @param send_status 发放状态
	 * @param province_id 使用地区id
	 * @param start_time 优惠券生效日期
	 * @param end_time 优惠券失效日期
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/list-json")
	public GridJsonResult listJson(Integer stype, String coupons_name, String coupons_type, String status, String send_status, Integer province_id, String start_time, String end_time){
		Map map = new HashMap();
		map.put("stype", stype);
		map.put("keyword", coupons_name);
		map.put("coupons_type", coupons_type);
		map.put("coupons_status", status);
		map.put("send_status", send_status);
		map.put("province_id", province_id);
		map.put("start_date", start_time);
		map.put("end_date", end_time);
		
		Page webpage = this.storeCouponsManager.getStoreCouponsList(this.getPage(), this.getPageSize(), this.storeCouponsManager.SELF_STORE_ID_KEY, map);
		return JsonResultUtil.getGridJson(webpage);
	}
	
	/**
	 * 跳转至新增优惠券页面
	 * @return
	 */
	@RequestMapping(value="/add")
	public ModelAndView add() {
		ModelAndView view = new ModelAndView();
		view.addObject("provinceList", this.regionsManager.listProvince());
		view.setViewName("/b2b2c/admin/coupons/self-coupons-add");
		return view;
	}
	
	/**
	 * 保存新增优惠券信息
	 * @param storeCoupons 优惠券信息
	 * @param startDate 优惠券生效日期
	 * @param endDate 优惠券失效日期
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/save-add")
	public JsonResult saveAdd(StoreCoupons storeCoupons, String startDate, String endDate){
		try {
			//验证优惠券名称是否为空或是否超过10个字符
			if (StringUtil.isEmpty(storeCoupons.getCoupons_name())) {
				return JsonResultUtil.getErrorJson("请输入优惠券名称！");
			} else {
				if (storeCoupons.getCoupons_name().length() > 10) {
					return JsonResultUtil.getErrorJson("优惠券名称不得超过10个字符！");
				}
			}
			
			//验证优惠券类型是否为空
			if (storeCoupons.getCoupons_type() == null) {
				return JsonResultUtil.getErrorJson("请选择优惠券类型！");
			} else {
				
				//如果优惠券类型为满减券或者现金券
				if (storeCoupons.getCoupons_type().equals(0) || storeCoupons.getCoupons_type().equals(2)) {
					//如果优惠券面额等于空
					if (storeCoupons.getCoupons_money() == null) {
						return JsonResultUtil.getErrorJson("优惠券面额不能为空！");
					}
					
					//如果优惠券面额为0
					if (storeCoupons.getCoupons_money().doubleValue() <= 0) {
						return JsonResultUtil.getErrorJson("优惠券面额不能等于或小于0！");
					}
				} 
				
				//如果优惠券类型为折扣券
				if (storeCoupons.getCoupons_type().equals(1)) {
					//如果优惠券折扣为空
					if (storeCoupons.getCoupons_discount() == null) {
						return JsonResultUtil.getErrorJson("优惠券折扣不能为空！");
					}
					
					//如果优惠券折扣大于等于100或者小于等于0
					if (storeCoupons.getCoupons_discount().intValue() >= 100 && storeCoupons.getCoupons_discount().intValue() <= 0) {
						return JsonResultUtil.getErrorJson("优惠券折扣只能输入0~100内的整数！");
					}
				}
			}
			
			//如果优惠券类型不是现金券
			if (!storeCoupons.getCoupons_type().equals(2)) {
				//如果最小订单金额为空
				if (storeCoupons.getMin_order_money() == null) {
					return JsonResultUtil.getErrorJson("买家需消费的金额不能为空！");
				}
				
				//如果最小订单金额为0
				if (storeCoupons.getMin_order_money().doubleValue() <= 0) {
					return JsonResultUtil.getErrorJson("买家需消费的金额不能等于或小于0！");
				}
			}
			
			//如果生效日期为空
			if (StringUtil.isEmpty(startDate)) {
				return JsonResultUtil.getErrorJson("优惠券生效日期不能为空！");
			}
			
			//如果失效日期为空
			if (StringUtil.isEmpty(endDate)) {
				return JsonResultUtil.getErrorJson("优惠券失效日期不能为空！");
			}
			
			long start_date = DateUtil.getDateline(startDate + " 00:00:00", "yyyy-MM-dd HH:mm:ss");
			long end_date = DateUtil.getDateline(endDate + " 23:59:59", "yyyy-MM-dd HH:mm:ss");
			
			//如果生效日期大于或等于失效日期
			if (start_date >= end_date) {
				return JsonResultUtil.getErrorJson("优惠券生效日期不能大于或等于失效日期！");
			}
			
			//验证是否选择了优惠券使用地区
			if (storeCoupons.getProvince_id().equals(0)) {
				return JsonResultUtil.getErrorJson("请选择优惠券使用地区！");
			}
			
			//如果优惠券的发行量为空
			if (storeCoupons.getCoupons_stock() == null) {
				return JsonResultUtil.getErrorJson("发行量不能为空！");
			}
			
			//如果优惠券的发行量为0
			if (storeCoupons.getCoupons_stock().equals(0)) {
				return JsonResultUtil.getErrorJson("发行量不能等于或小于0！");
			}
			
			//如果优惠券的限领数量为空
			if (storeCoupons.getLimit_num() == null) {
				return JsonResultUtil.getErrorJson("限领数量不能为空！");
			}
			
			//如果优惠券的限领数量为0
			if (storeCoupons.getLimit_num().equals(0)) {
				return JsonResultUtil.getErrorJson("限领数量不能等于或小于0");
			}
			
			//如果优惠券的限领数量大于发行量
			if (storeCoupons.getLimit_num().intValue() > storeCoupons.getCoupons_stock().intValue()) {
				return JsonResultUtil.getErrorJson("限领数量不能大于发行量！");
			}
			
			storeCoupons.setStore_id(this.storeCouponsManager.SELF_STORE_ID_KEY);
			
			storeCoupons.setCreate_date(DateUtil.getDateline());
			storeCoupons.setStart_date(start_date);
			storeCoupons.setEnd_date(end_date);
			
			//添加优惠券使用地区的名称
			storeCoupons.setProvince_name(this.regionsManager.get(storeCoupons.getProvince_id()).getLocal_name());
			
			storeCoupons.setSend_status(0);
			storeCoupons.setStatus(0);
			storeCoupons.setIs_deleted(0);
			
			this.storeCouponsManager.add(storeCoupons);
			return JsonResultUtil.getSuccessJson("新增优惠券成功！");
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("新增失败：", e);
			return JsonResultUtil.getErrorJson("新增失败！");
		}
	}
	
	/**
	 * 跳转至优惠券修改页面
	 * @param coupons_id 优惠券id
	 * @return
	 */
	@RequestMapping(value="/edit")
	public ModelAndView edit(Integer coupons_id) {
		ModelAndView view = new ModelAndView();
		view.addObject("storeCoupons", this.storeCouponsManager.get(coupons_id, this.storeCouponsManager.SELF_STORE_ID_KEY));
		view.addObject("provinceList", this.regionsManager.listProvince());
		view.setViewName("/b2b2c/admin/coupons/self-coupons-edit");
		return view;
	}
	
	/**
	 * 保存修改的优惠券信息
	 * @param storeCoupons 优惠券信息
	 * @param startDate 优惠券生效日期
	 * @param endDate 优惠券失效日期
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/save-edit")
	public JsonResult saveEdit(StoreCoupons storeCoupons, String startDate, String endDate){
		try {
			//验证优惠券名称是否为空或是否超过10个字符
			if (StringUtil.isEmpty(storeCoupons.getCoupons_name())) {
				return JsonResultUtil.getErrorJson("请输入优惠券名称！");
			} else {
				if (storeCoupons.getCoupons_name().length() > 10) {
					return JsonResultUtil.getErrorJson("优惠券名称不得超过10个字符！");
				}
			}
			
			//验证优惠券类型是否为空
			if (storeCoupons.getCoupons_type() == null) {
				return JsonResultUtil.getErrorJson("请选择优惠券类型！");
			} else {
				
				//如果优惠券类型为满减券或者现金券
				if (storeCoupons.getCoupons_type().equals(0) || storeCoupons.getCoupons_type().equals(2)) {
					//如果优惠券面额等于空
					if (storeCoupons.getCoupons_money() == null) {
						return JsonResultUtil.getErrorJson("优惠券面额不能为空！");
					}
					
					//如果优惠券面额为0
					if (storeCoupons.getCoupons_money().doubleValue() <= 0) {
						return JsonResultUtil.getErrorJson("优惠券面额不能等于或小于0！");
					}
				} 
				
				//如果优惠券类型为折扣券
				if (storeCoupons.getCoupons_type().equals(1)) {
					//如果优惠券折扣为空
					if (storeCoupons.getCoupons_discount() == null) {
						return JsonResultUtil.getErrorJson("优惠券折扣不能为空！");
					}
					
					//如果优惠券折扣大于等于100或者小于等于0
					if (storeCoupons.getCoupons_discount().intValue() >= 100 && storeCoupons.getCoupons_discount().intValue() <= 0) {
						return JsonResultUtil.getErrorJson("优惠券折扣只能输入0~100内的整数！");
					}
				}
			}
			
			//如果优惠券类型不是现金券
			if (!storeCoupons.getCoupons_type().equals(2)) {
				//如果最小订单金额为空
				if (storeCoupons.getMin_order_money() == null) {
					return JsonResultUtil.getErrorJson("买家需消费的金额不能为空！");
				}
				
				//如果最小订单金额为0
				if (storeCoupons.getMin_order_money().doubleValue() <= 0) {
					return JsonResultUtil.getErrorJson("买家需消费的金额不能等于或小于0！");
				}
			}
			
			//如果生效日期为空
			if (StringUtil.isEmpty(startDate)) {
				return JsonResultUtil.getErrorJson("优惠券生效日期不能为空！");
			}
			
			//如果失效日期为空
			if (StringUtil.isEmpty(endDate)) {
				return JsonResultUtil.getErrorJson("优惠券失效日期不能为空！");
			}
			
			long start_date = DateUtil.getDateline(startDate + " 00:00:00", "yyyy-MM-dd HH:mm:ss");
			long end_date = DateUtil.getDateline(endDate + " 23:59:59", "yyyy-MM-dd HH:mm:ss");
			
			//如果生效日期大于或等于失效日期
			if (start_date >= end_date) {
				return JsonResultUtil.getErrorJson("优惠券生效日期不能大于或等于失效日期！");
			}
			
			//验证是否选择了优惠券使用地区
			if (storeCoupons.getProvince_id().equals(0)) {
				return JsonResultUtil.getErrorJson("请选择优惠券使用地区！");
			}
			
			//如果优惠券的发行量为空
			if (storeCoupons.getCoupons_stock() == null) {
				return JsonResultUtil.getErrorJson("发行量不能为空！");
			}
			
			//如果优惠券的发行量为0
			if (storeCoupons.getCoupons_stock().equals(0)) {
				return JsonResultUtil.getErrorJson("发行量不能等于或小于0！");
			}
			
			//如果优惠券的限领数量为空
			if (storeCoupons.getLimit_num() == null) {
				return JsonResultUtil.getErrorJson("限领数量不能为空！");
			}
			
			//如果优惠券的限领数量为0
			if (storeCoupons.getLimit_num().equals(0)) {
				return JsonResultUtil.getErrorJson("限领数量不能等于或小于0");
			}
			
			//如果优惠券的限领数量大于发行量
			if (storeCoupons.getLimit_num().intValue() > storeCoupons.getCoupons_stock().intValue()) {
				return JsonResultUtil.getErrorJson("限领数量不能大于发行量！");
			}
			
			storeCoupons.setStart_date(start_date);
			storeCoupons.setEnd_date(end_date);
			
			//添加优惠券使用地区的名称
			storeCoupons.setProvince_name(this.regionsManager.get(storeCoupons.getProvince_id()).getLocal_name());
			
			this.storeCouponsManager.edit(storeCoupons);
			return JsonResultUtil.getSuccessJson("优惠券修改成功！");
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("修改失败：", e);
			return JsonResultUtil.getErrorJson("修改失败！");
		}
	}
	
	/**
	 * 发放优惠券
	 * @param coupons_id 优惠券id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/send-coupons")
	public JsonResult sendCoupons(Integer coupons_id){
		try {
			this.storeCouponsManager.send(coupons_id, this.storeCouponsManager.SELF_STORE_ID_KEY);
			return JsonResultUtil.getSuccessJson("已成功将优惠券发放！");
		} catch (Exception e) {
			this.logger.error("优惠券发放失败：", e);
			return JsonResultUtil.getErrorJson("优惠券发放失败！");
		}
	}
	
	/**
	 * 设置优惠券失效
	 * @param coupons_id 优惠券id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/invalid-coupons")
	public JsonResult invalidCoupons(Integer coupons_id){
		try {
			this.storeCouponsManager.invalid(coupons_id, this.storeCouponsManager.SELF_STORE_ID_KEY);
			return JsonResultUtil.getSuccessJson("已成功将此优惠券设置为无效！");
		} catch (Exception e) {
			this.logger.error("设置失效失败：", e);
			return JsonResultUtil.getErrorJson("优惠券设置失效失败！");
		}
	}
	
	/**
	 * 删除优惠券
	 * @param coupons_id 优惠券id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/delete-coupons")
	public JsonResult deleteCoupons(Integer coupons_id){
		try {
			this.storeCouponsManager.delete(coupons_id, this.storeCouponsManager.SELF_STORE_ID_KEY);
			return JsonResultUtil.getSuccessJson("优惠券删除成功！");
		} catch (Exception e) {
			this.logger.error("删除失败：", e);
			return JsonResultUtil.getErrorJson("优惠券删除失败！");
		}
	}
	
	/**
	 * 跳转至追加已发放的优惠券数量页面
	 * @return
	 */
	@RequestMapping(value="/append")
	public String append() {
		return "/b2b2c/admin/coupons/self-append-coupons";
	}
	
	/**
	 * 追加已发放的优惠券数量
	 * @param coupons_id 优惠券id
	 * @param coupons_stock 追加的数量
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/append-coupons")
	public JsonResult appendCoupons(Integer coupons_id, Integer coupons_stock){
		try {
			
			//如果追加的优惠券数量为空
			if (coupons_stock == null) {
				return JsonResultUtil.getErrorJson("追加的优惠券数量不能为空！");
			}
			
			//如果追加的优惠券数量小于等于0
			if (coupons_stock.intValue() <= 0) {
				return JsonResultUtil.getErrorJson("追加的优惠券数量不能为0或不能小于0！");
			}
			
			this.storeCouponsManager.append(coupons_id, this.storeCouponsManager.SELF_STORE_ID_KEY, coupons_stock);
			return JsonResultUtil.getSuccessJson("已成功追加发放"+coupons_stock+"张优惠券！");
		} catch (Exception e) {
			this.logger.error("优惠券追加发放失败：", e);
			return JsonResultUtil.getErrorJson("优惠券追加发放失败！");
		}
	}
	
	/**
	 * 跳转至已发放的优惠券列表
	 * @param coupons_id 优惠券id
	 * @return
	 */
	@RequestMapping(value="/list-detail")
	public ModelAndView listDetail(Integer coupons_id){
		ModelAndView view = this.getGridModelAndView();
		view.addObject("coupons_id", coupons_id);
		view.setViewName("/b2b2c/admin/coupons/self-coupons-detail");
		return view;
	}
	
	/**
	 * 获取已发放的优惠券分页列表json
	 * @param coupons_id 店铺优惠券id
	 * @param is_received 是否被领取
	 * @param is_used 是否被使用
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/list-detail-json")
	public GridJsonResult listDetailJson(Integer coupons_id, String is_received, String is_used){
		Map map = new HashMap();
		map.put("is_received", is_received);
		map.put("is_used", is_used);
		
		Page webpage = this.storeCouponsManager.listSendCoupons(this.getPage(), this.getPageSize(), coupons_id, this.storeCouponsManager.SELF_STORE_ID_KEY, map);
		return JsonResultUtil.getGridJson(webpage);
	}
	
	/**
	 * 设置一张已经发放但未被领取的优惠券为无效状态
	 * @param mcoup_id 已发放的优惠券id
	 * @param coupons_id 店铺优惠券id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/invalid-send-coupons")
	public JsonResult invalidSendCoupons(Integer mcoup_id, Integer coupons_id){
		try {
			this.storeCouponsManager.invalidCoupons(mcoup_id, coupons_id, this.storeCouponsManager.SELF_STORE_ID_KEY);
			return JsonResultUtil.getSuccessJson("已成功将此优惠券设置为无效！");
		} catch (Exception e) {
			this.logger.error("设置失效失败：", e);
			return JsonResultUtil.getErrorJson("优惠券设置失效失败！");
		}
	}
	
	/**
	 * 删除一张已经发放但未被领取的优惠券
	 * @param mcoup_id 已发放的优惠券id
	 * @param coupons_id 店铺优惠券id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/delete-send-coupons")
	public JsonResult deleteSendCoupons(Integer mcoup_id, Integer coupons_id){
		try {
			this.storeCouponsManager.deleteCoupons(mcoup_id, coupons_id, this.storeCouponsManager.SELF_STORE_ID_KEY);
			return JsonResultUtil.getSuccessJson("优惠券删除成功！");
		} catch (Exception e) {
			this.logger.error("删除失败：", e);
			return JsonResultUtil.getErrorJson("优惠券删除失败！");
		}
	}
	
	/**
	 * 跳转至自营店线下使用优惠券页面
	 * @return
	 */
	@RequestMapping(value="/offline-use")
	public String offlineUse(){
		return "/b2b2c/admin/coupons/offline-use-coupons";
	}
	
	/**
	 * 对线下使用的优惠券的各项信息进行验证
	 * @param coupons_code 优惠券识别码
	 * @param price 消费金额
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/check-code")
	public JsonResult checkCode(String coupons_code, Double price){
		try {
			//优惠券识别码不能为空
			if (StringUtil.isEmpty(coupons_code)) {
				return JsonResultUtil.getErrorJson("请填写优惠券识别码！");
			}
			MemberCoupons memberCoupons = this.memberCouponsManager.get(coupons_code);
			
			//如果优惠券信息为空
			if (memberCoupons == null) {
				return JsonResultUtil.getErrorJson("此优惠券识别码不存在！");
			}
			
			//如果优惠券所属的店铺id与当前使用的店铺id不相等
			if (!memberCoupons.getStore_id().equals(this.storeCouponsManager.SELF_STORE_ID_KEY)) {
				return JsonResultUtil.getErrorJson("此优惠券不属于自营店铺发放，不可使用！");
			}
			
			//如果优惠券已经失效
			if (memberCoupons.getStatus() == 1 || memberCoupons.getMcoup_end_date() < DateUtil.getDateline()) {
				return JsonResultUtil.getErrorJson("此优惠券已失效！");
			}
			
			//如果优惠券还没有到生效期
			if (memberCoupons.getMcoup_start_date() > DateUtil.getDateline()) {
				return JsonResultUtil.getErrorJson("此优惠券还没有生效！");
			}
			
			//如果优惠券已经被使用了
			if (memberCoupons.getIs_used() == 1) {
				return JsonResultUtil.getErrorJson("此优惠券已经使用过！");
			}
			
			//如果优惠券还没有被领取过
			if (memberCoupons.getIs_received() == 0) {
				return JsonResultUtil.getErrorJson("此优惠券还未被领取，不可使用！");
			}
			
			Store store = this.storeManager.getStore(this.storeCouponsManager.SELF_STORE_ID_KEY);
			
			//如果优惠券的使用地区id和当前使用优惠券的店铺所在地区id不相等
			if (!memberCoupons.getMcoup_province_id().equals(store.getStore_provinceid())) {
				return JsonResultUtil.getErrorJson("此优惠券只可在" + memberCoupons.getMcoup_province_name() + "地区使用！");
			}
			
			//如果优惠券类型不为现金券
			if (!memberCoupons.getMcoup_type().equals(2)) {
				
				//如果消费金额小于优惠券最小订单消费金额
				if (price.doubleValue() < memberCoupons.getMin_order_money()) {
					return JsonResultUtil.getErrorJson("消费金额达到" + memberCoupons.getMin_order_money() + "元才可使用此优惠券！");
				}
			}
			
			//优惠券优惠的金额
			double discount = 0.0;
			//实际消费的金额
			double need = 0.0;
			
			//如果优惠券为满减券或者现金券
			if (memberCoupons.getMcoup_type() == 0 || memberCoupons.getMcoup_type() == 2) {
				
				//如果消费金额小于优惠券面额
				if (price.doubleValue() < memberCoupons.getMcoup_money().doubleValue()) {
					discount = price;
				} else {
					discount = memberCoupons.getMcoup_money();
				}
				need = CurrencyUtil.sub(price, discount);
			} else {
				double percent = memberCoupons.getMcoup_discount() / 100.00;
				need = CurrencyUtil.mul(price, percent);
				discount = CurrencyUtil.sub(price, need);
			}
			
			return JsonResultUtil.getSuccessJson("此优惠券属于会员："+memberCoupons.getMember_name()+"，消费优惠："+discount+"元，实际消费："+need+"元，请先确认客户已付款，然后再点击确定！");
		} catch (Exception e) {
			this.logger.error("验证失败：", e);
			return JsonResultUtil.getErrorJson("验证失败！");
		}
	}
	
	/**
	 * 根据优惠券识别码，线下使用优惠券
	 * @param coupons_code 优惠券识别码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/use-coupons")
	public JsonResult useCoupons(String coupons_code){
		try {
			MemberCoupons memberCoupons = this.memberCouponsManager.get(coupons_code);
			
			this.memberCouponsManager.useCoupons(memberCoupons.getMcoup_id(), memberCoupons.getCoupons_id(), memberCoupons.getMember_id(), null, null);
			return JsonResultUtil.getSuccessJson("优惠券使用成功！");
		} catch (Exception e) {
			this.logger.error("使用失败：", e);
			return JsonResultUtil.getErrorJson("使用失败！");
		}
	}
	
	@RequestMapping(value="/send-for-member")
	public ModelAndView sendForMember(Integer coupons_id){
		ModelAndView view = this.getGridModelAndView();
		view.addObject("coupons_id", coupons_id);
		view.addObject("memberLvList", this.memberLvManager.list());
		view.setViewName("/b2b2c/admin/coupons/self-coupons-allot");
		return view;
	}
	
	/**
	 * 验证按会员等级发放优惠券的各项参数
	 * @param lv_id 会员等级id
	 * @param coupons_id 会员优惠券id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/check-member-lv")
	public JsonResult checkMemberLv(Integer lv_id, Integer coupons_id){
		try {
			//获取自营店铺信息
			Store store = this.storeManager.getStore(this.storeCouponsManager.SELF_STORE_ID_KEY);
			
			//如果等级id或者店铺优惠券id为空
			if (lv_id == null || coupons_id == null) {
				return JsonResultUtil.getErrorJson("请求参数错误，请重试！");
			}
			
			//如果等级id等于0
			if (lv_id.intValue() == 0) {
				return JsonResultUtil.getErrorJson("请选择用户等级！");
			}
			
			//根据会员等级id获取除当前店铺会员的所有会员数量
			int membernum = this.storeCouponsManager.getMemberLvNum(lv_id, store.getMember_id());
			//如果会员数量等于0
			if (membernum == 0) {
				return JsonResultUtil.getErrorJson("您选择的等级暂时还没有会员达到，不能发放优惠券！");
			}
			
			//根据店铺会员优惠券获取会员优惠券有效并且没有被领取的优惠券
			int couponsnum = this.memberCouponsManager.getMemberCouponsNum(coupons_id);
			//如果优惠券的数量等于0
			if (couponsnum == 0) {
				return JsonResultUtil.getErrorJson("此优惠券可发放的数量已经为零，不能再进行发放！");
			}
			
			String json = "";
			
			//如果会员数量大于优惠券数量
			if (membernum > couponsnum) {
				json = "您选择的等级共有" + membernum + "个会员，可发放的优惠券数量为" + couponsnum + "个，"
						+ "会员数量多于优惠券数量，系统将会按照会员等级积分从高到低进行发放，会有" + (membernum-couponsnum) + "个会员无法获取发放的优惠券，确定要发放吗？";
			} else {
				json = "您选择的等级共有" + membernum + "个会员，可发放的优惠券数量为" + couponsnum + "个，系统将会按照会员等级积分从高到低进行发放，确定要发放吗？";
			}
			
			return JsonResultUtil.getSuccessJson(json);
		} catch (Exception e) {
			this.logger.error("验证失败", e);
			return JsonResultUtil.getErrorJson("发放失败，请重试！");
		}
	}
	
	/**
	 * 按会员等级发放优惠券
	 * @param lv_id 会员等级id
	 * @param coupons_id 店铺优惠券id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/send-member-lv")
	public JsonResult sendMemberLv(Integer lv_id, Integer coupons_id){
		try {
			//获取自营店铺信息
			Store store = this.storeManager.getStore(this.storeCouponsManager.SELF_STORE_ID_KEY);
			
			//根据店铺优惠券id获取店铺优惠券信息
			StoreCoupons storeCoupons = this.storeCouponsManager.getCoupons(coupons_id);
			
			//判断店铺优惠券是否属于当前登录的店铺，如果不属于，不允许像用户发放
			if (!store.getStore_id().equals(storeCoupons.getStore_id())) {
				return JsonResultUtil.getErrorJson("发放的优惠券不属于当前登录的店铺，请检查优惠券信息是否正确！");
			}
			
			//获取店铺已经发放并且有效，而且未被领取的优惠券集合
			List<MemberCoupons> couponsList = this.memberCouponsManager.getCouponsList(coupons_id, store.getStore_id());
			
			//根据会员等级id获取除当前登录的会员之外的所有会员集合
			List<Member> memberList = this.memberManager.memberListByLv(lv_id, store.getMember_id());
			
			int send_num = 0;
			
			//如果优惠券的数量大于或等于会员的数量，发放优惠券的数量以会员数量为准，反之则以优惠券数量为准进行发放
			if (couponsList.size() >= memberList.size()) {
				send_num = memberList.size();
				
				//循环发放优惠券
				for (int i = 0; i < send_num; i++) {
					Member member = memberList.get(i);
					this.memberCouponsManager.receive(coupons_id, store.getStore_id(), member.getMember_id(), 2);
				}
			} else {
				send_num = couponsList.size();
				
				//循环发放优惠券
				for (int i = 0; i < send_num; i++) {
					Member member = memberList.get(i);
					this.memberCouponsManager.receive(coupons_id, store.getStore_id(), member.getMember_id(), 2);
				}
			}
			
			return JsonResultUtil.getSuccessJson("已成功发放"+send_num+"张优惠券！");
		} catch (Exception e) {
			this.logger.error("发放成功", e);
			return JsonResultUtil.getErrorJson("发放失败，请重试！");
		}
	}
	
	/**
	 * 验证按会员所属地区发放优惠券的各项参数
	 * @param coupons_id 店铺优惠券id
	 * @param province_id 会员所在省份id
	 * @param city_id 会员所在城市id
	 * @param region_id 会员所在地区(县)id 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/check-member-region")
	public JsonResult checkMemberRegion(Integer coupons_id, Integer province_id, Integer city_id, Integer region_id){
		try {
			
			//获取自营店铺信息
			Store store = this.storeManager.getStore(this.storeCouponsManager.SELF_STORE_ID_KEY);
			
			//校验参数是否为空
			if (coupons_id == null || province_id == null || city_id == null || region_id == null) {
				return JsonResultUtil.getErrorJson("请求参数错误，请重试！");
			}
			
			//会员所在地区的省份id、城市id和地区（县）id必须有一项不等于0
			if (province_id.intValue() == 0 && city_id.intValue() == 0 && region_id.intValue() == 0) {
				return JsonResultUtil.getErrorJson("请选择会员所在地区！");
			}
			
			//根据会员所在地区id获取，除当前店铺会员之外的所有会员数量
			int membernum = this.storeCouponsManager.getMemberRegionNum(province_id, city_id, region_id, store.getMember_id());
			//如果会员数量等于0
			if (membernum == 0) {
				return JsonResultUtil.getErrorJson("没有会员属于你选择的地区，无法发放优惠券！");
			}
			
			//根据店铺会员优惠券获取会员优惠券有效并且没有被领取的优惠券
			int couponsnum = this.memberCouponsManager.getMemberCouponsNum(coupons_id);
			//如果优惠券的数量等于0
			if (couponsnum == 0) {
				return JsonResultUtil.getErrorJson("此优惠券可发放的数量已经为零，不能再进行发放！");
			}
			
			String json = "";
			
			//如果会员数量大于优惠券数量
			if (membernum > couponsnum) {
				json = "您选择的地区共有" + membernum + "个会员，可发放的优惠券数量为" + couponsnum + "个，"
						+ "会员数量多于优惠券数量，系统将会按照会员所在省份编号依次进行发放，会有" + (membernum-couponsnum) + "个会员无法获取发放的优惠券，确定要发放吗？";
			} else {
				json = "您选择的地区共有" + membernum + "个会员，可发放的优惠券数量为" + couponsnum + "个，系统将会按照会员所在省份编号依次进行发放，确定要发放吗？";
			}
			
			return JsonResultUtil.getSuccessJson(json);
		} catch (Exception e) {
			this.logger.error("验证失败", e);
			return JsonResultUtil.getErrorJson("发放失败，请重试！");
		}
	}
	
	/**
	 * 按会员所在地区发放优惠券
	 * @param coupons_id 店铺优惠券id
	 * @param province_id 会员所在省份id
	 * @param city_id 会员所在城市id
	 * @param region_id 会员所在地区id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/send-member-region")
	public JsonResult sendMemberRegion(Integer coupons_id, Integer province_id, Integer city_id, Integer region_id){
		try {
			//获取自营店铺信息
			Store store = this.storeManager.getStore(this.storeCouponsManager.SELF_STORE_ID_KEY);
			
			//根据店铺优惠券id获取店铺优惠券信息
			StoreCoupons storeCoupons = this.storeCouponsManager.getCoupons(coupons_id);
			
			//判断店铺优惠券是否属于当前登录的店铺，如果不属于，不允许像用户发放
			if (!store.getStore_id().equals(storeCoupons.getStore_id())) {
				return JsonResultUtil.getErrorJson("发放的优惠券不属于当前登录的店铺，请检查优惠券信息是否正确！");
			}
			
			//获取店铺已经发放并且有效，而且未被领取的优惠券集合
			List<MemberCoupons> couponsList = this.memberCouponsManager.getCouponsList(coupons_id, store.getStore_id());
			
			//根据会员所在区域的id(包括省份id、城市id，地区(县)id)获取除当前登录的会员之外的所有回去集合
			List<Member> memberList = this.memberManager.memberListByRegion(province_id, city_id, region_id, store.getMember_id());
			
			int send_num = 0;
			
			//如果优惠券的数量大于或等于会员的数量，发放优惠券的数量以会员数量为准，反之则以优惠券数量为准进行发放
			if (couponsList.size() >= memberList.size()) {
				send_num = memberList.size();
				
				//循环发放优惠券
				for (int i = 0; i < send_num; i++) {
					Member member = memberList.get(i);
					this.memberCouponsManager.receive(coupons_id, store.getStore_id(), member.getMember_id(), 2);
				}
			} else {
				send_num = couponsList.size();
				
				//循环发放优惠券
				for (int i = 0; i < send_num; i++) {
					Member member = memberList.get(i);
					this.memberCouponsManager.receive(coupons_id, store.getStore_id(), member.getMember_id(), 2);
				}
			}
			
			return JsonResultUtil.getSuccessJson("已成功发放"+send_num+"张优惠券！");
		} catch (Exception e) {
			this.logger.error("发放成功", e);
			return JsonResultUtil.getErrorJson("发放失败，请重试！");
		}
	}
	
	/**
	 * 按会员用户名关键字搜索会员
	 * @param keyword 用户名关键字
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/search-member")
	public JsonResult searchMember(String keyword){
		try {
			
			//获取自营店铺信息
			Store store = this.storeManager.getStore(this.storeCouponsManager.SELF_STORE_ID_KEY);
			
			//如果搜索关键字为空
			if (StringUtil.isEmpty(keyword)) {
				return JsonResultUtil.getErrorJson("请输入搜索关键字！");
			}
			
			List<Member> memberList = this.memberManager.memberListByKeyword(keyword, store.getMember_id());
			
			//如果会员集合为空
			if (memberList.size() == 0) {
				return JsonResultUtil.getErrorJson("未搜索到相关会员！");
			} else {
				return JsonResultUtil.getObjectJson(memberList);
			}
			
		} catch (Exception e) {
			this.logger.error("搜索失败！", e);
			return JsonResultUtil.getErrorJson("搜索失败！");
		}
	}
	
	/**
	 * 验证按会员名称发放优惠券的各项参数
	 * @param coupons_id 店铺优惠券id
	 * @param memberids 会员id组
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/check-member-uname")
	public JsonResult checkMemberUname(Integer coupons_id, Integer[] memberids){
		try {
			//如果搜索关键字为空
			if (coupons_id == null || memberids == null) {
				return JsonResultUtil.getErrorJson("请求参数错误，请重试！");
			}
			
			//如果会员id组长度为0
			if (memberids.length == 0) {
				return JsonResultUtil.getErrorJson("请添加会员到发放列表！");
			}
			
			//根据店铺会员优惠券获取会员优惠券有效并且没有被领取的优惠券
			int couponsnum = this.memberCouponsManager.getMemberCouponsNum(coupons_id);
			//如果优惠券的数量等于0
			if (couponsnum == 0) {
				return JsonResultUtil.getErrorJson("此优惠券可发放的数量已经为零，不能再进行发放！");
			}
			
			String json = "";
			int membernum = memberids.length;
			
			//如果会员数量大于优惠券数量
			if (membernum > couponsnum) {
				json = "您选择的会员数量为" + membernum + "个，可发放的优惠券数量为" + couponsnum + "个，"
						+ "会员数量多于优惠券数量，系统将会按照您选择的会员依次进行发放，会有" + (membernum-couponsnum) + "个会员无法获取发放的优惠券，确定要发放吗？";
			} else {
				json = "您选择的会员数量为" + membernum + "个，可发放的优惠券数量为" + couponsnum + "个，系统将会按照您选择的会员依次进行发放，确定要发放吗？";
			}
			
			return JsonResultUtil.getSuccessJson(json);
		} catch (Exception e) {
			this.logger.error("验证失败！", e);
			return JsonResultUtil.getErrorJson("发放失败，请重试！");
		}
	}
	
	/**
	 * 按会员用户名发放优惠券
	 * @param coupons_id 店铺优惠券id
	 * @param memberids 会员id组
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/send-member-uname")
	public JsonResult sendMemberUname(Integer coupons_id, Integer[] memberids){
		try {
			//获取自营店铺信息
			Store store = this.storeManager.getStore(this.storeCouponsManager.SELF_STORE_ID_KEY);
			
			//根据店铺优惠券id获取店铺优惠券信息
			StoreCoupons storeCoupons = this.storeCouponsManager.getCoupons(coupons_id);
			
			//判断店铺优惠券是否属于当前登录的店铺，如果不属于，不允许像用户发放
			if (!store.getStore_id().equals(storeCoupons.getStore_id())) {
				return JsonResultUtil.getErrorJson("发放的优惠券不属于当前登录的店铺，请检查优惠券信息是否正确！");
			}
			
			//获取店铺已经发放并且有效，而且未被领取的优惠券集合
			List<MemberCoupons> couponsList = this.memberCouponsManager.getCouponsList(coupons_id, store.getStore_id());
			
			int send_num = 0;
			
			//如果优惠券的数量大于或等于会员的数量，发放优惠券的数量以会员数量为准，反之则以优惠券数量为准进行发放
			if (couponsList.size() >= memberids.length) {
				send_num = memberids.length;
				
				//循环发放优惠券
				for (int i = 0; i < send_num; i++) {
					this.memberCouponsManager.receive(coupons_id, store.getStore_id(), memberids[i], 2);
				}
			} else {
				send_num = couponsList.size();
				
				//循环发放优惠券
				for (int i = 0; i < send_num; i++) {
					this.memberCouponsManager.receive(coupons_id, store.getStore_id(), memberids[i], 2);
				}
			}
			
			return JsonResultUtil.getSuccessJson("已成功发放"+send_num+"张优惠券！");
		} catch (Exception e) {
			this.logger.error("发放成功", e);
			return JsonResultUtil.getErrorJson("发放失败，请重试！");
		}
	}
}
