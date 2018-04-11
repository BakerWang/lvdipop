package com.enation.app.service.core.servicegoods.service;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.app.service.core.servicegoods.model.BookingGoods;
import com.enation.framework.database.Page;

/**
 * 
 * @ClassName: IBookingGoodsManager 
 * @Description: 预约商品管理接口
 * @author: liuyulei
 * @date: 2016年9月23日 上午9:47:38 
 * @since:v61
 */
public interface IBookingGoodsManager {

	/**
	 * 
	 * @Title: add 
	 * @Description: TODO  添加预约性质商品
	 * @param bookingGoods
	 * @return: void
	 * @author： liuyulei
	 * @date：2016年9月23日 上午9:48:49
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void add(BookingGoods bookingGoods);
	
	/**
	 * 
	 * @Title: edit 
	 * @Description: TODO  修改预约商品
	 * @param bookingGoods
	 * @return: void
	 * @author： liuyulei
	 * @date：2016年9月23日 上午9:50:00
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void  edit(BookingGoods bookingGoods) ;
	
	/**
	 * 
	 * @Title: queryByBookingGoodsId 
	 * @Description: TODO  根据id查询预约信息
	 * @param id   预约信息id
	 * @return
	 * @return: BookingGoods   预约信息
	 * @author： liuyulei
	 * @date：2016年9月23日 上午9:51:45
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public BookingGoods queryByBookingGoodsId(Integer id);
	
	/**
	 * 
	 * @Title: queryListByMemberId 
	 * @Description:   根据会员id查询预约信息列表
	 * @param member_id  会员id
	 * @return
	 * @return: List  预约信息列表
	 * @author： liuyulei
	 * @date：2016年9月23日 上午9:53:17
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public List queryListByMemberId(Integer member_id);
	
	/**
	 * 
	 * @Title: queryListByStoreId 
	 * @Description: TODO  根据店铺id查询预约信息列表
	 * @param store_id 店铺id
	 * @return
	 * @return: List  预约信息列表
	 * @author： liuyulei
	 * @date：2016年9月23日 上午9:54:38
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public List queryListByStoreId(Integer store_id);
	
	/**
	 * 
	 * @Title: searchBookingGoods 
	 * @Description: TODO  搜索预约商品
	 * @param pageNo  
	 * @param pageSize
	 * @param map
	 * @return
	 * @return: Page  预约商品数据  分页
	 * @author： liuyulei
	 * @date：2016年9月23日 上午10:01:11
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public Page searchBookingGoods(Integer pageNo,Integer pageSize,Map map);
	
	
	
	
	
}
