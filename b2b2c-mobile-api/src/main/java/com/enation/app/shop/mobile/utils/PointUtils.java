package com.enation.app.shop.mobile.utils;

import com.enation.eop.sdk.context.EopSetting;
import com.enation.framework.util.CurrencyUtil;

/**
 * 积分策略工具类
 * 
 * @author furen
 *
 */
public final class PointUtils {

	/**
	 * 使用积分可优惠的最大的钱
	 * 
	 * @param point
	 * @return
	 */
	 public static Double usePointMaxDiscount(double goodsPrice) {
		if (goodsPrice == 0) {
			return 0d;
		}
		return CurrencyUtil.round(CurrencyUtil.mul(goodsPrice, EopSetting.PAYRATE), 2);
	}

	/**
	 * 根据商品价格计算可使用的积分
	 * 
	 * @param goodsPrice
	 * @param existPoint
	 * @return
	 */
	public static Integer canUsePoint(double goodsPrice, int existPoint) {
		if (existPoint <= 0 || goodsPrice == 0 ) {
			return 0;
		}
		double discountMoney = usePointMaxDiscount(goodsPrice);
		Integer canUsePoint = (int)CurrencyUtil.mul(discountMoney, EopSetting.POINTRATE);
		if (existPoint < canUsePoint) {
			canUsePoint = existPoint;
		}
		return canUsePoint;
	}

	/**
	 * 实际使用现有积分可优惠的钱
	 * 
	 * @param exsitPoint
	 * @return
	 */
	public static Double usePointFacDiscount(double canUsePoint) {
		if (canUsePoint == 0) {
			return 0d;
		}
		return CurrencyUtil.round(CurrencyUtil.div(canUsePoint, EopSetting.POINTRATE), 2);
	}
	
	/**
	 * 计算店铺使用积分可优惠的价格
	 * 
	 * @param goodsPrice
	 * @param useDiscountPoint
	 * @return
	 */
	public static Double countStoreDiscount(double storeGoodsPrice, double totalGoodsPrice, double usedDiscountPoint) {
		Double useRate = CurrencyUtil.div(storeGoodsPrice, totalGoodsPrice, 2);
		Double storeUsedDiscountPoint = CurrencyUtil.mul(usedDiscountPoint, useRate);
		return CurrencyUtil.round(CurrencyUtil.div(storeUsedDiscountPoint, EopSetting.POINTRATE), 2);
	}
	
	
	/**
	 * 计算店铺使用积分可优惠的积分
	 * 
	 * @param goodsPrice
	 * @param useDiscountPoint
	 * @return
	 */
	public static Integer countStoreConsumePoint(double storeGoodsPrice, double totalGoodsPrice, int usedDiscountPoint) {
		Double useRate = CurrencyUtil.div(storeGoodsPrice, totalGoodsPrice, 2);
		return (int) CurrencyUtil.round(CurrencyUtil.mul(usedDiscountPoint, useRate),0);
	}
	
	

}
