package com.enation.app.shop.component.product.plugin.store;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.app.base.core.service.auth.IPermissionManager;
import com.enation.app.base.core.service.auth.impl.PermissionConfig;
import com.enation.app.shop.core.goods.model.DepotUser;
import com.enation.app.shop.core.goods.model.StoreLog;
import com.enation.app.shop.core.goods.model.WarnNum;
import com.enation.app.shop.core.goods.plugin.AbstractGoodsStorePlugin;
import com.enation.app.shop.core.goods.plugin.IGoodsDeleteEvent;
import com.enation.app.shop.core.goods.plugin.IStockSaveEvent;
import com.enation.app.shop.core.goods.plugin.IStoreSaveEvent;
import com.enation.app.shop.core.goods.service.IGoodsStoreManager;
import com.enation.app.shop.core.goods.service.IStoreLogManager;
import com.enation.eop.processor.core.freemarker.FreeMarkerPaser;
import com.enation.eop.resource.model.AdminUser;
import com.enation.eop.sdk.context.UserConext;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.IDBRouter;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.IntegerMapper;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.StringUtil;

/**
 * 普通商品库存插件
 * 
 * @author kingapex
 * 
 */
@Component
public class GenericGoodsStorePlugin extends AbstractGoodsStorePlugin implements IGoodsDeleteEvent,IStockSaveEvent,IStoreSaveEvent {

	
	@Autowired
	private IPermissionManager permissionManager;
	
	@Autowired
	private IGoodsStoreManager goodsStoreManager;
	
	@Autowired
	private IDaoSupport daoSupport;
	
	@Autowired
	private IDBRouter baseDBRouter;
	
	@Autowired
	private IStoreLogManager storeLogManager;
	
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.goods.plugin.AbstractGoodsStorePlugin#getStoreHtml(java.util.Map)
	 */
	@Override
	public String getStoreHtml(Map goods) {
		Integer goodsid = (Integer) goods.get("goods_id");
		Integer productid = this.getProductId(goodsid);

		List<Map> storeList = null;
		AdminUser adminUser = UserConext.getCurrentAdminUser();
		if (adminUser.getFounder() != 1) { // 非超级管理员
			boolean isStorer = permissionManager.checkHaveAuth(PermissionConfig.getAuthId("depot_admin")); // 是否有库存管理权限

			if (isStorer) {
				DepotUser depotUser = (DepotUser) adminUser;
				int depotid = depotUser.getDepotid();
				storeList = this.goodsStoreManager.ListProductDepotStore(productid, depotid);
			} else {
				return "您没有修改库存的权限";
			}
		} else {
			storeList = this.goodsStoreManager.listProductStore(productid);
		}

		FreeMarkerPaser freeMarkerPaser = FreeMarkerPaser.getInstance();
		freeMarkerPaser.putData("storeList", storeList);
		return freeMarkerPaser.proessPageContent();
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.goods.plugin.AbstractGoodsStorePlugin#getStockHtml(java.util.Map)
	 */
	@Override
	public String getStockHtml(Map goods) {
		Integer goodsid = (Integer) goods.get("goods_id");
		Integer productid = this.getProductId(goodsid);

		List<Map> storeList = null;
		AdminUser adminUser = UserConext.getCurrentAdminUser();
		if (adminUser.getFounder() != 1) { // 非超级管理员
			boolean isStorer = permissionManager.checkHaveAuth(PermissionConfig.getAuthId("depot_admin")); // 是否有库存管理权限
			if (isStorer) {
				DepotUser depotUser = (DepotUser) adminUser;
				if(depotUser.getDepotid()==null)
					return "管理员没有设置库房";
				int depotid = depotUser.getDepotid();
				storeList = this.goodsStoreManager.ListProductDepotStore(productid, depotid);
			} else {
				return "您没有修改库存的权限";
			}
		} else {
			storeList = this.goodsStoreManager.listProductStore(productid);
		}

		FreeMarkerPaser freeMarkerPaser = FreeMarkerPaser.getInstance();
		freeMarkerPaser.setPageName("stockList");
		freeMarkerPaser.putData("storeList", storeList);
		return freeMarkerPaser.proessPageContent();
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.goods.plugin.AbstractGoodsStorePlugin#getShipHtml(java.util.Map)
	 */
	@Override
	public String getShipHtml(Map goods) {
		Integer goodsid = (Integer) goods.get("goods_id");
		Integer productid = this.getProductId(goodsid);

		List<Map> storeList = null;
		AdminUser adminUser = UserConext.getCurrentAdminUser();
		if (adminUser.getFounder() != 1) { // 非超级管理员
			boolean isStorer = permissionManager.checkHaveAuth(PermissionConfig.getAuthId("depot_admin")); // 是否有库存管理权限
			if (isStorer) {
				DepotUser depotUser = (DepotUser) adminUser;
				int depotid = depotUser.getDepotid();
				storeList = this.goodsStoreManager.ListProductDepotStore(productid, depotid);
			} else {
				return "您没有修改库存的权限";
			}
		} else {
			storeList = this.goodsStoreManager.listProductStore(productid);
		}

		FreeMarkerPaser freeMarkerPaser = FreeMarkerPaser.getInstance();
		freeMarkerPaser.setPageName("shipList");
		freeMarkerPaser.putData("storeList", storeList);
		return freeMarkerPaser.proessPageContent();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.goods.plugin.IStockSaveEvent#onStockSave(java.util.Map)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void onStockSave(Map goods) {
		boolean isSuperAdmin = this.permissionManager.checkHaveAuth(PermissionConfig.getAuthId("super_admin"));// 超级管理员权限
		boolean isDepotAdmin = this.permissionManager.checkHaveAuth(PermissionConfig.getAuthId("depot_admin"));// 库存管理权限

		Integer goodsid = (Integer) goods.get("goods_id");
		Integer productid = this.getProductId(goodsid);

		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String[] storeidAr = request.getParameterValues("storeid");
		String[] storeAr = request.getParameterValues("storeNum");
		String[] depotidAr = request.getParameterValues("depotid");

		AdminUser adminUser = UserConext.getCurrentAdminUser();
		boolean isFounder = adminUser.getFounder() == 1;
		boolean isStorer = false;
		if (!isFounder) { // 非超级管理员
			isStorer = permissionManager.checkHaveAuth(PermissionConfig.getAuthId("depot_admin")); // 是否有库存管理权限
		}

		if (!isFounder && !isStorer) {
			throw new RuntimeException("您没有权限维护库存");
		}
		int total = 0;
		for (int i = 0; i < storeidAr.length; i++) {
			int storeid = StringUtil.toInt(storeidAr[i], true);
			int store = StringUtil.toInt(storeAr[i], true);
			int depotid = StringUtil.toInt(depotidAr[i], true);
			//2014-12-19新增可用库存的逻辑
			if (storeid == 0) { // 新库存
				this.daoSupport.execute("insert into es_product_store(goodsid,productid,depotid,store,enable_store)values(?,?,?,?,?)", goodsid, productid, depotid, store,store);
			} else { // 更新库存
				this.daoSupport.execute("update es_product_store set store=store+?,enable_store=enable_store+? where storeid=?", store,store, storeid);
			}
			total += store;
		}

		// 更新商品为进货完成状态
		this.daoSupport.execute("update es_goods_depot set iscmpl=1 where goodsid=? and depotid=?", goodsid, depotidAr[0]);

		// 更新商品总库存
		this.daoSupport.execute("update es_goods set store=(select sum(store) from es_product_store"
						+ " where goodsid=?) where goods_id=? ", goodsid, goodsid);
		this.daoSupport.execute("update es_goods set enable_store=(select sum(enable_store) from  es_product_store  where goodsid=?) where goods_id=? ", goodsid, goodsid);

		this.daoSupport.execute("update es_product set store=(select sum(store) from es_product_store"
						+ " where productid=?) where product_id=? ", productid,	productid);
		this.daoSupport.execute("update es_product set enable_store=(select sum(enable_store) from  es_product_store  where productid=?) where product_id=? ", productid,	productid);

		if (isSuperAdmin) {
			for (String deptoid : depotidAr) {
				int did = StringUtil.toInt(deptoid, true);
				StoreLog storeLog = new StoreLog();
				storeLog.setGoodsid(goodsid);
				storeLog.setGoodsname(goods.get("name").toString());
				storeLog.setDepot_type(0);
				storeLog.setOp_type(1);
				storeLog.setDepotid(did);
				storeLog.setDateline(DateUtil.getDateline());
				storeLog.setNum(total);
				storeLog.setUserid(adminUser.getUserid());
				storeLog.setUsername(adminUser.getUsername());
				storeLog.setEnable_store(total);
				storeLogManager.add(storeLog);
			}
		} else if (isDepotAdmin) {
			DepotUser depotUser = (DepotUser) adminUser;
			StoreLog storeLog = new StoreLog();
			storeLog.setGoodsid(goodsid);
			storeLog.setGoodsname(goods.get("name").toString());
			storeLog.setDepot_type(1);
			storeLog.setOp_type(0);
			storeLog.setDepotid(depotUser.getDepotid());
			storeLog.setDateline(DateUtil.getDateline());
			storeLog.setNum(total);
			storeLog.setEnable_store(total);
			storeLog.setUserid(adminUser.getUserid());
			storeLog.setUsername(adminUser.getUsername());
			
			storeLogManager.add(storeLog);
		} else {
			throw new RuntimeException("没有操作库存的权限");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.goods.plugin.AbstractGoodsStorePlugin#onShipSave(java.util.Map)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void onShipSave(Map goods) {
		Integer goodsid = (Integer) goods.get("goods_id");
		Integer productid = this.getProductId(goodsid);

		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String[] storeidAr = request.getParameterValues("storeid");
		String[] storeAr = request.getParameterValues("store");

		AdminUser adminUser = UserConext.getCurrentAdminUser();
		boolean isFounder = adminUser.getFounder() == 1;
		boolean isStorer = false;
		if (!isFounder) { // 非超级管理员
			isStorer = permissionManager.checkHaveAuth(PermissionConfig.getAuthId("depot_admin")); // 是否有库存管理权限
		}

		if (!isFounder && !isStorer) {
			throw new RuntimeException("您没有权限维护库存");
		}
		int total = 0;
		for (int i = 0; i < storeidAr.length; i++) {
			int storeid = StringUtil.toInt(storeidAr[i], true);
			int store = StringUtil.toInt(storeAr[i], true);

			if (storeid == 0) { // 新库存
				throw new RuntimeException("商品库存为0，不能出货");
			} else { // 更新库存
				this.daoSupport.execute("update es_product_store set store=store-? where storeid=?", store, storeid);
			}
			total += store;
		}
		// 更新商品总库存
		this.daoSupport.execute("update es_goods"
						+ " set store=(select sum(store) from es_product_store"
						+ " where goodsid=?) where goods_id=? ", goodsid, goodsid);
		this.daoSupport.execute("update es_product set store=(select sum(store) from product_store" 
						+ " where productid=?) where product_id=? ", productid,	productid);
		// 库存日志
		DepotUser depotUser = (DepotUser) adminUser;
		StoreLog storeLog = new StoreLog();
		storeLog.setGoodsid(goodsid);
		storeLog.setGoodsname(goods.get("name").toString());
		storeLog.setDepot_type(1);
		storeLog.setOp_type(1);
		storeLog.setDepotid(depotUser.getDepotid());
		storeLog.setDateline(DateUtil.getDateline());
		storeLog.setNum(total);
		storeLog.setUserid(adminUser.getUserid());
		storeLog.setUsername(adminUser.getUsername());
		storeLogManager.add(storeLog);
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.goods.plugin.IStoreSaveEvent#onStoreSave(java.util.Map)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void onStoreSave(Map goods) {
		Integer goodsid = (Integer) goods.get("goods_id");
		Integer productid = this.getProductId(goodsid);

		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String[] storeidAr = request.getParameterValues("storeid");
		String[] storeAr = request.getParameterValues("storeNum");
		String[] depotidAr = request.getParameterValues("depotid");

		AdminUser adminUser = UserConext.getCurrentAdminUser();
		boolean isFounder = adminUser.getFounder() == 1;
		boolean isStorer = false;
		if (!isFounder) { // 非超级管理员
			isStorer = permissionManager.checkHaveAuth(PermissionConfig.getAuthId("depot_admin")); // 是否有库存管理权限
		}

		if (!isFounder && !isStorer) {
			throw new RuntimeException("您没有权限维护库存");
		}

		for (int i = 0; i < storeidAr.length; i++) {
			int storeid = StringUtil.toInt(storeidAr[i], true);
			int store = StringUtil.toInt(storeAr[i], true);
			int depotid = StringUtil.toInt(depotidAr[i], true);

			if (storeid == 0) { // 新库存
				this.daoSupport.execute("insert into es_product_store(goodsid,productid,depotid,store,enable_store)values(?,?,?,?,?)", goodsid, productid, depotid, store,store);
			} else { // 更新库存
				
				this.daoSupport.execute("update es_product_store set enable_store=enable_store+(?-store) where storeid=?",	store, storeid);
				this.daoSupport.execute("update es_product_store set store=? where storeid=?",	store, storeid);
			}
		}

		// 更新商品总库存
		this.daoSupport.execute("update es_goods"
						+ " set store=(select sum(store) from es_product_store"
						+ " where goodsid=?) where goods_id=? ", goodsid, goodsid);
		
		this.daoSupport.execute("update es_goods set enable_store=(select sum(enable_store) from  es_product_store  where goodsid=?) where goods_id=? ", goodsid, goodsid);
		
		this.daoSupport.execute("update es_product"
				+ " set store=(select sum(store) from es_product_store"
				+ " where productid=?) where product_id=? ", productid,	productid);
		
		this.daoSupport.execute("update es_product set enable_store=(select sum(enable_store) from  es_product_store  where productid=?) where product_id=? ", productid,	productid);
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.goods.plugin.IGoodsDeleteEvent#onGoodsDelete(java.lang.Integer[])
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void onGoodsDelete(Integer[] goodsid) {
		if (goodsid == null || goodsid.length == 0)
			return;
		this.daoSupport.execute("delete from es_product_store where goodsid in(" + StringUtil.arrayToString(goodsid, ",") + ")");
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.goods.plugin.AbstractGoodsStorePlugin#canBeExecute(java.util.Map)
	 */
	@Override
	public boolean canBeExecute(Map goods) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.goods.plugin.AbstractGoodsStorePlugin#getWarnNumHtml(java.util.Map)
	 */
	@Override
	public String getWarnNumHtml(Map goods) {
		AdminUser adminUser = UserConext.getCurrentAdminUser();
		int foud = adminUser.getFounder();
		if (foud != 1) {
			return "您没有修改报警数的权限";
		}
		Integer goodsid = Integer.valueOf(goods.get("goods_id").toString());
		List<WarnNum> warnList = this.goodsStoreManager.listWarns(goodsid);
		FreeMarkerPaser freeMarkerPaser = FreeMarkerPaser.getInstance();
		freeMarkerPaser.setPageName("warn_num");
		freeMarkerPaser.putData("warnList", warnList);
		return freeMarkerPaser.proessPageContent();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.goods.plugin.AbstractGoodsStorePlugin#onWarnSave(java.util.Map)
	 */
	@Override
	public void onWarnSave(Map goods) {
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		Integer goodsid = (Integer) goods.get("goods_id");

		String[] idArr = request.getParameterValues("warnid");
		String[] warn_numAr = request.getParameterValues("warn_num");
		if (idArr == null || idArr.length == 0)
			return;
		for (int i = 0; i < idArr.length; i++) {
			int id = StringUtil.toInt(idArr[i], true);
			int warn_num = StringUtil.toInt(warn_numAr[i], true);

			WarnNum warnNum = new WarnNum();
			warnNum.setId(id);
			warnNum.setGoods_id(goodsid);
			warnNum.setWarn_num(warn_num);
			if (warnNum.getId() == 0) { // 没有此货品，新增
				this.daoSupport.insert("es_warn_num", warnNum);
			} else {
				this.daoSupport.execute("update es_warn_num set warn_num = ? where id=?", warnNum.getWarn_num(), warnNum.getId());
			}
		}
	}
	private Integer getProductId(Integer goodsid) {
		String sql = "select product_id from es_product where goods_id = ?";
		List<Integer> productidList = this.daoSupport.queryForList(sql,	new IntegerMapper(), goodsid);
		Integer productid = productidList.get(0);
		return productid;
	}

}