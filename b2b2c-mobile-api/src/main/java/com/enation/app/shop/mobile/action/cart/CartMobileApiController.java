/**
 * 描述：购物车api  
 * 修改人：  
 * 修改时间：
 * 修改内容：
 */
package com.enation.app.shop.mobile.action.cart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.enation.app.b2b2c.core.goods.service.StoreCartContainer;
import com.enation.app.b2b2c.core.member.service.IStoreMemberAddressManager;
import com.enation.app.b2b2c.core.order.model.cart.StoreCartItem;
import com.enation.app.b2b2c.core.order.service.cart.IStoreCartManager;
import com.enation.app.b2b2c.core.order.service.cart.IStoreProductManager;
import com.enation.app.b2b2c.core.store.model.Store;
import com.enation.app.b2b2c.core.store.service.IStoreDlyTypeManager;
import com.enation.app.b2b2c.core.store.service.IStoreManager;
import com.enation.app.b2b2c.core.store.service.IStoreTemplateManager;
import com.enation.app.b2b2c.front.api.order.publicmethod.StoreCartPublicMethod;
import com.enation.app.base.core.model.Member;
import com.enation.app.base.core.service.IMemberManager;
import com.enation.app.base.core.service.ISettingService;
import com.enation.app.javashop.customized.core.service.IMemberCouponsManager;
import com.enation.app.shop.component.bonus.service.IBonusTypeManager;
import com.enation.app.shop.core.goods.model.Goods;
import com.enation.app.shop.core.goods.model.Product;
import com.enation.app.shop.core.goods.service.IGoodsManager;
import com.enation.app.shop.core.goods.service.IProductManager;
import com.enation.app.shop.core.order.model.Cart;
import com.enation.app.shop.core.order.model.support.OrderPrice;
import com.enation.app.shop.core.order.plugin.cart.CartPluginBundle;
import com.enation.app.shop.core.order.service.ICartManager;
import com.enation.app.shop.core.other.service.IActivityGiftManager;
import com.enation.app.shop.core.other.service.IActivityManager;
import com.enation.app.shop.mobile.service.CrmPointManager;
import com.enation.app.shop.mobile.service.impl.ApiCartManager;
import com.enation.app.shop.mobile.utils.CommonRequest;
import com.enation.app.shop.mobile.utils.PointUtils;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.CurrencyUtil;
import com.enation.framework.util.JsonResultUtil;

/**
 * 购物车Api 
 * 提供购物车增删改查、结算   
 * @author hoser
 * @version v1.1 2016-04-10
 * @since v1.1
 * 
 */
@SuppressWarnings("serial")
@Controller
@Scope("prototype")
@RequestMapping("/api/mobile/cart")
public class CartMobileApiController{
	protected final Logger logger = Logger.getLogger(CartMobileApiController.class);
	
	@Autowired
	private IProductManager productManager;
	@Autowired
	private ICartManager cartManager;
	@Autowired
	private IStoreCartManager storeCartManager;
	@Autowired
	private IStoreMemberAddressManager storeMemberAddressManager;
	@Autowired
	private IStoreTemplateManager storeTemplateManager;
	@Autowired
	private IStoreDlyTypeManager storeDlyTypeManager;
	@Autowired
	private CartPluginBundle cartPluginBundle;
	@Autowired
    private IStoreProductManager storeProductManager;
	@Autowired
	private StoreCartPublicMethod storeCartPublicMethod;
//	@Autowired
//	private IB2b2cBonusManager b2b2cBonusManager;
	@Autowired
	private IActivityGiftManager activityGiftManager;
	@Autowired
    private IBonusTypeManager bonusTypeManager;
	@Autowired
	private IActivityManager activityManager;
	@Autowired
	IMemberCouponsManager memberCouponsManager;
	@Autowired
	private ISettingService settingService;
	@Autowired
	private IGoodsManager goodsManager;
	@Autowired
	private IMemberManager memberManager;
	@Autowired
	private ApiCartManager apiCartManager;
	@Autowired
	private CrmPointManager crmPointManager;
	@Autowired
	private IStoreManager storeManager;
	
	
	 /**
     * 将一个货品添加至购物车
     * 需要传递productid和num参数
     * @param productid   货品id，int型
     * @param num         数量
     * @return 返回json串
     * result  为1表示调用成功0表示失败 ，int型
     * message 为提示信息
     */
	@ResponseBody
	@RequestMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult add(Integer productid, Integer num, Integer showCartData, Integer activity_id) {
		Integer member_id = CommonRequest.getMemberID();
		if (showCartData == null) {
			showCartData = 1;
		}
		Product product = productManager.get(productid);
		return this.addProductToCart(product, num, showCartData, activity_id,member_id);
	}
	

	
	/**
	 * 获取购物车中的商品列表
	 * 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	@ResponseBody
	@RequestMapping(value="/list.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult list() {
		try {
//			this.storeCartManager.countPrice(null);
//			List<Map> list = StoreCartContainer.getStoreCartListFromSession();
			Integer member_id = CommonRequest.getMemberID();
			List<Map> list = apiCartManager.countPriceForCart(member_id);
			return JsonResultUtil.getObjectJson(list);
		} catch (RuntimeException e) {
			this.logger.error("获取购物车列表出错", e);
			return JsonResultUtil.getErrorJson(e.getMessage());
		}
	}
	
	
	
	/**
	 * 更新购物车的数量
	 * 
	 * @param cartid
	 *            :要更新的购物车项id，int型，即 CartItem.item_id
	 * @param num
	 *            :要更新数量,int型
	 * @return 返回json字串 result： 为1表示调用成功0表示失败 int型 store: 此商品的库存 int型
	 */
	@ResponseBody
	@RequestMapping(value="/update-num.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult updateNum() {
		JsonResult result=new JsonResult();
		try {
			Integer member_id = CommonRequest.getMemberID();
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			String cartid = request.getParameter("cartid");
			int num = NumberUtils.toInt(request.getParameter("num"), 1);
			String productid = request.getParameter("productid");
			Product product = productManager.get(Integer.valueOf(productid));
			Integer store = product.getEnable_store();
			if (store == null)
				store = 0;
			if (store >= num) {
//				cartManager.updateNum(request.getSession().getId(),
//						Integer.valueOf(cartid), Integer.valueOf(num));
				apiCartManager.updateNum(member_id,
						Integer.valueOf(cartid), Integer.valueOf(num));
//				this.storeCartManager.countPrice("no");
//				List<Map> list = StoreCartContainer.getStoreCartListFromSession();
//				String listStr = JsonUtil.ListToJson(list);
//				result.setResult(1);
//				result.setData(listStr);
				List<Map> list = apiCartManager.countPrice(member_id);
				return JsonResultUtil.getObjectJson(list);
			} else {
				return JsonResultUtil.getErrorJson("要购买的商品数量超出库存！");
			}
		} catch (RuntimeException e) {
			this.logger.error("更新购物车数量出现意外错误", e);
			JsonResultUtil.getErrorJson(e.getMessage());
		}
		return result;
	}
	
	/**
	 * 删除购物车一项
	 * 
	 * @param cartid
	 *            :要删除的购物车id,int型,即 CartItem.item_id
	 * @return 返回json字串 result 为1表示调用成功0表示失败 message 为提示信息
	 *         <p/>
	 *         {@link com.enation.app.shop.core.model.support.CartItem }
	 */
	
	@ResponseBody
	@RequestMapping(value="/delete.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult delete() {
		try {
			Integer member_id = CommonRequest.getMemberID();
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			String cartid = request.getParameter("cartid");
			cartManager.delete(member_id,
					Integer.valueOf(cartid));
			Integer count = this.cartManager.countItemNum(member_id);
			return JsonResultUtil.getNumberJson("count", count);
		} catch (RuntimeException e) {
			this.logger.error("删除购物项失败", e);
			return JsonResultUtil.getErrorJson(e.getMessage());
		}
	}
	

	@ResponseBody
	@RequestMapping(value="/deleteBatch.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult deleteBatch(String cartids) {
		try {
			Integer member_id = CommonRequest.getMemberID(); 	
				cartManager.delete(member_id, cartids);
				Integer count = this.cartManager.countItemNum(member_id);
				return JsonResultUtil.getNumberJson("count", count);
		} catch (RuntimeException e) {
			this.logger.error("删除购物项失败", e);
			return JsonResultUtil.getErrorJson(e.getMessage());
		}
	}
	
	
	/**
	 * 清空购物车
	 */
	@ResponseBody
	@RequestMapping(value="/clean.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult clean() {
		Integer member_id = CommonRequest.getMemberID();
		try {
			cartManager.clean(member_id);
			return JsonResultUtil.getSuccessJson("清空购物车成功");
		} catch (RuntimeException e) {
			this.logger.error("清空购物车出错", e);
			return JsonResultUtil.getErrorJson(e.getMessage());
		}
	}
	
	
	
	/**
     * 获取购物车中的选中的商品列表
     * @return 购物车商品列表json
     */
//    @ResponseBody
//    @RequestMapping(value = "/list-selected", produces = MediaType.APPLICATION_JSON_VALUE)
//    public JsonResult listSelected() {
//    	//清空存放在session中的已勾选的优惠券，防止用户进入订单结算页，选定优惠券后，返回购物车添加或修改商品时，选定的优惠券还存在的问题。
//    	B2b2cBonusSession.cleanAll();
//        Member member = UserConext.getCurrentMember();
//        //获取购物车商品列表
//        storeCartManager.countSelectPrice("yes");
//        List<Map> cartItemList = StoreCartContainer.getSelectStoreCartListFromSession();
//        if(cartItemList != null && cartItemList.size() > 0) {
//            for (Map map : cartItemList) {
//                List<StoreCartItem> List = (List) map.get("goodslist");
//                for (StoreCartItem storeCartItem : List) {
//                    storeCartItem.setImage_default(StaticResourcesUtil.convertToUrl(storeCartItem.getImage_default()));
//                }
//                //店铺的优惠券列表
//                Integer store_id = NumberUtils.toInt(map.get("store_id").toString(), 0);
//                OrderPrice orderPrice = (OrderPrice)map.get("storeprice");
//                if(member != null){
//                	//TODO
//                //这块是促销活动红包信息
////				Page webPage = this.b2b2cBonusManager.getMyBonusByIsUsable(1, 100, member.getMember_id(), 1,
////							orderPrice.getGoodsPrice(), store_id);
////				List bonusList = (java.util.List) webPage.getResult();
//                List couponList = memberCouponsManager.getCouponsList(member.getMember_id(), store_id, orderPrice.getGoodsPrice());
//				map.put("couponList", couponList);
//                }
              //店铺活动详情
//                if(map.containsKey("activity_id")){
//                    int activity_id = NumberUtils.toInt(map.get("activity_id").toString(), 0);
//                    Activity activity = activityManager.get(activity_id);
//                    if(activity != null){
//                        Map activityMap = activityManager.getActMap(activity.getActivity_id());
//                        if(activityMap.containsKey("is_send_gift") && NumberUtils.toInt(activityMap.get("is_send_gift").toString(), 0) == 1){
//                            int gift_id = NumberUtils.toInt(activityMap.get("gift_id").toString(), 0);
//                            if(gift_id > 0){
//                                ActivityGift gift = activityGiftManager.get(gift_id);
//                                if(gift != null){
//                                    gift.setGift_img(StaticResourcesUtil.convertToUrl(gift.getGift_img()));
//                                    activityMap.put("gift", gift);
//                                }
//                            }
//                        }
//                        //判断是否包含优惠券
//        				if(activityMap.containsKey("is_send_bonus") && NumberUtils.toInt(activityMap.get("is_send_bonus").toString(), 0) == 1){
//        					int bonus_id = NumberUtils.toInt(activityMap.get("bonus_id").toString(), 0);
//        					if(bonus_id>0){
//        						BonusType bonus =  this.bonusTypeManager.get(bonus_id);
//        						activityMap.put("bonus", bonus);
//        					}
//        				}
//                        map.put("activity", activityMap);
//                    }
//                }
//            }
//        }
//        return JsonResultUtil.getObjectJson(cartItemList);
//    }
    
    
    

	/**
     * 获取购物车中的选中的商品列表
     * @return 购物车商品列表json
     */
    @ResponseBody
    @RequestMapping(value = "/list-selected", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult listSelected() {
    	Map<String,Object> returnMap = new HashMap<String,Object>();
    	double totalMoney = 0.0d;
    	double goodsPrice = 0.0d;
		Integer member_id = CommonRequest.getMemberID();
		String ticket = CommonRequest.getReqTicket();
		memberCouponsManager.releaseCouponsAll(member_id);//释放掉所有的优惠券
		List<Map> cartItemList = apiCartManager.countSelectPrice("yes", member_id,null);
		if (cartItemList != null && cartItemList.size() > 0) {
			for (Map map : cartItemList) {
				List<StoreCartItem> List = (List) map.get("goodslist");
				for (StoreCartItem storeCartItem : List) {
					storeCartItem.setImage_default(StaticResourcesUtil.convertToUrl(storeCartItem.getImage_default()));
				}
				// 店铺的优惠券列表
				Integer store_id = NumberUtils.toInt(map.get("store_id").toString(), 0);
				OrderPrice orderPrice = (OrderPrice) map.get("storeprice");
				totalMoney = CurrencyUtil.add(totalMoney, orderPrice.getNeedPayMoney());
				goodsPrice = CurrencyUtil.add(goodsPrice,orderPrice.getGoodsPrice());
				if (member_id != null) {
					List couponList = memberCouponsManager.getCouponsList(member_id, store_id,
							orderPrice.getGoodsPrice());
					map.put("couponList", couponList);
				}
			}
		}
		returnMap.put("cartItem", cartItemList);//订单商品行
		returnMap.put("totalPrice", totalMoney);//应付总价
		returnMap.put("goodsPrice",goodsPrice);//商品总价
		//调用积分接口,可用积分为商品总价的10%,积分不够的话则按剩余积分算，积分与钱的比例为1:10
		int totalPoint = 0;
		try {
			totalPoint = crmPointManager.queryMemberPoint(ticket);
		} catch (Exception e) {
			logger.error("request member point error");
		}
		int canUsePoint = PointUtils.canUsePoint(goodsPrice, totalPoint);
		Double relief = PointUtils.usePointFacDiscount(canUsePoint);
		returnMap.put("canUsePoint", canUsePoint);//可使用积分
		returnMap.put("relief", relief);//可使用积分减免价格r
		returnMap.put("reliefedPay", CurrencyUtil.sub(totalMoney, relief));//可使用积分后应付的价格
		return JsonResultUtil.getObjectJson(returnMap);
		
		
    
	//  商家设置可否使用积分方案   ***************************************************************************
		
//		Map<String,Object> returnMap = new HashMap<String,Object>();
//    	double totalMoney = 0.0d;
//    	double goodsPrice = 0.0d;
//    	double point_goodsPrice = 0.0d;
//		Integer member_id = CommonRequest.getMemberID();
//		String ticket = CommonRequest.getReqTicket();
//		memberCouponsManager.releaseCouponsAll(member_id);//释放掉所有的优惠券
//		List<Map> cartItemList = apiCartManager.countSelectPrice("yes", member_id,null);
//		if (cartItemList != null && cartItemList.size() > 0) {
//			for (Map map : cartItemList) {
//				List<StoreCartItem> List = (List) map.get("goodslist");
//				for (StoreCartItem storeCartItem : List) {
//					storeCartItem.setImage_default(StaticResourcesUtil.convertToUrl(storeCartItem.getImage_default()));
//				}
//				// 店铺的优惠券列表
//				Integer store_id = NumberUtils.toInt(map.get("store_id").toString(), 0);
//				Store store = storeManager.getStore(store_id);
//				//不为1的时候是不可以使用积分
//				boolean enablepoint = store.getEnablepoint() != 1;
//				OrderPrice orderPrice = (OrderPrice) map.get("storeprice");
//				totalMoney = CurrencyUtil.add(totalMoney, orderPrice.getNeedPayMoney());
//				goodsPrice = CurrencyUtil.add(goodsPrice,orderPrice.getGoodsPrice());
//				if(enablepoint){
//					point_goodsPrice = CurrencyUtil.add(point_goodsPrice,orderPrice.getGoodsPrice());
//				}
//				if (member_id != null) {
//					List couponList = memberCouponsManager.getCouponsList(member_id, store_id,
//							orderPrice.getGoodsPrice());
//					map.put("couponList", couponList);
//				}
//			}
//		}
//		returnMap.put("cartItem", cartItemList);//订单商品行
//		returnMap.put("totalPrice", totalMoney);//应付总价
//		returnMap.put("goodsPrice",goodsPrice);//商品总价
//		//调用积分接口,可用积分为商品总价的10%,积分不够的话则按剩余积分算，积分与钱的比例为1:10
//		int totalPoint = 0;
//		try {
//			totalPoint = crmPointManager.queryMemberPoint(ticket);
//		} catch (Exception e) {
//			logger.error("request member point error");
//		}
//		
//		int canUsePoint = PointUtils.canUsePoint(point_goodsPrice, totalPoint);
//		Double relief = PointUtils.usePointFacDiscount(canUsePoint);
//		returnMap.put("canUsePoint", canUsePoint);//可使用积分
//		returnMap.put("relief", relief);//可使用积分减免价格r
//		returnMap.put("reliefedPay", CurrencyUtil.sub(totalMoney, relief));//可使用积分后应付的价格
//		return JsonResultUtil.getObjectJson(returnMap);
		
	}
    
    
    /**
	 * 更改收货地址
	 * @return 含有价格信息的json串
	 */
	@SuppressWarnings("rawtypes")
	@ResponseBody
	@RequestMapping(value = "/change-address", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult changeArgsType_address(Integer addressId) {
    	Map<String,Object> returnMap = new HashMap<String,Object>();
    	double totalMoney = 0.0d;
    	double goodsPrice = 0.0d;
    	Integer member_id = CommonRequest.getMemberID();
    	String ticket = CommonRequest.getReqTicket();
		List<Map> cartItemList = apiCartManager.countSelectPrice("yes", member_id,addressId);
		if (cartItemList != null && cartItemList.size() > 0) {
			for (Map map : cartItemList) {
				List<StoreCartItem> List = (List) map.get("goodslist");
				for (StoreCartItem storeCartItem : List) {
					storeCartItem.setImage_default(StaticResourcesUtil.convertToUrl(storeCartItem.getImage_default()));
				}
				Integer store_id = NumberUtils.toInt(map.get("store_id").toString(), 0);
				OrderPrice orderPrice = (OrderPrice) map.get("storeprice");
				totalMoney = CurrencyUtil.add(totalMoney, orderPrice.getNeedPayMoney());
				goodsPrice = CurrencyUtil.add(goodsPrice,orderPrice.getGoodsPrice());
				
				if (member_id != null) {
					List couponList = memberCouponsManager.getCouponsList(member_id, store_id,
							orderPrice.getGoodsPrice());
					map.put("couponList", couponList);
				}
			}
		}
		returnMap.put("cartItem", cartItemList);
		returnMap.put("totalPrice", totalMoney);
		//调用积分接口,可用积分为商品总价的10%,积分不够的话则按剩余积分算，积分与钱的比例为1:10
		int totalPoint = 0;
		try {
			totalPoint = crmPointManager.queryMemberPoint(ticket);
		} catch (Exception e) {
			logger.error("request member point error");
		} 
		//根据商品价格计算可以使用的积分
		int canUsePoint = PointUtils.canUsePoint(goodsPrice, totalPoint);
		Double relief = PointUtils.usePointFacDiscount(canUsePoint);
		returnMap.put("canUsePoint", canUsePoint);//可使用积分
		returnMap.put("relief", relief);//可使用积分减免价格
		returnMap.put("reliefedPay", CurrencyUtil.sub(totalMoney, relief));//可使用积分后应付的价格
		return JsonResultUtil.getObjectJson(returnMap);
		
		
		
		// 商户可用积分 ********************************************************************************
//		Map<String,Object> returnMap = new HashMap<String,Object>();
//    	double totalMoney = 0.0d;
//    	double pointGoodsPrice = 0.0d;
//    	Integer member_id = CommonRequest.getMemberID();
//    	String ticket = CommonRequest.getReqTicket();
//		List<Map> cartItemList = apiCartManager.countSelectPrice("yes", member_id,addressId);
//		if (cartItemList != null && cartItemList.size() > 0) {
//			for (Map map : cartItemList) {
//				List<StoreCartItem> List = (List) map.get("goodslist");
//				for (StoreCartItem storeCartItem : List) {
//					storeCartItem.setImage_default(StaticResourcesUtil.convertToUrl(storeCartItem.getImage_default()));
//				}
//				Integer store_id = NumberUtils.toInt(map.get("store_id").toString(), 0);
//				
//				OrderPrice orderPrice = (OrderPrice) map.get("storeprice");
//				totalMoney = CurrencyUtil.add(totalMoney, orderPrice.getNeedPayMoney());
//				//新增不可用积分的商户      TODO
//				Store store = storeManager.getStore(store_id);
//				//不为1的时候是不可以使用积分
//				boolean enablepoint = store.getEnablepoint() != 1;
//				if(enablepoint){
//					pointGoodsPrice = CurrencyUtil.add(pointGoodsPrice, orderPrice.getGoodsPrice());
//				}
//				if (member_id != null) {
//					List couponList = memberCouponsManager.getCouponsList(member_id, store_id,
//							orderPrice.getGoodsPrice());
//					map.put("couponList", couponList);
//				}
//			}
//		}
//		returnMap.put("cartItem", cartItemList);
//		returnMap.put("totalPrice", totalMoney);
//		//调用积分接口,可用积分为商品总价的10%,积分不够的话则按剩余积分算，积分与钱的比例为1:10
//		int totalPoint = 0;
//		try {
//			totalPoint = crmPointManager.queryMemberPoint(ticket);
//		} catch (Exception e) {
//			logger.error("request member point error");
//		} 
//		int canUsePoint = PointUtils.canUsePoint(pointGoodsPrice,totalPoint);
//		Double relief = PointUtils.usePointFacDiscount(canUsePoint);
//		returnMap.put("canUsePoint", canUsePoint);//可使用积分
//		returnMap.put("relief", relief);//可使用积分减免价格
//		returnMap.put("reliefedPay", CurrencyUtil.sub(totalMoney, relief));//可使用积分后应付的价格
//		return JsonResultUtil.getObjectJson(returnMap);
		
	}
    
    
    
    

	@ResponseBody
	@RequestMapping(value="/list-app.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult listApp() {
		String sessionid = ThreadContextHolder.getHttpRequest().getSession().getId();
		OrderPrice orderprice  =this.cartManager.countPrice(cartManager.listGoods(sessionid), null, null);

		int count = this.cartManager.countItemNum(sessionid);

		java.util.Map<String, Object> data = new HashMap();
		data.put("count", count);//购物车中的商品数量
		data.put("total", orderprice.getOrderPrice());//总价
		//购物车中的商品列表
		//	        data.put("goodslist", storeCartManager.listGoods(sessionid));
		this.storeCartManager.countPrice(null);
		//	        购物车中的商品列表
		data.put("goodslist", StoreCartContainer.getSelectStoreCartListFromSession());
 
		return JsonResultUtil.getObjectJson(data);
	} 
	

	
	/**
	 * 更新购物车的数量
	 *
	 * @param cartid:要更新的购物车项id，int型，即 CartItem.item_id
	 * @param num:要更新数量,int型
	 * @return 返回json字串
	 * result： 为1表示调用成功0表示失败 int型
	 * store: 此商品的库存 int型
	 */
	@ResponseBody
	@RequestMapping(value="/update-num-app.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult updateNumApp() {
		try {
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			String cartid = request.getParameter("cartid");
			int num = NumberUtils.toInt(request.getParameter("num"), 1);
			String productid = request.getParameter("productid");
			Product product = productManager.get(Integer.valueOf(productid));
			Integer store = product.getEnable_store();

			if (store == null)
				store = 0;

			if (store >= num) {
				cartManager.updateNum(request.getSession().getId(), Integer.valueOf(cartid), Integer.valueOf(num));
				return JsonResultUtil.getSuccessJson("更新数量成功！");
			}else{
				return JsonResultUtil.getErrorJson("要购买的商品数量超出库存！");
			}
		} catch (RuntimeException e) {
			this.logger.error("更新购物车数量出现意外错误", e);
			return JsonResultUtil.getErrorJson("要购买的商品数量超出库存！");
		}
		
	}

	/**
	 * 购物车的价格总计信息
	 * 
	 * @param 无
	 * @return 返回json字串 result： 为1表示调用成功0表示失败 int型 orderprice: 订单价格，OrderPrice型
	 *         {@link com.enation.app.shop.core.model.support.OrderPrice}
	 */
	@ResponseBody
	@RequestMapping(value="/total.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult total() {
		try {
//			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			Integer memberId = CommonRequest.getMemberID();
			OrderPrice orderprice = this.cartManager.countPrice(
					cartManager.listGoods(memberId), null,
					null);
			return JsonResultUtil.getObjectJson(orderprice);
		} catch (RuntimeException e) {
			this.logger.error("计算总价出错", e);
			return JsonResultUtil.getErrorJson(e.getMessage());
		}
	}

	/**
	 * 计算购物车货物总数
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/count.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult count() {
		try {
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			Integer count = this.cartManager.countItemNum(request.getSession()
					.getId());
			return JsonResultUtil.getNumberJson("count", count);
		} catch (RuntimeException e) {
			this.logger.error("计算货物总数出错", e);
			return JsonResultUtil.getErrorJson(e.getMessage());
		}
	}


	  /**
     * 选择或取消选择货品进行下单
     * @param product_id 货品id
     * @param checked    是否选中
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/check-product.do", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult checkProduct(Integer product_id, boolean checked) {
    	Integer member_id = CommonRequest.getMemberID();
        apiCartManager.checkProduct(member_id, product_id, checked);
        return JsonResultUtil.getSuccessJson("选择购物车商品成功");
    }

    /**
     * 选择或取消选择所有货品
     *
     * @param checked 是否选中
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/check-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult checkAll(boolean checked) {
//        String sessionId = ThreadContextHolder.getHttpRequest().getSession().getId();
    	Integer member_id = CommonRequest.getMemberID();
        cartManager.checkAll(member_id, checked);
        return JsonResultUtil.getSuccessJson("选择购物车商品成功");
    }
    
    
    /**
     * 选择或取消选择店铺下的货品
     * @param store_id 店铺id
     * @param checked  是否选中
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/check-store", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult checkStore(Integer store_id, boolean checked) {
    	Integer member_id = CommonRequest.getMemberID();
        try {
            storeCartManager.checkStoreAll(member_id, checked, store_id);
            return JsonResultUtil.getSuccessJson("选择购物车商品成功");
        } catch (RuntimeException e) {
            return JsonResultUtil.getErrorJson("选择购物车商品出错");
        }
    }
    
	/**
	 * 获取购物车数据
	 * @param 无
	 * @return 返回json串
	 * result  为1表示调用成功0表示失败
	 * data.count：购物车的商品总数,int 型
	 * data.total:购物车总价，int型
	 * 
	 */
	@ResponseBody  
	@RequestMapping(value="/get-cart-data", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult getCartData() {
		try {
//			String sessionid = ThreadContextHolder.getHttpRequest().getSession().getId();
			Integer member_id = CommonRequest.getMemberID();
			Double goodsTotal = cartManager.countGoodsTotalCheck(member_id);
			int count = this.cartManager.countItemNum(member_id);
			Map<String, Object> data = new HashMap<String,Object>();
			data.put("count", count);// 购物车中的商品数量
			data.put("total", goodsTotal);// 总价
			return JsonResultUtil.getObjectJson(data);
		} catch (RuntimeException e) {
			this.logger.error("获取购物车数据出错", e);
			return JsonResultUtil.getErrorJson("获取购物车数据出错");
		}
	}
	
	
	
	
	
	
    /**
	 * 添加货品的购物车
	 * @param product
	 * @param num 数量
	 * @param showCartData 是否在返回的json中显示购物车
	 * @return
	 */
	private JsonResult addProductToCart(Product product, int num, int showCartData, Integer activity_id,Integer member_id){
//		String sessionid =ThreadContextHolder.getHttpRequest().getSession().getId();
		if(product!=null){
			try{
				//判断商品是否下架 商品是否删除
				Goods good=goodsManager.getGoods(product.getGoods_id());
				if(good.getMarket_enable()!=1){
					return JsonResultUtil.getErrorJson("抱歉！您所选择的货品已经下架");
				}
				//商家能否购买自己的商品
				final String canBuySelf = settingService.getSetting("store", "buy_self_auth");
				if (!"1".equals(canBuySelf)) {
					//该商品是否为自己的商品
//					Member nowaMember = UserConext.getCurrentMember();
					if (member_id != null) {
						Member belongTo = memberManager.getByGoodsId(product.getGoods_id());
						if (belongTo != null && belongTo.getMember_id().equals(member_id)) {
							throw new RuntimeException("抱歉！您不能购买自己的商品。");
						}
					}
				}
				int enableStore = product.getEnable_store();
				if (enableStore < num) {
					throw new RuntimeException("抱歉！您所选择的货品库存不足。");
				}
				
				//查询已经存在购物车里的商品
				Cart tempCart = cartManager.getCartByProductId(product.getProduct_id(), member_id);
				if(tempCart != null){
					int tempNum = tempCart.getNum();
					if (enableStore < num + tempNum) {
						throw new RuntimeException("抱歉！您所选择的货品库存不足。");
					}
				}
				
				HttpServletRequest request=ThreadContextHolder.getHttpRequest();//获取当前请求
				String exchange=request.getParameter("exchange");//获取exchange参数
				if("true".equals(exchange)){//如果是积分商品 这走下面的插件桩的方法
					this.cartPluginBundle.onAddProductToCart(product,num);
				}
				Cart cart = new Cart();
				cart.setGoods_id(product.getGoods_id());
				cart.setProduct_id(product.getProduct_id());
//				cart.setSession_id(sessionid);
				cart.setNum(num);
				cart.setItemtype(0); //0为product和产品 ，当初是为了考虑有赠品什么的，可能有别的类型。
				cart.setWeight(product.getWeight());
				cart.setPrice( product.getPrice());
				cart.setName(product.getName());
				// 服务商品   add by liuyulei  2016-09-13
				cart.setGoods_type(Integer.parseInt((good.getGoods_type() == null ? "0" : good.getGoods_type())));
				//默认商品添加购物车选中 
				cart.setIs_check(1);
				//如果商品参加了促销活动，就将促销活动ID添加至购物车表
				//add by DMRain 2016-1-15
				if(activity_id != null && activity_id != 0){
					cart.setActivity_id(activity_id);
				}
				this.cartManager.add(cart,member_id);
				//需要同时显示购物车信息
				if(showCartData==1){
					return this.getCartData();
				}
				return JsonResultUtil.getSuccessJson("添加成功");
			}catch(RuntimeException e){
				this.logger.error("将货品添加至购物车出错",e);
				return JsonResultUtil.getErrorJson("添加失败: " + e.getMessage());
			}
		}else{
			return JsonResultUtil.getErrorJson("该货品不存在，未能添加到购物车");
		}
	}
	
    
//	 /**
//	  * 重新put
//	  * @return
//	  */
//	private void reputSelect(){
//		HttpServletRequest request =ThreadContextHolder.getHttpRequest();
//		String sessionid  = request.getSession().getId(); 
//		List<CartItem> cartList  = cartManager.selectListGoods(sessionid);
//		//计算订单价格
//		OrderPrice orderprice  =this.cartManager.countPrice(cartList, null,null); 
//		storeCartManager.countSelectPrice("yes");
//		//激发价格计算事件
//		orderprice  = this.cartPluginBundle.coutPrice(orderprice);
//		 
//	}
	
	public static String toInString(Integer[] cartids) {
		StringBuffer result = new StringBuffer();
		for (Integer cartid : cartids) {
			result.append(cartid).append(",");
		}
		return result.toString().substring(0, result.toString().length() - 1);
	}
	
	
}
