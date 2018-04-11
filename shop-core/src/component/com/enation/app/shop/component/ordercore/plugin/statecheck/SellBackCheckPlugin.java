package com.enation.app.shop.component.ordercore.plugin.statecheck;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.base.core.plugin.job.IEveryDayExecuteEvent;
import com.enation.app.shop.component.ordercore.plugin.setting.OrderSetting;
import com.enation.app.shop.core.order.model.SellBackList;
import com.enation.app.shop.core.order.model.SellBackStatus;
import com.enation.app.shop.core.order.service.ISellBackManager;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.plugin.AutoRegisterPlugin;
import com.enation.framework.util.DateUtil;

/**
 * 售后检测插件
 * 超时未入库则取消退货申请
 * @author Kanon
 *
 */
@Component
public class SellBackCheckPlugin extends AutoRegisterPlugin implements IEveryDayExecuteEvent{

	@Autowired
	private IDaoSupport daoSupport;
	
	@Autowired
	private ISellBackManager sellBackManager;
	@Override
	public void everyDay() {
		Integer time=OrderSetting.getCancel_sellback_day()*24*60*60;
		String sql="select * from es_sellback_list where type=2 and tradestatus=? and confirm_time+?<?";
		List<SellBackList>  sellBakc_list  =  this.daoSupport.queryForList(sql,SellBackList.class, SellBackStatus.in_storage.getValue(),time,DateUtil.getDateline());
		
		//取消退货申请
		for (SellBackList sellBackList : sellBakc_list) {
			
			sellBackManager.cancle(sellBackList);
		}
	}

}
