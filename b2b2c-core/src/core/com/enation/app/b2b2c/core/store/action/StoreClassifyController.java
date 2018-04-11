package com.enation.app.b2b2c.core.store.action;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.enation.app.b2b2c.core.store.model.StoreClassify;
import com.enation.app.b2b2c.core.store.service.IStoreClassifyManager;
import com.enation.app.base.core.model.Regions;
import com.enation.app.base.core.service.IRegionsManager;
import com.enation.framework.action.JsonResult;
import com.enation.framework.util.JsonResultUtil;

/**
 * 店铺分类后台Controller
 * @author Sylow
 * @version v1.0
 * @since v6.1
 */
@Controller
@RequestMapping("/b2b2c/admin/classify")
public class StoreClassifyController {

	@Autowired
	private IStoreClassifyManager storeClassifyManager;
	
	@Autowired
	private IRegionsManager regionsManager;
	
	/**
	 * 跳转到店铺分类管理页面
	 * @return 店铺分类管理页面
	 */
	@RequestMapping("/list")
	public String list(){
		return "/b2b2c/admin/classify/classify_list";
	}
	
	
	@ResponseBody
	@RequestMapping("/list-children")
	public Object listChildren(Integer parent_id){
		
		return this.storeClassifyManager.listChildrenAsyn(parent_id);
	}
	
	@ResponseBody
	@RequestMapping("/list-json")
	public Object listJson(){
		
		return this.storeClassifyManager.getAll();
	}
	
	
	@RequestMapping("/add")
	public ModelAndView add(){
		ModelAndView view=new ModelAndView();
		view.addObject("regions", this.regionsManager.listChildren(0));
		view.setViewName("/b2b2c/admin/classify/classify_add");
		return view;	
	}
	
	@RequestMapping("/edit")
	public ModelAndView edit(Integer classify_id){	
		ModelAndView view=new ModelAndView();
		
		StoreClassify storeClassify = storeClassifyManager.getStoreClassify(classify_id);
		Integer grade = storeClassify.getGrade();
		
		Regions region = null;
		
		Integer nowAddressId = 0;
		
		// 根据等级 获取省 或者市,获取同级地区
		if (grade.intValue() == 1) {
			nowAddressId = storeClassify.getProvince_id();
			region = this.regionsManager.get(nowAddressId);
		} else if (grade.intValue() == 2) {
			nowAddressId = storeClassify.getCity_id();
			region = this.regionsManager.get(nowAddressId);
			
		} else {
			nowAddressId = storeClassify.getCity_id();
			region = this.regionsManager.get(nowAddressId);
		}
		
		// 再获取同级所有地区
		Integer pid = region.getP_region_id();
		view.addObject("regions", this.regionsManager.listChildren(pid));
		
		view.addObject("classifys", storeClassify);
		view.addObject("address_id", nowAddressId);
		
		view.setViewName("/b2b2c/admin/classify/classify_edit");
		return view;	
	}

	
	@RequestMapping("/children")
	public ModelAndView children(Integer classify_id){
		ModelAndView view=new ModelAndView();
		StoreClassify storeClassify = storeClassifyManager.getStoreClassify(classify_id);
		Integer grade = storeClassify.getGrade();
		
		
		//获取正确的父级id
		Integer pid = 0;
		
		// 如果当前是省级 就获取 市级作为列表，如果当前为市级 还是选择市级作为候选列表
		if (grade.intValue() == 1) {
			pid = storeClassify.getProvince_id();
		} else if (grade.intValue() == 2) {
			Regions region = this.regionsManager.get(storeClassify.getCity_id());
			pid = region.getP_region_id();
		} else {
			Regions region = this.regionsManager.get(storeClassify.getCity_id());
			pid = region.getP_region_id();
		}
		
		
		view.addObject("regions", this.regionsManager.listChildren(pid));
		view.addObject("classifys", storeClassify);
		view.setViewName("/b2b2c/admin/classify/classify_children");
		return view;	
	}
	
	
	@ResponseBody
	@RequestMapping("/save-add")
	public JsonResult saveAdd(StoreClassify store_classify, Integer address_id){
		try{
			
			String regionName = this.getRegionName(address_id);
			store_classify = this.setStoreClassifyAddress(store_classify, regionName, address_id);
			storeClassifyManager.add(store_classify);
			return JsonResultUtil.getSuccessJson("分类添加成功");
		}catch(Exception e){
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("分类添加失败");
		}
	}
	
	@ResponseBody
	@RequestMapping("/save-edit")
	public JsonResult saveEdit(StoreClassify store_classify, Integer address_id){
		try{
			String regionName = this.getRegionName(address_id);
			store_classify = this.setStoreClassifyAddress(store_classify, regionName, address_id);
			storeClassifyManager.edit(store_classify);
			return JsonResultUtil.getSuccessJson("修改成功");
			
		}catch(Exception e){
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("修改失败");
		}
	}
	
	
	
	@ResponseBody
	@RequestMapping("/save-add-children")
	public JsonResult saveAddChildren(StoreClassify store_classify, Integer address_id){
		try{
			String regionName = this.getRegionName(address_id);
			store_classify = this.setStoreClassifyAddress(store_classify, regionName, address_id);
			storeClassifyManager.add(store_classify);
			return JsonResultUtil.getSuccessJson("子分类添加成功");
		}catch(Exception e){
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("子分类添加失败");
		}
	}
	
	@ResponseBody
	@RequestMapping("/delete")
	public JsonResult delete(Integer classify_id){
		try {
			this.storeClassifyManager.delete(classify_id);
			return JsonResultUtil.getSuccessJson("删除成功");
		} catch (RuntimeException e) {
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("删除失败");
		}
	}
	
	@ResponseBody
	@RequestMapping("/get-list-by-city-id")
	public JsonResult getListByCityId(Integer city_id){
		try {
			List list = this.storeClassifyManager.getByCityId(city_id);
			return JsonResultUtil.getObjectJson(list);
		} catch (RuntimeException e) {
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("获取数据失败:" + e.getMessage());
		}
	}
	

	/**
	 *  设置分类里的地区
	 * @param storeClassify 分类对象
	 * @param regionName
	 * @param regionId
	 * @return 传递过来的 storeClassify 分类对象
	 */
	private StoreClassify setStoreClassifyAddress(StoreClassify storeClassify, String regionName, Integer regionId){
		
		Integer grade = storeClassify.getGrade();
		
		if (grade.intValue() == 1) {
			storeClassify.setProvince_id(regionId);
			storeClassify.setProvince_name(regionName);
		} else if (grade.intValue() == 2) {
			storeClassify.setCity_id(regionId);
			storeClassify.setCity_name(regionName);
		} else {
			storeClassify.setCity_id(regionId);
			storeClassify.setCity_name(regionName);
		}
		
		
		return storeClassify;
	}
	
	/**
	 * 获取一个地区的名字
	 * @param regionId 地区id
	 * @return 地区名字
	 */
	private String getRegionName(Integer regionId) {
		
		
		String name = "";
		
		if (regionId != 0) {
			Regions regions = this.regionsManager.get(regionId);
			name = regions.getLocal_name();
		}
		
		return name;
	}
	
}
