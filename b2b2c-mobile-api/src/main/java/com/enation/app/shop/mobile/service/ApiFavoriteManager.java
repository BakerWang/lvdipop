package com.enation.app.shop.mobile.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.enation.app.shop.core.member.model.Favorite;
import com.enation.eop.sdk.database.BaseSupport;
import com.enation.framework.database.Page;

/**
 * Created by Dawei on 4/29/15.
 */
@Service
public class ApiFavoriteManager extends BaseSupport<Favorite> {

    /**
     * 根据ID获取收藏信息
     * @param favorite_id
     * @return
     */
    public Favorite get(int favorite_id) {
        String sql = "SELECT * FROM favorite WHERE favorite_id=?";
        return baseDaoSupport.queryForObject(sql, Favorite.class, favorite_id);
    }

    /**
     * 根据商品ID和会员ID获取收藏信息
     * @param goodsid
     * @param memberid
     * @return
     */
    public Favorite get(int goodsid, int memberid){
        String sql = "SELECT * FROM favorite WHERE goods_id=? AND member_id=?";
        List<Favorite> favoriteList = baseDaoSupport.queryForList(sql, Favorite.class, goodsid, memberid);
        if(favoriteList.size() > 0){
            return favoriteList.get(0);
        }
        return null;
    }

    /**
     * 根据商品ID和会员ID删除收藏信息
     * @param goodsid
     * @param memberid
     */
    public void delete(int goodsid, int memberid){
        baseDaoSupport.execute("DELETE FROM favorite WHERE goods_id=? AND member_id=?", goodsid, memberid);
    }


    /**
     * 是否已经收藏某个商品
     * @param goodsid
     * @param memeberid
     * @return
     */
    public boolean isFavorited(int goodsid, int memeberid){
        return this.baseDaoSupport.queryForInt("SELECT COUNT(0) FROM favorite WHERE goods_id=? AND member_id=?", goodsid,memeberid) > 0;
    }


    /**
     * 添加收藏
     * @param goodsid
     */
    public void add(Integer goodsid, int memberid) {
        String sql = "INSERT INTO favorite(member_id,goods_id,favorite_time) VALUES(?,?,?)";
        baseDaoSupport.execute(sql, memberid, goodsid, com.enation.framework.util.DateUtil.getDateline());
    }
    
    
    public Page list(int memberid,int pageNo,int pageSize){
    	String sql = "select g.*, f.favorite_id from es_favorite"
				+ " f left join es_goods"
				+ " g on g.goods_id = f.goods_id";
		sql += " where f.member_id = ? order by f.favorite_time desc";	//改为根据收藏时间倒序排序，最新收藏的显示在前面	修改人：DMRain 2015-12-16
		Page page = this.daoSupport.queryForPage(sql, pageNo, pageSize,memberid);
		return page;
    }

}
