package com.enation.app.b2b2c.component.plugin.store.base;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.b2b2c.component.plugin.store.IStoreTabShowEvent;
import com.enation.app.b2b2c.core.store.model.Store;
import com.enation.app.b2b2c.core.store.service.IStoreClassifyManager;
import com.enation.eop.processor.core.freemarker.FreeMarkerPaser;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.plugin.AutoRegisterPlugin;

/**
 * 店铺基本信息插件页
 * @author Kanon
 *
 */
@Component
public class StoreBaseDataPlugin extends AutoRegisterPlugin implements IStoreTabShowEvent {

	@Autowired
	private IStoreClassifyManager storeClassifyManager;
	
	@Override
	public String getTabName() {
		// TODO Auto-generated method stub
		return "基本信息";
	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 1;
	}


	@Override
	public String getEditTabHtml(Store store) {
		FreeMarkerPaser freeMarkerPaser = FreeMarkerPaser.getInstance();
		freeMarkerPaser.setPageName("base");
		freeMarkerPaser.putData("store",store);
		
		Integer cityId = store.getStore_cityid();
		
		List classifys = this.storeClassifyManager.getByCityId(cityId);
		
		freeMarkerPaser.putData("classifys",classifys);
		
		return freeMarkerPaser.proessPageContent();
	}

	@Override
	public String getAddTabHtml() {
		HttpServletRequest request=ThreadContextHolder.getHttpRequest();
		
		
		FreeMarkerPaser freeMarkerPaser = FreeMarkerPaser.getInstance();
		freeMarkerPaser.setPageName("add_base");
		freeMarkerPaser.putData("uname",request.getParameter("uname"));
		return freeMarkerPaser.proessPageContent();
		}
}
