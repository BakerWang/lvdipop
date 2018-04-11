package com.enation.app.javashop.customized.front.api;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.b2b2c.component.plugin.order.StoreCartPluginBundle;
import com.enation.app.b2b2c.core.goods.service.StoreCartContainer;
import com.enation.app.b2b2c.core.goods.service.StoreCartKeyEnum;
import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.b2b2c.core.store.model.Store;
import com.enation.app.b2b2c.core.store.model.StoreCoupons;
import com.enation.app.b2b2c.core.store.service.IStoreManager;
import com.enation.app.b2b2c.core.store.service.activity.IStoreActivityGiftManager;
import com.enation.app.javashop.customized.core.service.CouponsSession;
import com.enation.app.javashop.customized.core.service.IMemberCouponsManager;
import com.enation.app.javashop.customized.core.service.IStoreCouponsManager;
import com.enation.app.shop.component.bonus.model.MemberCoupons;
import com.enation.app.shop.core.order.model.support.OrderPrice;
import com.enation.app.shop.core.order.service.ICartManager;
import com.enation.app.shop.core.other.model.ActivityDetail;
import com.enation.app.shop.core.other.service.IActivityDetailManager;
import com.enation.framework.action.JsonResult;
import com.enation.framework.util.CurrencyUtil;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;

/**
 * 会员优惠券API管理类
 * @author DMRain
 * @date 2016-10-10
 * @since v61
 * @version 1.0
 */
@Controller
@Scope("prototype")
@RequestMapping("/api/customized/member-coupons")
public class MemberCouponsApiController {

	protected final Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	private IMemberCouponsManager memberCouponsManager;
	
	@Autowired
	private IStoreCouponsManager storeCouponsManager;
	
	@Autowired
	private IStoreMemberManager storeMemberManager;
	
	@Autowired
	private IStoreManager storeManager;
	
	@Autowired
	private ICartManager cartManager;
	
	@Autowired
	private StoreCartPluginBundle storeCartPluginBundle;
	
	@Autowired
	private IActivityDetailManager activityDetailManager;
	
	@Autowired
	private IStoreActivityGiftManager storeActivityGiftManager;
	
	/**
	 * 领取店铺优惠券
	 * @param store_id 店铺id
	 * @param coupons_id 店铺优惠券id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/receive-coupons", produces=MediaType.APPLICATION_JSON_VALUE)
	public JsonResult receiveCoupons(Integer store_id, Integer coupons_id){
		try {
			StoreMember member = this.storeMemberManager.getStoreMember();
			
			//判断当前会员是否登陆，如果没有登陆不可领取优惠券
			if (member == null) {
				return JsonResultUtil.getErrorJson("请登陆后再领取优惠券！");
			}
			
			StoreCoupons storeCoupons = this.storeCouponsManager.get(coupons_id, store_id);
			
			if (storeCoupons.getEnd_date() < DateUtil.getDateline() || storeCoupons.getStatus().equals(1)) {
				return JsonResultUtil.getErrorJson("此优惠券已经失效，无法领取！");
			}
			
			int limitNum = storeCoupons.getLimit_num();
			int stock = storeCoupons.getCoupons_stock();
			int receivedNum = storeCoupons.getReceived_num();
			
			if (receivedNum >= stock) {
				return JsonResultUtil.getErrorJson("此优惠券已被领完！");
			}
			
			int memberNum = this.memberCouponsManager.getMemberCouponsNum(coupons_id, store_id, member.getMember_id());
			if (memberNum >= limitNum) {
				return JsonResultUtil.getErrorJson("此优惠券限领"+limitNum+"个，不可再次领取！");
			}
			
			if (member.getStore_id().equals(store_id)) {
				return JsonResultUtil.getErrorJson("不可领取自己店铺的优惠券！");
			}
			
			this.memberCouponsManager.receive(coupons_id, store_id, member.getMember_id(), 0);
			return JsonResultUtil.getSuccessJson("领取成功！");
		} catch (Exception e) {
			this.logger.error("领取失败：", e);
			return JsonResultUtil.getErrorJson("领取失败！");
		}
	}
	
	/**
	 * 对线下使用的优惠券的各项信息进行验证
	 * @param coupons_code 优惠券识别码
	 * @param price 消费金额
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/check-code", produces=MediaType.APPLICATION_JSON_VALUE)
	public JsonResult checkCode(String coupons_code, Double price){
		try {
			
			//优惠券识别码不能为空
			if (StringUtil.isEmpty(coupons_code)) {
				return JsonResultUtil.getErrorJson("请填写优惠券识别码！");
			}
			StoreMember member = this.storeMemberManager.getStoreMember();
			MemberCoupons memberCoupons = this.memberCouponsManager.get(coupons_code);
			
			//如果优惠券信息为空
			if (memberCoupons == null) {
				return JsonResultUtil.getErrorJson("此优惠券识别码不存在！");
			}
			
			//如果优惠券所属的店铺id与当前使用的店铺id不相等
			if (!memberCoupons.getStore_id().equals(member.getStore_id())) {
				return JsonResultUtil.getErrorJson("此优惠券不属于本店铺发放，不可使用！");
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
			
			Store store = this.storeManager.getStore(member.getStore_id());
			
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
	@RequestMapping(value="/use-coupons", produces=MediaType.APPLICATION_JSON_VALUE)
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
	
	/**
	 * 改变店铺的配送方式以及红包<br>
	 * 调用此api时必须已经访问过购物车列表<br>
	 * 
	 * @return 含有价格信息的json串
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@RequestMapping(value = "change-args-type")
	public JsonResult changeArgsType(Integer region_id, Integer store_id, Integer type_id, Integer mcoup_id) {

		// 修改优惠券
		// changeBonus(bonus_id, store_id);

		// 由购物车列表中获取此店铺的相关信息
		Map storeData = StoreCartContainer.getStoreData(store_id);

		// 获取此店铺的购物列表
		List list = (List) storeData.get(StoreCartKeyEnum.goodslist.toString());

		// 配送地区
		String regionid_str = region_id == null ? "" : region_id + "";

		// 计算此配送方式时的店铺相关价格
		OrderPrice orderPrice = this.cartManager.countPrice(list, type_id, regionid_str);

		// 激发计算子订单价格事件
		orderPrice = storeCartPluginBundle.countChildPrice(orderPrice);

		// 获取购物车中已经选择的商品的订单价格 by_DMRain 2016-6-28
		OrderPrice storePrice = (OrderPrice) storeData.get("storeprice");
		Double act_discount = storePrice.getActDiscount();

		// 如果促销活动优惠的现金不为空 by_DMRain 2016-6-28
		if (act_discount != null && act_discount != 0) {
			orderPrice.setActDiscount(act_discount);
			orderPrice.setNeedPayMoney(orderPrice.getNeedPayMoney() - act_discount);
		}

		Integer activity_id = (Integer) storeData.get("activity_id");

		// 如果促销活动id不为空 by_DMRain 2016-6-28
		if (activity_id != null) {
			ActivityDetail detail = this.activityDetailManager.getDetail(activity_id);
			// 如果促销活动包含了免运费的优惠内容 by_DMRain 2016-6-28
			if (detail.getIs_free_ship() == 1) {
				orderPrice.setIs_free_ship(1);
				orderPrice.setAct_free_ship(orderPrice.getShippingPrice());
				orderPrice.setShippingPrice(0d);
			}

			// 如果促销含有送积分的活动
			if (detail.getIs_send_point() == 1) {
				orderPrice.setPoint(detail.getPoint_value());
			}

			// 如果促销含有送赠品的活动
			if (detail.getIs_send_gift() == 1) {
				// 获取赠品的可用库存
				Integer enable_store = this.storeActivityGiftManager.get(detail.getGift_id()).getEnable_store();

				// 如果赠品的可用库存大于0
				if (enable_store > 0) {
					orderPrice.setGift_id(detail.getGift_id());
				}
			}

			// 如果促销含有送优惠券的活动
			if (detail.getIs_send_bonus() == 1) {
				// 获取店铺优惠券信息
				StoreCoupons coupons = this.storeCouponsManager.getCoupons(detail.getBonus_id());
				
				// 优惠券发行量
				int createNum = coupons.getCoupons_stock();

				// 获取优惠券已被领取的数量
				int count = coupons.getReceived_num();

				// 如果优惠券的发行量大于已经被领取的优惠券数量
				if (createNum > count) {
					orderPrice.setBonus_id(detail.getBonus_id());
				}
			}
		}

		// 新增如果选择不使用优惠券，就将已经放进session中的店铺优惠券信息删除 add_by DMRain 2016-7-14
		if (mcoup_id != 0) {
			MemberCoupons coupons = this.memberCouponsManager.get(mcoup_id);
			changeBonus(orderPrice, coupons, store_id);
		} else {
			CouponsSession.cancelB2b2cCoupons(store_id);
		}

		// 重新压入此店铺的订单价格和配送方式id
		storeData.put(StoreCartKeyEnum.storeprice.toString(), orderPrice);
		storeData.put(StoreCartKeyEnum.shiptypeid.toString(), type_id);

		return JsonResultUtil.getObjectJson(orderPrice, "storeprice");

	}
	
	/**
	 * 修改优惠券选项
	 * @param orderprice 订单价格
	 * @param coupons 优惠券信息
	 * @param storeid 店铺id
	 * @return
	 */
	private OrderPrice changeBonus(OrderPrice orderprice, MemberCoupons coupons, Integer storeid) {

		// set 红包
		CouponsSession.use(storeid, coupons);

		//如果优惠券类型为满减券或者是现金券
		if (coupons.getMcoup_type() == 0 || coupons.getMcoup_type() == 2) {
			// 如果优惠券面额大于商品优惠价格的话 那么优惠价格为商品价格
			if (orderprice.getNeedPayMoney() <= coupons.getMcoup_money()) {
				orderprice.setDiscountPrice(orderprice.getNeedPayMoney());
				orderprice.setNeedPayMoney(0.0);
			} else {
				// 计算需要支付的金额
				orderprice.setNeedPayMoney(CurrencyUtil.add(orderprice.getNeedPayMoney(), -coupons.getMcoup_money()));

				orderprice.setDiscountPrice(coupons.getMcoup_money());
			}
		}
		
		//如果优惠券类型为折扣券
		if (coupons.getMcoup_type() == 1) {
			double discountPercent = coupons.getMcoup_discount() / 100.00;
			double discountPrice = orderprice.getNeedPayMoney() - (orderprice.getNeedPayMoney() * discountPercent);
			
			orderprice.setNeedPayMoney(orderprice.getNeedPayMoney() * discountPercent);
			orderprice.setDiscountPrice(discountPrice);
		}
		
		return orderprice;

	}
	
}
