package com.enation.app.b2b2c.core.order.service.cart;

import java.util.List;
import java.util.Map;

import com.enation.app.b2b2c.core.order.model.cart.StoreCartItem;

/**
 * 店铺购物车管理类
 * @author LiFenLong
 *
 */
public interface IStoreCartManager {
	public static final String FILTER_KEY = "cartFilter"; 
	
	/**
	 * 根据session信息计算费用<br>
	 * 计算结果要通过{@link StoreCartContainer#getStoreCartListFromSession()} 获取到
	 * @param isCountShip 是否计算费用
	 */
	public void countPrice(String isCountShip);
	
	/**
	 * 根据session信息计算费用<br>
	 * 计算结果要通过{@link StoreCartContainer#getStoreCartListFromSession()} 获取到
	 * @param isCountShip 是否计算费用
	 */
	public void countSelectPrice(String isCountShip );
	
	
	/**
	 * 获取购物车商品列表
	 * @param sessionid
	 * @return List<StoreCartItem>
	 */
	public List<StoreCartItem> listGoods(String sessionid);
	
	
	/**
	 * 获取选中购物车商品列表
	 * @param member_id
	 * @return List<StoreCartItem>
	 */
	public List<StoreCartItem> listGoods(Integer member_id);
	
	
	/**
	 * 获取所有购物车商品列表信息
	 * @param member_id
	 * @return
	 */
	public List<StoreCartItem> listGoodsAll(Integer member_id);
 

	
	/**
	 * 获取分店铺购物车列表<br>
	 * 
	 * @param sessionid
	 * @return 返回的list中map结构如下：<br>
	 * <li>key为store_id的值为店铺id</li>
	 * <li>key为store_name的值为店铺名称</li>
	 * <li>key为goodslist为此店铺的购物车列表</li>
	 * <li>key为storeprice为此店铺的价格对像 {@link OrderPrice}</li>
	 * @see StoreCartKeyEnum
	 */
	public List<Map> storeListGoods(String sessionid);
	
	
	
	
	/**
	 * 清除购物车
	 * @param sessionid
	 */
	public void  clean(String sessionid);
	
	/**
	 * 修改购物车中价格，根据货品id
	 * @param product_id
	 */
	public void updatePriceByProductid(Integer product_id,Double price);
	
	
	public void checkStoreAll(String sessionid,boolean checked,Integer store_id);
	
	public void checkStoreAll(Integer member_id, boolean checked, Integer store_id);
}
