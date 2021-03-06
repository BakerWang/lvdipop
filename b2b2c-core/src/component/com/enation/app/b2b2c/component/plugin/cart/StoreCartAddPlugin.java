package com.enation.app.b2b2c.component.plugin.cart;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.b2b2c.core.order.service.cart.IStoreCartManager;
import com.enation.app.shop.core.goods.model.Product;
import com.enation.app.shop.core.goods.service.IGoodsManager;
import com.enation.app.shop.core.order.model.Cart;
import com.enation.app.shop.core.order.plugin.cart.ICartAddEvent;
import com.enation.app.shop.core.order.plugin.cart.ICartUpdateEvent;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.plugin.AutoRegisterPlugin;
import com.enation.framework.util.StringUtil;
@Component
public class StoreCartAddPlugin extends AutoRegisterPlugin implements ICartAddEvent, ICartUpdateEvent{
	
	@Autowired
	private IDaoSupport daoSupport;
	
	@Autowired
	private IGoodsManager goodsManager;
	
	@Autowired
	private IStoreCartManager storeCartManager;
	
	@Override
	public void add(Cart cart) {
		
	}

	@Override
	public void afterAdd(Cart cart) {
		try {
			// 购物车添加店铺Id
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			String store_id = request.getParameter("store_id");
			
			// 如果没有通过request传递过来 就自行查询 20150824 _ add _ by _ Sylow
			if (store_id == null || "".equals(store_id)) {
				int goodsId = cart.getGoods_id();
				Map goods = goodsManager.get(goodsId);
				String storeId = goods.get("store_id").toString();
				store_id = storeId;
			}
			
			String sql = "update es_cart set store_id = ?";
			sql += " where cart_id = ?";
			daoSupport.execute(sql, store_id, cart.getCart_id());
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.plugin.cart.ICartUpdateEvent#onUpdate(java.lang.String, java.lang.Integer)
	 */
	@Override
	public void onUpdate(String sessionId, Integer cartId) {
		
		//重新计算价格
		storeCartManager.countPrice("no");
	}


	@Override
	public void onBeforeUpdate(String sessionId, Integer cartId, Integer num) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAddProductToCart(Product product, Integer num) {
		// TODO Auto-generated method stub
		
	}

}
