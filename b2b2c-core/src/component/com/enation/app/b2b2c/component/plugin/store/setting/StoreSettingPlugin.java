package com.enation.app.b2b2c.component.plugin.store.setting;

import org.springframework.stereotype.Component;

import com.enation.app.b2b2c.core.store.model.StoreSetting;
import com.enation.app.base.core.plugin.setting.IOnSettingInputShow;
import com.enation.app.base.core.plugin.setting.IOnSettingSaveEnvent;
import com.enation.framework.plugin.AutoRegisterPlugin;

/**
 * 店铺设置插件
 * @author Kanon
 *
 */
@Component
public class StoreSettingPlugin extends AutoRegisterPlugin implements IOnSettingInputShow,IOnSettingSaveEnvent{

	@Override
	public String onShow() {
		// TODO Auto-generated method stub
		return "store-setting";
	}

	@Override
	public String getSettingGroupName() {
		// TODO Auto-generated method stub
		return StoreSetting.setting_key;
	}

	@Override
	public String getTabName() {
		// TODO Auto-generated method stub
		return "店铺设置";
	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 3;
	}

	@Override
	public void onSave() {
		StoreSetting.load();
		
	}


}
