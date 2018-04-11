/**
 * Created by DMRain on 2016/9/22.
 */
$(function(){
	//  优惠券普通搜索
    (function(){
        var btn = $('.coupons-seach .btn'), input = $('.coupons-seach input'), keyWord = GetQueryString('keyword');
        if(keyWord){
            input.val(keyWord);
        };
        btn.unbind('click').on('click', function(){
            goSeach();
        });
        input.keyup(function(event){
            if(event.keyCode == 13){
                goSeach();
            };
        });

        function goSeach(){
            var val = $.trim(input.val());
            if(!val){
                location.href = './store-coupons-list.html';
            }else {
                location.href = './store-coupons-list.html?search_type=0&keyword='+val;
            };
        }
    })();
	
	//  新增、修改优惠劵
    (function(){
        var btn = $('.coupon-add, .coupon-edit');
        btn.unbind('click').on('click', function(){
            var _this = $(this), coupons_id = _this.attr('coupons_id'), url = '', title = '';
            if(coupons_id){
                url = './store-coupons-edit.html?coupons_id=' + coupons_id;
                title = '修改优惠劵';
            }else {
                url = './store-coupons-add.html';
                title = '新增优惠劵';
            };

            $.ajax({
                url: url,
                type: 'GET',
                success: function(html){
                    $.dialog({
                        title: title,
                        html : html,
                        showCall: function(){
                            $('#coupons_form input').blur(function(){
                                checkCounpon($(this));
                            });
                            
                            $('#coupons-money').blur(function(){
                            	checkMoney();
                            });
                            
                            $('#coupons-discount').blur(function(){
                            	checkDiscount();
                            });
                            
                            $('#min-order-money').blur(function(){
                            	checkOrderMoney();
                            });
                            
                            $('select[name="province_id"]').blur(function(){
                            	checkArea();
                            });
                            
                            $('#limit_num').blur(function(){
                                checkLimit();
                            });
                        },
                        callBack: function(){
                            var input = $('#coupons_form input'), inputLen = input.length;
                            for(var i = 0; i < inputLen; i ++){
                                checkCounpon(input.eq(i));
                            };
                            
                            checkMoney();
                            
                            checkDiscount();
                            
                            checkOrderMoney();
                            
                            checkArea();
                            
                            checkLimit();

                            if($('#coupons_form').find('.error').length > 0){
                                $.message.error('表单填写有误，请检查！');
                                return false;
                            }else {
                                var options = {
                                    url : ctx + '/api/customized/coupons/'+ $('.submit-type').val()+'.do',
                                    type: 'POST',
                                    dataType: 'json',
                                    success: function(result){
                                        if(result.result == 1){
                                            $.message.success('保存成功！', 'reload');
                                        }else {
                                            $.message.error(result.message);
                                            return false;
                                        };
                                    },
                                    error: function(){
                                        $.message.error('出现错误，请重试！');
                                        return false;
                                    }
                                };
                                $('#coupons_form').ajaxSubmit(options);
                            };
                        }
                    });
                },
                error: function(){
                    $.message.error('出现错误，请重试！');
                }
            });

        });
    })();
    
    //  发放优惠券
    (function(){
        var btn = $('.coupon-send');
        btn.unbind('click').on('click', function(){
            var _this = $(this), coupons_id = _this.attr('coupons_id'), send_num = _this.attr('send_num');
            $.confirm('注意：优惠券发放后则不可修改！确定要发放优惠券吗？', function(){
                $.ajax({
                    url : ctx + '/api/customized/coupons/send-coupons.do?coupons_id='+ coupons_id,
                    cache : false,
                    dataType: "json",
                    success : function(data) {
                        if(data.result==1){
                            $.message.success('已成功发放' + send_num + '张优惠券！', 'reload');
                        }else{
                            $.message.error(data.message);
                        }
                    },
                    error : function() {
                        $.message.error('出现错误，请重试！');
                    }
                });
            });
        });
    })();
    
    //  追加发放的优惠券库存
    (function(){
        var btn = $('.coupon-append');
        btn.unbind('click').on('click', function(){
            var _this = $(this), coupons_id = _this.attr('coupons_id');
            $.ajax({
                url: './append-coupons.html',
                type: 'GET',
                success: function(html){
                    $.dialog({
                        title: '追加优惠券数量',
                        html : html,
                        showCall: function(){
                        	$('#store_coupons_form input').blur(function(){
                                checkStock($(this));
                            });  
                        },
                        callBack: function(){
                        	var input = $('#store_coupons_form input'), inputLen = input.length;
                            for(var i = 0; i < inputLen; i ++){
                            	checkStock(input.eq(i));
                            };
                            
                            if($('#store_coupons_form').find('.error').length > 0){
                                $.message.error('表单填写有误，请检查！');
                                return false;
                            }else {
                                var options = {
                                    url : ctx + '/api/customized/coupons/append-coupons.do?coupons_id='+coupons_id,
                                    type: 'POST',
                                    dataType: 'json',
                                    success: function(result){
                                        if(result.result == 1){
                                            $.message.success(result.message, 'reload');
                                        }else {
                                            $.message.error(result.message);
                                            return false;
                                        };
                                    },
                                    error: function(){
                                        $.message.error('出现错误，请重试！');
                                        return false;
                                    }
                                };
                                $('#store_coupons_form').ajaxSubmit(options);
                            };
                        }
                    });
                },
                error: function(){
                    $.message.error('出现错误，请重试！');
                }
            });
        });
    })();
    
    //  使优惠劵无效
    (function(){
        var btn = $('.coupon-invalid');
        btn.unbind('click').on('click', function(){
            var _this = $(this), coupons_id = _this.attr('coupons_id');
            $.confirm('注意：已被领取的优惠券不会受到此操作的影响，仍可正常使用！确定要将此优惠券设置为失效状态吗？', function(){
                $.ajax({
                    url : ctx + '/api/customized/coupons/invalid-coupons.do?coupons_id='+ coupons_id,
                    cache : false,
                    dataType: "json",
                    success : function(data) {
                        if(data.result==1){
                            $.message.success('已成功将此优惠券设置为无效！', 'reload');
                        }else{
                            $.message.error(data.message);
                        }
                    },
                    error : function() {
                        $.message.error('出现错误，请重试！');
                    }
                });
            });
        });
    })();
    
    //  删除优惠劵
    (function(){
        var btn = $('.coupon-delete');
        btn.unbind('click').on('click', function(){
            var _this = $(this), coupons_id = _this.attr('coupons_id');
            $.confirm('确定要删除此优惠劵吗？', function(){
                $.ajax({
                    url : ctx + '/api/customized/coupons/delete-coupons.do?coupons_id='+ coupons_id,
                    cache : false,
                    dataType: "json",
                    success : function(data) {
                        if(data.result==1){
                            $.message.success('删除成功！', 'reload');
                        }else{
                            $.message.error(data.message);
                        }
                    },
                    error : function() {
                        $.message.error('出现错误，请重试！');
                    }
                });
            });
        });
    })();
    
    /* 新增、修改优惠劵校验
    ============================================================================ */
   function checkCounpon(_this){
       var _this = _this, val = $.trim(_this.val());
       if(!val){
           err(_this, 'error');
       }else if(_this.is('.coupons-stock')){
    	   if(_this.val() == '0'){
               err(_this, 'error');
               $.message.error('发行量不能为0！');
           }else {
               err(_this, 'succe');
           };
       }else if(_this.is('.limit-num')){
    	   if(_this.val() == '0'){
               err(_this, 'error');
               $.message.error('限领数量不能为0！');
           }else {
               err(_this, 'succe');
           };
       }else {
           err(_this, 'succe');
       };
       //  校验日期
       var s = $('.dialog-time-start').val(), e = $('.dialog-time-end').val();
       if(s && e){
           var node = $('.dialog-time-start, .dialog-time-end');
           if(s > e){
               err(node, 'error');
               $.message.error('开始日期不得大于结束日期！');
           }else {
               err(node, 'succe');
           };
       };
   };

   function checkStock(_this){
	   var _this = _this, val = $.trim(_this.val());
	   if (!val) {
           err(_this, 'error');
	   } else if (_this.is('.coupons-num')) {
		   if (_this.val() == '0') {
               err(_this, 'error');
               $.message.error('追加的优惠券数量不能为0！');
           } else {
               err(_this, 'succe');
           };
	   } else {
		   err(_this, 'succe');
	   }
   }
   
   /** 检查优惠券面额不能为0 */
   function checkMoney(){
	   var coupons_money = $('#coupons-money').val(), coupons_type = $("input[name='coupons_type']:checked").val(), money = $('#coupons-money');
	   if (coupons_type == 0) {
		   if (coupons_money == 0) {
			   err(money, 'error');
			   $.message.error('优惠券面额不能为0！');
		   } else {
			   err(money, 'succe');
			   $('#coupons-discount').parent().removeClass('has-error error');
		   }
	   }
   };
   
   /** 检查优惠券折扣不能为0 */
   function checkDiscount(){
	   var coupons_discount = $('#coupons-discount').val(), coupons_type = $("input[name='coupons_type']:checked").val(), discount = $('#coupons-discount');
	   if (coupons_type == 1) {
		   if (coupons_discount == 0) {
			   err(discount, 'error');
			   $.message.error('优惠券折扣不能为0！');
		   } else {
			   err(discount, 'succe');
			   $('#coupons-money').parent().removeClass('has-error error');
		   }
	   }
   };
   
   /** 检查买家消费金额不能为0 */
   function checkOrderMoney(){
	   var order_money = $('#min-order-money').val(), coupons_type = $("input[name='coupons_type']:checked").val(), orderMoney = $('#min-order-money');
	   if (coupons_type != 2) {
		   if (order_money == 0) {
			   err(orderMoney, 'error');
			   $.message.error('买家消费不能为0！');
		   }else {
			   err(orderMoney, 'succe');
		   }
	   } else {
		   $('#coupons-discount').parent().removeClass('has-error error');
		   $('#min-order-money').parent().removeClass('has-error error');
	   }
   };
   
   /** 检查优惠券使用地区是否选择 */
   function checkArea(){
	 var province_id = $('select[name="province_id"]').val(), province = $('select[name="province_id"]');
	 if (province_id == 0) {
		err(province, 'error');
		if(province.parent().find('.province-error-span').length == 0){
            $('<span class="province-error-span" style="color: #a94442; font-size: 12px; ">请选择使用地区！</span>').appendTo(province.parent());
        }else {
            $('.province-error-span').html('请选择使用地区！');
        };
	 } else {
		err(province, 'succe');
		$('.province-error-span').html('');
	 }
   };
   
   /** 检查优惠券领取量不能大于发行量 */
   function checkLimit(){
       var coupons_stock = $('#coupons-stock').val(), limit_num = $('#limit-num').val(), limit = $('#limit-num');
       if(coupons_stock && limit_num){
           if(parseInt(coupons_stock) < parseInt(limit_num)){
               err(limit, 'error');
               if(limit.parent().find('.limit-error-span').length == 0){
                   $('<span class="limit-error-span" style="color: #a94442; font-size: 12px; ">个人领取量不能大于发行量！</span>').appendTo(limit.parent());
               }else {
                   $('.limit-error-span').html('个人领取量不能大于发行量！');
               };
           }else {
               err(limit, 'succe');
               $('.limit-error-span').html('');
           };
       };
   };

   function err(node, str){
       if(str == 'error'){
           node.parent().addClass('has-error error');
       }else {
           node.parent().removeClass('has-error error');
       };
   };
});