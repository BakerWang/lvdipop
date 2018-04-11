package com.enation.app.shop.front.api.order;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.base.core.model.Member;
import com.enation.app.base.core.service.IMemberManager;
import com.enation.app.base.core.service.ISettingService;
import com.enation.app.shop.core.goods.model.Goods;
import com.enation.app.shop.core.goods.model.Product;
import com.enation.app.shop.core.goods.service.IGoodsManager;
import com.enation.app.shop.core.goods.service.IProductManager;
import com.enation.app.shop.core.order.model.Cart;
import com.enation.app.shop.core.order.model.support.CartItem;
import com.enation.app.shop.core.order.model.support.OrderPrice;
import com.enation.app.shop.core.order.plugin.cart.CartPluginBundle;
import com.enation.app.shop.core.order.service.ICartManager;
import com.enation.eop.sdk.context.UserConext;
import com.enation.framework.action.GridController;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;
import com.enation.framework.util.TestUtil;

/**
 * 购物车api
 * @author kingapex
 *2013-7-19下午12:58:43
 * @version 2.0,2016-02-16 Sylow v60版本改造
 * @since v6.0
 */
@Controller 
@RequestMapping("/api/shop/cart")
public class CartApiController extends GridController {
	
	@Autowired
	private ICartManager cartManager;
	@Autowired
	private IProductManager productManager;
	@Autowired
	private IMemberManager memberManager;
	@Autowired
	private ISettingService settingService;
	@Autowired
	private IGoodsManager goodsManager;
	@Autowired
	private CartPluginBundle cartPluginBundle;
	/**
	 * 将一个货品添加至购物车。
	 * 需要传递productid和num参数
	 * 
	 * @param productid 货品id，int型
	 * @num 数量，int 型
	 * 
	 * @return 返回json串
	 * result  为1表示调用成功0表示失败 ，int型
	 * message 为提示信息
	 */
	@ResponseBody  
	@RequestMapping(value="/add-product", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult addProduct(Integer productid, Integer num, Integer showCartData, Integer activity_id){
		// 无效参数判定
		if (showCartData == null) {
			showCartData = 0;
		}
		Product product = productManager.get(productid);
		return this.addProductToCart(product, num, showCartData, activity_id);
	}
	
	/**
	 * 将一个商品添加到购物车
	 * 需要传递goodsid 和num参数
	 * @param goodsid 商品id，int型
	 * @param num 数量，int型
	 * 
	 * @return 返回json串
	 * result  为1表示调用成功0表示失败
	 * message 为提示信息
	 */
	@ResponseBody  
	@RequestMapping(value="/add-goods", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult addGoods(Integer goodsid, Integer num,@RequestParam(value = "showCartData", required = false)  Integer showCartData, Integer activity_id){

		Product product = productManager.getByGoodsId(goodsid);
		showCartData=showCartData==null?0:showCartData;
		return this.addProductToCart(product, num, showCartData, activity_id);
	}
	
	/**
	 * 获取购物车数据
	 * @param 无
	 * @return 返回json串
	 * result  为1表示调用成功0表示失败
	 * data.count：购物车的商品总数,int 型
	 * data.total:购物车总价，int型
	 * 
	 */
	@ResponseBody  
	@RequestMapping(value="/get-cart-data", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult getCartData(){
		
		try{
			String sessionid =ThreadContextHolder.getHttpRequest().getSession().getId();
			
			Double goodsTotal = cartManager.countGoodsTotal( sessionid );
			int count = this.cartManager.countItemNum(sessionid);
			
			java.util.Map<String, Object> data =new HashMap();
			data.put("count", count);//购物车中的商品数量
			data.put("total", goodsTotal);//总价
			
			return JsonResultUtil.getObjectJson(data);
		}catch(RuntimeException e ){
			TestUtil.print(e);
			this.logger.error("获取购物车数据出错",e);
			return JsonResultUtil.getErrorJson("获取购物车数据出错");
		}
	}
	
	
	
	/**
	 * 删除购物车一项
	 * @param cartid:要删除的购物车id,int型,即 CartItem.item_id
	 * 
	 * @return 返回json字串
	 * result  为1表示调用成功0表示失败
	 * message 为提示信息
	 * 
	 * {@link CartItem }
	 */
	@ResponseBody  
	@RequestMapping(value="/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult delete(){
		try{
			HttpServletRequest request =ThreadContextHolder.getHttpRequest();
			String cartid= request.getParameter("cartid");
			cartManager.delete(request.getSession().getId(), Integer.valueOf(cartid));
			return JsonResultUtil.getSuccessJson("删除购物项成功");
		}catch(RuntimeException e){
			TestUtil.print(e);
			this.logger.error("删除购物项失败",e);
			return JsonResultUtil.getErrorJson("删除购物项失败");
		}
	}
	
	/**
	 * 更新购物车的数量
	 * @param cartid:要更新的购物车项id，int型，即 CartItem.item_id
	 * @param num:要更新数量,int型
	 * @return 返回json字串
	 * result： 为1表示调用成功0表示失败 int型
	 * store: 此商品的库存 int型
	 */
	@ResponseBody  
	@RequestMapping(value="/update-num", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult updateNum(){
		try{
			HttpServletRequest request =ThreadContextHolder.getHttpRequest();
			String cartid= request.getParameter("cartid");
			String num= request.getParameter("num");
			num = StringUtil.isEmpty(num)?"1":num;//lzf add 20110113
			String productid= request.getParameter("productid");
			Product product=productManager.get(Integer.valueOf(productid));
			Integer store=product.getEnable_store();
			if(store==null) {
				store=0;
			}
			if(store >=Integer.valueOf(num)){
				cartManager.updateNum(request.getSession().getId(),  Integer.valueOf(cartid),  Integer.valueOf(num));
			}
			return JsonResultUtil.getNumberJson("store", store);
		}catch(RuntimeException e){
//			TestUtil.print(e);
			this.logger.error("更新购物车数量出现意外错误", e);
			return JsonResultUtil.getErrorJson("更新购物车数量出现意外错误:"+e.getMessage());
		}
	}
	
	/**
	 * 购物车的价格总计信息
	 * @param 无
	 * @return 返回json字串
	 * result： 为1表示调用成功0表示失败 int型
	 * orderprice: 订单价格，OrderPrice型
	 * {@link OrderPrice}  
	 */
	@ResponseBody  
	@RequestMapping(value="/get-total", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult getTotal(){
		HttpServletRequest request =ThreadContextHolder.getHttpRequest();
		String sessionid  = request.getSession().getId();
		OrderPrice orderprice  =this.cartManager.countPrice(cartManager.listGoods(sessionid), null, null);
		return JsonResultUtil.getObjectJson(orderprice);
	}
	
	/**
	 * 清空购物车
	 */
	@ResponseBody  
	@RequestMapping(value="/clean", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult clean(){	
		HttpServletRequest  request = ThreadContextHolder.getHttpRequest();
		try{
			cartManager.clean(request.getSession().getId());
			return JsonResultUtil.getSuccessJson("清空购物车成功");
		}catch(RuntimeException e){
			this.logger.error("清空购物车出错",e);
			return JsonResultUtil.getErrorJson("清空购物车出错");
		}
	}
	
	/**
	 * 选择货品进行下单
	 */
	@ResponseBody  
	@RequestMapping(value="/check-product", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult checkProduct(Integer product_id,boolean checked){	
		HttpServletRequest  request = ThreadContextHolder.getHttpRequest();
		try{
			String sessionid  = request.getSession().getId();
			cartManager.checkProduct(sessionid, product_id, checked);
			return JsonResultUtil.getSuccessJson("选择购物车商品成功");
		}catch(RuntimeException e){
			this.logger.error("选择购物车商品出错",e);
			return JsonResultUtil.getErrorJson("选择购物车商品出错");
		}
	}
	/**
	 * 选择货品进行下单
	 */
	@ResponseBody  
	@RequestMapping(value="/check-all", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult checkAll(boolean checked){	
		HttpServletRequest  request = ThreadContextHolder.getHttpRequest();
		try{
			String sessionid  = request.getSession().getId();
			cartManager.checkAll(sessionid,checked);
			return JsonResultUtil.getSuccessJson("选择购物车商品成功");
		}catch(RuntimeException e){
			this.logger.error("选择购物车商品出错",e);
			return JsonResultUtil.getErrorJson("选择购物车商品出错");
		}
	}
	
	/**
	 * 添加货品的购物车
	 * @param product
	 * @param num 数量
	 * @param showCartData 是否在返回的json中显示购物车
	 * @return
	 */
	private JsonResult addProductToCart(Product product, int num, int showCartData, Integer activity_id){
		String sessionid =ThreadContextHolder.getHttpRequest().getSession().getId();
		
		if(product!=null){
			try{
				//判断商品是否下架 商品是否删除
				Goods good=goodsManager.getGoods(product.getGoods_id());
				if(good.getMarket_enable()!=1){
					return JsonResultUtil.getErrorJson("抱歉！您所选择的货品已经下架");
				}
				//商家能否购买自己的商品
				final String canBuySelf = settingService.getSetting("store", "buy_self_auth");
				if (!"1".equals(canBuySelf)) {
					//该商品是否为自己的商品
					Member nowaMember = UserConext.getCurrentMember();
					if (nowaMember != null) {
						Member belongTo = memberManager.getByGoodsId(product.getGoods_id());
						if (belongTo != null && belongTo.getMember_id().equals(nowaMember.getMember_id())) {
							
							throw new RuntimeException("抱歉！您不能购买自己的商品。");
						}
					}
				}
				
				int enableStore = product.getEnable_store();
				if (enableStore < num) {
					throw new RuntimeException("抱歉！您所选择的货品库存不足。");
				}
				//查询已经存在购物车里的商品
				
				Cart tempCart = cartManager.getCartByProductId(product.getProduct_id(), sessionid);
				if(tempCart != null){
					int tempNum = tempCart.getNum();
					if (enableStore < num + tempNum) {
						throw new RuntimeException("抱歉！您所选择的货品库存不足。");
					}
				}
				HttpServletRequest request=ThreadContextHolder.getHttpRequest();//获取当前请求
				String exchange=request.getParameter("exchange");//获取exchange参数
				if("true".equals(exchange)){//如果是积分商品 这走下面的插件桩的方法
					this.cartPluginBundle.onAddProductToCart(product,num);
				}
				Cart cart = new Cart();
				cart.setGoods_id(product.getGoods_id());
				cart.setProduct_id(product.getProduct_id());
				cart.setSession_id(sessionid);
				cart.setNum(num);
				cart.setItemtype(0); //0为product和产品 ，当初是为了考虑有赠品什么的，可能有别的类型。
				cart.setWeight(product.getWeight());
				cart.setPrice( product.getPrice());
				cart.setName(product.getName());
				
				// 服务商品   add by liuyulei  2016-09-13
				cart.setGoods_type(Integer.parseInt((good.getGoods_type() == null ? "0" : good.getGoods_type())));
				
				//默认商品添加购物车选中 
				//add by Kanon 2016-7-12
				cart.setIs_check(1);
				//如果商品参加了促销活动，就将促销活动ID添加至购物车表
				//add by DMRain 2016-1-15
				if(activity_id != null && activity_id != 0){
					cart.setActivity_id(activity_id);
				}
				
				this.cartManager.add(cart);
				
				//需要同时显示购物车信息
				if(showCartData==1){
					return this.getCartData();
				}
				
				return JsonResultUtil.getSuccessJson("添加成功");
			}catch(RuntimeException e){
				this.logger.error("将货品添加至购物车出错",e);
				return JsonResultUtil.getErrorJson("添加失败: " + e.getMessage());
			}
			
		}else{
			return JsonResultUtil.getErrorJson("该货品不存在，未能添加到购物车");
		}
	}

}
