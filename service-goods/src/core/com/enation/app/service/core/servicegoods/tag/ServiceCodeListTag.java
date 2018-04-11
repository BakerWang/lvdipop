package com.enation.app.service.core.servicegoods.tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.service.core.servicegoods.model.ServiceGoods;
import com.enation.app.service.core.servicegoods.service.IServiceGoodsManager;
import com.enation.framework.taglib.BaseFreeMarkerTag;

import freemarker.template.TemplateModelException;

/**
 * 
 * @ClassName: ServiceCodeListTag
 * @Description: 服务型商品验证码列表 标签
 * @author: liuyulei
 * @date: 2016年9月13日 下午4:24:35
 */
@Component
public class ServiceCodeListTag extends BaseFreeMarkerTag {

	@Autowired
	private IServiceGoodsManager serviceGoodsManager;

	/**
	 * 根据订单项id 获取服务商品卡密信息
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Object exec(Map params) throws TemplateModelException {
		Integer itemid = (Integer) params.get("itemid");
		List<ServiceGoods> sGoodsList = new ArrayList<ServiceGoods>();
		
		Map map = new HashMap();
		try {
			sGoodsList = serviceGoodsManager.getCodeByParam(itemid);

			String isSeeCode = "0";
			if(sGoodsList!=null){
				for (ServiceGoods serviceGoods : sGoodsList) {
					if(serviceGoods.getLook_status()==1){  //判断卡密是否查 看   即  status = 1
						isSeeCode = "1";
					}
				}
			}
			
			map.put("sGoodsList", sGoodsList);
			map.put("isSeeCode", isSeeCode);
		} catch (Exception e) {
			this.logger.error("获取卡密列表失败!", e);
			e.printStackTrace();
		}
		
		return map;
	}

}
