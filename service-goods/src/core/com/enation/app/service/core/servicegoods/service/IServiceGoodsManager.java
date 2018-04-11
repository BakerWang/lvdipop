package com.enation.app.service.core.servicegoods.service;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.app.service.core.servicegoods.model.ServiceGoods;
import com.enation.framework.database.Page;
/**
 * 
 * @ClassName: IServiceGoodsManager 
 * @Description: 服务商品接口
 * @author: liuyulei
 * @date: 2016年9月14日 下午4:50:46 
 * @since:v61
 */
public interface IServiceGoodsManager {

	/**
	 * 
	 * @Title: add 
	 * @Description: TODO 添加服务商品卡密信息
	 * @param sGoods
	 * @return: void
	 * @author： liuyulei
	 * @date：2016年9月14日 下午3:02:06
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void add(ServiceGoods sGoods);

	/**
	 * 
	 * @Title: edit 
	 * @Description: 服务商品卡密信息修改
	 * @param sGoods
	 * @return: void
	 * @author： liuyulei
	 * @date：2016年9月14日 下午3:02:42
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void edit(ServiceGoods sGoods);

	/**
	 * 
	 * @Title: selectCodeByParam 
	 * @Description: TODO  获取卡密列表
	 * @param itemId  订单项id
	 * @return 卡密列表
	 * @return: List 
	 * @author： liuyulei
	 * @date：2016年9月14日 下午3:02:56
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public List getCodeByParam(Integer itemId);

	/**
	 * 
	 * @Title: checkCode 
	 * @Description: TODO  校验 验证码
	 * @param Code  验证码
	 * @return 
	 * @return: Map
	 * @author： liuyulei
	 * @date：2016年9月14日 下午3:05:00
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public Map checkCode(String Code);

	/**
	 * 使用 修改服务状态
	 * 
	 * @param code
	 *            服务码 status 状态
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public Map used(String Code, Integer status);
	
	/**
	 * 
	 * @Title: queryIsEnable 
	 * @Description: TODO	查询是否可用    根据有效期
	 * @return
	 * @return: List<Map>
	 * @author： liuyulei
	 * @date：2016年9月18日 下午5:15:41
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Map> queryIsEnable();
	
	
	/**
	 * 
	 * @Title: searchServiceGoods 
	 * @Description: TODO  卡密列表信息查询
	 * @param goodsMap 搜索参数
	 * @param page 分页
	 * @param pageSize 分页每页数量
	 * @param other 其他
	 * @return
	 * @return: Page
	 * @author： liuyulei
	 * @date：2016年9月18日 下午5:50:47
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public Page searchServiceGoods(Map goodsMap, int page, int pageSize, String other,String sort,String order);
	
	
	/**
	 * 
	 * @Title: listServiceGoods 
	 * @Description: TODO  获取卡密列表
	 * @param pageNo
	 * @param pageSize
	 * @param map
	 * @return
	 * @return: Page
	 * @author： liuyulei
	 * @date：2016年9月19日 下午1:41:05
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public Page serviceGoodsList(Integer pageNo,Integer pageSize,Map map);
	
	/**
	 * 
	 * @Title: getServiceGoods 
	 * @Description: TODO  根据参数获取卡密信息
	 * @param map 参数map
	 * @return: Map  卡密信息
	 * @author： liuyulei
	 * @date：2016年9月19日 下午2:50:41
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public Map getServiceGoods(Map map);
	
	/**
	 * 
	 * @Title: seeCodeEdit 
	 * @Description: TODO   根据订单项id   修改卡密状态        查看卡密时修改
	 * @param itemid  订单项id
	 * @return: void
	 * @author： liuyulei
	 * @date：2016年9月20日 下午5:19:36
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void seeCodeEdit(Integer itemid);
	
	/**
	 * 
	 * @Title: getCodeLsitByOrderId     
	 * @Description: TODO   根据订单id获取卡密列表信息
	 * @param orderId  订单id
	 * @return: List
	 * @author： liuyulei
	 * @date：2016年9月21日 下午2:03:23
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public List getCodeLsitByOrderId(Integer orderId);
	
	

}
