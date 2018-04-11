package com.enation.app.shop.mobile.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.enation.app.b2b2c.component.plugin.order.StoreCartPluginBundle;
import com.enation.app.b2b2c.core.goods.service.StoreCartKeyEnum;
import com.enation.app.b2b2c.core.order.model.cart.StoreCartItem;
import com.enation.app.b2b2c.core.order.service.cart.IStoreCartManager;
import com.enation.app.b2b2c.core.store.service.IStoreDlyTypeManager;
import com.enation.app.b2b2c.core.store.service.IStoreTemplateManager;
import com.enation.app.javashop.customized.core.service.IMemberCouponsManager;
import com.enation.app.shop.component.bonus.model.MemberCoupons;
import com.enation.app.shop.core.member.model.MemberAddress;
import com.enation.app.shop.core.member.service.IMemberAddressManager;
import com.enation.app.shop.core.order.model.support.OrderPrice;
import com.enation.app.shop.core.order.plugin.cart.CartPluginBundle;
import com.enation.app.shop.core.order.service.ICartManager;
import com.enation.app.shop.core.order.service.IDlyTypeManager;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.util.CurrencyUtil;

@Service
public class ApiCartManager{
	@Autowired
	private CartPluginBundle cartPluginBundle;
	@Autowired
	private IDlyTypeManager dlyTypeManager;
	@Autowired
	private IStoreDlyTypeManager storeDlyTypeManager;
	@Autowired
	private IStoreTemplateManager storeTemplateManager;
	@Autowired
	private IMemberAddressManager memberAddressManager;
	@Autowired
	private ICartManager cartManager;
	/** 店铺促销活动管理接口 by_DMRain 2016-6-20 */
//	@Autowired
//	private IStoreActivityManager storeActivityManager;
	/** 促销活动优惠详细管理接口 by_DMRain 2016-6-20 */
//	@Autowired
//	private IActivityDetailManager activityDetailManager;
	/** 店铺促销活动赠品管理接口 by_DMRain 2016-6-20 */
//	@Autowired
//	private IStoreActivityGiftManager storeActivityGiftManager;
	/**店铺购物车插件桩  add by jianghongyan 2016-7-1*/
	@Autowired
	private StoreCartPluginBundle storeCartPluginBundle;
	/** 促销活动管理接口 by_DMRain 2016-7-13 */
//	@Autowired
//	private IActivityManager activityManager;
//	@Autowired
//	private IStoreBonusManager storeBonusManager;
	@Autowired
	private IStoreCartManager storeCartManager;
	@Autowired
	private IMemberCouponsManager memberCouponsManager;
	@Autowired
	private IDaoSupport daoSupport;
	
	
	
	@SuppressWarnings("rawtypes")
	public List countPriceForCart(Integer member_id){
		List<Map> storeGoodsList=storeListGoodsAll(member_id);
		for(Map map : storeGoodsList){ 
			List list = (List) map.get(StoreCartKeyEnum.goodslist.toString());
			OrderPrice orderPrice =  this.cartManager.countPrice(list, null, null);
			this.storeCartPluginBundle.coutPrice(orderPrice, map);
			map.put(StoreCartKeyEnum.storeprice.toString(), orderPrice);
		} 
		return storeGoodsList;
	}
	
	
	@SuppressWarnings("rawtypes")
	public List countPrice(Integer member_id){
		List<Map> storeGoodsList=storeListGoods(member_id);
		for(Map map : storeGoodsList){ 
			List list = (List) map.get(StoreCartKeyEnum.goodslist.toString());
			OrderPrice orderPrice =  this.cartManager.countPrice(list, null, null);
			this.storeCartPluginBundle.coutPrice(orderPrice, map);
			map.put(StoreCartKeyEnum.storeprice.toString(), orderPrice);
		} 
		return storeGoodsList;
	}
	
	
	public void updateNum(Integer member_id, Integer cartid, Integer num) {
		try {
			String sql = "";
			//判断当前是否有会员登陆，如果有就按照member_id和cart_id修改，没有就按照sessionid和cart_id修改
			if (member_id != null) {
				//修改购物车数量
				sql = "update es_cart set num = ? where member_id = " + member_id + " and cart_id = ?";
			}
			this.daoSupport.execute(sql, num, cartid);
		}catch (RuntimeException e) {
			throw e;
		}
	}
	
	public void checkProduct(Integer member_id,Integer product_id, boolean check) {
		StringBuffer sql=new StringBuffer("update es_cart set is_check=? where product_id=?");
		this.cartPluginBundle.filterCheckProductSql(sql);
		if(member_id != null){
			sql.append(" and member_id=?");
			this.daoSupport.execute(sql.toString(), check==true?1:0,product_id,member_id);
		}
	}
	
	
	public void checkAll(Integer member_id, boolean check) {
		StringBuffer sql = new StringBuffer("update es_cart set is_check=? ");
		if (member_id != null) {
			sql.append(" where member_id=?");
			this.daoSupport.execute(sql.toString(), check == true ? 1 : 0, member_id);
		}
	}
	
	
	public List<Map> countSelectPrice(String isCountShip, Integer member_id, Integer addressId) {
		Integer regionid = null;
		if ("yes".equals(isCountShip)) {
			MemberAddress address = null;
			if (addressId != null) {
				address = memberAddressManager.getAddress(addressId);
			} else {
				address = memberAddressManager.getMemberDefault(member_id);
			}
			if (address != null) {
				regionid = address.getRegion_id();
			}
		}
		if(regionid == null){
			isCountShip = "no";
		}
		List<Map> storeGoodsList = storeListGoods(member_id);
		for (Map map : storeGoodsList) {
			List list = (List) map.get(StoreCartKeyEnum.goodslist.toString());
			OrderPrice orderPrice = this.cartManager.countPrice(list, null, null);
			// 店铺金额追加优惠券优惠
			int store_id = Integer.parseInt(map.get("store_id").toString());
			MemberCoupons memberCoupons = memberCouponsManager.getCheckCoupons(member_id, store_id);
			// //如果会员优惠券信息不为空
			 if (memberCoupons != null) {
			 this.selectCoupons(orderPrice, memberCoupons, store_id);
			 }
			this.storeCartPluginBundle.coutPrice(orderPrice, map);
			// 为店铺信息压入店铺的各种价格
			map.put(StoreCartKeyEnum.storeprice.toString(), orderPrice);
			// 如果指定计算运费，则计算每个店的的运费，并设置好配送方式列表，在结算页中此参数应该设置为yes
			if ("yes".equals(isCountShip)) {
				orderPrice = countShipPrice2(map, regionid);
				map.put(StoreCartKeyEnum.storeprice.toString(), orderPrice);
			}
		}
		return storeGoodsList;
	}
	
	public OrderPrice countShipPrice2(Map map,int regionid){
		System.out.println("countshipprice");
		//是否免运费
		boolean free_ship=false;
		int storeid= (Integer)map.get(StoreCartKeyEnum.store_id.toString());
		List<StoreCartItem> list = (List) map.get(StoreCartKeyEnum.goodslist.toString());
		Integer activity_id = (Integer) map.get(StoreCartKeyEnum.activity_id.toString());
		//先检测购物商品中是否有免运费的
		for (StoreCartItem storeCartItem : list) {
			//如果有一个商品免运费则此店铺订单免运费
			if (1== storeCartItem.getGoods_transfee_charge()){
				free_ship=true;
				break;
			}
		}
		//取出之前计算好的订单价格
		OrderPrice orderPrice = (OrderPrice)map.get(StoreCartKeyEnum.storeprice.toString());
		//得到商品总计和重量，以便计算运费之用
		Double goodsprice = orderPrice.getGoodsPrice();
		Double weight = orderPrice.getWeight();
		//生成配送方式列表，此map中已经含有计算后的运费
		List<Map> shipList  = this.getShipTypeList(storeid, regionid, weight, goodsprice);
		//向店铺信息中压入配送方式列表
		map.put(StoreCartKeyEnum.shiptype_list.toString(),shipList);
		//如果免运费，向配送方式列表中加入免运费项
		if(free_ship ||shipList.isEmpty()){
			//修改逻辑问题 edit by jianghongyan
				//生成免运费项
				Map freeType = new HashMap();
				freeType.put("type_id", 0);
				freeType.put("name", "免运费");
				freeType.put("shipPrice", 0);
				//将免运费项加入在第一项
				shipList.add(0, freeType);
				//设置运费价格和配送方式id
				orderPrice.setShippingPrice(0d);
				map.put(StoreCartKeyEnum.shiptypeid.toString(), 0);
		}else{
			//如果不免运费用第一个配送方式 设置运费价格和配送方式id
			Map firstShipType =shipList.get(0);
			Double shipprice = (Double)firstShipType.get("shipPrice");
			orderPrice.setShippingPrice(shipprice);
			//订单总费用为商品价格+运费
			double orderTotal = CurrencyUtil.add(goodsprice, shipprice);
			orderPrice.setOrderPrice(orderTotal);
			orderPrice.setNeedPayMoney(CurrencyUtil.add(orderPrice.getNeedPayMoney(), shipprice));
			map.put(StoreCartKeyEnum.shiptypeid.toString(), firstShipType.get("type_id"));
		}
//		orderPrice.setNeedPayMoney(CurrencyUtil.add(orderPrice.getOrderPrice(),-orderPrice.getDiscountPrice()));
//		orderPrice.setNeedPayMoney(orderPrice.getOrderPrice());
		return orderPrice;
	}
	
	
	
	public List<Map> storeListGoods(Integer member_id) {
		List<Map> storeGoodsList = new ArrayList<Map>();
		List<StoreCartItem> goodsList = new ArrayList();
		goodsList = storeCartManager.listGoods(member_id);
		for (StoreCartItem item : goodsList) {
			item.setImage_default(StaticResourcesUtil.convertToUrl((String) item.getImage_default()));
			findStoreMap(storeGoodsList, item);
		}
		return storeGoodsList;
	}
	
	
	public List<Map> storeListGoodsAll(Integer member_id) {
		List<Map> storeGoodsList= new ArrayList<Map>();
		List<StoreCartItem> goodsList =new ArrayList();
		goodsList= storeCartManager.listGoodsAll(member_id);
		for (StoreCartItem item : goodsList) {
			item.setImage_default(StaticResourcesUtil.convertToUrl((String) item.getImage_default()));
			findStoreMap(storeGoodsList, item);
		}
		return storeGoodsList;
	}
	
	/**
	 * 获取店铺商品列表
	 * @param storeGoodsList
	 * @param map
	 * @param StoreCartItem
	 * @return list<Map>
	 */
	private void findStoreMap(List<Map> storeGoodsList,StoreCartItem item){
		int is_store=0;
		if (storeGoodsList.isEmpty()){
			addGoodsList(item, storeGoodsList);
		}else{
			for (Map map: storeGoodsList) {
				if(map.containsValue(item.getStore_id())){
					List list=(List) map.get(StoreCartKeyEnum.goodslist.toString());
					list.add(item);
					is_store=1;
				}
			}
			if(is_store==0){
				addGoodsList(item, storeGoodsList);
			}
		}
	}

	/**
	 * @param item
	 * @param storeGoodsList
	 */
	@SuppressWarnings("rawtypes")
	private void addGoodsList(StoreCartItem item,List<Map> storeGoodsList){
		Map map=new HashMap();
		List list=new ArrayList();
		list.add(item);
		map.put(StoreCartKeyEnum.store_id.toString(), item.getStore_id());
		map.put(StoreCartKeyEnum.store_name.toString(), item.getStore_name());
		map.put(StoreCartKeyEnum.goodslist.toString(), list);
		storeGoodsList.add(map);
	}
	
	
	
	
	
	private List<Map> getShipTypeList(int storeid,int regionid,double weight,double goodsprice){
		List<Map> newList  = new ArrayList();
		if(Double.valueOf(weight)!=0d){
			Integer tempid = this.storeTemplateManager.getDefTempid(storeid);
			List<Map> list =   this.storeDlyTypeManager.getDlyTypeList(tempid);
			for(Map maps:list){
				Map newMap = new HashMap();
				String name = (String)maps.get("name");
				Integer typeid = (Integer) maps.get("type_id");
				Double[] priceArray = this.dlyTypeManager.countPrice(typeid, Double.valueOf(weight), goodsprice, regionid+"");
				Double dlyPrice = priceArray[0];//配送费用
				newMap.put("name", name);
				newMap.put("type_id", typeid);
				newMap.put("shipPrice", dlyPrice);
				newList.add(newMap);
			}
		}
		return newList;
	}
	
	/**
	 * 新版使用优惠券功能
	 * @author DMRain 2016-10-10
	 * @param orderprice 订单各项价格
	 * @param memberCoupons 优惠券信息
	 * @param storeid 店铺id
	 * @return
	 */
	private OrderPrice selectCoupons(OrderPrice orderprice, MemberCoupons memberCoupons,
			Integer storeid) {
		Integer coupons_type = memberCoupons.getMcoup_type();
		//如果优惠券类型为满减券或者现金券
		if (coupons_type.equals(0) || coupons_type.equals(2)) {
			Double coupons_money = memberCoupons.getMcoup_money();
			// 如果优惠券面额大于商品价格的话 那么优惠价格为商品价格
			if (orderprice.getNeedPayMoney().doubleValue() <= coupons_money.doubleValue()) {
				orderprice.setDiscountPrice(orderprice.getNeedPayMoney());
				orderprice.setNeedPayMoney(0.0);
			} else {
				// 计算需要支付的金额
				orderprice.setNeedPayMoney(CurrencyUtil.add(orderprice.getNeedPayMoney(), -coupons_money));

				orderprice.setDiscountPrice(coupons_money);
			}
		}
		//如果优惠券类型为折扣券
		if (coupons_type.equals(1)) {
			Integer coupons_discount = memberCoupons.getMcoup_discount();
			double discountPercent = coupons_discount / 100.00;
			double discountPrice = orderprice.getNeedPayMoney() - (orderprice.getNeedPayMoney() * discountPercent);
			orderprice.setNeedPayMoney(orderprice.getNeedPayMoney() * discountPercent);
			orderprice.setDiscountPrice(discountPrice);
		}
		return orderprice;

	}

	
	
	
}
