package com.enation.app.service.component.plugin;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.b2b2c.core.goods.model.StoreGoods;
import com.enation.app.b2b2c.core.goods.service.IStoreGoodsManager;
import com.enation.app.base.core.plugin.job.IEveryHourExecuteEvent;
import com.enation.app.service.core.servicegoods.model.ServiceGoods;
import com.enation.app.service.core.servicegoods.service.IServiceGoodsManager;
import com.enation.app.service.core.utils.GoodsType;
import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.plugin.order.IOrderConfirmPayEvent;
import com.enation.app.shop.core.order.service.OrderStatus;
import com.enation.app.shop.core.order.service.impl.OrderManager;
import com.enation.eop.sdk.context.EopSetting;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.plugin.AutoRegisterPlugin;
import com.enation.framework.util.DateUtil;

/**
 * 服务商品 确认付款插件
 * 
 * @author liuyulei 2016-09-13
 *
 */
@Component
public class ServiceGoodsOrderConfirmPayPlugin extends AutoRegisterPlugin
		implements IOrderConfirmPayEvent, IEveryHourExecuteEvent {

	@Autowired
	private OrderManager orderManager;

	@Autowired
	private IServiceGoodsManager serviceGoodsManager;

	@SuppressWarnings("rawtypes")
	@Autowired
	private IDaoSupport daoSupport;
	
	@Autowired
	private IStoreGoodsManager storeGoodsManager;


	/**
	 * 确认付款
	 * <p>
	 * 根据订单项中商品类型<br>
	 * 若是服务商品 ,确认付款后, 修改订单状态为成功,货运状态修改为已收货 <span>goods_type=1</span><br>
	 * </p>
	 * <p>
	 * 并且在服务商品订单表中添加数据，根据订单项中商品数量循环添加
	 * </p>
	 */
	@Override
	public void confirmPay(Order order) {
		List<Map> orderItems = orderManager.getOrderItem(order.getOrder_id());
		boolean isServiceGoods = true;// 订单项是否全部为服务商品 默认为是
		if(orderItems!=null && orderItems.isEmpty()){
			isServiceGoods = false;
		}
		for (Map map : orderItems) {
			Integer goods_type = (Integer) map.get("goods_type");
			Integer goods_id = (Integer) map.get("goods_id");
			this.storeGoodsManager.getGoods(goods_id);
			if (goods_type.equals(GoodsType.ServiceGoods.getType())) {// 判断是否为服务商品
																		// 确认是否添加服务商品属性信息
				int count = (Integer) map.get("num");
				for (int i = 0; i < count; i++) {
					ServiceGoods sGoods = new ServiceGoods();
					sGoods.setItem_id((Integer) map.get("item_id"));
					sGoods.setCode(this.createCode()); //生成服务码
					sGoods.setValid_term(15); // 默认有效期十五天
					sGoods.setCreate_time(DateUtil.getDateline()); // 当前日期 即购买日期
					sGoods.setLook_status(0);
					if("b2b2c".equals(EopSetting.PRODUCT)){     //如果是多店则设置店铺id
						sGoods.setStore_id(this.getStoreIdByGoodsId(goods_id));
					}

					Calendar rightNow = Calendar.getInstance();
					rightNow.setTime(new Date(sGoods.getCreate_time() * 1000));
					rightNow.add(Calendar.DATE, sGoods.getValid_term());
					sGoods.setEnable_time(rightNow.getTimeInMillis() / 1000); // 可用时间
																				// 即create_time~enable_time期间
																				// 为可用日期
					sGoods.setStatus(0); // 默认为 0 为 可以使用
					this.serviceGoodsManager.add(sGoods);
				}
			} else {
				isServiceGoods = false;
			}
		}
		int shipStatus = OrderStatus.SHIP_ROG;
		if (isServiceGoods) { // 判断订单中全部是服务商品
			// 更新订单状态为已发货
			StringBuffer sql = new StringBuffer();
			sql.append("update es_order set status=?,ship_status=?,sale_cmpl_time=? ");
			sql.append(" where order_id=?");
			try {
				this.daoSupport.execute(sql.toString(), OrderStatus.ORDER_COMPLETE, shipStatus, DateUtil.getDateline(),
						order.getOrder_id());
			} catch (Exception e) {
				this.logger.error("服务商品，确认付款后，修改订单状态失败!", e);
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non Javadoc)
	 * 
	 * @Title: everyHour
	 * 
	 * @Description: TODO 定时任务 每小时修改数据   条件  可用日期小于等于当前日期    修改  status=2
	 * 
	 * @see
	 * com.enation.app.base.core.plugin.job.IEveryHourExecuteEvent#everyHour()
	 * 
	 * @author： liuyulei
	 * 
	 * @date：2016年9月18日 下午1:06:57
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void everyHour() {
		try {
			Map map = new HashMap<Object,Object>();
			map.put("status", 2);      //设置键值对   status=2
			this.daoSupport.update("es_goods_servicegoods", map, 
					" FROM_unixtime(enable_time) > SYSDATE() = 0 ");//满足该条件的修改状态
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 创建服务号（日期+两位随机数）
	 */
	private String createCode() {
		boolean isHave = true; // 数据库中是否存在该服务号 code
		String code = ""; // 服务号 code
		// 如果存在当前服务号
		while (isHave) {
			StringBuffer codeSb = new StringBuffer(DateUtil.getDateline() + "");
			codeSb.append(getRandom());
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT count(service_id) FROM es_goods_servicegoods WHERE code =?");
			int count = this.daoSupport.queryForInt(sql.toString(), codeSb.toString());
			if (count == 0) { // 如果验证码不存在
				code = codeSb.toString();
				isHave = false;
			}
		}
		return code;
	}
	
	
	/**
	 * 
	 * @Title: getStoreIdByGoodsId 
	 * @Description: TODO  根据商品id获取店铺id
	 * @param goodsId   商品id
	 * @return: Integer   店铺id  
	 * @author： liuyulei
	 * @date：2016年9月21日 下午5:58:50
	 */
	private Integer getStoreIdByGoodsId(Integer goodsId){
		StoreGoods storeGoods = this.storeGoodsManager.getGoods(goodsId);
		return storeGoods.getStore_id();
	}
	

	/**
	 * 获取随机数
	 * 
	 * @return
	 */
	private int getRandom() {
		Random random = new Random();
		int num = Math.abs(random.nextInt()) % 100;
		if (num < 10) { // 取两位随机数
			num = getRandom();
		}
		return num;
	}

}
