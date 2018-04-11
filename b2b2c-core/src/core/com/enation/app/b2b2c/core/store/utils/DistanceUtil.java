package com.enation.app.b2b2c.core.store.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * 
 * @ClassName: DistanceUtil 
 * @Description: 根据两个位置的经纬度，来计算两地的距离（单位为KM）
 * @author: liuyulei
 * @date: 2016年10月10日 下午9:42:40 
 * @since:v61
 */
public class DistanceUtil {

	
    private static final double EARTH_RADIUS = 6378.137;
    private static NumberFormat nf = NumberFormat.getNumberInstance();
    private static DecimalFormat df = new DecimalFormat("#.00");   // 小数点后保留两位	显示格式为:943199.05

    private static double rad(double d){
        return d * Math.PI / 180.0;
    }

    
    /**
     * 
     * @Title: GetDistance 
     * @Description: TODO
     * @param long1		[double]	位置1经度
 * @param lat1	        [double]	位置1纬度
     * @param long2     [double]	位置2经度
     * @param lat2      [double]	位置2纬度
     * @return  距离    保留两位 小数   单位:千米  
     * @return: Stirng
     * @author： liuyulei
     * @date：2016年10月10日 下午9:43:20
     */
    public static String getDistance(double long1, double lat1, double long2, double lat2) {
        double a, b, d, sa2, sb2;
        lat1 = rad(lat1);
        lat2 = rad(lat2);
        a = lat1 - lat2;
        b = rad(long1 - long2);

        sa2 = Math.sin(a / 2.0);
        sb2 = Math.sin(b / 2.0);
        d = 2   * EARTH_RADIUS
                * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1)
                * Math.cos(lat2) * sb2 * sb2));
        nf.setMaximumFractionDigits(2);   // 小数点后保留两位      显示格式为  943,199.05
        return nf.format(d);
    }
}
