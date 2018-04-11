package com.enation.app.b2b2c.core.goods.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.app.b2b2c.core.goods.model.StoreGoods;
import com.enation.app.b2b2c.core.goods.service.IStoreGoodsCatManager;
import com.enation.app.b2b2c.core.goods.service.IStoreGoodsManager;
import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.shop.core.goods.model.Cat;
import com.enation.app.shop.core.goods.model.Product;
import com.enation.app.shop.core.goods.plugin.GoodsPluginBundle;
import com.enation.app.shop.core.goods.service.IGoodsCatManager;
import com.enation.app.shop.core.goods.service.IProductManager;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.util.StringUtil;

/**
 * 多店商品管理类
 * @author Kanon 2016-3-2；6.0版本改造
 *
 */
@Service("storeGoodsManager")
public class StoreGoodsManager implements IStoreGoodsManager{
	
	@Autowired
	private IDaoSupport daoSupport;
	
	@Autowired
	private IStoreMemberManager storeMemberManager;
	
	@Autowired
	private GoodsPluginBundle goodsPluginBundle;
	
	@Autowired
	private IGoodsCatManager goodsCatManager;
	
	@Autowired
	private IProductManager productManager;
	
	@Autowired
	private IStoreGoodsCatManager storeGoodsCatManager;
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.service.goods.IStoreGoodsManager#storeGoodsList(java.lang.Integer, java.lang.Integer, java.util.Map)
	 */
	@Override
	public Page storeGoodsList(Integer pageNo,Integer pageSize,Map map) {
		Integer store_id=Integer.valueOf(map.get("store_id").toString());
		Integer disable = (Integer) ObjectUtils.defaultIfNull(map.get("disable"),0);
		String store_cat=String.valueOf(map.get("store_cat"));
		String goodsName=String.valueOf(map.get("goodsName"));
		String market_enable=String.valueOf(map.get("market_enable"));
		String goods_type=String.valueOf(map.get("goods_type"));
		StringBuffer sql=new StringBuffer("SELECT g.*,c.store_cat_name from es_goods g LEFT JOIN es_store_cat c ON g.store_cat_id=c.store_cat_id where g.store_id="+store_id +" and  g.disabled="+disable);
		if(!StringUtil.isEmpty(store_cat)&&!StringUtil.equals(store_cat, "null")&&!StringUtil.equals(store_cat, "0")){
			//根据店铺分类ID获取分类的父ID add by DMRain 2016-1-19
			Integer pId = this.storeGoodsCatManager.is_children(Integer.parseInt(store_cat));
			
			//如果店铺分类父ID为0，证明要查询的分类为父分类下的所有子分类和父分类本身	add by DMRain 2016-1-19
			if(pId == 0){
				sql.append(" and (c.store_cat_pid="+store_cat+" or g.store_cat_id="+store_cat+")");
			}else{
				sql.append(" and g.store_cat_id="+store_cat);
			}
		}
		if(!StringUtil.isEmpty(goodsName)&&!StringUtil.equals(goodsName, "null")){
			sql.append(" and ((g.name like '%"+goodsName+"%') or (g.sn like '%"+goodsName+"%') )");
		}
		if(!StringUtil.isEmpty(market_enable)&&!StringUtil.equals(market_enable, "null")){
			if(!market_enable.equals("99")){
				sql.append(" and g.market_enable="+market_enable);
			}
		}
		//  服务商品搜索    add by liuyulei 2016-09-22
		if(!StringUtil.isEmpty(goods_type)&&!StringUtil.equals(goods_type, "null")){
			if(!goods_type.equals("-1")){
				sql.append(" and g.goods_type="+goods_type);
			}
		}
		
		sql.append(" order by g.create_time desc");
		return this.daoSupport.queryForPage(sql.toString(), pageNo, pageSize);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.service.goods.IStoreGoodsManager#storeGoodsList(int, java.util.Map)
	 */
	@Override
	public List<Map> storeGoodsList(int storeid, Map map) {
		
		StringBuffer sql=new StringBuffer("SELECT g.* from es_goods g where g.store_id="+storeid +" and  g.disabled=0");
		String store_catid=String.valueOf(map.get("store_catid"));
		String keyword=String.valueOf(map.get("keyword"));
		if(!StringUtil.isEmpty(store_catid) && !"0".equals(store_catid)){ //按店铺分类搜索
			sql.append(" and g.store_cat_id="+store_catid);
		}
		
		if(!StringUtil.isEmpty(keyword) ){
			sql.append(" and ((g.name like '%"+keyword+"%') or (g.sn like '%"+keyword+"%') )");
		}
		return this.daoSupport.queryForList(sql.toString());
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.service.goods.IStoreGoodsManager#b2b2cGoodsList(java.lang.Integer, java.lang.Integer, java.util.Map)
	 */
	@Override
	public Page b2b2cGoodsList(Integer pageNo, Integer pageSize, Map map) {
		String keyword=(String) (map.get("namekeyword")==null?"":map.get("namekeyword"));
		String cat_id=(String) (map.get("cat_id")==null?"":map.get("cat_id"));
		String search_type=(String) (map.get("search_type")==null?"":map.get("search_type")); //0:默认、1:销量、2:价格
		StringBuffer sql=new StringBuffer("select g.*,s.store_name as store_name,s.qq as qq from es_goods g inner join es_store s on s.store_id=g.store_id INNER JOIN es_brand b ON b.brand_id=g.brand_id  where s.disabled=1 and g.disabled=0 and g.market_enable=1");
			
		if(!StringUtil.isEmpty(keyword)){
			sql.append("  and ((g.name like '%"+keyword+"%') or ( g.sn like '%"+keyword+"%') or(b.name like '%"+keyword+"%'))");
		}
		if (!StringUtil.isEmpty(cat_id) && cat_id!="0") {
			Cat cat = this.goodsCatManager.getById(Integer.parseInt(cat_id));
			sql.append(" and  g.cat_id in(select c.cat_id from es_goods_cat c where c.cat_path like '" + cat.getCat_path()+ "%')");
		}
		if(!StringUtil.isEmpty(search_type)){
			if(search_type.equals("1")){
				sql.append(" order by buy_num desc");
			}else if(search_type.equals("2")){
				sql.append(" order by price desc");
			}else{
				sql.append(" order by goods_id desc");
			}
		}
		//System.out.println(sql.toString());
		return this.daoSupport.queryForPage(sql.toString(), pageNo, pageSize);
	}
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.service.goods.IStoreGoodsManager#store_searchGoodsList(java.lang.Integer, java.lang.Integer, java.util.Map)
	 */
	@Override
	public Page store_searchGoodsList(Integer page, Integer pageSize, Map params) {
		Integer storeid = (Integer) params.get("storeid");
		String keyword = (String) params.get("keyword");
		String start_price = (String) params.get("start_price");
		String end_price = (String) params.get("end_price");
		Integer key = (Integer) params.get("key");
		String order = (String) params.get("order");
		Integer cat_id = (Integer) params.get("stc_id");
		
		StringBuffer sql=new StringBuffer("select * from es_goods g where disabled=0 and market_enable=1 and store_id="+storeid);
		
		if(!StringUtil.isEmpty(keyword)){
			sql.append("  and g.name like '%"+keyword+"%' ");
		}
		
		if(!StringUtil.isEmpty(start_price)){
			sql.append(" and price>="+ Double.valueOf(start_price));
		}
		if(!StringUtil.isEmpty(end_price)){
 			sql.append(" and price<="+ Double.valueOf(end_price));
		}
		
		if (cat_id!=null && cat_id!=0) {
			
			List<Map> list  =this.daoSupport.queryForList("select store_cat_id from es_store_cat where store_cat_pid=?", cat_id);
			String cat_str=cat_id+",";
			for (Map map : list) {
				cat_str += map.get("store_cat_id").toString()+",";
			}
			sql.append(" and  g.store_cat_id in("+cat_str.substring(0, cat_str.length()-1)+")");
		}
		
		if(key!=null){
			if(key==1){			//1:新品
				sql.append(" order by goods_id "+order);
			}else if(key==2){	//2:价格
				sql.append(" order by price "+order);
			}else if(key==3){	//3:销量
				sql.append(" order by buy_num "+order);
			}else if(key==4){	//4:收藏
				sql.append(" order by goods_id "+order);
			}else if(key==5){	//5:人气
				sql.append(" order by goods_id "+order);
			}
		}
		 
		Page webpage = this.daoSupport.queryForPage(sql.toString(), page, pageSize);
		return webpage;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.service.goods.IStoreGoodsManager#saveGoodsStore(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void saveGoodsStore(Integer storeid,Integer goods_id, Integer storeNum) {
		Product product=productManager.getByGoodsId(goods_id);
		Integer productid=product.getProduct_id();
		
				
		if (storeid == 0) { // 新库存
			this.daoSupport.execute("insert into es_product_store(goodsid,productid,depotid,store,enable_store)values(?,?,?,?,?)", goods_id, productid, 1, storeNum,storeNum);
		}else{
			//如果 现有可用库存小于修改后的库存 现有可用库存为准 
			Map nowStore = this.daoSupport.queryForMap("select enable_store,store from es_product_store where goodsid = ?", goods_id);
			 
			if(Integer.parseInt(nowStore.get("enable_store").toString())-Integer.parseInt((String) nowStore.get("store").toString())+storeNum<0){
				storeNum=Integer.parseInt(nowStore.get("store").toString())-Integer.parseInt(nowStore.get("enable_store").toString());
			} 
			// 更新库存
			this.daoSupport.execute("update es_product_store set enable_store=enable_store-store+?,store=? where goodsid=?", storeNum,storeNum, goods_id);
		}
		
		
		
		
		this.daoSupport.execute("update es_goods set enable_store=enable_store-store+?,store=? where goods_id=?", storeNum,storeNum, goods_id);
		this.daoSupport.execute("update es_product set enable_store=enable_store-store+?,store=? where product_id=?", storeNum,storeNum, productid);
	}
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void saveGoodsSpecStore(Integer[] store_id,Integer goods_id, Integer[] storeNum,Integer[] product_id){
		for(int i= 0;i<store_id.length ;i ++){
			if(store_id[i] == 0) { //新库存
				this.daoSupport.execute("insert into es_product_store(goodsid,productid,depotid,store,enable_store)values(?,?,?,?,?)", goods_id, product_id[i], 1, storeNum[i],storeNum[i]);
			}else{ //更新库存  
				
				//如果 现有可用库存小于修改后的库存 现有可用库存为准 
				Map nowStore = this.daoSupport.queryForMap("select enable_store,store from es_product_store where storeid = ?", store_id[i]);
				
				if(Integer.parseInt(nowStore.get("enable_store").toString())-Integer.parseInt((String) nowStore.get("store").toString())+storeNum[i]<0){
					storeNum[i]=Integer.parseInt(nowStore.get("store").toString())-Integer.parseInt(nowStore.get("enable_store").toString());
				}
				
				this.daoSupport.execute("update es_product_store set enable_store=enable_store-store+?,store=? where storeid=?", storeNum[i],storeNum[i], store_id[i]);
			}
			//更新某个货品的总库存 
			this.daoSupport.execute("update es_product set enable_store=enable_store-store+?,store=? where product_id=?", storeNum[i],storeNum[i], product_id[i]);
		}
		//更新商品总库存
		this.daoSupport.execute("update es_goods set store=(select sum(store) from es_product_store where goodsid=?),enable_store=(select sum(enable_store) from es_product_store where goodsid=?) where goods_id=? ", goods_id,goods_id,goods_id);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.service.goods.IStoreGoodsManager#transactionList(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List transactionList(Integer pageNo, Integer pageSize,
			Integer goods_id) {
		String sql="select * from  es_transaction_record where goods_id=? order by record_id";
		return  daoSupport.queryForListPage(sql, pageNo, pageSize, goods_id);
	}
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.service.goods.IStoreGoodsManager#transactionCount(java.lang.Integer)
	 */
	@Override
	public int transactionCount(Integer goods_id) {
		String sql="select count(0) from  es_transaction_record where goods_id=? ";
		return	this.daoSupport.queryForInt(sql, goods_id);
	}
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.service.goods.IStoreGoodsManager#getGoods(java.lang.Integer)
	 */
	@Override
	public StoreGoods getGoods(Integer goods_id) {
		String sql  = "select * from es_goods where goods_id=?";
		StoreGoods goods = (StoreGoods) this.daoSupport.queryForObject(sql, StoreGoods.class, goods_id);
		return goods;
	}
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.service.goods.IStoreGoodsManager#getStoreGoodsNum(int)
	 */
	@Override
	public int getStoreGoodsNum(int struts) {
		StoreMember member  = storeMemberManager.getStoreMember();
		StringBuffer sql=new StringBuffer("SELECT count(goods_id) from es_goods where store_id=? and  disabled=0 and market_enable=?");
		return this.daoSupport.queryForInt(sql.toString(), member.getStore_id(),struts);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.goods.service.IStoreGoodsManager#getGoodsStore(java.lang.Integer)
	 */
	@Override
	public Map getGoodsStore(Integer goods_id) {
		 List<Map> list= this.daoSupport.queryForList("select * from es_product_store where goodsid=?", goods_id);
		 if(list.size()>0){
			 return list.get(0);
		 }else{
			 return null;
		 }
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.goods.service.IStoreGoodsManager#getGoodsSpecStore(java.lang.Integer)
	 */
	@Override
	public List getGoodsSpecStore(Integer goods_id){
		List<Map> list= this.daoSupport.queryForList("select * from es_product_store where goodsid=?", goods_id);
		if(list.size()>0){
			return list;
		}else{
			return new ArrayList();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.goods.service.IStoreGoodsManager#addStoreGoodsComment(java.lang.Integer)
	 */
	@Override
	public void addStoreGoodsComment(Integer goods_id) {

		String sql="update es_goods set comment_num=comment_num+1 where goods_id="+goods_id;
		this.daoSupport.execute(sql);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.goods.service.IStoreGoodsManager#editStoreGoodsGrade(java.lang.Integer)
	 */
	@Override
	public void editStoreGoodsGrade(Integer goods_id){
		int gradeAvg = this.getGoodsGradeAvg(goods_id);
		String sql = "update es_goods set grade = ? where goods_id = ?";
		this.daoSupport.execute(sql, gradeAvg, goods_id);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.goods.service.IStoreGoodsManager#authStoreGoodsList(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public Page authStoreGoodsList(Integer page,Integer pageSize) {
		String sql="select g.*,b.name as brand_name ,c.name as cat_name from es_goods  g left join es_goods_cat c on g.cat_id=c.cat_id left join es_brand b on g.brand_id = b.brand_id and b.disabled=0 where is_auth=3 and market_enable=0";
		
		return daoSupport.queryForPage(sql, page, pageSize);
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.goods.service.IGoodsManager#authStoreGoods(java.lang.Integer, java.lang.Integer, java.lang.String)
	 */
	@Override
	public void authStoreGoods(Integer goods_id, Integer pass, String message) {

		daoSupport.execute("update es_goods set is_auth=?,auth_message=?,market_enable=?  where goods_id=?", pass,message,pass,goods_id);
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.goods.service.IGoodsManager#editMarketEnable(java.lang.Integer[])
	 */
	@Override
	public void editMarketEnable(Integer[] goods_id) {
		String id_str = StringUtil.arrayToString(goods_id, ",");
		String sql="update es_goods set market_enable=0 where goods_id in("+ id_str +")";
		daoSupport.execute(sql);
	}
	
	/**
	 * 根据商品id获取商品评分的平均值
	 * @param goods_id 商品id
	 * @return gradeAvg 商品评分的平均值
	 */
	private int getGoodsGradeAvg(Integer goods_id){
		int gradeAvg;
		String sql = "select avg(grade) from es_member_comment where goods_id = ? and type = 1";
		gradeAvg = this.daoSupport.queryForInt(sql, goods_id);
		return gradeAvg;
	}

	@Override
	public Object getB2b2cGoodsStore(Integer goods_id) {
		 List<Map> list= this.daoSupport.queryForList("select * from es_product where goods_id=?", goods_id);
		 if(list.size()>0){
			 return list.get(0);
		 }else{
			 return null;
		 }
	}
}
