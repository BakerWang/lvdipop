/**
 * Created by Andste on 2016/5/11.
 */
var lteIE8 = false,   //  全局变量，当它为true时，说明浏览器为IE9【不包括IE9】以下
    Sys = {};         //  全局变量，此为对象。
                      // Sys.ie --> IE浏览器版本【IE11检测不到】 Sys.firefox --> 火狐浏览器版本  Sys.chrome  -->  chrome浏览器版本  Sys.opera  -->  opera浏览器版本
(function(){
    var jq183 = '<script type="text/javascript" src="'+ctxPath+'/e_tools/js/jquery-1.8.3.min.js"></script>',
        jq214 = '<script type="text/javascript" src="'+ctxPath+'/e_tools/js/jquery-2.1.4.min.js"></script>',
        bootstrapJs335 = '<script type="text/javascript" src="'+ctxPath+'/e_tools/js/bootstrap.min-3.3.5.js"></script>',
        bootstrapCss335 = '<link rel="stylesheet" href="'+ctxPath+'/e_tools/css/bootstrap.min-3.3.5.css">',
        bootstrapJs232 = '<script type="text/javascript" src="'+ctxPath+'/e_tools/js/bootstrap.min-2.3.2.js"></script>',
        bootstrapCss232 = '<link rel="stylesheet" href="'+ctxPath+'/e_tools/css/bootstrap.min-2.3.2.css">',
        e_baseJS = '<script type="text/javascript" src="'+ctxPath+'/e_tools/js/e_base.js"></script>'
    var ua = navigator.userAgent.toLowerCase();
    var s;
    (s = ua.match(/msie ([\d.]+)/)) ? Sys.ie = s[1]
        : (s = ua.match(/firefox\/([\d.]+)/)) ? Sys.firefox = s[1]
        : (s = ua.match(/chrome\/([\d.]+)/)) ? Sys.chrome = s[1]
        : (s = ua.match(/opera.([\d.]+)/)) ? Sys.opera = s[1]
        : (s = ua.match(/version\/([\d.]+).*safari/)) ? Sys.safari = s[1] : 0;
    if (Sys.ie) {
        var ver = parseFloat(Sys.ie);
        ver >=9 ? heiLoad() : lowLoad();
        if(ver <=8 ){
            lteIE8 = true;
        };
    }else {
        heiLoad();
    };
    function lowLoad(){
        document.write(jq183);
        document.write(bootstrapJs232);
        document.write(bootstrapCss232);
    };
    function heiLoad(){
        document.write(jq214);
        document.write(bootstrapJs335);
        document.write(bootstrapCss335);
    };

    (function(){
        document.write(e_baseJS);
    }())
})();

