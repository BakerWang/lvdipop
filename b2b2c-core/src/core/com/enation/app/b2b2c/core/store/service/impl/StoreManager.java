package com.enation.app.b2b2c.core.store.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.app.b2b2c.component.plugin.store.StorePluginBundle;
import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.b2b2c.core.store.model.Store;
import com.enation.app.b2b2c.core.store.model.StoreThemes;
import com.enation.app.b2b2c.core.store.service.IStoreManager;
import com.enation.app.b2b2c.core.store.service.IStoreThemesManager;
import com.enation.app.b2b2c.core.store.utils.DistanceUtil;
import com.enation.app.base.core.model.Member;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.StringUtil;

/**
 * 店铺管理类
 * 
 * @author Kanon
 * @version 1.1 增加方法获取当前已经登录的店铺,修改店铺信息刷新存储session中的店铺信息
 * @since v3.1
 */
@Service("storeManager")
public class StoreManager implements IStoreManager {
	@Autowired
	private IDaoSupport daoSupport;

	@Autowired
	private IStoreMemberManager storeMemberManager;

	@Autowired
	private IStoreThemesManager storeThemesManager;

	@Autowired
	private StorePluginBundle storePluginBundle;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#apply(com.enation.
	 * app.b2b2c.core.model.Store)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void apply(Store store) {
		// 获取当前用户信息
		Member member = storeMemberManager.getStoreMember();
		if (member != null) {
			store.setMember_id(member.getMember_id());
			store.setMember_name(member.getUname());
		}
		if (StringUtil.isEmpty(store.getId_img())) {
			store.setId_img(null);
		}
		if (StringUtil.isEmpty(store.getLicense_img())) {
			store.setLicense_img(null);
		}
		this.daoSupport.insert("es_store", store);
		store.setStore_id(this.daoSupport.getLastId("es_store"));
		storePluginBundle.onAfterApply(store);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#audit_pass(java.
	 * lang.Integer)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void audit_pass(Integer member_id, Integer storeId, Integer pass, Integer name_auth, Integer store_auth,
			Double commission) {
		if (pass == null) {
			auth_pass(storeId, name_auth, store_auth);
			;
			return;
		}
		if (pass == 1) {
			store_auth = store_auth == null ? 0 : store_auth;
			name_auth = name_auth == null ? 0 : name_auth;
			StoreThemes storeThemes = storeThemesManager.getDefaultStoreThemes();
			this.daoSupport.execute(
					"update es_store set create_time=?,name_auth=?,store_auth=?,commission=?,themes_id=?,themes_path=? where store_id=?",
					DateUtil.getDateline(), name_auth, store_auth, commission, storeThemes.getId(),
					storeThemes.getPath(), storeId);
			this.editStoredis(1, storeId);
			storePluginBundle.onAfterPass(this.getStore(storeId));
		} else {
			// 审核未通过
			this.daoSupport.execute("update es_store set disabled=? where store_id=?", -1, storeId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#store_list(java.
	 * util.Map, int, int)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Page store_list(Map other, Integer disabled, int pageNo, int pageSize) {
		StringBuffer sql = new StringBuffer("");
		disabled = disabled == null ? 1 : disabled;
		String store_name = other.get("store_name") == null ? "" : other.get("store_name").toString();
		String name = other.get("name") == null ? "" : other.get("name").toString();
		String city_name = other.get("city_name") == null ? "" : other.get("city_name").toString();
		Double longitude = Double.parseDouble(other.get("longitude") == null ? "0" : other.get("longitude").toString());
		Double latitude = Double.parseDouble(other.get("latitude") == null ? "0" : other.get("latitude").toString());
		Double sclassify_id = Double.parseDouble(other.get("sclassify_id") == null ? "-1" : other.get("sclassify_id").toString());
		String searchType = other.get("searchType") == null ? "" : other.get("searchType").toString();

		String recently = "select count(0) from es_order eo where eo.store_id = s.store_id and status = 7 and create_time between "
				+ ((new Date().getTime() / 1000) - 7776000) + " and " + new Date().getTime() / 1000;

		// 店铺状态
		if (disabled.equals(-2)) {
			sql.append("select (" + recently + ") recently , s.* from es_store s   where  disabled!="// 添加
																										// 查询到的列
																										// recently
																										// 最近成交量
					+ disabled);
		} else {
			sql.append("select (" + recently + ") recently ,s.* from es_store s   where  disabled="// 添加
																									// 查询到的列
																									// recently
																									// 最近成交量
					+ disabled);
		}

		// 查询时取出自营店铺信息，不让自营店铺信息显示在后台店铺列表中 add by DMRain 2016-1-25
		// sql.append(" and s.store_id != 1");

		if (!StringUtil.isEmpty(store_name)) {
			sql.append("  and s.store_name like '%" + store_name + "%'");
		}
		
		if (!StringUtil.isEmpty(name)) {
			sql.append("  and s.store_name like '%" + name + "%'");
		}
		
		//  添加搜索条件   城市名称  分类id   add by liuyulei   2016-10-11
		if (!StringUtil.isEmpty(city_name)) {
			sql.append("  and s.store_city like '%" + city_name + "%'");
		}
		if (sclassify_id != -1) {
			sql.append("  and s.store_sclassify_id =" + sclassify_id );
		}
		
		
		if (!StringUtil.isEmpty(searchType) && !searchType.equals("default")) {
			sql.append(" order by " + searchType + " desc");
		} else {
			sql.append(" order by store_region" + " desc");
		}
		Page page = this.daoSupport.queryForPage(sql.toString(), pageNo, pageSize);
		//*****************计算距离开始*******************
		//根据入参经纬度  及数据库中经纬度   计算两点之间距离     并放入list中    add by liuyulei  2016-10-11
		List<Map> list = (List) page.getResult();
		for (Map map : list) {
			Double lon = Double.parseDouble(map.get("longitude") == null ? "0" : map.get("longitude").toString());
			Double lat = Double.parseDouble(map.get("latitude") == null ? "0" : map.get("latitude").toString());
			if (lon == 0 || lat == 0 || longitude == 0 || latitude == 0) { // 如果数据库中经纬度为空,且入参中经纬度为空
																			// 则设置距离为-1
																			// 即无法计算距离
				map.put("distance", -1);
			} else {
				map.put("distance", DistanceUtil.getDistance(lon, lat, longitude, latitude)); //单位:米
			}
		}
		
		//*****************计算距离结束*******************
		return page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#disStore(java.lang
	 * .Integer)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void disStore(Integer storeId) {
		// 关闭店铺
		this.daoSupport.execute("update es_store set  end_time=? where store_id=?", DateUtil.getDateline(), storeId);
		this.editStoredis(2, storeId);
		// 修改会员店铺状态
		this.daoSupport.execute("update es_member set is_store=? where member_id=?", 3,
				this.getStore(storeId).getMember_id());
		// 更高店铺商品状态
		this.daoSupport.execute("update es_goods set disabled=?,market_enable=? where store_id=?",1,1,storeId);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#useStore(java.lang
	 * .Integer)
	 */
	@Override
	public void useStore(Integer storeId) {
		this.editStoredis(1, storeId);
		this.daoSupport.execute(
				"update es_member set is_store=" + 1 + " where member_id=" + this.getStore(storeId).getMember_id());
		// 更高店铺商品状态
		this.daoSupport.execute("update es_goods set disabled=? where store_id=?", 0, storeId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#getStore(java.lang
	 * .Integer)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Store getStore(Integer storeId) {
		String sql = "select * from es_store where store_id=" + storeId;
		List<Store> list = this.daoSupport.queryForList(sql, Store.class);
		Store store = (Store) list.get(0);
		if (store.getId_img() != null && !StringUtil.isEmpty(store.getId_img())) {
			store.setId_img(StaticResourcesUtil.convertToUrl(store.getId_img()));
		}
		if (store.getLicense_img() != null && !StringUtil.isEmpty(store.getLicense_img())) {
			store.setLicense_img(StaticResourcesUtil.convertToUrl(store.getLicense_img()));
		}
		return store;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.enation.app.b2b2c.core.store.service.IStoreManager#getStore()
	 */
	@Override
	public Store getStore() {
		Store store = (Store) ThreadContextHolder.getSession().getAttribute(this.CURRENT_STORE_KEY);
		return store;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#editStore(com.
	 * enation.app.b2b2c.core.model.store.Store)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void editStore(Store store) {
		StoreMember member = storeMemberManager.getStoreMember();
		this.daoSupport.update("es_store", store, "store_id=" + member.getStore_id());

		ThreadContextHolder.getSession().setAttribute(IStoreManager.CURRENT_STORE_KEY, store);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#editStoreInfo(com.
	 * enation.app.b2b2c.core.model.store.Store)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void editStoreInfo(Store store) {

		this.daoSupport.update("es_store", store, " store_id=" + store.getStore_id());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.store.service.IStoreManager#editStore(java.
	 * util.Map)
	 */
	@Override
	public void editStore(Map store) {
		this.daoSupport.update("es_store", store, " store_id=" + store.get("store_id"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.enation.app.b2b2c.core.service.store.IStoreManager#checkStore()
	 */
	@Override
	public boolean checkStore() {
		Member member = storeMemberManager.getStoreMember();
		String sql = "select count(store_id) from es_store where member_id=?";
		int isHas = this.daoSupport.queryForInt(sql, member.getMember_id());
		if (isHas > 0)
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#save(com.enation.
	 * app.b2b2c.core.model.Store)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void save(Store store) {
//		store.setMember_id(this.storeMemberManager.getMember(store.getMember_name()).getMember_id());
		store.setCreate_time(DateUtil.getDateline());
		store.setDisabled(1);
		this.daoSupport.insert("es_store", store);
		store.setStore_id(this.daoSupport.getLastId("es_store"));

		storePluginBundle.onAfterPass(store);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.store.service.IStoreManager#registStore(com.
	 * enation.app.b2b2c.core.store.model.Store)
	 */
	@Override
	public void registStore(Store store) {
		// 暂时先将店铺等级定为默认等级，以后版本升级更改此处
		store.setStore_level(1);
		// 保存商店
		save(store);
		// 申请开店
		String sql = "update es_member set is_store=1,store_id=? where member_id=?";
		daoSupport.execute(sql, store.getStore_id(), store.getMember_id());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#checkIdNumber(java
	 * .lang.String)
	 */
	@Override
	public Integer checkIdNumber(String idNumber) {
		String sql = "select member_id from es_store where id_number=?";
		List result = this.daoSupport.queryForList(sql, idNumber);
		return result.size() > 0 ? 1 : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#editStoreOnekey(
	 * java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void editStoreOnekey(String key, String value) {
		StoreMember member = storeMemberManager.getStoreMember();
		Map map = new HashMap();
		map.put(key, value);
		
		
		this.daoSupport.update("es_store", map, "store_id=" + member.getStore_id());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#collect(java.lang.
	 * Integer)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void addcollectNum(Integer storeid) {
		String sql = "update es_store set store_collect = IFNULL(store_collect,0) + 1 where store_id=?";
		this.daoSupport.execute(sql, storeid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#getStore(java.lang
	 * .String)
	 */
	@Override
	public boolean checkStoreName(String storeName) {
		String sql = "select  count(store_id) from es_store where store_name=? and disabled=1";
		Integer count = this.daoSupport.queryForInt(sql, storeName);
		return count != 0 ? true : false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#reduceCollectNum(
	 * java.lang.Integer)
	 */
	@Override
	public void reduceCollectNum(Integer storeid) {
		String sql = "update es_store set store_collect = store_collect-1 where store_id=?";
		this.daoSupport.execute(sql, storeid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#saveStoreLicense(
	 * java.lang.Integer, java.lang.String, java.lang.String, java.lang.Integer,
	 * java.lang.Integer)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void saveStoreLicense(Integer storeid, String id_img, String license_img, Integer store_auth,
			Integer name_auth) {
		if (store_auth == 2) {
			this.daoSupport.execute("update es_store set store_auth=?,license_img=? where store_id=?", store_auth,
					license_img, storeid);
		}
		if (name_auth == 2) {
			this.daoSupport.execute("update es_store set name_auth=?,id_img=? where store_id=?", name_auth, id_img,
					storeid);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#auth_list(java.
	 * util.Map, java.lang.String, int, int)
	 */
	@Override
	public Page auth_list(Map other, Integer disabled, int pageNo, int pageSize) {
		StringBuffer sql = new StringBuffer("select s.* from es_store s   where  disabled=" + disabled);
		sql.append(" and (store_auth=2 or name_auth=2)");
		return this.daoSupport.queryForPage(sql.toString(), pageNo, pageSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#auth_pass(java.
	 * lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public void auth_pass(Integer store_id, Integer name_auth, Integer store_auth) {
		if (store_auth != null) {
			this.daoSupport.execute("update es_store set store_auth=? where store_id=?", store_auth, store_id);
		}
		if (name_auth != null) {
			this.daoSupport.execute("update es_store set name_auth=? where store_id=?", name_auth, store_id);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.b2b2c.core.service.store.IStoreManager#getStoreByMember(
	 * java.lang.Integer)
	 */
	@Override
	public Store getStoreByMember(Integer memberId) {
		return (Store) this.daoSupport.queryForObject("select * from es_store where member_id=?", Store.class,
				memberId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.enation.app.b2b2c.core.service.store.IStoreManager#reApply(com.
	 * enation.app.b2b2c.core.model.store.Store)
	 */
	@Override
	public void reApply(Store store) {
		// 获取当前用户信息
		StoreMember member = storeMemberManager.getStoreMember();
		if (StringUtil.isEmpty(store.getId_img())) {
			store.setId_img(null);
			store.setName_auth(0);
		}
		if (StringUtil.isEmpty(store.getLicense_img())) {
			store.setLicense_img(null);
			store.setStore_auth(0);
		}
		if (member != null) {
			store.setMember_id(member.getMember_id());
			store.setMember_name(member.getUname());
		}
		// 店铺状态
		store.setDisabled(0);
		store.setStore_id(member.getStore_id());

		this.daoSupport.update("es_store", store, "store_id=" + store.getStore_id());
		storePluginBundle.onAfterApply(store);
	}

	/**
	 * 更改店铺状态
	 * 
	 * @param disabled
	 * @param store_id
	 */
	private void editStoredis(Integer disabled, Integer store_id) {
		this.daoSupport.execute("update es_store set disabled=? where store_id=?", disabled, store_id);
	}

	@Override
	public List<Store> getStoreList() {
		return this.daoSupport.queryForList("select * from es_store where disabled = 1");
	}
	
}