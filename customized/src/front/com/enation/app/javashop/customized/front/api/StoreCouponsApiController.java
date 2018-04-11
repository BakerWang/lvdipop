package com.enation.app.javashop.customized.front.api;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.b2b2c.core.store.model.StoreCoupons;
import com.enation.app.base.core.model.Member;
import com.enation.app.base.core.service.IMemberManager;
import com.enation.app.base.core.service.IRegionsManager;
import com.enation.app.javashop.customized.core.service.IMemberCouponsManager;
import com.enation.app.javashop.customized.core.service.IStoreCouponsManager;
import com.enation.app.shop.component.bonus.model.MemberCoupons;
import com.enation.framework.action.JsonResult;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;

/**
 * 店铺优惠券管理类
 * @author DMRain
 * @date 2016-9-22
 * @since v61
 * @version 1.0
 */
@Controller
@Scope("prototype")
@RequestMapping("/api/customized/coupons")
public class StoreCouponsApiController {

	protected final Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	private IStoreCouponsManager storeCouponsManager;
	
	@Autowired
	private IMemberCouponsManager memberCouponsManager;
	
	@Autowired
	private IRegionsManager regionsManager;
	
	@Autowired
	private IStoreMemberManager storeMemberManager;
	
	@Autowired
	private IMemberManager memberManager;
	
	/**
	 * 保存新增店铺优惠券
	 * @param storeCoupons 店铺优惠券信息
	 * @param startDate 有效期开始日期
	 * @param endDate 有效期结束日期
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/save-add", produces=MediaType.APPLICATION_JSON_VALUE)
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
					if (storeCoupons.getCoupons_money().equals(0)) {
						return JsonResultUtil.getErrorJson("优惠券面额不能为0！");
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
				if (storeCoupons.getMin_order_money().equals(0)) {
					return JsonResultUtil.getErrorJson("买家需消费的金额不能为0！");
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
				return JsonResultUtil.getErrorJson("发行量不能为0！");
			}
			
			//如果优惠券的限领数量为空
			if (storeCoupons.getLimit_num() == null) {
				return JsonResultUtil.getErrorJson("限领数量不能为空！");
			}
			
			//如果优惠券的限领数量为0
			if (storeCoupons.getLimit_num().equals(0)) {
				return JsonResultUtil.getErrorJson("限领数量不能为0");
			}
			
			//如果优惠券的限领数量大于发行量
			if (storeCoupons.getLimit_num().intValue() > storeCoupons.getCoupons_stock().intValue()) {
				return JsonResultUtil.getErrorJson("限领数量不能大于发行量！");
			}
			
			StoreMember storeMember = this.storeMemberManager.getStoreMember();
			storeCoupons.setStore_id(storeMember.getStore_id());
			
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
			this.logger.error("保存失败：", e);
			return JsonResultUtil.getErrorJson("保存失败！");
		}
	}
	
	/**
	 * 保存修改的优惠券信息
	 * @param storeCoupons 优惠券信息
	 * @param startDate 有效期开始日期
	 * @param endDate 有效期结束日期
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/save-edit", produces=MediaType.APPLICATION_JSON_VALUE)
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
				//如果优惠券类型为满减券
				if (storeCoupons.getCoupons_type().equals(0)) {
					//如果优惠券面额等于空
					if (storeCoupons.getCoupons_money() == null) {
						return JsonResultUtil.getErrorJson("优惠券面额不能为空！");
					}
					
					//如果优惠券面额为0
					if (storeCoupons.getCoupons_money().equals(0)) {
						return JsonResultUtil.getErrorJson("优惠券面额不能为0！");
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
				
				//如果优惠券类型为现金券
				if (storeCoupons.getCoupons_type().equals(2)) {
					//如果优惠券面额等于空
					if (storeCoupons.getCoupons_money() == null) {
						return JsonResultUtil.getErrorJson("优惠券面额不能为空！");
					}
					
					//如果优惠券面额为0
					if (storeCoupons.getCoupons_money().equals(0)) {
						return JsonResultUtil.getErrorJson("优惠券面额不能为0！");
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
				if (storeCoupons.getMin_order_money().equals(0)) {
					return JsonResultUtil.getErrorJson("买家需消费的金额不能为0！");
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
				return JsonResultUtil.getErrorJson("优惠券生效日期不能大于失效日期！");
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
				return JsonResultUtil.getErrorJson("发行量不能为0！");
			}
			
			//如果优惠券的限领数量为空
			if (storeCoupons.getLimit_num() == null) {
				return JsonResultUtil.getErrorJson("限领数量不能为空！");
			}
			
			//如果优惠券的限领数量为0
			if (storeCoupons.getLimit_num().equals(0)) {
				return JsonResultUtil.getErrorJson("限领数量不能为0");
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
			return JsonResultUtil.getSuccessJson("修改优惠券成功！");
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("保存失败：", e);
			return JsonResultUtil.getErrorJson("保存失败！");
		}
	}
	
	/**
	 * 设置优惠券为无效状态
	 * @param coupons_id 店铺优惠券id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/invalid-coupons", produces=MediaType.APPLICATION_JSON_VALUE)
	public JsonResult invalidCoupons(Integer coupons_id){
		try {
			//获取当前登录的店铺会员信息
			StoreMember member = this.storeMemberManager.getStoreMember();
			//获取当前登录的店铺id
			Integer store_id = member.getStore_id();
			
			this.storeCouponsManager.invalid(coupons_id, store_id);
			return JsonResultUtil.getSuccessJson("已成功将优惠券设置为失效！");
		} catch (Exception e) {
			this.logger.error("设置失效失败：", e);
			return JsonResultUtil.getErrorJson("优惠券设置失效失败！");
		}
	}
	
	/**
	 * 删除店铺优惠券
	 * @param coupons_id 店铺优惠券id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/delete-coupons", produces=MediaType.APPLICATION_JSON_VALUE)
	public JsonResult deleteCoupons(Integer coupons_id){
		try {
			//获取当前登录的店铺会员信息
			StoreMember member = this.storeMemberManager.getStoreMember();
			//获取当前登录的店铺id
			Integer store_id = member.getStore_id();
			
			this.storeCouponsManager.delete(coupons_id, store_id);
			return JsonResultUtil.getSuccessJson("删除成功！");
		} catch (Exception e) {
			this.logger.error("删除失败：", e);
			return JsonResultUtil.getErrorJson("优惠券删除失败！");
		}
	}
	
	/**
	 * 发放店铺优惠券
	 * @param coupons_id 店铺优惠券id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/send-coupons", produces=MediaType.APPLICATION_JSON_VALUE)
	public JsonResult sendCoupons(Integer coupons_id){
		try {
			//获取当前登录的店铺会员信息
			StoreMember member = this.storeMemberManager.getStoreMember();
			//获取当前登录的店铺id
			Integer store_id = member.getStore_id();
			
			this.storeCouponsManager.send(coupons_id, store_id);
			return JsonResultUtil.getSuccessJson("已成功将优惠券发放！");
		} catch (Exception e) {
			this.logger.error("优惠券发放失败：", e);
			return JsonResultUtil.getErrorJson("优惠券发放失败！");
		}
	}
	
	/**
	 * 追加已经发放的优惠券数量
	 * @param coupons_id 店铺优惠券id
	 * @param coupons_stock 追加发放的数量
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/append-coupons", produces=MediaType.APPLICATION_JSON_VALUE)
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
			
			//获取当前登录的店铺会员信息
			StoreMember member = this.storeMemberManager.getStoreMember();
			//获取当前登录的店铺id
			Integer store_id = member.getStore_id();
			
			this.storeCouponsManager.append(coupons_id, store_id, coupons_stock);
			return JsonResultUtil.getSuccessJson("已成功追加发放"+coupons_stock+"张优惠券！");
		} catch (Exception e) {
			this.logger.error("优惠券追加发放失败：", e);
			return JsonResultUtil.getErrorJson("优惠券追加发放失败！");
		}
	}
	
	/**
	 * 使一张已经发放的还未被领取的优惠券失效
	 * @param mcoup_id 已发放的优惠券id
	 * @param coupons_id 店铺优惠券id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/invalid-send-coupons", produces=MediaType.APPLICATION_JSON_VALUE)
	public JsonResult invalidSendCoupons(Integer mcoup_id, Integer coupons_id){
		try {
			//获取当前登录的店铺会员信息
			StoreMember member = this.storeMemberManager.getStoreMember();
			//获取当前登录的店铺id
			Integer store_id = member.getStore_id();
			
			this.storeCouponsManager.invalidCoupons(mcoup_id, coupons_id, store_id);
			return JsonResultUtil.getSuccessJson("已成功将优惠券设置为失效！");
		} catch (Exception e) {
			this.logger.error("设置失效失败：", e);
			return JsonResultUtil.getErrorJson("优惠券设置失效失败！");
		}
	}
	
	/**
	 * 删除一张已经发放的还未被领取的优惠券
	 * @param mcoup_id 已发放的优惠券id
	 * @param coupons_id 店铺优惠券id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/delete-send-coupons", produces=MediaType.APPLICATION_JSON_VALUE)
	public JsonResult deleteSendCoupons(Integer mcoup_id, Integer coupons_id){
		try {
			//获取当前登录的店铺会员信息
			StoreMember member = this.storeMemberManager.getStoreMember();
			//获取当前登录的店铺id
			Integer store_id = member.getStore_id();
			
			this.storeCouponsManager.deleteCoupons(mcoup_id, coupons_id, store_id);
			return JsonResultUtil.getSuccessJson("删除成功！");
		} catch (Exception e) {
			this.logger.error("删除失败：", e);
			return JsonResultUtil.getErrorJson("优惠券删除失败！");
		}
	}
	
	/**
	 * 验证按会员等级发放优惠券的各项参数
	 * @param lv_id 会员等级id
	 * @param coupons_id 会员优惠券id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/check-member-lv", produces=MediaType.APPLICATION_JSON_VALUE)
	public JsonResult checkMemberLv(Integer lv_id, Integer coupons_id){
		try {
			//获取当前登录的店铺id
			Integer member_id = this.storeMemberManager.getStoreMember().getMember_id();
			
			//如果等级id或者店铺优惠券id为空
			if (lv_id == null || coupons_id == null) {
				return JsonResultUtil.getErrorJson("请求参数错误，请重试！");
			}
			
			//如果等级id等于0
			if (lv_id.intValue() == 0) {
				return JsonResultUtil.getErrorJson("请选择用户等级！");
			}
			
			//根据会员等级id获取除当前店铺会员的所有会员数量
			int membernum = this.storeCouponsManager.getMemberLvNum(lv_id, member_id);
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
	@RequestMapping(value="/send-member-lv", produces=MediaType.APPLICATION_JSON_VALUE)
	public JsonResult sendMemberLv(Integer lv_id, Integer coupons_id){
		try {
			//获取当前登录的店铺会员信息
			StoreMember storeMember = this.storeMemberManager.getStoreMember();
			
			//根据店铺优惠券id获取店铺优惠券信息
			StoreCoupons storeCoupons = this.storeCouponsManager.getCoupons(coupons_id);
			
			//判断店铺优惠券是否属于当前登录的店铺，如果不属于，不允许像用户发放
			if (!storeMember.getStore_id().equals(storeCoupons.getStore_id())) {
				return JsonResultUtil.getErrorJson("发放的优惠券不属于当前登录的店铺，请检查优惠券信息是否正确！");
			}
			
			//获取店铺已经发放并且有效，而且未被领取的优惠券集合
			List<MemberCoupons> couponsList = this.memberCouponsManager.getCouponsList(coupons_id, storeMember.getStore_id());
			
			//根据会员等级id获取除当前登录的会员之外的所有会员集合
			List<Member> memberList = this.memberManager.memberListByLv(lv_id, storeMember.getMember_id());
			
			int send_num = 0;
			
			//如果优惠券的数量大于或等于会员的数量，发放优惠券的数量以会员数量为准，反之则以优惠券数量为准进行发放
			if (couponsList.size() >= memberList.size()) {
				send_num = memberList.size();
				
				//循环发放优惠券
				for (int i = 0; i < send_num; i++) {
					Member member = memberList.get(i);
					this.memberCouponsManager.receive(coupons_id, storeMember.getStore_id(), member.getMember_id(), 2);
				}
			} else {
				send_num = couponsList.size();
				
				//循环发放优惠券
				for (int i = 0; i < send_num; i++) {
					Member member = memberList.get(i);
					this.memberCouponsManager.receive(coupons_id, storeMember.getStore_id(), member.getMember_id(), 2);
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
	@RequestMapping(value="/check-member-region", produces=MediaType.APPLICATION_JSON_VALUE)
	public JsonResult checkMemberRegion(Integer coupons_id, Integer province_id, Integer city_id, Integer region_id){
		try {
			
			//获取当前登录的店铺id
			Integer member_id = this.storeMemberManager.getStoreMember().getMember_id();
			
			//校验参数是否为空
			if (coupons_id == null || province_id == null || city_id == null || region_id == null) {
				return JsonResultUtil.getErrorJson("请求参数错误，请重试！");
			}
			
			//会员所在地区的省份id、城市id和地区（县）id必须有一项不等于0
			if (province_id.intValue() == 0 && city_id.intValue() == 0 && region_id.intValue() == 0) {
				return JsonResultUtil.getErrorJson("请选择会员所在地区！");
			}
			
			//根据会员所在地区id获取，除当前店铺会员之外的所有会员数量
			int membernum = this.storeCouponsManager.getMemberRegionNum(province_id, city_id, region_id, member_id);
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
	@RequestMapping(value="/send-member-region", produces=MediaType.APPLICATION_JSON_VALUE)
	public JsonResult sendMemberRegion(Integer coupons_id, Integer province_id, Integer city_id, Integer region_id){
		try {
			//获取当前登录的店铺会员信息
			StoreMember storeMember = this.storeMemberManager.getStoreMember();
			
			//根据店铺优惠券id获取店铺优惠券信息
			StoreCoupons storeCoupons = this.storeCouponsManager.getCoupons(coupons_id);
			
			//判断店铺优惠券是否属于当前登录的店铺，如果不属于，不允许像用户发放
			if (!storeMember.getStore_id().equals(storeCoupons.getStore_id())) {
				return JsonResultUtil.getErrorJson("发放的优惠券不属于当前登录的店铺，请检查优惠券信息是否正确！");
			}
			
			//获取店铺已经发放并且有效，而且未被领取的优惠券集合
			List<MemberCoupons> couponsList = this.memberCouponsManager.getCouponsList(coupons_id, storeMember.getStore_id());
			
			//根据会员所在区域的id(包括省份id、城市id，地区(县)id)获取除当前登录的会员之外的所有回去集合
			List<Member> memberList = this.memberManager.memberListByRegion(province_id, city_id, region_id, storeMember.getMember_id());
			
			int send_num = 0;
			
			//如果优惠券的数量大于或等于会员的数量，发放优惠券的数量以会员数量为准，反之则以优惠券数量为准进行发放
			if (couponsList.size() >= memberList.size()) {
				send_num = memberList.size();
				
				//循环发放优惠券
				for (int i = 0; i < send_num; i++) {
					Member member = memberList.get(i);
					this.memberCouponsManager.receive(coupons_id, storeMember.getStore_id(), member.getMember_id(), 2);
				}
			} else {
				send_num = couponsList.size();
				
				//循环发放优惠券
				for (int i = 0; i < send_num; i++) {
					Member member = memberList.get(i);
					this.memberCouponsManager.receive(coupons_id, storeMember.getStore_id(), member.getMember_id(), 2);
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
	@RequestMapping(value="/search-member", produces=MediaType.APPLICATION_JSON_VALUE)
	public JsonResult searchMember(String keyword){
		try {
			
			//获取当前登录的店铺id
			Integer member_id = this.storeMemberManager.getStoreMember().getMember_id();
			
			//如果搜索关键字为空
			if (StringUtil.isEmpty(keyword)) {
				return JsonResultUtil.getErrorJson("请输入搜索关键字！");
			}
			
			List<Member> memberList = this.memberManager.memberListByKeyword(keyword, member_id);
			
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
	@RequestMapping(value="/check-member-uname", produces=MediaType.APPLICATION_JSON_VALUE)
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
	@RequestMapping(value="/send-member-uname", produces=MediaType.APPLICATION_JSON_VALUE)
	public JsonResult sendMemberUname(Integer coupons_id, Integer[] memberids){
		try {
			//获取当前登录的店铺会员信息
			StoreMember storeMember = this.storeMemberManager.getStoreMember();
			
			//根据店铺优惠券id获取店铺优惠券信息
			StoreCoupons storeCoupons = this.storeCouponsManager.getCoupons(coupons_id);
			
			//判断店铺优惠券是否属于当前登录的店铺，如果不属于，不允许像用户发放
			if (!storeMember.getStore_id().equals(storeCoupons.getStore_id())) {
				return JsonResultUtil.getErrorJson("发放的优惠券不属于当前登录的店铺，请检查优惠券信息是否正确！");
			}
			
			//获取店铺已经发放并且有效，而且未被领取的优惠券集合
			List<MemberCoupons> couponsList = this.memberCouponsManager.getCouponsList(coupons_id, storeMember.getStore_id());
			
			int send_num = 0;
			
			//如果优惠券的数量大于或等于会员的数量，发放优惠券的数量以会员数量为准，反之则以优惠券数量为准进行发放
			if (couponsList.size() >= memberids.length) {
				send_num = memberids.length;
				
				//循环发放优惠券
				for (int i = 0; i < send_num; i++) {
					this.memberCouponsManager.receive(coupons_id, storeMember.getStore_id(), memberids[i], 2);
				}
			} else {
				send_num = couponsList.size();
				
				//循环发放优惠券
				for (int i = 0; i < send_num; i++) {
					this.memberCouponsManager.receive(coupons_id, storeMember.getStore_id(), memberids[i], 2);
				}
			}
			
			return JsonResultUtil.getSuccessJson("已成功发放"+send_num+"张优惠券！");
		} catch (Exception e) {
			this.logger.error("发放成功", e);
			return JsonResultUtil.getErrorJson("发放失败，请重试！");
		}
	}
}
