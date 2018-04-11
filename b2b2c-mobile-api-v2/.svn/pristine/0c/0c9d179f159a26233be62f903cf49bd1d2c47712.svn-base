package com.enation.app.shop.mobile.action.api;

import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.b2b2c.component.bonus.model.StoreBonus;
import com.enation.app.b2b2c.component.bonus.model.StoreBonusType;
import com.enation.app.b2b2c.component.bonus.service.IB2b2cBonusManager;
import com.enation.app.b2b2c.core.goods.service.IStoreGoodsCatManager;
import com.enation.app.b2b2c.core.goods.service.IStoreGoodsManager;
import com.enation.app.b2b2c.core.goods.service.IStoreGoodsTagManager;
import com.enation.app.b2b2c.core.member.model.MemberCollect;
import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreCollectManager;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.b2b2c.core.store.model.Store;
import com.enation.app.b2b2c.core.store.service.IStoreManager;
import com.enation.app.shop.mobile.model.ApiBonus;
import com.enation.app.shop.mobile.model.ApiGoods;
import com.enation.app.shop.mobile.model.ApiStore;
import com.enation.app.shop.mobile.model.ApiStoreCat;
import com.enation.app.shop.mobile.service.ApiStoreManager;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.action.JsonResult;
import com.enation.framework.database.Page;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.JsonResultUtil;

/**
 * Author: Dawei
 * Datetime: 2016-11-02 17:10
 */
@Controller("mobileStoreApiController")
@RequestMapping("/api/mobile/store")
public class StoreApiController {

    @Autowired
    private IStoreManager storeManager;

    @Autowired
    private IB2b2cBonusManager b2b2cBonusManager;

    @Autowired
    private IStoreGoodsTagManager storeGoodsTagManager;

    @Autowired
    private IStoreGoodsManager storeGoodsManager;

    @Autowired
    private IStoreMemberManager storeMemberManager;

    @Autowired
    private IStoreCollectManager storeCollectManager;

    @Autowired
    private ApiStoreManager apiStoreManager;

    @Autowired
    private IStoreGoodsCatManager storeGoodsCatManager;

    private static final int PAGE_SIZE = 20;

    /**
     * 获取店铺详情
     *
     * @param storeid 店铺ID
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/detail", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult detail(Integer storeid) {
        Store store = storeManager.getStore(storeid);
        if (store == null) {
            return JsonResultUtil.getErrorJson("此店铺不存在！");
        }
        ApiStore apiStore = new ApiStore();
        try {
            BeanUtils.copyProperties(apiStore, store);
        } catch (Exception ex) {
        }

        //统计商品数量
        String[] marks = new String[]{"new", "hot", "recommend"};
        Map<String, List<Map>> goodsMap = new HashMap<>();
        for (String mark : marks) {
            Map map = new HashMap();
            map.put("mark", mark);
            map.put("storeid", storeid);

            Map result = new HashMap();
            //查询标签商品列表
            Page webpage = storeGoodsTagManager.getGoodsList(map, 1, 1);
            if (mark.equals("new")) {
                apiStore.setNew_num(webpage.getTotalCount());
            } else if (mark.equals("hot")) {
                apiStore.setHot_num(webpage.getTotalCount());
            } else if (mark.equals("recommend")) {
                apiStore.setRecommend_num(webpage.getTotalCount());
            }
        }

        //统计是否收藏
        StoreMember member = storeMemberManager.getStoreMember();
        if (member != null) {
            boolean result = storeCollectManager.isCollect(member.getMember_id(), storeid);
            apiStore.setFavorited(result ? 1 : 0);
        } else {
            apiStore.setFavorited(0);
        }

        return JsonResultUtil.getObjectJson(apiStore);
    }

    /**
     * 获取店铺的优惠券列表
     * @param storeid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/bonus-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult bonusList(Integer storeid) {
        Store store = storeManager.getStore(storeid);
        if (store == null) {
            return JsonResultUtil.getErrorJson("此店铺不存在！");
        }
        Map result = new HashMap();
        result.put("add_time_from", "");
        result.put("add_time_to", "");
        //标识是店铺显示 只显示没有过期的优惠券 当当前日期>优惠券最后时间  则过期
        String sign_time = DateUtil.toString(new Date(), "yyyy-MM-dd");
        result.put("sign_time", sign_time);
        Page bonusPage = b2b2cBonusManager.getConditionBonusList(1, 100, storeid, result);

        List<ApiBonus> bonuses = new ArrayList<>();
        List<StoreBonusType> list = (List<StoreBonusType>) bonusPage.getResult();
        for (StoreBonusType bonus : list) {
            ApiBonus apiBonus = new ApiBonus();
            try {
                BeanUtils.copyProperties(apiBonus, bonus);
            } catch (Exception ex) {
            }
            bonuses.add(apiBonus);
        }
        return JsonResultUtil.getObjectJson(bonuses);
    }



    /**
     * 获取首页要显示的商品
     *
     * @param storeid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/index-goods", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult indexGoods(Integer storeid) {
        Store store = storeManager.getStore(storeid);
        if (store == null) {
            return JsonResultUtil.getErrorJson("此店铺不存在！");
        }
        String[] marks = new String[]{"new", "hot", "recommend"};
        Map<String, List<Map>> goodsMap = new HashMap<>();
        for (String mark : marks) {
            Map map = new HashMap();
            map.put("mark", mark);
            map.put("storeid", storeid);

            Map result = new HashMap();
            //查询标签商品列表
            Page webpage = storeGoodsTagManager.getGoodsList(map, 1, 8);
            List<Map> list = (List<Map>) webpage.getResult();
            for (Map m : list) {
                if (m.containsKey("thumbnail") && m.get("thumbnail") != null) {
                    m.put("thumbnail", StaticResourcesUtil.convertToUrl(m.get("thumbnail").toString()));
                }
            }
            goodsMap.put(mark, list);
        }
        return JsonResultUtil.getObjectJson(goodsMap);
    }

    /**
     * 按标签显示商品，支持new、hot、recommend
     *
     * @param storeid
     * @param tag
     * @param page
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/tag-goods", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult tagGoods(Integer storeid,String tag,@RequestParam(value = "page", defaultValue = "1", required = false) Integer page) {
        if (StringUtils.isEmpty(tag)) {
            return JsonResultUtil.getErrorJson("系统参数错误！");
        }
        if (!tag.equals("new") && !tag.equals("hot") && !tag.equals("recommend")) {
            return JsonResultUtil.getErrorJson("系统参数错误！");
        }
        Store store = storeManager.getStore(storeid);
        if (store == null) {
            return JsonResultUtil.getErrorJson("此店铺不存在！");
        }

        Map map = new HashMap();
        map.put("mark", tag);
        map.put("storeid", storeid);
        
        Map result = new HashMap();
        //查询标签商品列表
        Page webpage = storeGoodsTagManager.getGoodsList(map, page, 8);
        List<Map> list = (List<Map>) webpage.getResult();
        for (Map m : list) {
            if (m.containsKey("thumbnail") && m.get("thumbnail") != null) {
                m.put("thumbnail", StaticResourcesUtil.convertToUrl(m.get("thumbnail").toString()));
            }
        }
        return JsonResultUtil.getObjectJson(list);
    }

    /**
     * 获取店铺的全部商品列表
     *
     * @param storeid
     * @param keyword
     * @param start_price
     * @param end_price
     * @param key
     * @param order
     * @param cat_id
     * @param page
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/goods-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult goodsList(Integer storeid,
                                @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                                @RequestParam(value = "start_price", required = false, defaultValue = "") String start_price,
                                @RequestParam(value = "end_price", required = false, defaultValue = "") String end_price,
                                @RequestParam(value = "key", required = false, defaultValue = "3") Integer key,
                                @RequestParam(value = "order", required = false, defaultValue = "DESC") String order,
                                @RequestParam(value = "cat_id", required = false, defaultValue = "0") Integer cat_id,
                                @RequestParam(value = "page", required = false, defaultValue = "1") Integer page) {
        Store store = storeManager.getStore(storeid);
        if (store == null) {
            return JsonResultUtil.getErrorJson("此店铺不存在！");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("storeid", storeid);
        params.put("keyword", keyword);
        params.put("start_price", start_price);
        params.put("end_price", end_price);
        params.put("key", key);
        params.put("order", order);
        params.put("stc_id", cat_id);
        Page webpage = this.storeGoodsManager.store_searchGoodsList(page, PAGE_SIZE, params);
        List<Map> list = (List<Map>) webpage.getResult();
        List<ApiGoods> goodsList = new ArrayList<>();
        for (Map m : list) {
            if (m.containsKey("thumbnail") && m.get("thumbnail") != null) {
                m.put("thumbnail", StaticResourcesUtil.convertToUrl(m.get("thumbnail").toString()));
            }
            ApiGoods apiGoods = new ApiGoods();
            try {
                BeanUtils.copyProperties(apiGoods, m);
            } catch (Exception ex) {
            }
            goodsList.add(apiGoods);
        }
        return JsonResultUtil.getObjectJson(goodsList);
    }

    /**
     * 关注店铺
     *
     * @param storeid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/collect", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult collect(Integer storeid) {
        StoreMember member = storeMemberManager.getStoreMember();
        if (member == null) {
            return JsonResultUtil.getErrorJson("请您登录后再关注此店铺");
        }
        Store store = storeManager.getStore(storeid);
        if (store == null) {
            return JsonResultUtil.getErrorJson("此店铺不存在！");
        }
        MemberCollect collect = new MemberCollect();
        collect.setMember_id(member.getMember_id());
        collect.setStore_id(storeid);
        boolean result = storeCollectManager.isCollect(member.getMember_id(), storeid);
        if (!result) {
            this.storeCollectManager.addCollect(collect);
            this.storeManager.addcollectNum(storeid);
        }
        return JsonResultUtil.getSuccessJson("关注店铺成功！");
    }

    /**
     * 取消关注店铺
     *
     * @param storeid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "uncollect", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult uncollect(Integer storeid) {
        StoreMember member = storeMemberManager.getStoreMember();
        if (member == null) {
            return JsonResultUtil.getErrorJson("请您登录后再取消关注此店铺");
        }
        Store store = storeManager.getStore(storeid);
        if (store == null) {
            return JsonResultUtil.getErrorJson("此店铺不存在！");
        }
        apiStoreManager.deleteCollect(storeid, member.getMember_id());
        this.storeManager.reduceCollectNum(storeid);
        return JsonResultUtil.getSuccessJson("取消关注店铺成功！");
    }

    /**
     * 获取店铺的商品分类
     *
     * @param storeid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/category", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult category(Integer storeid) {
        Store store = storeManager.getStore(storeid);
        if (store == null) {
            return JsonResultUtil.getErrorJson("此店铺不存在！");
        }
        List<Map> catList = storeGoodsCatManager.storeCatList(storeid);

        List<ApiStoreCat> apiCatList = new ArrayList<>();
        List<ApiStoreCat> subCatList = new ArrayList<>();
        for (Map map : catList) {
            ApiStoreCat cat = new ApiStoreCat();
            try {
                BeanUtils.copyProperties(cat, map);
            } catch (Exception ex) {
            }
            if (cat.getStore_cat_pid() != null && cat.getStore_cat_pid().intValue() == 0) {
                apiCatList.add(cat);
            } else {
                subCatList.add(cat);
            }
        }

        for (ApiStoreCat cat : apiCatList) {
            for (ApiStoreCat subcat : subCatList) {
                if (subcat.getStore_cat_pid() != null && subcat.getStore_cat_pid().equals(cat.getStore_cat_id())) {
                    cat.getChildren().add(subcat);
                }
            }
        }
        return JsonResultUtil.getObjectJson(apiCatList);
    }

    /**
     * 店铺搜索
     *
     * @param keyword
     * @param page
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult search(String keyword,
                             @RequestParam(value = "page", defaultValue = "1", required = false) Integer page) {
    	if(keyword == null){
            keyword = "";
        }
        Map params = new HashMap();
        params.put("name", keyword);
        params.put("store_credit", "");
        params.put("searchType", "");
        Page webPage = storeManager.store_list(params, 1, page, PAGE_SIZE);

        List<ApiStore> storeList = new ArrayList<>();
        List<Map> list = (List<Map>)webPage.getResult();
        for(Map map : list){
            ApiStore apiStore = new ApiStore();
            try{
                BeanUtils.copyProperties(apiStore, map);
            }catch(Exception ex){}
            apiStore.setStore_logo(StaticResourcesUtil.convertToUrl(apiStore.getStore_logo()));

            //商品列表
            Map goodsParams = new HashMap();
            goodsParams.put("store_id", apiStore.getStore_id());
            goodsParams.put("disable", 0);
            goodsParams.put("market_enable", 1);
            Page goodsPage = storeGoodsManager.storeGoodsList(1, 5, goodsParams);
            if(goodsPage != null && goodsPage.getResult() != null){
                List<Map> goodsMapList = (List<Map>)goodsPage.getResult();
                for(Map m : goodsMapList){
                    ApiGoods apiGoods = new ApiGoods();
                    try{
                        BeanUtils.copyProperties(apiGoods, m);
                    }catch(Exception ex){}
                    apiGoods.setThumbnail(StaticResourcesUtil.convertToUrl(apiGoods.getThumbnail()));
                    apiStore.getGoodsList().add(apiGoods);
                }
            }

            storeList.add(apiStore);
        }
        return JsonResultUtil.getObjectJson(storeList);
    }

}
