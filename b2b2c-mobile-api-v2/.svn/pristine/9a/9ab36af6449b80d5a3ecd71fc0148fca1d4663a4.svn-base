package com.enation.app.shop.mobile.action.api;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.enation.app.shop.mobile.service.ApiMemberManager;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.enation.app.b2b2c.core.member.service.IStoreCollectManager;
import com.enation.app.base.core.model.Member;
import com.enation.app.base.core.service.IMemberManager;
import com.enation.app.base.core.service.ISmsManager;
import com.enation.app.base.core.upload.IUploader;
import com.enation.app.base.core.upload.UploadFacatory;
import com.enation.app.shop.core.member.service.IPointHistoryManager;
import com.enation.app.shop.core.order.service.OrderStatus;
import com.enation.app.shop.mobile.model.IMUser;
import com.enation.app.shop.mobile.model.SmsLog;
import com.enation.app.shop.mobile.service.ApiFavoriteManager;
import com.enation.app.shop.mobile.service.ApiOrderManager;
import com.enation.app.shop.mobile.utils.ValidateUtils;
import com.enation.eop.sdk.context.UserConext;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.Page;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.EncryptionUtil1;
import com.enation.framework.util.FileUtil;
import com.enation.framework.util.HttpUtil;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;

/**
 * Created by Dawei on 4/28/15.
 */
@Controller("mobileMemberApiController")
@RequestMapping("/api/mobile/member")
public class MemberApiController {

	@Autowired
	private IMemberManager memberManager;

	@Autowired
	private ISmsManager smsManager;

	@Autowired
	private ApiFavoriteManager apiFavoriteManager;

	@Autowired
	private ApiOrderManager apiOrderManager;

	@Autowired
	private IPointHistoryManager pointHistoryManager;

	@Autowired
	private ApiMemberManager apiMemberManager;
	
	@Autowired
	private IStoreCollectManager storeCollectManager;	//xulipeng

	private final int PAGE_SIZE = 20;

	/**
	 * 短信验证码前缀
	 */
	private static final String SMS_PREFIX = "【Javashop】";


	/**
	 * 会员登录
	 * @param username		用户名
	 * @param password		密码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult login(String username, String password) {
		if (memberManager.login(username, password) != 1) {
			return JsonResultUtil.getErrorJson("账号密码错误");
		}

		String cookieValue = EncryptionUtil1.authcode("{username:\"" + username
				+ "\",password:\"" + StringUtil.md5(password) + "\"}",
				"ENCODE", "", 0);
		HttpUtil.addCookie(ThreadContextHolder.getHttpResponse(),
				"JavaShopUser", cookieValue, 60 * 60 * 24 * 14);

		Member member = UserConext.getCurrentMember();

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("username", member.getUname());
		map.put("face", StaticResourcesUtil.convertToUrl(member.getFace()));
		map.put("level", member.getLvname());

        map.put("imuser", "");
        map.put("impass", "");

		return JsonResultUtil.getObjectJson(map);
	}

	/**
	 * 是否已登录
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/islogin", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult isLogin() {
		if (UserConext.getCurrentMember() == null) {
			return JsonResultUtil.getErrorJson("尚未登陆");
		}
		return JsonResultUtil.getSuccessJson("已经登录");
	}

	/**
	 * 退出登录
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult logout() {
		if(UserConext.getCurrentMember() != null) {
			this.memberManager.logout();
		}
		HttpUtil.addCookie(ThreadContextHolder.getHttpResponse(), "JavaShopUser", null, 0);
		return JsonResultUtil.getSuccessJson("注销成功");
	}

	/**
	 * 修改密码
	 * @param oldpass		旧密码
	 * @param password		新密码
	 * @param repass		重复密码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/change-password", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult changePassword(String oldpass, String password,
			String repass) {
		Member member = UserConext.getCurrentMember();
		if (member == null) {
			return JsonResultUtil.getErrorJson("请您登录后再修改密码！");
		}

		oldpass = StringUtil.md5(oldpass);
		if (!oldpass.equals(member.getPassword())) {
			return JsonResultUtil.getErrorJson("您的旧密码不正确！");
		}

		if (!repass.equals(password)) {
			return JsonResultUtil.getErrorJson("您两次输入的密码不一致！");
		}

		try {
			memberManager.updatePassword(password);
			return JsonResultUtil.getSuccessJson("修改密码成功！");
		} catch (Exception e) {
		}
		return JsonResultUtil.getErrorJson("修改密码失败！");
	}

    /**
     * 使用手机修改密码
     * @param mobile		手机号码
     * @param mobilecode	手机验证码
     * @param password		新密码
     * @return
     */
	@ResponseBody
	@RequestMapping(value = "/mobile-change-pass", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult mobileChangePassword(String mobile, String mobilecode,
			String password) {
		if (StringUtil.isEmpty(mobile) || !ValidateUtils.isMobile(mobile)) {
			return JsonResultUtil.getErrorJson("请输入正确的手机号码！");
		}
		if (StringUtil.isEmpty(mobilecode)) {
			return JsonResultUtil.getErrorJson("请验证手机号码后再修改密码！");
		}
		if (StringUtil.isEmpty(password) || password.length() < 6
				|| password.length() > 12) {
			return JsonResultUtil.getErrorJson("新密码长度为6到12位！");
		}

		// 验证短信验证码
		SmsLog smsLog = (SmsLog)SmsLog.cache.get(mobile);
        if(smsLog == null || smsLog.getTimeList().size() <= 0){
        	return JsonResultUtil.getErrorJson("短信验证码不正确！");
        }
        if (StringUtils.isEmpty(smsLog.getMobileCode()) || !smsLog.getMobileCode().equals(mobilecode)) {
        	return JsonResultUtil.getErrorJson("短信验证码不正确！");
        }
        if(!mobile.equals(smsLog.getMobile())){
        	return JsonResultUtil.getErrorJson("短信验证码不正确！");
        }
        if(DateUtil.getDateline() - smsLog.getTimeList().get(smsLog.getTimeList().size()-1) > 10 * 60){
        	return JsonResultUtil.getErrorJson("短信验证码已过期，请您重新发送！");
        }

		// 根据手机号码获取用户
		Member member = memberManager.getMemberByMobile(mobile);
		if (member == null) {
			return JsonResultUtil.getErrorJson("不存在此手机号码！");
		}

		try {
			memberManager.updatePassword(member.getMember_id(), password);
			return JsonResultUtil.getSuccessJson("修改密码成功");
		} catch (Exception e) {
		}
		return JsonResultUtil.getErrorJson("修改密码失败");
	}

	/**
	 * 注册
	 * @param username		要注册的用户名
	 * @param password		要注册的密码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult register(String username, String password) {
		Member member = new Member();
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String registerip = request.getRemoteAddr();

		if (StringUtil.isEmpty(username)) {
			return JsonResultUtil.getErrorJson("用户名不能为空！");
		}
		if (username.length() < 4 || username.length() > 20) {
			return JsonResultUtil.getErrorJson("用户名的长度为4-20个字符！");
		}
		if (username.contains("@")) {
			return JsonResultUtil.getErrorJson("用户名中不能包含@等特殊字符！");
		}
		if (StringUtil.isEmpty(password)) {
			return JsonResultUtil.getErrorJson("密码不能为空！");
		}
		if (memberManager.checkname(username) > 0) {
			return JsonResultUtil.getErrorJson("此用户名已经存在，请您选择另外的用户名!");
		}

		member.setMobile("");
		member.setUname(username);
		member.setPassword(password);
		member.setEmail(username);
		member.setRegisterip(registerip);

		if (memberManager.register(member) != 1) {
			return JsonResultUtil.getErrorJson("用户名[" + member.getUname()
					+ "]已存在!");
		}

		this.memberManager.login(username, password);
		String cookieValue = EncryptionUtil1.authcode("{username:\"" + username
				+ "\",password:\"" + StringUtil.md5(password) + "\"}",
				"ENCODE", "", 0);
		HttpUtil.addCookie(ThreadContextHolder.getHttpResponse(),
				"JavaShopUser", cookieValue, 60 * 60 * 24 * 14);
		member = UserConext.getCurrentMember();

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("username", member.getUname());
		map.put("face", StaticResourcesUtil.convertToUrl(member.getFace()));
		map.put("level", member.getLvname());

        map.put("imuser", "");
        map.put("impass", "");

		return JsonResultUtil.getObjectJson(map);
	}

	/**
	 * 使用手机注册
	 * @param mobile		手机号码
	 * @param mobilecode	手机验证码
	 * @param password		密码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/mobile-register", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult mobileRegister(String mobile, String mobilecode,
			String password) {
		Member member = new Member();
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String registerip = request.getRemoteAddr();

		if (StringUtil.isEmpty(mobile) || !ValidateUtils.isMobile(mobile)) {
			return JsonResultUtil.getErrorJson("请输入正确的手机号码！");
		}

		// 验证短信验证码
		if (StringUtil.isEmpty(mobilecode)) {
			return JsonResultUtil.getErrorJson("短信验证码不正确！");
		}

		SmsLog smsLog = (SmsLog)SmsLog.cache.get(mobile);
        if(smsLog == null || smsLog.getTimeList().size() <= 0){
        	return JsonResultUtil.getErrorJson("短信验证码不正确！");
        }
        if (StringUtils.isEmpty(smsLog.getMobileCode()) || !smsLog.getMobileCode().equals(mobilecode)) {
        	return JsonResultUtil.getErrorJson("短信验证码不正确！");
        }
        if(!mobile.equals(smsLog.getMobile())){
        	return JsonResultUtil.getErrorJson("短信验证码不正确！");
        }
        if(DateUtil.getDateline() - smsLog.getTimeList().get(smsLog.getTimeList().size()-1) > 10 * 60){
        	return JsonResultUtil.getErrorJson("短信验证码已过期，请您重新发送！");
        }

		member.setUname(mobile);
		member.setMobile(mobile);

		if (StringUtil.isEmpty(password)) {
			return JsonResultUtil.getErrorJson("密码不能为空！");
		}
		if (password.length() < 6 || password.length() > 12) {
			return JsonResultUtil.getErrorJson("密码长度为6到12位！");
		}
		if (memberManager.checkname(mobile) > 0
				|| memberManager.checkMobile(mobile) > 0) {
			return JsonResultUtil.getErrorJson("此手机号码已被注册，请您选择另外的用户名!");
		}
		member.setPassword(password);
		member.setEmail(mobile);
		member.setRegisterip(registerip);

		if (memberManager.register(member) != 1) {
			return JsonResultUtil.getErrorJson("手机号码 [" + member.getUname()
					+ "] 已被注册!");
		}
		ThreadContextHolder.getHttpRequest().getSession()
				.removeAttribute("mobileCode");
		ThreadContextHolder.getHttpRequest().getSession()
		.removeAttribute("mobile");

		this.memberManager.login(mobile, password);
		String cookieValue = EncryptionUtil1.authcode("{username:\"" + mobile
				+ "\",password:\"" + StringUtil.md5(password) + "\"}",
				"ENCODE", "", 0);
		HttpUtil.addCookie(ThreadContextHolder.getHttpResponse(),
				"JavaShopUser", cookieValue, 60 * 60 * 24 * 14);

		member = UserConext.getCurrentMember();

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("username", member.getUname());
		map.put("face", StaticResourcesUtil.convertToUrl(member.getFace()));
		map.put("level", member.getLvname());

        map.put("imuser", "");
        map.put("impass", "");

		return JsonResultUtil.getObjectJson(map);
	}

	/**
	 * 发送注册短信验证码
	 * @param mobile	手机号码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/send-register-code", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult sendRegisterCode(String mobile) {
		if (StringUtil.isEmpty(mobile) || !ValidateUtils.isMobile(mobile)) {
			return JsonResultUtil.getErrorJson("系统参数错误！");
		}

		// 验证是否已被注册
		if (memberManager.checkname(mobile) > 0
				|| memberManager.checkMobile(mobile) > 0) {
			return JsonResultUtil.getErrorJson("此手机号码已被注册!");
		}

		//验证是否发送过频
		SmsLog smsLog = (SmsLog)SmsLog.cache.get(mobile);
        if(smsLog != null){
            long lasttime = smsLog.getTimeList().get(smsLog.getTimeList().size() - 1);
            if(DateUtil.getDateline() - lasttime < 60){
                return JsonResultUtil.getErrorJson("发送短信太频繁，请您稍后再试！");
            }
            if(smsLog.getTimeList().size() >= 6){
            	long pretime = smsLog.getTimeList().get(smsLog.getTimeList().size() - 6);
                if(DateUtil.getDateline() - pretime < 60 * 60){
                	return JsonResultUtil.getErrorJson("发送短信太频繁，请您稍后再试！");
                }
            }
        }

		try {
			String mobileCode = "" + (int) ((Math.random() * 9 + 1) * 100000);

			if(smsLog != null){
				smsLog.setMobileCode(mobileCode);
            }else{
            	smsLog = new SmsLog(mobile, mobileCode);
            }
			smsLog.getTimeList().add(DateUtil.getDateline());
			SmsLog.cache.put(mobile, smsLog);

			String content = SMS_PREFIX + "您的验证码为：【" + mobileCode + "】";
			System.out.println(content);
			try {
				this.smsManager.send(mobile, content, new HashMap());
			} catch (Exception ex) {
			}
			return JsonResultUtil.getSuccessJson("发送成功");
		} catch (Exception e) {
			return JsonResultUtil.getErrorJson("发送失败");
		}
	}

	/**
	 * 发送找回密码短信验证码
	 * @param mobile	手机号码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/send-find-code", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult sendFindPassCode(String mobile) {
		if (StringUtil.isEmpty(mobile) || !ValidateUtils.isMobile(mobile)) {
			return JsonResultUtil.getErrorJson("请您输入正确的手机号码！");
		}

		// 验证是否已被注册
		if (memberManager.checkMobile(mobile) <= 0) {
			return JsonResultUtil.getErrorJson("此手机号码不存在!");
		}

		//验证是否发送过频
		SmsLog smsLog = (SmsLog)SmsLog.cache.get(mobile);
        if(smsLog != null){
            long lasttime = smsLog.getTimeList().get(smsLog.getTimeList().size() - 1);
            if(DateUtil.getDateline() - lasttime < 60){
                return JsonResultUtil.getErrorJson("发送短信太频繁，请您稍后再试！");
            }
            if(smsLog.getTimeList().size() >= 6){
            	long pretime = smsLog.getTimeList().get(smsLog.getTimeList().size() - 6);
                if(DateUtil.getDateline() - pretime < 60 * 60){
                	return JsonResultUtil.getErrorJson("发送短信太频繁，请您稍后再试！");
                }
            }
        }

		try {
			String mobileCode = "" + (int) ((Math.random() * 9 + 1) * 100000);
			if(smsLog != null){
				smsLog.setMobileCode(mobileCode);
            }else{
            	smsLog = new SmsLog(mobile, mobileCode);
            }
			smsLog.getTimeList().add(DateUtil.getDateline());
			SmsLog.cache.put(mobile, smsLog);

			String content = SMS_PREFIX + "您的验证码为：【" + mobileCode + "】";
			System.out.println(content);
			try {
				this.smsManager.send(mobile, content, new HashMap());
			} catch (Exception ex) {
			}
			return JsonResultUtil.getSuccessJson("发送成功");
		} catch (Exception e) {
			return JsonResultUtil.getErrorJson("发送失败");
		}
	}

	/**
	 * 验证手机短信验证码
	 * @param mobile		手机号码
	 * @param mobilecode	手机验证码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/valid-mobile", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult validMobile(String mobile, String mobilecode) {
		if (StringUtil.isEmpty(mobile) || !ValidateUtils.isMobile(mobile)) {
			return JsonResultUtil.getErrorJson("请输入正确的手机号码！");
		}

		// 验证短信验证码
		if (StringUtil.isEmpty(mobilecode)) {
			return JsonResultUtil.getErrorJson("短信验证码不正确！");
		}

		SmsLog smsLog = (SmsLog)SmsLog.cache.get(mobile);
        if(smsLog == null || smsLog.getTimeList().size() <= 0){
        	return JsonResultUtil.getErrorJson("短信验证码不正确！");
        }
        if (StringUtils.isEmpty(smsLog.getMobileCode()) || !smsLog.getMobileCode().equals(mobilecode)) {
        	return JsonResultUtil.getErrorJson("短信验证码不正确！");
        }
        if(!mobile.equals(smsLog.getMobile())){
        	return JsonResultUtil.getErrorJson("短信验证码不正确！");
        }
        if(DateUtil.getDateline() - smsLog.getTimeList().get(smsLog.getTimeList().size()-1) > 10 * 60){
        	return JsonResultUtil.getErrorJson("短信验证码已过期，请您重新发送！");
        }
		return JsonResultUtil.getSuccessJson("验证成功");
	}

	/**
	 * 获取当前登录的用户信息
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult info() {
		Member member = UserConext.getCurrentMember();
		if (member == null) {
			return JsonResultUtil.getErrorJson("请您登录后再进行此项操作！");
		}
		Map map = new HashMap<String, String>();
		//基本信息
		map.put("nick_name", member.getNickname());
		map.put("username", member.getUname());
		map.put("face", StaticResourcesUtil.convertToUrl(member.getFace()));
		map.put("level_id", member.getLv_id());
		map.put("level", member.getLvname());
		map.put("name", member.getName());
		map.put("sex", member.getSex());
		map.put("birthday", member.getBirthday());
		map.put("province", member.getProvince());
		map.put("province_id", member.getProvince_id());
		map.put("city", member.getCity());
		map.put("city_id", member.getCity_id());
		map.put("region", member.getRegion());
		map.put("region_id", member.getRegion_id());
		map.put("address", member.getAddress());
		map.put("zip", member.getZip());
		map.put("mobile", member.getMobile());
		map.put("tel", member.getTel());

		//扩展信息
		map.put("favoriteCount", apiFavoriteManager.count(member.getMember_id()));	//收藏的商品
		map.put("favoriteStoreCount", apiFavoriteManager.storeCount(member.getMember_id()));	//关注的店铺
		map.put("point", member.getPoint());	//等级积分
		map.put("mp", member.getMp());			//消费积分
		map.put("paymentOrderCount", apiOrderManager.count(OrderStatus.ORDER_NOT_PAY, member.getMember_id()));	//待付款订单数
		map.put("shippingOrderCount", apiOrderManager.count(OrderStatus.ORDER_SHIP, member.getMember_id()));	//待收货订单数
		map.put("commentOrderCount", apiOrderManager.commentGoodsCount(member.getMember_id()));		//待评论订单数
		map.put("returnedOrderCount", apiOrderManager.returnedCount(member.getMember_id()));	//退换货订单数
		
		Page webPage = this.storeCollectManager.getList(member.getMember_id(), 1, 10);
		map.put("collectNum", webPage.getTotalCount());		//xulipeng   关注店铺的数量
		
		return JsonResultUtil.getObjectJson(map);
	}

	/**
	 * 修改用户信息
	 * @param member	会员实体
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult save(@ModelAttribute Member member, @RequestParam(value = "photo", required = false) MultipartFile photo) {
		if (UserConext.getCurrentMember() == null) {
			return JsonResultUtil.getErrorJson("请您先登录后再修改资料！");
		}
		Member dbMember = memberManager.get(UserConext.getCurrentMember().getMember_id());

		//先上传图片
		String faceField = "faceFile";
		if(photo != null){
			//判断文件类型
			if(!FileUtil.isAllowUpImg(photo.getOriginalFilename())){
				return JsonResultUtil.getErrorJson("头像文件格式不正确，请上传jpg或png的格式文件！");
			}

			//判断文件大小
			if(photo.getSize() > 2000 * 1024){
				return JsonResultUtil.getErrorJson("头像图片不能大于2Mb！");
			}

			InputStream stream=null;
			try {
				stream = photo.getInputStream();
			} catch (Exception e) {
				e.printStackTrace();
				return JsonResultUtil.getErrorJson("保存资料失败，请您重试！");
			}

			IUploader uploader = UploadFacatory.getUploaer();
			String imgPath=	uploader.upload(stream, faceField, photo.getOriginalFilename());
			dbMember.setFace(imgPath);
		}

		if (member.getBirthday() == null || member.getBirthday() == 0) {
			dbMember.setBirthday(19700101L);
		}

		if (StringUtil.isEmpty(member.getMobile())) {
			return JsonResultUtil.getErrorJson("手机号码不能为空！");
		}

		if (!ValidateUtils.isMobile(member.getMobile())) {
			return JsonResultUtil.getErrorJson("手机号码格式不正确！");
		}

		dbMember.setName(member.getName());
		dbMember.setSex(member.getSex());
		dbMember.setBirthday(member.getBirthday());
		dbMember.setProvince(member.getProvince());
		dbMember.setProvince_id(member.getProvince_id());
		dbMember.setCity(member.getCity());
		dbMember.setCity_id(member.getCity_id());
		dbMember.setRegion(member.getRegion());
		dbMember.setRegion_id(member.getRegion_id());
		dbMember.setAddress(member.getAddress());
		dbMember.setMobile(member.getMobile());
		dbMember.setZip(member.getZip());
		dbMember.setTel(member.getTel());

		try {
			this.memberManager.edit(dbMember);
			return JsonResultUtil.getSuccessJson("保存资料成功！");
		} catch (Exception e) {
			return JsonResultUtil.getErrorJson("保存资料失败，请您重试！");
		}
	}

	/**
	 * 积分明细
	 * @param page
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/point-history", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult pointHistory(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page) {
		Member member =UserConext.getCurrentMember();
        if (member == null) {
        	return JsonResultUtil.getErrorJson("请登录后再进行此项操作！");
        }
        if(page == null || page <= 0)
        	page = 1;
        Page pointHistoryPage = pointHistoryManager.pagePointHistory(Integer
				.valueOf(page), PAGE_SIZE);
        List list = (List) pointHistoryPage.getResult();
        return JsonResultUtil.getObjectJson(list);
	}

	/**
	 * 优惠券列表
	 * @param type	1:未使用; 2:已使用; 3:已过期
	 * @return
     */
	@ResponseBody
	@RequestMapping(value = "/bonus", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult bonus(@RequestParam(value = "type", required = false, defaultValue = "1") Integer type,
							@RequestParam(value = "page", required = false, defaultValue = "1") Integer page){
		Member member =UserConext.getCurrentMember();
		if (member == null) {
			return JsonResultUtil.getErrorJson("请登录后再进行此项操作！");
		}
		Page webPage = apiMemberManager.getBonusListByMemberid(member.getMember_id(), type, page, PAGE_SIZE);
		return JsonResultUtil.getObjectJson(webPage.getResult());
	}

	/**
	 * 获取优惠券数量
	 * @return
     */
	@ResponseBody
	@RequestMapping(value = "/bonus-count", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult bonusCount(){
		Member member =UserConext.getCurrentMember();
		if (member == null) {
			return JsonResultUtil.getErrorJson("请登录后再进行此项操作！");
		}
		Map<String, Object> map = new HashMap<>();
		map.put("unused_count", apiMemberManager.getBounsCountByMemberId(member.getMember_id(), 1));
		map.put("used_count", apiMemberManager.getBounsCountByMemberId(member.getMember_id(), 2));
		map.put("expired_count", apiMemberManager.getBounsCountByMemberId(member.getMember_id(), 3));
		return JsonResultUtil.getObjectJson(map);
	}
}
