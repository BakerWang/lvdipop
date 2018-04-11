/**
 * 
 */
package com.enation.app.shop.component.member.plugin.cart;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.base.core.model.Member;
import com.enation.app.shop.core.member.plugin.IMemberLoginEvent;
import com.enation.app.shop.core.order.model.Cart;
import com.enation.app.shop.core.order.model.support.CartItem;
import com.enation.app.shop.core.order.service.impl.CartManager;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.plugin.AutoRegisterPlugin;

/**
 * 购物车“记住我”插件
 * @author kingapex
 * @version [v1.0 2015年12月22日]
 * @since v6.0
 */

@Component
public class CartRememberMePlugin extends AutoRegisterPlugin implements IMemberLoginEvent {

	@Autowired
	private IDaoSupport daoSupport;
	@Autowired
	private CartManager cartManager;


	/**
	 * 当会员登陆时更新cart表中的member_id字段，以便按会员记录
	 */
	@Override
	public void onLogin(Member member, Long upLogintime) {

		String sessionid  = ThreadContextHolder.getHttpRequest().getSession().getId();
		//获取在未登录情况下购物车物品
		List<Cart> seCart=(List<Cart>) this.daoSupport.queryForList("select * from es_cart WHERE session_id=? ", Cart.class, sessionid);
		//获取本用户购物车的所有物品
		List<CartItem> carts=this.cartManager.listGoods(sessionid);
		//循环判断此用户购物车是否有本件商品  有的话原购物车num+1 删除未登录购物车此商品
		for (int i = 0; i < seCart.size(); i++) {
			for (int j = 0; j < carts.size(); j++) {
				if(carts.get(j).getGoods_id().equals(seCart.get(i).getGoods_id())){
					//得到相同商品num的总和
					int num=carts.get(j).getNum()+seCart.get(i).getNum();
					//更新远购物车数量
					this.daoSupport.execute("update es_cart set num=? where member_id=? and goods_id=?", num,member.getMember_id(),carts.get(j).getGoods_id());
					//删除此session中的商品
					this.daoSupport.execute("delete from es_cart where session_id=? and goods_id=?", sessionid,carts.get(j).getGoods_id());
				}
			}
		}

		//修改不相同商品的member_id
		this.daoSupport.execute("update es_cart set member_id=? where session_id=? and member_id is NULL ", member.getMember_id(),sessionid);

	}

}
