package com.enation.app.service.core.servicegoods.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.app.service.core.servicegoods.model.ServiceGoods;
import com.enation.app.service.core.servicegoods.service.IServiceGoodsManager;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.util.StringUtil;

/**
 * 
 * @ClassName: ServiceGoodsManager
 * @Description: 服务商品实现类
 * @author: liuyulei
 * @date: 2016年9月14日 下午4:51:20
 * @since:v61
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@Service("serviceGoodsManager")
public class ServiceGoodsManager implements IServiceGoodsManager {

	@Autowired
	private IDaoSupport daoSupport;

	/*
	 * (non Javadoc)
	 * 
	 * @Title: add
	 * 
	 * @Description: TODO
	 * 
	 * @param sGoods
	 * 
	 * @see
	 * com.enation.app.service.core.servicegoods.service.IServiceGoodsManager#
	 * add(com.enation.app.service.core.servicegoods.model.ServiceGoods)
	 * 
	 * @author： liuyulei
	 * 
	 * @date：2016年9月14日 下午3:43:52
	 */
	@Override
	public void add(ServiceGoods sGoods) {
		this.daoSupport.insert("es_goods_servicegoods", sGoods);
	}

	/*
	 * (non Javadoc)
	 * 
	 * @Title: edit
	 * 
	 * @Description: TODO
	 * 
	 * @param sGoods
	 * 
	 * @see
	 * com.enation.app.service.core.servicegoods.service.IServiceGoodsManager#
	 * edit(com.enation.app.service.core.servicegoods.model.ServiceGoods)
	 * 
	 * @author： liuyulei
	 * 
	 * @date：2016年9月14日 下午4:21:39
	 */
	@Override
	public void edit(ServiceGoods sGoods) {
		this.daoSupport.update("es_goods_servicegoods", sGoods, "code = " + sGoods.getCode());

	}

	/*
	 * (non Javadoc)
	 * 
	 * @Title: getCodeByParam
	 * 
	 * @Description: TODO
	 * 
	 * @param itemId
	 * 
	 * @return
	 * 
	 * @see
	 * com.enation.app.service.core.servicegoods.service.IServiceGoodsManager#
	 * getCodeByParam(java.lang.Integer)
	 * 
	 * @author： liuyulei
	 * 
	 * @date：2016年9月14日 下午4:21:46
	 */
	@Override
	public List getCodeByParam(Integer itemId) {
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
		List<ServiceGoods> sGoodsList = this.daoSupport.queryForList(sql.toString(), ServiceGoods.class, itemId);
		return sGoodsList;
	}

	/*
	 * (non Javadoc)
	 * 
	 * @Title: checkCode
	 * 
	 * @Description: TODO
	 * 
	 * @param code
	 * 
	 * @return
	 * 
	 * @see
	 * com.enation.app.service.core.servicegoods.service.IServiceGoodsManager#
	 * checkCode(java.lang.String)
	 * 
	 * @author： liuyulei
	 * 
	 * @date：2016年9月14日 下午4:21:50
	 */
	@Override
	public Map checkCode(String code) {
		StringBuffer sql = new StringBuffer();
		sql.append(" select s.* ");
		sql.append(" from  es_goods_servicegoods s  ");
		sql.append(" where s.code = ? ");
		Integer count = this.daoSupport
				.queryForInt("select count(service_id) from  es_goods_servicegoods s where code = ? ", code);
		if (count > 0) {// 判断没有相关数据
			return this.daoSupport.queryForMap(sql.toString(), code);
		} else {
			return null;
		}
	}

	/*
	 * (non Javadoc)
	 * 
	 * @Title: used
	 * 
	 * @Description: TODO
	 * 
	 * @param code
	 * 
	 * @param status
	 * 
	 * @return
	 * 
	 * @see
	 * com.enation.app.service.core.servicegoods.service.IServiceGoodsManager#
	 * used(java.lang.String, java.lang.Integer)
	 * 
	 * @author： liuyulei
	 * 
	 * @date：2016年9月14日 下午4:22:05
	 */
	@Override
	public Map used(String code, Integer status) {
		StringBuffer sql = new StringBuffer();
		sql.append("update es_goods_servicegoods set status = ? where code = ?");
		this.daoSupport.execute(sql.toString(), status, code);

		Integer count = this.daoSupport.queryForInt(
				"select count(service_id) from  es_goods_servicegoods s where code = ? and status = ?", code, status);
		if (count > 0) {// 判断是否有相关数据 如果存在
			StringBuffer sqlS = new StringBuffer();
			sqlS.append(" select s.* ");
			sqlS.append(" from  es_goods_servicegoods s ");
			sqlS.append(" where s.code = ? ");
			return this.daoSupport.queryForMap(sqlS.toString(), code);
		} else {
			return null;
		}
	}

	/*
	 * (non Javadoc)
	 * 
	 * @Title: list
	 * 
	 * @Description: TODO
	 * 
	 * @return
	 * 
	 * @see
	 * com.enation.app.service.core.servicegoods.service.IServiceGoodsManager#
	 * list()
	 * 
	 * @author： liuyulei
	 * 
	 * @date：2016年9月18日 下午1:11:28
	 */
	@Override
	public List<Map> queryIsEnable() {
		StringBuffer sql = new StringBuffer();
		sql.append(
				"select s.*,FROM_unixtime(s.enable_time)>SYSDATE() as isEnable from es_goods_servicegoods s where s.status <> 2");// 状态为2
																																	// 表示超过有效期
		return this.daoSupport.queryForList(sql.toString());

	}

	/*
	 * (non Javadoc)
	 * 
	 * @see
	 * com.enation.app.service.core.servicegoods.service.IServiceGoodsManager#
	 * searchServiceGoods(java.util.Map, int, int, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 * 
	 * @author： liuyulei
	 * 
	 * @date：2016年9月18日 下午5:20:19
	 */
	@Override
	public Page searchServiceGoods(Map goodsMap, int page, int pageSize, String other, String sort, String order) {
		StringBuffer sql = this.createServiceGoodsListSQL();
		String keyword = (String) goodsMap.get("keyword");

		if (StringUtil.isEmpty(sort)) {
			sort = " create_time";
		}

		if (StringUtil.isEmpty(order)) {
			order = " desc";
		}

		if (StringUtil.isEmpty(other)) {
			other = " ";
		}
		if (!StringUtil.isEmpty(keyword)) {
			sql.append(" and ((gs.code like '%" + keyword + "%') ");
			sql.append(" or (g.name like  '%" + keyword + "%') ");
			sql.append(" or (s.store_name like '%" + keyword + "%')) ");
		}

		sql.append(" and s.store_id = 1");
		sql.append(" order by " + sort + " " + order);

		Page webpage = this.daoSupport.queryForPage(sql.toString(), page, pageSize);

		return webpage;
	}

	@Override
	public Page serviceGoodsList(Integer pageNo, Integer pageSize, Map map) {

		String keyword = String.valueOf(map.get("keyword"));
		String store_id = String.valueOf(map.get("store_id"));

		StringBuffer sql = this.createServiceGoodsListSQL();

		if (!StringUtil.isEmpty(keyword)) {
			sql.append(" and ((gs.code like '%" + keyword + "%') ");
			sql.append(" or (g.name like  '%" + keyword + "%') ");
			sql.append(" or (s.store_name like '%" + keyword + "%')) ");
		}

		if (!StringUtil.isEmpty(store_id)) {
			sql.append(" and s.store_id = " + store_id);
		}

		return this.daoSupport.queryForPage(sql.toString(), pageNo, pageSize);
	}

	/*
	 * (non Javadoc)
	 * 
	 * @Title: getServiceGoods
	 * 
	 * @Description: TODO
	 * 
	 * @param map
	 * 
	 * @return
	 * 
	 * @see
	 * com.enation.app.service.core.servicegoods.service.IServiceGoodsManager#
	 * getServiceGoods(java.util.Map)
	 * 
	 * @author： liuyulei
	 * 
	 * @date：2016年9月19日 下午2:51:44
	 */
	@Override
	public Map getServiceGoods(Map map) {
		StringBuffer sql = new StringBuffer();
		sql.append("select * from es_goods_servicegoods s where s.service_id = ?");
		Integer service_id = Integer.parseInt(map.get("service_id").toString());
		return this.daoSupport.queryForMap(sql.toString(), service_id);

	}

	private StringBuffer createServiceGoodsListSQL() {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT gs.service_id, ");
		sql.append("gs.`code` as code, ");
		sql.append("g.`name` as goods_name,g.goods_id as goods_id, ");
		sql.append("s.store_name as store_name, ");
		sql.append("m.uname as nickname, ");
		sql.append("gs.create_time as create_time, ");
		sql.append("gs.valid_term as yxq, ");
		sql.append("gs.enable_time as enable_time, ");
		sql.append("case gs.`status` when 0 then '未消费' when 1 then '已消费' when 2 then '超过有效期' when 3 then '作废' end as status ");
		sql.append("FROM ");
		sql.append("es_goods_servicegoods gs ");
		sql.append("LEFT JOIN es_order_items i ON gs.item_id = i.item_id ");
		sql.append("LEFT JOIN es_order o ON i.order_id = o.order_id ");
		sql.append("LEFT JOIN es_member m ON o.member_id = m.member_id ");
		sql.append("LEFT JOIN es_goods g ON i.goods_id = g.goods_id  ");
		sql.append("LEFT JOIN es_store s ON s.store_id = g.store_id where 1=1 ");
		return sql;
	}

	/* (non Javadoc) 
	 * @Title: seeCodeEdit
	 * @Description: TODO
	 * @param itemid 
	 * @see com.enation.app.service.core.servicegoods.service.IServiceGoodsManager#seeCodeEdit(java.lang.Integer) 
	 * @author： liuyulei
	 * @date：2016年9月20日 下午5:21:08
	 */
	@Override
	public void seeCodeEdit(Integer itemid) {
		Map map = new HashMap();
		map.put("look_status", 1);  //查看状态   无法修改
		this.daoSupport.update("es_goods_servicegoods", map, " item_id = "+itemid);;
	}

	/* (non Javadoc) 
	 * @Title: getCodeLsitByOrderId
	 * @Description: TODO
	 * @param orderId
	 * @return 
	 * @see com.enation.app.service.core.servicegoods.service.IServiceGoodsManager#getCodeLsitByOrderId(java.lang.Integer) 
	 * @author： liuyulei
	 * @date：2016年9月21日 下午2:04:22
	 */
	@Override
	public List getCodeLsitByOrderId(Integer orderId) {
		StringBuffer sql = new StringBuffer();
		sql.append("select s.* from es_goods_servicegoods s ,es_order_items i where s.item_id = i.item_id and i.order_id = ?");
		List list = this.daoSupport.queryForList(sql.toString(), orderId);
		
		return list;
	}

}
