var Cart={
		init:function(staticserver){
			var self=this;			
			this.bindEvent();
		},
		bindEvent:function(){
			var self=this;
			
			//购物数量调整
			$(".Numinput .increase,.Numinput .decrease").click(function(){
				$this = $(this);
				var number = $this.parents(".Numinput");
				var itemid =number.attr("itemid");
				var productid =number.attr("productid");
				var objipt = number.find("input");
				var num=objipt.val();
				num =parseInt(num);
				if (!isNaN(num)){
					if($this.hasClass("increase")){
						num++;
					}
					if($this.hasClass("decrease")){
						 if(num == 1 ){
							 if(confirm("确定要删除该商品?")){
								 self.deleteGoodsItem(itemid);
							 }
							 return false;
						} 
						num--;
					}
					 num = (num <=1 || num > 100000) ? 1 : num;
					 self.updateNum(itemid, num, productid,objipt);
				}
			});
			
			//购物数量手工输入
            $(".Numinput input").keydown(function(e){
                var kCode = $.browser.msie ? event.keyCode : e.which;
                //判断键值  
                if (((kCode > 47) && (kCode < 58)) 
                    || ((kCode > 95) && (kCode < 106)) 
                    || (kCode == 8) || (kCode == 39) 
                    || (kCode == 37)) { 
                    return true;
                } else{ 
                    return false;  
                }
            }).focus(function() {
                this.style.imeMode='disabled';// 禁用输入法,禁止输入中文字符
            }).keyup(function(){
                var pBuy   = $(this).parent();//获取父节点
                var itemid  = pBuy.attr("itemid");
                var productid  = pBuy.attr("productid");
                var numObj = pBuy.find("input[name='num']");//获取当前商品数量
                var num    = parseInt(numObj.val());
                if (!isNaN(num)) {
                    var numObj = $(this);
                    var num    = parseInt(numObj.val());
                    num = (num <=1 || num > 100000) ? 1 : num;
                    self.updateNum(itemid, num, productid,numObj);
                }
            });
            
			//删除商品
			$("#cart-wrapper .delete").click(function(){
				var cartid = $(this).parents("tr").attr("itemid");
				if(confirm("您确实要把该商品移出购物车吗？") ){
					self.deleteGoodsItem(cartid);
				}
			});
			
			//清空购物车
			$("#cart-wrapper .clean_btn").click(function(){
				if ($('#cart-wrapper input[name="product_id"]:checked').length == 0) {
					alert('请至少选择一件商品');
					return;
				}
				if(confirm("您确认要清空购物车吗？") ){
					self.clean();
				}
			});
			
			//继续购物
			$("#cart-wrapper .returnbuy_btn").click(function(){
				location.href="index.html";
			});
			
			//去结算
			$("#cart-wrapper .checkout-btn").click(function(){
				if(isLogin){
					if ($('#cart-wrapper input[name="product_id"]:checked').length == 0) {
						alert('请至少选择一件商品');
						return;
					}
					location.href="checkout.html";
				}else{
					self.showLoginWarnDlg();					
				}
			});
			//选择货品
			$("input[name='product_id']").click(function(){
				self.checkProduct(this);
			});
			//全选货品
			$("input[name='select_all']").click(function(){
				self.checkAllProduct(this);
			});
			
			//显示促销活动详细
			$("#cart-wrapper .act_detail").click(function(){
				var actId = $(this).attr("act_id");
				$(".goods-add-float").load("detail/activity_detail.html?activityId="+actId);
			});
		},
		
		//提示登录信息
		showLoginWarnDlg:function(btnx,btny){
			var html = $("#login_tip").html();
			$.dialog({ title:'提示信息',content:html,lock:true,init:function(){
				
				$(".button-wrapper .btn").jbtn();
				$(".button-wrapper .to-login-btn").click(function(){
					 location.href="login.html?forward=checkout.html";
				});

				//
				$(".button-wrapper .to-checkout-btn").click(function(){
					location.href="register.html?forward=checkout.html";
				});
				
//				// 跳转到结算页  取消 直接购买  
//				$(".button-wrapper .to-register-btn").click(function(){
//					location.href="register.html?forward=checkout.html";
//				});
				
				
				
			}});
		},
		
		//删除一个购物项
		deleteGoodsItem:function(itemid){
			var self=this;
			$.Loading.show("请稍候...");
			$.ajax({
				url:ctx+"/api/shop/cart/delete.do",
				data:"cartid="+itemid,
				dataType:"json",
				success:function(result){
					if(result.result==1){
						self.refreshTotal();
						self.removeItem(itemid);
						var cartItem = $("#cart-wrapper table tr").length;
						if(cartItem <= "1"){
							$("#cart-wrapper").empty();
							$("#cart-wrapper").load(ctx+"/cart/cart_item_blank.html");
						}
					}else{
						$.alert(result.message);
					}	
					$.Loading.hide();
				},
				error:function(){
					$.Loading.hide();
					$.alert("出错了:(");
				}
			});
		},
		
		//移除商品项
		removeItem:function(itemid){
			$("#cart-wrapper tr[itemid="+itemid+"]").remove();
		},
		
		//清空购物车
		clean:function(){
			$.Loading.show("请稍候...");
			var self=this;
			$.ajax({
				url:ctx+"/api/shop/cart/clean.do",
				dataType:"json",
				success:function(result){
					$.Loading.hide();
					if(result.result==1){
						location.href='cart.html';
					}else{
						$.alert("清空失败:"+result.message);
					}				 
				},
				error:function(){
					$.Loading.hide();
					$.alert("出错了:(");
				}
			});		
		},
		
		//更新数量
		updateNum:function(itemid,num,productid,num_input){
			var self = this;
			$.ajax({
				url:ctx+"/api/shop/cart/update-num.do",
				data:"cartid="+itemid +"&num="+num +"&productid="+productid,
				dataType:"json",
				success:function(result){
					if(result.result==1){
						if(result.data.store>=num){
							self.refreshTotal();
							var price = parseFloat($("tr[itemid="+itemid+"]").attr("price"));
							//price =price* num;
							price =self.changeTwoDecimal_f(price* num);
							$("tr[itemid="+itemid+"] .itemTotal").html("￥"+price);
							var point = $("tr[itemid="+itemid+"]").attr("point");
							if(typeof(point)!="undefined"&&point!=''){
								var html=$("tr[itemid="+itemid+"] .itemTotal").html();
								$("tr[itemid="+itemid+"] .itemTotal").html(html+"+"+parseInt(point)*num+"分")
							}
							num_input.val(num);
						}else{
							num_input.val(result.data.store);
							alert("抱歉！您所选选择的货品库存不足。");
							$.ajax({
								url:ctx+"/api/shop/cart/update-num.do",
								data:"cartid="+itemid +"&num="+result.data.store +"&productid="+productid,
								dataType:"json",
								success:function(result){

								},
								error:function(){
									alert("出错了:(");
								}
							});		
						}
					}else{
						alert(result.message);
					}
				},
				error:function(){
					alert("出错了:(");
				}
			});		
		},
		
		//刷新价格
		refreshTotal:function(){
			var self = this;
			$.ajax({
				url:"cart/cartTotal.html",
				dataType:"html",
				success:function(html){
					 $(".yes_bonded").html(html);
				},
				error:function(){
					alert("糟糕，出错了:(");
				}
			});
		},
		
		changeTwoDecimal_f:function(x) {
	        var f_x = parseFloat(x);
	        if (isNaN(f_x)) {
	            alert('参数为非数字，无法转换！');
	            return false;
	        }
	        var f_x = Math.round(x * 100) / 100;
	        var s_x = f_x.toString();
	        var pos_decimal = s_x.indexOf('.');
	        if (pos_decimal < 0) {
	            pos_decimal = s_x.length;
	            s_x += '.';
	        }
	        while (s_x.length <= pos_decimal + 2) {
	            s_x += '0';
	        }
	        return s_x;
	    },
	       fixCheckbox: function() {
	    	var checkboxes = $('input[name="product_id"]');
	    	var all = $('input[name="select_all"]');
	    	var state = (checkboxes.length === checkboxes.filter(':checked').length);
	    	all.prop('checked', state);
	    },
	    checkProduct:function(product){
	    	var self = this;
	    	$.ajax({
				url:ctx+"/api/shop/cart/check-product.do",
				data:{"checked":product.checked,"product_id":$(product).val(),"exchange":$(product).attr("exchange")},
				dataType:"json",
				success:function(result){
					self.refreshTotal();
					self.fixCheckbox();
				},
				error:function(){
					alert("出错了:(");
				}
			});		
	    },
	    checkAllProduct:function(product){
	    	var self = this;
	    	if(product.checked){
	    		$("input[name='product_id']").attr("checked","checked");
	    	}else{
	    		$("input[name='product_id']").removeAttr("checked");
	    	}
	    	$.ajax({
				url:ctx+"/api/shop/cart/check-all.do",
				data:{"checked":product.checked},
				dataType:"json",
				success:function(result){
					self.refreshTotal();
				},
				error:function(){
					alert("出错了:(");
				}
			});		
	    }
};

$(function(){
	Cart.init();
	Cart.fixCheckbox();
});