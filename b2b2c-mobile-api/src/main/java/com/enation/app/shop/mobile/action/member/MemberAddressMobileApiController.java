package com.enation.app.shop.mobile.action.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.enation.app.base.core.model.Member;
import com.enation.app.base.core.model.Regions;
import com.enation.app.base.core.service.IRegionsManager;
import com.enation.app.shop.core.member.model.MemberAddress;
import com.enation.app.shop.core.member.service.IMemberAddressManager;
import com.enation.app.shop.mobile.utils.CommonRequest;
import com.enation.eop.sdk.context.UserConext;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;
import com.enation.framework.util.TestUtil;

/**
 * 会员地址api
 * @author Sylow
 * @version v1.0 , 2015-08-24
 * @since v1.0
 */
@Scope("prototype")
@Controller
@RequestMapping("/api/mobile/address")
public class MemberAddressMobileApiController{
	protected final Logger logger = Logger.getLogger(getClass());
	@Autowired
	private IMemberAddressManager memberAddressManager;
	@Autowired
	private IRegionsManager regionsManager;


	/**
	 * 根据地址id 获得一个地址详情
	 * @param id 必填
	 * @return 
	 */
	@ResponseBody
	@RequestMapping(value="/get",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult get(){
		try {
			HttpServletRequest request=ThreadContextHolder.getHttpRequest();
			String addrId = request.getParameter("addr_id");
			MemberAddress address = memberAddressManager.getAddress(Integer.parseInt(addrId));
			return JsonResultUtil.getObjectJson(address);
		} catch (Exception e) {
			this.logger.error("获取账户收货地址出错", e);
			return JsonResultUtil.getErrorJson("获取账户收货地址出错[" + e.getMessage() + "]");
			

		}
	}


	/**
	 * 获取会员的默认收货地址
	 *
	 * @param 无
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@RequestMapping(value="/default-address",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult defaultAddress() {
		// Member member = UserConext.getCurrentMember();

		Integer member_id = CommonRequest.getMemberID();
		try {
			if (member_id == null) {
				return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
			}
			List<MemberAddress> addressList = memberAddressManager.listAddress(member_id);
			if (addressList == null || addressList.size() == 0) {
				return JsonResultUtil.getSuccessJson("您还没有添加地址！");
			}
			MemberAddress defaultAddress = null;
			for (MemberAddress address : addressList) {
				if (address.getDef_addr() != null && address.getDef_addr().intValue() == 1) {
					defaultAddress = address;
					break;
				}
			}
			if (defaultAddress == null) {
				defaultAddress = addressList.get(0);
			}
			Map data = new HashMap();
			data.put("defaultAddress", defaultAddress);
			return JsonResultUtil.getObjectJson(data);

		} catch (RuntimeException e) {
			TestUtil.print(e);
			this.logger.error("获取账户默认收货地址出错", e);
			return JsonResultUtil.getErrorJson("获取账户默认收货地址出错[" + e.getMessage() + "]");
		}
	}

	/**
	 * 获取会员地址
	 *
	 * @param 无
	 * @return json字串
	 * result  为1表示调用正确，0表示失败 ，int型
	 * data: 地址列表
	 * <p/>
	 * @link com.enation.app.base.core.model.MemberAddress
	 * 如果没有登陆返回空数组
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@RequestMapping(value="/list",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult list() {
		try {
			Integer member_id = CommonRequest.getMemberID();
			if (member_id == null) {
				return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
			}
			Map data = new HashMap();
			data.put("addressList", memberAddressManager.listAddress(member_id));
			return JsonResultUtil.getObjectJson(data);

		} catch (RuntimeException e) {

			this.logger.error("获取收货地址出错", e);
			return JsonResultUtil.getErrorJson("获取收货地址出错[" + e.getMessage() + "]");
		}
	}
	/**
	 * 根据parentid获取地区列表
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/list-app",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult listApp(){
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		int parentId = NumberUtils.toInt(request.getParameter("parentid"));
		Map data = new HashMap();
		List regionList = regionsManager.listChildrenByid(parentId);
		data.put("list", regionList);
		return  JsonResultUtil.getObjectJson(data);
	}
	/**
	 * 添加一会员地址
	 *
	 * @param name：收货人姓名,String型，必填
	 * @param province_id:所在省id,int型，参见：{@link com.enation.app.base.core.model.Regions.region_id}，必填
	 * @param city_id:                         所在城市id,int型，参见：{@link com.enation.app.base.core.model.Regions.region_id}，必填
	 * @param region_id:                       所在地区id,int型，参见：{@link com.enation.app.base.core.model.Regions.region_id}	，必填
	 * @param addr：详细地址,String型                ，必填
	 * @param mobile：手机,String型                ，手机，必填
	 * @return json字串
	 * result  为1表示添加成功，0表示失败 ，int型
	 * message 为提示信息 ，String型
	 * {@link MemberAddress}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@RequestMapping(value="/add",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult add() {
		Integer member_id = CommonRequest.getMemberID();
		if (member_id == null) {
			return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
		}
		if (memberAddressManager.addressCount(member_id) >= 10) {
			return JsonResultUtil.getErrorJson("添加失败，您最多可以设置10个收货地址！");
		}
		MemberAddress address = new MemberAddress();
		try {
			address = this.fillAddressFromReq(address);
			int addr_id = memberAddressManager.addAddress(address, member_id);
			System.out.println(addr_id);

			Map data = new HashMap();
			data.put("address", memberAddressManager.getAddress(addr_id));
			System.out.println(memberAddressManager.getAddress(addr_id).toString());
			return JsonResultUtil.getObjectJson(data);
		} catch (RuntimeException e) {
			return JsonResultUtil.getErrorJson(e.getMessage());
		} catch (Exception e) {
			if (this.logger.isDebugEnabled()) {
				logger.error(e.getStackTrace());
			}
			return JsonResultUtil.getErrorJson("添加失败[" + e.getMessage() + "]");
		}
	}

	/**
	 * 修改收货地址
	 *
	 * @param addr_id：要修改的收货地址id,int型，必填
	 * @param name：收货人姓名,String型，必填
	 * @param province_id:所在省id,int型，参见：{@link Regions.region_id}，必填
	 * @param city_id:                         所在城市id,int型，参见：{@link Regions.region_id}，必填
	 * @param region_id:                       所在地区id,int型，参见：{@link Regions.region_id}	，必填
	 * @param addr：详细地址,String型                ，必填
	 * @param mobile：手机,String型                ，手机，必填
	 * @return json字串
	 * result  为1表示添加成功，0表示失败 ，int型
	 * message 为提示信息 ，String型
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@RequestMapping(value="/edit",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult edit() {
		Integer member_id = CommonRequest.getMemberID();
		if (member_id == null) {
			return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
		}
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		int addr_id = NumberUtils.toInt(request.getParameter("addr_id"), 0);
		MemberAddress address = memberAddressManager.getAddress(addr_id);
		int isDef = address.getDef_addr();	//记录是否是默认
		//如果地址等于空 或者会员id 不等于当前会员  就没权限
		if(address == null || !address.getMember_id().equals(member_id)){
			return JsonResultUtil.getErrorJson("您没有权限进行此项操作！");
		}
		try {
			address = this.fillAddressFromReq(address);
			address.setDef_addr(isDef);
			memberAddressManager.updateAddress(address);
			Map data = new HashMap();
			data.put("address", address);
			return JsonResultUtil.getObjectJson(data);
		} catch (Exception e) {
			if (this.logger.isDebugEnabled()) {
				logger.error(e.getStackTrace());
			}
			return JsonResultUtil.getErrorJson("修改失败[" + e.getMessage() + "]");
		}
	}

	/**
	 * 设置当前地址为默认地址
	 */
	@ResponseBody
	@RequestMapping(value="/isdefaddr",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult isdefaddr() {
		try {
//			Member member =UserConext.getCurrentMember();
			Integer member_id = CommonRequest.getMemberID();
			if (member_id == null) {
				return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
			}
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			int addr_id = NumberUtils.toInt(request.getParameter("addr_id"), 0);
			MemberAddress memberAddress = memberAddressManager.getAddress(addr_id);
			if(memberAddress == null || !memberAddress.getMember_id().equals(member_id)){
				return JsonResultUtil.getErrorJson("您没有权限进行此项操作！");
			}
//			memberAddressManager.updateAddressDefult();	
//			memberAddressManager.addressDefult("" + addr_id);
			memberAddressManager.updateMemberAddress(member_id, addr_id);
			return JsonResultUtil.getSuccessJson("设置为默认地址成功！");
		} catch (Exception e) {
			if (this.logger.isDebugEnabled()) {
				logger.error(e.getStackTrace());
			}
			return JsonResultUtil.getErrorJson("设置为默认地址失败！");
		}
	}

	/**
	 * 删除一个收货地址
	 *
	 * @param addr_id ：要删除的收货地址id,int型
	 *                result  为1表示添加成功，0表示失败 ，int型
	 *                message 为提示信息 ，String型
	 */
	@ResponseBody
	@RequestMapping(value="/delete",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult delete() {
		try {

//			Member member =UserConext.getCurrentMember();
			Integer member_id = CommonRequest.getMemberID();

			if (member_id == null) {
				return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
			}
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			int addr_id = NumberUtils.toInt(request.getParameter("addr_id"), 0);
			MemberAddress memberAddress = memberAddressManager.getAddress(addr_id);
			if(memberAddress == null || !memberAddress.getMember_id().equals(member_id)){
				return JsonResultUtil.getErrorJson("您没有权限进行此项操作！");
			}
			memberAddressManager.deleteAddress(addr_id);
			return JsonResultUtil.getSuccessJson("删除收货地址成功！");
		} catch (RuntimeException e) {
			if (this.logger.isDebugEnabled()) {
				logger.error(e.getStackTrace());
			}
			return JsonResultUtil.getErrorJson("删除收货地址失败！");
		}
	}

	/**
	 * 从request中填充address信息
	 *
	 * @param address
	 * @return
	 */
	private MemberAddress fillAddressFromReq(MemberAddress address) {
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();

		address.setDef_addr(0);
		String name = request.getParameter("name");
		if (StringUtil.isEmpty(name)) {
			throw new RuntimeException("收货人姓名不能为空！");
		}
		address.setName(name);
		Pattern p = Pattern.compile("^[0-9A-Za-z一-龥]{0,20}$");
		Matcher m = p.matcher(name);

		if (!m.matches()) {
			throw new RuntimeException("收货人姓名格式不正确！");
		}

		address.setTel("");

		String mobile = request.getParameter("mobile");
		address.setMobile(mobile);
		if (StringUtil.isEmpty(mobile) || !isMobile(mobile) == false) {
			throw new RuntimeException("请输入正确的手机号码！");
		}

		String province_id = request.getParameter("province_id");
		if (province_id == null || province_id.equals("")) {
			throw new RuntimeException("请选择所在地区中的省！");
		}
		address.setProvince_id(Integer.valueOf(province_id));

		Regions province = regionsManager.get(address.getProvince_id());
		if (province == null)
			throw new RuntimeException("系统参数错误！");
		address.setProvince(province.getLocal_name());

		String city_id = request.getParameter("city_id");
		if (city_id == null || city_id.equals("")) {
			throw new RuntimeException("请选择所在地区中的市！");
		}
		address.setCity_id(Integer.valueOf(city_id));

		Regions city = regionsManager.get(address.getCity_id());
		if (city == null) {
			throw new RuntimeException("系统参数错误！");
		}
		address.setCity(city.getLocal_name());

		String region_id = request.getParameter("region_id");
		if (region_id == null || region_id.equals("")) {
			throw new RuntimeException("请选择所在地区中的县！");
		}
		address.setRegion_id(Integer.valueOf(region_id));

		Regions region = regionsManager.get(address.getRegion_id());
		if (region == null) {
			throw new RuntimeException("系统参数错误！");
		}
		address.setRegion(region.getLocal_name());

		String addr = request.getParameter("addr");
		if (addr == null || addr.equals("")) {
			throw new RuntimeException("详细地址不能为空！");
		}
		address.setAddr(addr);
		address.setZip("");

		return address;
	}

	/* private static boolean isPhone(String str) {
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 验证手机号
        m = p.matcher(str);
        b = m.matches();
        return b;
    }*/

	private static boolean isMobile(String str) {
		Pattern p1 = null, p2 = null;
		Matcher m = null;
		boolean b = false;
		p1 = Pattern.compile("^[0][1-9]{2,3}-[0-9]{5,10}$");  // 验证带区号的
		p2 = Pattern.compile("^[1-9]{1}[0-9]{5,8}$");         // 验证没有区号的
		if (str.length() > 9) {
			m = p1.matcher(str);
			b = m.matches();
		} else {
			m = p2.matcher(str);
			b = m.matches();
		}
		return b;
	}

}
