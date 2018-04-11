package com.enation.app.service.component.plugin;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.ctc.wstx.util.StringUtil;
import com.enation.app.shop.core.goods.plugin.IGoodsBackendSearchFilter;
import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.model.Refund;
import com.enation.app.shop.core.order.plugin.order.IOrderRefundEvent;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.plugin.AutoRegisterPlugin;

/**
 * 
 * @ClassName: ServiceGoodsOrderShipPlugin
 * @Description: 服务商品插件
 * @author: liuyulei
 * @date: 2016年9月18日 下午4:34:11
 * @since:v61
 */
@Component
public class ServiceGoodsPlugin extends AutoRegisterPlugin implements IOrderRefundEvent, IGoodsBackendSearchFilter {

	@Override
	public void onRefund(Order order, Refund refund) {
		System.out.println("退款事件");
	}

	@Override
	public String getSelector() {
		return "";
	}

	@Override
	public String getFrom() {
		return "";
	}

	/**
	 * 拼装后台商品查询的sql中的Where部分<br>
	 * 加入自营店的店铺id过滤条件
	 */
	@Override
	public void filter(StringBuffer sql) {
		// 判断如果是服务商品表查询才执行此逻辑
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();

		try {
			// 如果是传递了goods_type参数 代表服务商品列表
			String goods_type = request.getParameter("goods_type");
			if (com.enation.framework.util.StringUtil.isEmpty(goods_type)) {
				return;
			}
			sql.append(" and g.goods_type =  " + goods_type);
		} catch (Exception e) {
			this.logger.error("服务商品列表查询失败");
			e.printStackTrace();
		}
		
		
	}

}
