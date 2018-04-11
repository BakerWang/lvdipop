package com.enation.app.shop.mobile.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.enation.app.b2b2c.core.goods.service.StoreCartKeyEnum;
import com.enation.app.b2b2c.core.order.model.StoreOrder;
import com.enation.app.b2b2c.core.order.service.IStoreOrderManager;
import com.enation.app.b2b2c.core.order.service.cart.IStoreCartManager;
import com.enation.app.b2b2c.core.store.model.Store;
import com.enation.app.b2b2c.core.store.service.impl.StoreManager;
import com.enation.app.base.core.model.Member;
import com.enation.app.base.core.service.IMemberManager;
import com.enation.app.base.core.service.ISettingService;
import com.enation.app.javashop.customized.core.service.IMemberCouponsManager;
import com.enation.app.shop.component.bonus.model.MemberCoupons;
import com.enation.app.shop.core.goods.service.impl.ProductManager;
import com.enation.app.shop.core.member.service.impl.PointHistoryManager;
import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.model.OrderItem;
import com.enation.app.shop.core.order.model.OrderMeta;
import com.enation.app.shop.core.order.model.OrderMetaEnumKey;
import com.enation.app.shop.core.order.model.SellBackList;
import com.enation.app.shop.core.order.model.SellBackStatus;
import com.enation.app.shop.core.order.model.support.CartItem;
import com.enation.app.shop.core.order.model.support.OrderPrice;
import com.enation.app.shop.core.order.plugin.cart.CartPluginBundle;
import com.enation.app.shop.core.order.plugin.order.OrderPluginBundle;
import com.enation.app.shop.core.order.service.ICartManager;
import com.enation.app.shop.core.order.service.IOrderFlowManager;
import com.enation.app.shop.core.order.service.IOrderMetaManager;
import com.enation.app.shop.core.order.service.OrderStatus;
import com.enation.app.shop.core.order.service.impl.SellBackManager;
import com.enation.app.shop.mobile.service.impl.ApiCartManager;
import com.enation.app.shop.mobile.utils.CommonRequest;
import com.enation.app.shop.mobile.utils.PointUtils;
import com.enation.app.shop.mobile.utils.RefundStatus;
import com.enation.app.shop.mobile.utils.SellerStatus;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.util.CurrencyUtil;
import com.enation.framework.util.JsonUtil;
import com.enation.framework.util.StringUtil;

@Service
public class ApiOrderManager  {
	
	@Autowired
	private ICartManager cartManager;
	@Autowired
	private IStoreCartManager storeCartManager;
	@Autowired
	private ISettingService settingService;
	@Autowired
	private IMemberManager memberManager;
	@Autowired
	private CartPluginBundle  cartPluginBundle;
	@Autowired
	private IOrderFlowManager OrderFlowManager;
	@Autowired
	private OrderPluginBundle orderPluginBundle;//订单插件桩
	@Autowired
	private IDaoSupport daoSupport;
	@Autowired
	private IStoreOrderManager storeOrderManager;
	@Autowired
	private IMemberCouponsManager memberCouponsManager;
	@Autowired
	private IOrderMetaManager orderMetaManager;
	@Autowired
	private ApiCartManager apiCartManager;
	@Autowired
	private SellBackManager sellBackManager;
	@Autowired
	private StoreManager storeManager;
	@Autowired
	private CrmPointManager crmPointManager;
	@Autowired
	private ProductManager productManager;
	
	
	/**
	 * 获取自定的item
	 * @param orderId
	 * @return
	 */
	public List<OrderItem> listGoodsItems(Integer orderId) {
		String sql = "select * from es_order_items where order_id = ?";
		List<OrderItem> itemList = this.daoSupport.queryForList(sql, OrderItem.class, orderId);
		return itemList;
	}
	
	
    /**
     * 获取指定状态的订单数量
     *
     * @param status
     * @param member_id
     * @return
     */
    public int count(int status, int member_id) {
        String sql = "select COUNT(0) from es_order where parent_id is NOT NULL and member_id=? and disabled=0";
        if (status >= 0) {
            //等待付款的订单 按付款状态查询
            if (status == 0) {
            	sql += " and ( ( payment_type!='cod' and  status=" + OrderStatus.ORDER_CONFIRM + ") ";// 非货到付款的，未付款状态的可以结算
				sql += " or ( payment_type='cod' and   status=" + OrderStatus.ORDER_ROG + "  ) )";// 货到付款的要发货或收货后才能结算
            } else {
                sql += " AND status='" + status + "'";
            }
        }
        return this.daoSupport.queryForInt(sql, member_id);
    }

    /**
     * 获取除去未付款的取订单列表
     * @param pageNo
     * @param pageSize
     * @param status
     * @param keyword
     * @return
     */
    public Page pageOrders(int pageNo, int pageSize, String status, String keyword,Integer member_id){
		StringBuffer sql =new StringBuffer("select * from es_order where parent_id is NOT NULL and member_id = '" + member_id + "' and disabled=0");
		if(!StringUtil.isEmpty(status)){
			int statusNumber = -999;
			statusNumber = StringUtil.toInt(status);
			//等待付款的订单 按付款状态查询
			if(statusNumber==0){
				sql.append(" AND status!="+OrderStatus.ORDER_CANCELLATION+" AND pay_status="+OrderStatus.PAY_NO);
			}else{
				sql.append(" AND status='" + statusNumber + "'");
			}
		}
		if(!StringUtil.isEmpty(keyword)){
			sql.append(" AND order_id in (SELECT i.order_id FROM es_order_items i LEFT JOIN es_order o ON i.order_id=o.order_id WHERE o.member_id='" + member_id + "' AND i.name like '%" + keyword + "%')");
		}
		sql.append("order by create_time desc");
		Page rpage = this.daoSupport.queryForPage(sql.toString(),pageNo, pageSize);
		return rpage;
    }
    
	public Page pagePayOrders(int pageNo, int pageSize, String status, Integer member_id) {
		StringBuffer sql = new StringBuffer(
				"SELECT DISTINCT a.* FROM es_order a LEFT JOIN es_order b ON a.order_id = b.parent_id WHERE a.parent_id IS NULL and a.member_id = '"
						+ member_id + "' and a.disabled=0");
		if (!StringUtil.isEmpty(status)) {
			int statusNumber = -999;
			statusNumber = StringUtil.toInt(status);
			// 等待付款的订单 按付款状态查询
			if (statusNumber == 0) {
				sql.append(
			" AND b.status!=" + OrderStatus.ORDER_CANCELLATION + " AND b.pay_status=" + OrderStatus.PAY_NO);
			} else {
				sql.append(" AND b.status='" + status + "'");
			}
		}
		sql.append(" order by a.create_time desc");
		System.out.println(sql.toString());
		Page rpage = this.daoSupport.queryForPage(sql.toString(), pageNo, pageSize, Order.class);
		List<Order> parentOrderList = (List<Order>) rpage.getResult();
		
		StringBuffer childOrderSql = new StringBuffer("select a.*,b.tel from es_order a left join es_store b on a.store_id = b.store_id where a.parent_id = ?");
		if (!StringUtil.isEmpty(status)) {
		 childOrderSql.append(" AND a.status='" + Integer.valueOf(status) + "'");
		}
		
		List result = new ArrayList();
		for (Order order : parentOrderList) {
			Integer orderId = order.getOrder_id();
			List<Map> chilOrderList = this.daoSupport.queryForList(childOrderSql.toString(), orderId);
			Map map = new HashMap();
			order.setPointDiscount(PointUtils.usePointFacDiscount(order.getConsumepoint()));
			map.put("parentOrder", order);
			for (Map item : chilOrderList) {
					String itemJson = (String) item.get("items_json");
					List<Map<String, Object>> items = JsonUtil.toList(itemJson);
					item.put("itemList", transMap2Bean2(items, OrderItem.class));
					item.put("pointDiscount",PointUtils.usePointFacDiscount((Integer)item.get("consumepoint")));
			}
			map.put("childOrder", chilOrderList);
			result.add(map);
		}
		rpage.setReuslt(result);
		return rpage;
	}
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map orderDetails(StoreOrder storeOrder) {
		Map resultMap = new HashMap();
		List<Map> itemList = new ArrayList<Map>();
		if (storeOrder.getParent_id() != null) { //如果是子订单
			// 查出父订单信息
			StoreOrder parentOrder = storeOrderManager.get(storeOrder.getParent_id());
			Store store = storeManager.getStore(storeOrder.getStore_id());
			resultMap.put("parentOrder", parentOrder);
			try {
				SellBackList  sellBack = sellBackManager.getSellBack(storeOrder.getOrder_id());
				Map item = BeanUtils.describe(storeOrder);
				if(sellBack != null){
					item.put("seller_remark", converstatus(sellBack));
				}
				List<Map<String, Object>> itemMap = JsonUtil.toList(storeOrder.getItems_json());
				
				List<OrderItem> itemJsonList = transMap2Bean2(itemMap, OrderItem.class);
				for (OrderItem orderItem : itemJsonList) {
					String specvalue = productManager.getSpecValByProductId(orderItem.getProduct_id());
					orderItem.setSpecs(specvalue);
				}
				item.put("itemList", itemJsonList);
				item.put("tel",store.getTel());
				item.put("pointDiscount",PointUtils.usePointFacDiscount(storeOrder.getConsumepoint()));
				itemList.add(item);
			} catch (Exception e) {
				e.printStackTrace();
			}
			resultMap.put("childOrder", itemList);
		} else {//父订单信息
			storeOrder.setPointDiscount(PointUtils.usePointFacDiscount(storeOrder.getConsumepoint()));
			resultMap.put("parentOrder", storeOrder);
			itemList = this.daoSupport.queryForList("select a.*,b.tel from es_order a left join es_store b on a.store_id = b.store_id where parent_id = ?",
					storeOrder.getOrder_id());
			for (Map item : itemList) {
				String itemJson = (String) item.get("items_json");
				List<Map<String, Object>> items = JsonUtil.toList(itemJson);
				List<OrderItem> itemJsonList = transMap2Bean2(items, OrderItem.class);
				for (OrderItem orderItem : itemJsonList) {
					String specvalue = productManager.getSpecValByProductId(orderItem.getProduct_id());
					orderItem.setSpecs(specvalue);
				}
				item.put("itemList", itemJsonList);
				item.put("pointDiscount",PointUtils.usePointFacDiscount((Integer)item.get("consumepoint")));
			}
			resultMap.put("childOrder", itemList);
		}
		return resultMap;
	}
	
	

    /**
     * 取待评论的商品数
     *
     * @param member_id
     * @return
     */
    public int commentGoodsCount(int member_id) {
        return this.daoSupport.queryForInt("SELECT count(0) FROM es_member_order_item m LEFT JOIN es_goods g ON m.goods_id=g.goods_id WHERE m.member_id=? AND m.commented=0", member_id);
    }

    /**
     * 获取退换货数量
     *
     * @param member_id
     * @return
     */
    public int returnedCount(int member_id) {
        return this.daoSupport.queryForInt("select count(0) from es_sellback_list where member_id=? and tradestatus!=?", member_id,SellBackStatus.refund.getValue());
    }

    /**
     * 获取订单项详情
     *
     * @param item_id
     * @return
     */
    public OrderItem getItem(int item_id) {
        return (OrderItem) this.daoSupport.queryForObject("SELECT * FROM es_order_items WHERE item_id=?", OrderItem.class, item_id);
    }

    /**
     * 根据订单ID获取退货换详情
     * @param order_id
     * @return
     */
    public SellBackList getSellBack(Integer order_id) {
        String sql = "select * from es_sellback_list where orderid=? and ( (type=1 and tradestatus!=?) or (type=2 and tradestatus!=?))";
        List<SellBackList> SellBacks = (List<SellBackList>) this.daoSupport.queryForList(sql, SellBackList.class, order_id, SellBackStatus.refuse.getValue(),SellBackStatus.cancel.getValue());
        if(SellBacks != null && SellBacks.size() > 0){
            return SellBacks.get(0);
        }
        return null;
    }
    
    
	@Transactional(propagation = Propagation.REQUIRED)
	public Order createOrder(Order order, Integer memberId,boolean usePoint) throws Exception {
		// 为防止订单的某些优惠活动在提交订单的时候结束，需要在创建子订单的时候重新计算一遍价格，并将信息重新set进session中
//		this.storeCartManager.countSelectPrice("yes");
		// 读取所有的购物项，用于创建主订单
		List<CartItem> cartItemList = this.cartManager.listGoods(memberId);
		if (cartItemList == null || cartItemList.size() == 0) {
			throw new RuntimeException("购物车不能为空");
		}
		// 商家能否购买自己的商品
		final String CAN_BY_SELF = settingService.getSetting("store", "buy_self_auth");
		if (!"1".equals(CAN_BY_SELF)) {
			if (memberId != null) {
				for (CartItem cartItem : cartItemList) {
					if (cartItem != null && cartItem.getIs_check() == 1) {
						Member belongTo = memberManager.getByGoodsId(cartItem.getGoods_id());
						if (belongTo != null && belongTo.getMember_id().equals(memberId)) {
							throw new RuntimeException("抱歉！您不能购买自己的商品：" + cartItem.getName() + "。");
						}
					}
				}
			}
		}
		// 调用核心api计算总订单的价格，商品价：所有商品，商品重量：
		OrderPrice orderPrice = cartManager.countPrice(cartItemList, order.getShipping_id(), "" + order.getRegionid());
		
		// 激发总订单价格事件
		orderPrice = countMainOrderPrice(orderPrice,memberId,order);
		
		//使用积分
		int totalPoint  = 0;
		int canUsePoint = 0;
		double pointDiscount = 0.0d;
		
		
		String ticket = CommonRequest.getReqTicket();
		if(usePoint){
			try {
				totalPoint = crmPointManager.queryMemberPoint(ticket);//计算抵扣的积分
				canUsePoint = PointUtils.canUsePoint(orderPrice.getGoodsPrice(), totalPoint);
				pointDiscount = PointUtils.usePointFacDiscount(canUsePoint);
				order.setConsumepoint(canUsePoint);//设置消费积分
				orderPrice.setNeedPayMoney(CurrencyUtil.sub(orderPrice.getNeedPayMoney(), pointDiscount));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// 设置订单价格，自动填充好各项价格，商品价格，运费等
		order.setOrderprice(orderPrice);
		order.setWeight(orderPrice.getWeight());
		// 调用核心api创建主订单
		Order mainOrder = null;
		try {
			mainOrder = this.OrderFlowManager.add(order, new ArrayList<CartItem>(), memberId);
		} catch (Exception e) {
			throw new RuntimeException("创建订单失败，您购买的商品库存不足");
		}
		// 创建子订单
		this.createChildOrder(mainOrder, memberId);
		// 创建完子订单再清空
		cartManager.clean(memberId);//创建完订单扣除积分
		
		//创建完订单扣除积分
		if(canUsePoint !=0){
			crmPointManager.subMemberPointByTicket(ticket, canUsePoint, mainOrder.getSn());
		}
		// 返回主订单
		return mainOrder;
	}
	
	
	/**
	 * 创建店铺子订单
	 * @param order 主订单
	 * @param sessionid 用户sessionid 
	 * @param shippingIds 配送方式数组,是按在结算页中的店铺顺序形成
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	private void createChildOrder(Order order,Integer member_id) {
		//获取以店铺id分类的购物车列表 
		List<Map> storeGoodsList = apiCartManager.countSelectPrice("yes", member_id, order.getAddress_id());
		int num=1;
		//以店铺分单位循环购物车列表
		for (Map map : storeGoodsList) {
			//当前店铺的配送方式 
			Integer shippingId = (Integer)map.get(StoreCartKeyEnum.shiptypeid.toString());
			//先将主订单的信息copy一份
			StoreOrder storeOrder =this.copyOrder(order);
			//如果copy属性异常，则抛出异常
			if(storeOrder==null){
				throw new RuntimeException("创建子订单出错，原因为：beanutils copy属性出错。");
			}
			//获取此店铺id
			int store_id =(Integer)map.get(StoreCartKeyEnum.store_id.toString());
			//获取店铺名称
			String store_name =(String) map.get(StoreCartKeyEnum.store_name.toString());
			//设置订单为未结算
			storeOrder.setBill_status(0);
			//设置店铺的id
			storeOrder.setStore_id(store_id);
			//店铺名称
			storeOrder.setStore_name(store_name);
			//配送方式id
			storeOrder.setShipping_id(shippingId);
			//设置父订id
			storeOrder.setParent_id(order.getOrder_id());
			//取得此店铺的购物列表
			List itemlist=(List) map.get(StoreCartKeyEnum.goodslist.toString());
			//调用核心api计算总订单的价格，商品价：所有商品，商品重量：
			OrderPrice orderPrice  =(OrderPrice)map.get(StoreCartKeyEnum.storeprice.toString());
			//如果订单中的优惠金额不为空或者不等于0，那么订单的实际付款金额要减去此优惠金额 add_by DMRain 2016-7-28
//			if (orderPrice.getDiscountPrice() != null && orderPrice.getDiscountPrice() != 0) {
//				// 计算需要支付的金额
//				orderPrice.setNeedPayMoney(CurrencyUtil.sub(
//						orderPrice.getNeedPayMoney(), orderPrice.getDiscountPrice()));
//			}
			//设置订单价格，自动填充好各项价格，商品价格，运费等
			storeOrder.setOrderprice(orderPrice); 			
			// 设置为子订单
			storeOrder.setIs_child_order(true);
			storeOrder.setSn(order.getSn()+"-"+num);
			storeOrder.setConsumepoint(PointUtils.countStoreConsumePoint(storeOrder.getGoods_amount(),
					order.getGoods_amount(), order.getConsumepoint()));
			storeOrder.setNeed_pay_money(CurrencyUtil.sub(storeOrder.getNeed_pay_money(),
					PointUtils.usePointFacDiscount(storeOrder.getConsumepoint())));
			//调用订单核心类创建子订单
			this.OrderFlowManager.add(storeOrder, itemlist, member_id);
			
			//如果订单属于子订单
			if (storeOrder.getParent_id() != null) {
			 MemberCoupons memberCoupons = memberCouponsManager.getCheckCoupons(member_id,storeOrder.getStore_id());
				//如果优惠券集合不为空
				if (map != null) {
						//如果会员优惠券不为空
						if (memberCoupons != null) {
							if (memberCoupons.getStore_id().equals(storeOrder.getStore_id())) {
								int mcoup_id = memberCoupons.getMcoup_id();
								int coupons_id = memberCoupons.getCoupons_id();
								this.memberCouponsManager.useCoupons(mcoup_id, coupons_id, storeOrder.getMember_id(), storeOrder.getOrder_id(), storeOrder.getSn());
								
								//添加订单拓展信息
								OrderMeta meta = new OrderMeta();
								meta.setOrderid(storeOrder.getOrder_id());
								meta.setMeta_key(OrderMetaEnumKey.coupons_discount.toString());
								meta.setMeta_value(storeOrder.getDiscount().toString());
								this.orderMetaManager.add(meta);
							}
						}
				}
			}
			
			num++;
		}
	}
	
	/**
	 * copy一个订单的属性 生成新的订单
	 * @param order  主订单
	 * @return 新的子订单
	 */
	private StoreOrder copyOrder(Order order){
		StoreOrder store_order = new StoreOrder();
		try {
			BeanUtils.copyProperties(store_order,order);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return store_order;
	}
	
	
public OrderPrice countMainOrderPrice(OrderPrice orderprice,Integer member_id,Order order) {
		//购物车列表，按店铺分类的。
	   List<Map> list = apiCartManager.countSelectPrice("yes", member_id, order.getAddress_id());
		if(list==null) return orderprice;
		//订单总计
		double orderTotal =0D;
		//商品总计
		double goodsTotal =0D;
		//运费总计
		double shipTotal=0D;
		//优惠总计
		double disTotal=0D;
		/** 促销总计 add by DMRain 2016-1-11 */
        double promotionTotal=0D;
        /** 促销免邮总计 add by DMRain 2016-6-28 */
        double freeShipTotal=0D;
		//应付总计
		double payTotal =0D;
		for (Map map : list) {
			OrderPrice storeOrderPrice  =(OrderPrice) map.get(StoreCartKeyEnum.storeprice.toString());
			//累计订单总价
			Double storeOrderTotal = storeOrderPrice.getOrderPrice();
			orderTotal=CurrencyUtil.add(orderTotal, storeOrderTotal);
			//累计商品总价
			Double storeGoodsTotal = storeOrderPrice.getGoodsPrice();
			goodsTotal= CurrencyUtil.add(goodsTotal, storeGoodsTotal);
			//累计运费总价
			Double orderShipTotal = storeOrderPrice.getShippingPrice();
			shipTotal= CurrencyUtil.add(shipTotal, orderShipTotal);
			//累计优惠总价
			Double storeDisTotal = storeOrderPrice.getDiscountPrice();
			disTotal=CurrencyUtil.add(disTotal, storeDisTotal);
			//累计促销总价  add by DMRain 2016-1-11
            Double storePromotionTotal = storeOrderPrice.getActDiscount();
            //如果店铺促销活动减价为空 add by DMRain 2016-1-11
            if (storePromotionTotal == null) {
            	storePromotionTotal = 0D;
            }
            promotionTotal = CurrencyUtil.add(promotionTotal, storePromotionTotal);
			
            //促销活动免运费总计 add by DMRain 2016-6-28
            Double storeFreeShipTotal = storeOrderPrice.getAct_free_ship();
            //如果店铺促销活动免运费为空 add by DMRain 2016-6-28
            if (storeFreeShipTotal == null) {
				storeFreeShipTotal = 0D;
			}
            freeShipTotal = CurrencyUtil.add(freeShipTotal, storeFreeShipTotal); //促销活动免运费总计 add by DMRain 2016-6-28
            payTotal = CurrencyUtil.sub(payTotal, storeFreeShipTotal); //应付总计应该减去店铺促销活动减免的运费 add by DMRain 2016-6-28
            shipTotal = CurrencyUtil.add(shipTotal, storeFreeShipTotal); //运费总计应该加上店铺促销活动减免的运费 add by DMRain 2016-6-28
            
			//累计应付总价
			Double storePayTotal = storeOrderPrice.getNeedPayMoney();
			payTotal=CurrencyUtil.add(payTotal, storePayTotal);
//			payTotal=CurrencyUtil.sub(payTotal, storeDisTotal);
		}
		orderprice.setDiscountPrice(disTotal);
		orderprice.setActDiscount(promotionTotal); // add by DMRain 2016-1-11
		orderprice.setAct_free_ship(freeShipTotal); //add by DMRain 2016-6-28
		orderprice.setGoodsPrice(goodsTotal);
		orderprice.setNeedPayMoney(payTotal);
		orderprice.setOrderPrice(orderTotal);
		orderprice.setShippingPrice(shipTotal);
		return orderprice;
	}


	public List<Map> getCodeByParam(String itemId){
		StringBuffer sql = new StringBuffer();
		sql.append(" select s.* ");
		sql.append(" from  es_goods_servicegoods s , ");
		sql.append(" es_order_items o where s.item_id = o.item_id and s.item_id = ? ");
		Integer count = this.daoSupport.queryForInt(
				"select count(service_id) from  es_goods_servicegoods s, es_order_items o where s.item_id = o.item_id and s.item_id = ?",
				itemId);
		if (count <= 0) {// 判断有没有相关数据
			return null;
		}
		System.out.println(itemId);
		System.out.println(sql.toString());
		List<Map> sGoodsList = this.daoSupport.queryForList(sql.toString(),itemId);
		return sGoodsList;
	}



	public static List transMap2Bean2(List<Map<String, Object>> mapList, Class clazz) {
		List list = new ArrayList();
		if (mapList == null) {
			return null;
		}
		try {
			for (Map map : mapList) {
				Object o = clazz.newInstance();
				BeanUtils.populate(o, map);
				list.add(o);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
		
	
	public static String converstatus(SellBackList sellBack){
		String status = "";
		if(sellBack.getType() == 1){
			status =  SellerStatus.valueOf(sellBack.getTradestatus());
		}
		if(sellBack.getType() == 2){
			status =  RefundStatus.valueOf(sellBack.getTradestatus());
		}
		return status;
	}
	


}
