package com.enation.app.service.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @ClassName: GoodsType 
 * @Description: 商品类型   enum 
 * @author: liuyulei
 * @date: 2016年9月14日 下午3:05:49
 */
public enum GoodsType {
	GeneralGoods("普通商品", 0), ServiceGoods("服务商品", 1),BookingGoods("预约商品",2);

	private Integer type;
	private String name;

	private GoodsType(String name, Integer type) {
		this.type = type;
		this.name = name;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 
	 * @Title: getGoodsTypeList 
	 * @Description: TODO  获取商品类型列表
	 * @return
	 * @return: List
	 * @author： liuyulei
	 * @date：2016年9月22日 下午1:46:48
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List getGoodsTypeList(){
		Map<Integer,Object> goodsTypes = new HashMap<Integer, Object>();
		//商品类型
		goodsTypes.put(GoodsType.GeneralGoods.getType(), GoodsType.GeneralGoods.getName());
		goodsTypes.put(GoodsType.ServiceGoods.getType(), GoodsType.ServiceGoods.getName());
		goodsTypes.put(GoodsType.BookingGoods.getType(), GoodsType.BookingGoods.getName());
		
		List list = new ArrayList(); 
		for (Integer str : goodsTypes.keySet()) {
			Map map=new HashMap();
	    	map.put("key", str);
	    	map.put("value", goodsTypes.get(str));
	    	list.add(map);
		}
		return list;
	}
	
	/**
	 * 
	 * @Title: getGoodsTypeMap 
	 * @Description: TODO  获取商品类型列表
	 * @return
	 * @return: List
	 * @author： liuyulei
	 * @date：2016年9月22日 下午2:47:52
	 */
	public static Map getGoodsTypeMap(){
		Map<Integer,Object> goodsTypes = new HashMap<Integer, Object>();
		
		//商品类型
		goodsTypes.put(GoodsType.GeneralGoods.getType(), GoodsType.GeneralGoods.getName());
		goodsTypes.put(GoodsType.ServiceGoods.getType(), GoodsType.ServiceGoods.getName());
		goodsTypes.put(GoodsType.BookingGoods.getType(), GoodsType.BookingGoods.getName());
		
		return goodsTypes;
	}
	
}
