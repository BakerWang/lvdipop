package com.enation.app.shop.component.bonus.api;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;

import com.enation.app.base.core.model.Member;
import com.enation.app.shop.component.bonus.model.MemberBonus;
import com.enation.app.shop.component.bonus.service.BonusSession;
import com.enation.app.shop.component.bonus.service.IBonusManager;
import com.enation.app.shop.core.order.service.ICartManager;
import com.enation.eop.sdk.context.UserConext;
import com.enation.framework.action.JsonResult;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;

/***
 * 红包API
 * @author Kanon 2016-2-29;6.0版本改造
 *
 */
@Controller
@Scope("prototype")
@RequestMapping("/api/shop/bonus")
public class BonusApiController  {
	protected  Logger logger = Logger.getLogger(getClass());
	@Autowired
	private IBonusManager bonusManager;
	
	@Autowired
	private ICartManager cartManager;
	
	
	/**
	 * 获取会员可用红包列表
	 * @param 无
	 * @return 红包列表，List<Map>型
	 * map内容
	 * type_name:红包名称
	 * type_money:红包金额
	 * send_type：红包类型 (0会员发放，1:按商品发放,2:按订单发放,3:线下发放的红包)
	 */
	@ResponseBody
	@RequestMapping(value="/get-member-bonus")
	public Object getMemberBonus(){
		try {
			Member member = UserConext.getCurrentMember();
			if(member ==null){
				return JsonResultUtil.getErrorJson("未登录，不能使用此api");
			}
			
			Double goodsprice = cartManager.countGoodsTotal(RequestContextHolder.getRequestAttributes().getSessionId());
			List bonusList  = bonusManager.getMemberBonusList(member.getMember_id(), goodsprice,1);
			return bonusList;
		} catch (Exception e) {
			this.logger.error("调用获取会员红包api出错",e);
			return JsonResultUtil.getErrorJson(e.getMessage());
		}
	
		
	}
	
	/**
	 * 使用一个红包
	 * @param bonusid:红包id
	 * @return json字串
	 * result  为1表示调用成功0表示失败 ，int型
	 * message 为提示信息
	 */
	@ResponseBody
	@RequestMapping(value="/use-one")
	public JsonResult useOne(Integer bonusid){
		try {
			
			//清除使用的红包
			if(bonusid==0){
				BonusSession.clean();
				return JsonResultUtil.getErrorJson("清除红包成功");
			}
			
			MemberBonus bonus  =bonusManager.getBonus(bonusid);
			
		
			
			Double goodsprice = cartManager.countGoodsTotal(RequestContextHolder.getRequestAttributes().getSessionId());
			if(goodsprice<= bonus.getMin_goods_amount()){
				
				return JsonResultUtil.getErrorJson("订单的商品金额不足["+bonus.getMin_goods_amount()+"],不能使用此红包");
				
			}
			
			BonusSession.useOne(bonus);
			return JsonResultUtil.getSuccessJson("红包使用成功");
		} catch (Exception e) {
			this.logger.error("使用红包发生错误",e);
			return JsonResultUtil.getErrorJson("使用红包发生错误["+e.getMessage()+"]");
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/use-sn")
	public JsonResult useSn( String sn, Double needPay){
		try {
			
			 
			if(StringUtil.isEmpty(sn)){
				
				return JsonResultUtil.getErrorJson("红包编号不能为空");
			}
			
			MemberBonus bonus  =bonusManager.getBonus(sn);
			if(bonus==null){
				
				return JsonResultUtil.getErrorJson("您输入的红包编号不正确");
			}
			
			
			if(bonus.getUsed_time()!=null){
				
				return JsonResultUtil.getErrorJson("此红包已被使用过");
			}
	
	
			Double goodsprice = cartManager.countGoodsTotal(RequestContextHolder.getRequestAttributes().getSessionId());
			if(goodsprice<= bonus.getMin_goods_amount()){
				
				return JsonResultUtil.getErrorJson("订单的商品金额不足["+bonus.getMin_goods_amount()+"],不能使用此红包");
				
			}
			
			//判断当前订单金额是否小于使用的优惠券最低订单金额，如果小于，那么此优惠券不能再用
			//add by DMRain 2016-4-28
			if (needPay <= bonus.getMin_goods_amount()) {
				return JsonResultUtil.getErrorJson("订单的商品金额不足["+bonus.getMin_goods_amount()+"],不能使用此红包");
			}
			
			long now = DateUtil.getDateline();
			if(bonus.getUse_start_date() > now){
				long l=Long.valueOf(bonus.getUse_start_date())*1000;
				return JsonResultUtil.getErrorJson("此红包还未到使用期，开始使用时间为["+DateUtil.toString(new Date(l), "yyyy年MM月dd日")+"]");
			}
			
			if(bonus.getUse_end_date() < now){
				long l=Long.valueOf(bonus.getUse_end_date())*1000;
				return JsonResultUtil.getErrorJson("此红包已过期，使用截至时间为["+DateUtil.toString(new Date(l), "yyyy年MM月dd日")+"]");
			}
			
			BonusSession.use(bonus);
			return JsonResultUtil.getSuccessJson("红包使用成功");
		} catch (Exception e) {
			
			this.logger.error("使用红包发生错误",e);
			return JsonResultUtil.getErrorJson("使用红包发生错误["+e.getMessage()+"]");
		}
	}
	
	/**
	 * 
	 * 用sn取消一个红包的使用
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/cancel-sn")
	public JsonResult cancelSn( String sn){
		
		try {
			if(StringUtil.isEmpty(sn)){
				
				return JsonResultUtil.getErrorJson("编号不能为空");
			}
			BonusSession.cancel(sn);
			return JsonResultUtil.getSuccessJson("取消成功");
		} catch (Exception e) {
			this.logger.error("取消红包发生错误",e);
			return JsonResultUtil.getErrorJson("取消红包发生错误["+e.getMessage()+"]");
		}
	}
	
	
	/**
	 * 获取使用的红包的金额
	 * @param 获取正在使用的红包金额
	 * @return json字串
	 * result  为1表示调用成功0表示失败 ，int型
	 * message 如果错误，为提示信息
	 * money: 如果调用成功，为使用红包的总金额
	 */
	@ResponseBody
	@RequestMapping(value="/get-use-bonus-money")
	public Object getUseBonusMoney(){
		
		try {
			double moneyCount = BonusSession.getUseMoney();
			
			Map map=new HashMap();
			map.put("result", 1);
			map.put("money", moneyCount);
			return map;
		
		} catch (Exception e) {
			this.logger.error("获取红包金额出错", e);
			return JsonResultUtil.getErrorJson("获取红包金额出错["+e.getMessage()+"]");
		}
	}
}
