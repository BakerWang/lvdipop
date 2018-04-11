package com.enation.app.b2b2c.core.store.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.app.b2b2c.core.store.model.StoreClassify;
import com.enation.app.b2b2c.core.store.service.IStoreClassifyManager;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.util.StringUtil;

/**
 * 
 * @ClassName: StoreClassifyManager
 * @Description: 店铺分类管理
 * @author: liuyulei
 * @date: 2016年10月10日 下午6:17:57
 * @since:v61
 */
@Service
public class StoreClassifyManager implements IStoreClassifyManager {

	@Autowired
	private IDaoSupport daoSupport;

	@Override
	public void add(StoreClassify storeClassify) {
		this.daoSupport.insert("es_store_classify", storeClassify);
	}

	/*
	 * (non Javadoc)
	 * 
	 * @Title: edit
	 * 
	 * @Description: TODO
	 * 
	 * @param storeClassify
	 * 
	 * @see
	 * com.enation.app.javashop.customized.core.service.IStoreClassifyManager#
	 * edit(com.enation.app.javashop.customized.core.model.StoreClassify)
	 * 
	 * @author： liuyulei
	 * 
	 * @date：2016年10月10日 下午7:08:18
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void edit(StoreClassify storeClassify) {
		this.daoSupport.update("es_store_classify", storeClassify,
				" sclassify_id = " + storeClassify.getSclassify_id());

	}

	/*
	 * (non Javadoc)
	 * 
	 * @Title: getStoreClassify
	 * 
	 * @Description: TODO
	 * 
	 * @param sclassify_id
	 * 
	 * @return
	 * 
	 * @see
	 * com.enation.app.javashop.customized.core.service.IStoreClassifyManager#
	 * getStoreClassify(java.lang.Integer)
	 * 
	 * @author： liuyulei
	 * 
	 * @date：2016年10月10日 下午7:08:23
	 */
	@Override
	public StoreClassify getStoreClassify(Integer sclassify_id) {
		StringBuffer sql = new StringBuffer();
		sql.append(" select * from es_store_classify where sclassify_id = ?");
		StoreClassify storeClassify = (StoreClassify) this.daoSupport
				.queryForObject(sql.toString(), StoreClassify.class,
						sclassify_id);
		return storeClassify;
	}

	/*
	 * (non Javadoc)
	 * 
	 * @Title: getStoreClassifyList
	 * 
	 * @Description: TODO
	 * 
	 * @param params
	 * 
	 * @return
	 * 
	 * @see
	 * com.enation.app.javashop.customized.core.service.IStoreClassifyManager#
	 * getStoreClassifyList(java.util.Map)
	 * 
	 * @author： liuyulei
	 * 
	 * @date：2016年10月10日 下午7:08:29
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List getStoreClassifyList(Map params) {
		String city_name = params.get("city_name") == null ? "" : params.get(
				"city_name").toString();
		Double longitude = Double
				.parseDouble(params.get("longitude") == null ? "0" : params
						.get("longitude").toString());
		Double latitude = Double
				.parseDouble(params.get("latitude") == null ? "0" : params.get(
						"latitude").toString());
		StringBuffer sql = new StringBuffer();
		sql.append("select s.sclassify_id,s.classify_name  from es_store_classify s  where 1=1  ");
		if (!StringUtil.isEmpty(city_name)) { // 判断城市名称是否为空
			sql.append(" and s.city_name like '%" + city_name + "%'");
		}

		/*
		 * 根据经纬度获取 分类列表
		 */
		
		List list = this.daoSupport.queryForList(sql.toString());
		return list;
	}

	/*
	 * (non Javadoc)
	 * 
	 * @Title: searchStoreClassify
	 * 
	 * @Description: TODO
	 * 
	 * @param pageNo
	 * 
	 * @param pageSize
	 * 
	 * @param params
	 * 
	 * @return
	 * 
	 * @see
	 * com.enation.app.javashop.customized.core.service.IStoreClassifyManager#
	 * searchStoreClassify(java.lang.Integer, java.lang.Integer, java.util.Map)
	 * 
	 * @author： liuyulei
	 * 
	 * @date：2016年10月10日 下午7:08:39
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Page searchStoreClassify(Integer pageNo, Integer pageSize, Map params) {

		String city_name = params.get("city_name") == null ? "" : params.get(
				"city_name").toString();
		Double longitude = Double
				.parseDouble(params.get("longitude") == null ? "0" : params
						.get("longitude").toString());
		Double latitude = Double
				.parseDouble(params.get("latitude") == null ? "0" : params.get(
						"latitude").toString());
		StringBuffer sql = new StringBuffer();
		sql.append("select s.sclassify_id,s.classify_name  from es_store_classify s  where 1=1  ");
		if (!StringUtil.isEmpty(city_name)) { // 判断城市名称是否为空
			sql.append(" and s.city_name like '%" + city_name + "%'");
		}

		Page page = this.daoSupport.queryForPage(sql.toString(), pageNo,
				pageSize);
		return page;
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreClassifyManager#listChildrenAsyn(java.lang.Integer)
	 */
	@Override
	public List listChildrenAsyn(Integer parentId) {
		String sql = "SELECT esc.*, tt.t_num AS totle_num FROM es_store_classify esc LEFT "
				+ "JOIN( SELECT c.pid c_pid, count(c.sclassify_id)AS t_num FROM es_store_classify c "
				+ "WHERE c.pid IN( SELECT sc.sclassify_id FROM es_store_classify as sc WHERE sc.pid = ? ) "
				+ "GROUP BY c.pid )tt ON esc.sclassify_id = tt.c_pid WHERE esc.pid = ? ORDER BY esc.classify_sort";

		List<Map> regionlist = this.daoSupport.queryForList(sql, parentId,parentId);

		for (Map map : regionlist) {
			if (map.get("totle_num") == null) { // 判断某一个分类下的子分类数量 null赋值为0
				map.put("totle_num", 0);
			}
			int totle_num = Integer.parseInt(map.get("totle_num").toString());
			if (totle_num != 0) { // 判断某一个分类下的子分类数量 不为0 则是(easyui)文件夹并且是闭合状态。
				map.put("state", "closed");
			}
		}
		return regionlist;
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreClassifyManager#delete(java.lang.Integer)
	 */
	@Override
	public void delete(Integer classifyId) {
		if (classifyId != null) {
			String sql = "DELETE FROM es_store_classify WHERE sclassify_id = ? OR pid = ?";
			this.daoSupport.execute(sql, classifyId, classifyId);
		}
	}
	@Override
	public List getAll() {
		Map map = new HashMap();
		map.put("sclassify_id", 0);
		map.put("classify_name","总分类");
		List list = this.daoSupport.queryForList("SELECT * FROM es_store_classify ORDER BY classify_sort");
		list.add(map);
		return list;
	}
	
	
	@Override
	public List getByCityId(Integer cityId) {
		return this.daoSupport.queryForList("SELECT * FROM es_store_classify WHERE city_id = ? ORDER BY classify_sort", cityId);
	}


}
