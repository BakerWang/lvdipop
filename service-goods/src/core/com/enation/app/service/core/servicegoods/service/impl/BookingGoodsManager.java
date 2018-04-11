package com.enation.app.service.core.servicegoods.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.service.core.servicegoods.model.BookingGoods;
import com.enation.app.service.core.servicegoods.service.IBookingGoodsManager;
import com.enation.app.shop.core.goods.plugin.GoodsPluginBundle;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.util.StringUtil;

/**
 * 
 * @ClassName: BookingGoodsManager 
 * @Description: 预约商品管理
 * @author: liuyulei
 * @date: 2016年9月23日 上午10:03:07 
 * @since:v61
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@Service("bookingGoodsManager")
public class BookingGoodsManager implements IBookingGoodsManager {

	
	@Autowired
	private IDaoSupport daoSupport;
	
	@Autowired
	private IStoreMemberManager storeMemberManager;
	
	@Autowired
	private GoodsPluginBundle goodsPluginBundle;
	
	
	/* (non Javadoc) 
	 * @Title: add
	 * @Description: TODO
	 * @param bookingGoods 
	 * @see com.enation.app.service.core.servicegoods.service.IBookingGoodsManager#add(com.enation.app.service.core.servicegoods.model.BookingGoods) 
	 * @author： liuyulei
	 * @date：2016年9月23日 上午10:04:04
	 */
	@Override
	public void add(BookingGoods bookingGoods) {
		this.daoSupport.insert("es_booking", bookingGoods);

	}

	/* (non Javadoc) 
	 * @Title: edit
	 * @Description: TODO
	 * @param bookingGoods 
	 * @see com.enation.app.service.core.servicegoods.service.IBookingGoodsManager#edit(com.enation.app.service.core.servicegoods.model.BookingGoods) 
	 * @author： liuyulei
	 * @date：2016年9月23日 上午10:10:35
	 */
	@Override
	public void edit(BookingGoods bookingGoods) {
		this.daoSupport.update("es_booking", bookingGoods, " booking_id = "+bookingGoods.getBooking_id());
	}

	/* (non Javadoc) 
	 * @Title: queryByBookingGoodsId
	 * @Description: TODO
	 * @param id
	 * @return 
	 * @see com.enation.app.service.core.servicegoods.service.IBookingGoodsManager#queryByBookingGoodsId(java.lang.Integer) 
	 * @author： liuyulei
	 * @date：2016年9月23日 上午10:10:41
	 */
	@Override
	public BookingGoods queryByBookingGoodsId(Integer id) {
		StringBuffer sql = new StringBuffer();
		sql.append("select * from es_booking where booking_id = ?");
		BookingGoods bookingGoods = (BookingGoods) this.daoSupport.queryForObject(sql.toString(), BookingGoods.class, id);
		return bookingGoods;
	}

	/* (non Javadoc) 
	 * @Title: queryListByMemberId
	 * @Description: TODO
	 * @param member_id
	 * @return 
	 * @see com.enation.app.service.core.servicegoods.service.IBookingGoodsManager#queryListByMemberId(java.lang.Integer) 
	 * @author： liuyulei
	 * @date：2016年9月23日 上午10:10:52
	 */
	@Override
	public List queryListByMemberId(Integer member_id) {
		StringBuffer sql = creatTempleteSQL();
		sql.append(" b.member_id = ?");
		List list = this.daoSupport.queryForList(sql.toString(), member_id);
		return list;
	}


	@Override
	public List queryListByStoreId(Integer store_id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non Javadoc) 
	 * @Title: searchBookingGoods
	 * @Description: TODO
	 * @param pageNo
	 * @param pageSize
	 * @param map
	 * @return 
	 * @see com.enation.app.service.core.servicegoods.service.IBookingGoodsManager#searchBookingGoods(java.lang.Integer, java.lang.Integer, java.util.Map) 
	 * @author： liuyulei
	 * @date：2016年10月8日 下午5:23:42
	 */
	@Override
	public Page searchBookingGoods(Integer pageNo, Integer pageSize, Map map) {
		/**
		 * 准备查询的参数，将来要转换为Object[] 传给jdbc
		 */
		List argsList  = new ArrayList();
		Integer member_id = Integer.valueOf((map.get("member_id")==null?"-1":map.get("member_id").toString()));//会员id
		Integer store_id = Integer.valueOf((map.get("store_id")==null?"-1":map.get("store_id").toString()));//店铺id
		Integer isStore = Integer.valueOf(map.get("isStore")==null ? "-1" : map.get("isStore").toString());
		String keyword = (map.get("keyword")==null?"":map.get("keyword")).toString();
		StringBuffer sql = creatTempleteSQL();
		this.goodsPluginBundle.onSearchFilter(sql);
		
		
		
		if(isStore == 1){  //判断   如果是商家中心搜索  
			if(store_id != -1){//根据会员id搜索该会员的预约信息
				sql.append(" and b.store_id = ?");
				argsList.add(store_id);
			}
		}else{ //如果是个人中心搜索
			if(member_id != -1){//根据会员id搜索该会员的预约信息
				sql.append(" and b.member_id = ?");
				argsList.add(member_id);
			}
		}
		
		
		if(!StringUtil.isEmpty(keyword)){
			sql.append(" and (g.name like ? or b.booking_name like ? or b.booking_mobile like ?)");
			argsList.add("%"+keyword+"%");
			argsList.add("%"+keyword+"%");
			argsList.add("%"+keyword+"%");
		}
		sql.append(" order by b.create_time desc");
		/**
		 * 将参数list 转为Object[]
		 */
		int size =argsList.size();
		Object[] args = argsList.toArray(new Object[size]);
		
		return this.daoSupport.queryForPage(sql.toString(), pageNo, pageSize, args);
	}
	
	
	/**
	 * 将po对象中有属性和值转换成map
	 * 
	 * @param po
	 * @return
	 */
	protected Map po2Map(Object po) {
		Map poMap = new HashMap();
		Map map = new HashMap();
		try {
			map = BeanUtils.describe(po);
		} catch (Exception ex) {
		}
		Object[] keyArray = map.keySet().toArray();
		for (int i = 0; i < keyArray.length; i++) {
			String str = keyArray[i].toString();
			if (str != null && !str.equals("class")) {
				if (map.get(str) != null) {
					poMap.put(str, map.get(str));
				}
			}
		}
		return poMap;
	}
	
	/**
	 * 
	 * @Title: creatTempleteSQL 
	 * @Description: TODO  创建sql
	 * @return
	 * @return: StringBuffer
	 * @author： liuyulei
	 * @date：2016年9月26日 下午4:50:09
	 */
	private StringBuffer creatTempleteSQL() {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append(" b.*, g.name,s.store_name, g.intro");
		sql.append(" FROM");
		sql.append(" es_booking b");
		sql.append(" LEFT JOIN es_goods g ON b.goods_id = g.goods_id");
		sql.append(" LEFT JOIN es_store s ON s.store_id = b.store_id");
		sql.append(" WHERE 1=1 ");
		return sql;
	}

}
