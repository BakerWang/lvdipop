package com.enation.app.shop.component.spec.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.enation.app.base.core.service.auth.IAdminUserManager;
import com.enation.app.base.core.service.auth.IPermissionManager;
import com.enation.app.base.core.service.auth.impl.PermissionConfig;
import com.enation.app.shop.core.goods.model.Depot;
import com.enation.app.shop.core.goods.model.DepotUser;
import com.enation.app.shop.core.goods.model.Product;
import com.enation.app.shop.core.goods.service.IDepotManager;
import com.enation.app.shop.core.goods.service.IProductManager;
import com.enation.eop.resource.model.AdminUser;
import com.enation.eop.sdk.context.UserConext;
import com.enation.eop.sdk.database.BaseSupport;
import com.enation.framework.util.StringUtil;


/**
 * 商品规格库存管理
 * @author kingapex
 *2012-3-24上午7:44:07
 */
@Service("goodsSpecStoreManager")
public class GoodsSpecStoreManager extends BaseSupport implements IGoodsSpecStoreManager {
	
	@Autowired
	private IDepotManager depotManager;
	
	@Autowired
	private IProductManager productManager;
	
	@Autowired
	private IPermissionManager permissionManager;
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.javashop.component.spec.service.IGoodsSpecStoreManager#listGoodsStore()
	 */
	@Override
	public List<Map> listGoodsStore(int goodsid) {
		
		boolean isSuperAdmin = this.permissionManager.checkHaveAuth(PermissionConfig.getAuthId("super_admin"));//超级管理员权限
		boolean isDepotAdmin = this.permissionManager.checkHaveAuth(PermissionConfig.getAuthId("depot_admin"));//库存管理权限
		
		if(!isSuperAdmin && !isDepotAdmin){
			throw new RuntimeException("没有维护库存的权限");
		}
		
		
		/**
		 * 首先取出这个商品的所有货品以及规格信息
		 */
		List<Product> prolist= productManager.list(goodsid);
		
		
		String sql ="select * from product_store where goodsid=?";
		List<Map> storeList = this.baseDaoSupport.queryForList(sql, goodsid);
		
		
		List<Depot> depotList  = depotManager.list();
		
		List list  = new ArrayList();
		

		
		AdminUser adminUser = UserConext.getCurrentAdminUser();
		Integer depotid= null;
		if(!isSuperAdmin){
			DepotUser depotUser = (DepotUser)adminUser;
			depotid = depotUser.getDepotid();
		}
		
		for(Depot depot:depotList){
			if(!isSuperAdmin){
				if( depotid==null || depot.getId().intValue()!= depotid.intValue()) continue;
			}
			Map depotMap = new HashMap();
			depotMap.put("depotid", depot.getId());
			depotMap.put("depotname", depot.getName());		
			
			List<Map> pList  = this.getProductList(depot.getId(), prolist, storeList);
			depotMap.put("productList", pList);
			list.add(depotMap);			
			
		}
		
		return list;
		
	}
	
	
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.javashop.component.spec.service.IGoodsSpecStoreManager#stock(int, java.lang.String[], java.lang.String[], java.lang.String[], java.lang.String[])
	 */
	@Override
	public int stock(int goodsid, String[] storeidAr, String[] productidAr,
			String[] depotidAr, String[] storeAr) {
		if(storeidAr==null) {
			return 0;
			
		}
		int total=0;
		for(int i= 0;i<storeidAr.length ;i ++){
			int storeid  = StringUtil.toInt(storeidAr[i],true);
			int store = StringUtil.toInt(storeAr[i],true);
			int depotid= StringUtil.toInt(depotidAr[i],true);
			int productid= StringUtil.toInt(productidAr[i],true);
			
			if(storeid == 0) { //新库存
				this.baseDaoSupport.execute("insert into product_store(goodsid,productid,depotid,store,enable_store)values(?,?,?,?,?)",goodsid,productid, depotid,store,store);
			}else{ //更新库存
				this.baseDaoSupport.execute("update product_store set  store=store+?,enable_store=enable_store+? where storeid=?", store,store,storeid);
			}
			
			//更新某个货品的总库存 
			this.daoSupport.execute("update "+this.getTableName("product")+" set store=(select sum(store) from es_product_store where productid=?) where product_id=? ", productid,productid);
			this.daoSupport.execute("update "+this.getTableName("product")+" set enable_store=(select sum(enable_store) from es_product_store where productid=?) where product_id=? ", productid,productid);
		
			total+=store;
		}
		
		//更新商品总库存
		this.daoSupport.execute("update "+this.getTableName("goods")+" set store=(select sum(store) from "+this.getTableName("product_store")+" where goodsid=?) where goods_id=? ", goodsid,goodsid);
		this.daoSupport.execute("update "+this.getTableName("goods")+" set enable_store=(select sum(enable_store) from "+this.getTableName("product_store")+" where goodsid=?) where goods_id=? ", goodsid,goodsid);
				
		return total;

		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.component.spec.service.IGoodsSpecStoreManager#saveStore(int, java.lang.String[], java.lang.String[], java.lang.String[], java.lang.String[])
	 */
	@Override
	public int saveStore(int goodsid, String[] storeidAr, String[] productidAr,
			String[] depotidAr, String[] storeAr) {
		 
		int total=0;
		for(int i= 0;i<storeidAr.length ;i ++){
			int storeid  = StringUtil.toInt(storeidAr[i],true);
			int store = StringUtil.toInt(storeAr[i],true);
			int depotid= StringUtil.toInt(depotidAr[i],true);
			int productid= StringUtil.toInt(productidAr[i],true);
			
			if(storeid == 0) { //新库存
				this.baseDaoSupport.execute("insert into product_store(goodsid,productid,depotid,store,enable_store)values(?,?,?,?,?)",goodsid,productid, depotid,store,store);
			}else{ //更新库存
				int differ = getStoreDiffer(storeid);
				
				this.baseDaoSupport.execute("update product_store set store=?,enable_store=? where storeid=?", store,store-differ,storeid);
			}
			
			//更新某个货品的总库存 
				this.daoSupport.execute("update "+this.getTableName("product")+" set store=(select sum(store) from "+ this.getTableName("product_store")+" where productid=?) where product_id=? ", productid,productid);
				this.daoSupport.execute("update "+this.getTableName("product")+" set enable_store=(select sum(enable_store) from "+ this.getTableName("product_store")+" where productid=?) where product_id=? ", productid,productid);
				
			total+=store;
		}
		
		//更新商品总库存
		this.daoSupport.execute("update "+this.getTableName("goods")+" set store=(select sum(store) from "+this.getTableName("product_store")+" where goodsid=?) where goods_id=? ", goodsid,goodsid);
		this.daoSupport.execute("update "+this.getTableName("goods")+" set enable_store=(select sum(enable_store) from "+this.getTableName("product_store")+" where goodsid=?) where goods_id=? ", goodsid,goodsid);
		
		return total;
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.component.spec.service.IGoodsSpecStoreManager#ship(int, java.lang.String[], java.lang.String[], java.lang.String[], java.lang.String[])
	 */
	@Override
	public int ship(int goodsid, String[] storeidAr, String[] productidAr,
			String[] depotidAr, String[] storeAr) {
		 
		int total=0;
		for(int i= 0;i<storeidAr.length ;i ++){
			int storeid  = StringUtil.toInt(storeidAr[i],true);
			int store = StringUtil.toInt(storeAr[i],true);
			int depotid= StringUtil.toInt(depotidAr[i],true);
			int productid= StringUtil.toInt(productidAr[i],true);
			
			if(storeid == 0) { //新库存
				this.baseDaoSupport.execute("insert into product_store(goodsid,productid,depotid,store)values(?,?,?,?)",goodsid,productid, depotid,store);
			}else{ //更新库存
				this.baseDaoSupport.execute("update product_store set  store=store-? where storeid=?", store,storeid);
			}
			
			//更新某个货品的总库存 
			this.daoSupport.execute("update "+this.getTableName("product")+" set store=(select sum(store) from "+ this.getTableName("product_store")+" where productid=?) where product_id=? ", productid,productid);
		
			total+=store;
		}
		
		//更新商品总库存
		this.daoSupport.execute("update "+this.getTableName("goods")+" set store=(select sum(store) from "+this.getTableName("product_store")+" where goodsid=?) where goods_id=? ", goodsid,goodsid);
		
		return total;
	}
	
	private List<Map> getProductList(int depotid,List<Product> productList,List<Map> storeList){
		
		List<Map> pList = new ArrayList<Map>();
		
		for(Product product:productList){
			Map pro = new HashMap();
			
			pro.put("specList",product.getSpecList());
			pro.put("sn", product.getSn());
			pro.put("name",product.getName());
			pro.put("product_id", product.getProduct_id());
			pro.put("storeid", 0);
			pro.put("store", 0);
			for(Map store:storeList){
				
				//找到此仓库、此货品
				if(
						depotid == ((Integer)store.get("depotid")).intValue() 
					&&  product.getProduct_id().intValue()== ((Integer)store.get("productid")).intValue() 
						
				){
					pro.put("storeid",(Integer)store.get("storeid"));
					pro.put("store", (Integer)store.get("store") );
					
				} 
				
			}
			
			pList.add(pro);
		}
		
		return pList;
	}
	
	/**
	 * 获取当前实际库存减去可用库存的差
	 * add by DMRain 2016-4-22
	 * @param storeid 
	 * @return
	 */
	private int getStoreDiffer(int storeid) {
		//获取当前实际库存 add by DMRain 2016-4-22
		int currStore = this.daoSupport.queryForInt("select sum(store) from es_product_store where storeid = ?", storeid);
		//获取当前可用库存 add by DMRain 2016-4-22
		int currEnableStore = this.daoSupport.queryForInt("select sum(enable_store) from es_product_store where storeid = ?", storeid);
		//得到实际库存与可用库存之间的差(因为实际库存肯定大于可用库存，所以用实际库存减去可用库存)
		//add by DMRain 2016-4-22
		int differ = currStore - currEnableStore;
		return differ;
	}
}
