/**
 * 结算地址修改、新增
 */

var checkOutAddress={
		
		//更换地址列表
		addressChange:function (){
			$(".c_addr").val($(".current").find("input").attr("shipAddr"));   //地址
			$(".c_addr1").val($(".current").find("input").attr("shipZip"));   //邮编
			$(".c_addr2").val($(".current").find("input").attr("shipName"));   //收货人
			$(".c_addr3").val($(".current").find("input").attr("shipMobile"));   //手机
			$(".c_addr4").val($(".current").find("input").attr("shipTel"));   //电话
			$(".c_addr5").val($(".current").find("input").attr("province_id"));   //省份ID
			$(".c_addr6").val($(".current").find("input").attr("city_id"));   //城市ID
			$(".c_addr7").val($(".current").find("input").attr("region_id"));   //街道ID
			$(".c_addr8").val($(".current").find("input").attr("province"));   //省份
			$(".c_addr9").val($(".current").find("input").attr("city"));   //城市
			$(".c_addr10").val($(".current").find("input").attr("region"));   //街道
			$(".c_addr11").val($(".current").find("input").val());   //收货ID
		},
		
		listaddress:function(){
			var self = this;
			$(".modify .list .checkout_addresslist").click(function(){
				$(".modify .list .checkout_addresslist").removeClass("current");
				$(".modify .list .checkout_addresslist").find("span").removeClass("checkout_yes");
				$(this).addClass("current");
				$(this).find("span").addClass("checkout_yes");
				
				//add_by DMRain 2016-8-5
				self.refreshDlyList();
				
				self.addressChange();
			})
		},
		
		
		//新增地址
		
		newListAddress:function(){
			
			$(".c_addr").val($(".addr").val());   //地址
			$(".c_addr1").val($(".addr1").val());   //邮编
			$(".c_addr2").val($(".addr2").val());   //收货人
			$(".c_addr3").val($(".addr3").val());   //手机
			$(".c_addr4").val($(".addr4").val());   //电话
			$(".c_addr5").val($("#province_id").val());   //省份ID
			$(".c_addr6").val($("#city_id").val());   //城市ID
			$(".c_addr7").val($("#region_id").val());   //街道ID
			$(".c_addr8").val($("#province_id").find("option:selected").text());   //省份
			$(".c_addr9").val($("#city_id").find("option:selected").text());   //城市
			$(".c_addr10").val($("#region_id").find("option:selected").text());   //街道
			$(".c_addr11").val("");   //收货ID
			$(".c_addr12").val($(".addr12").val());   //是否是默认
		},
		
		saveAddress:function(){
			var self = this;
			$("#add_btn").click(function(){
				
				self.newListAddress();
				//判断收货地址
				if($("#region_id").val()== "0"){
					alert("请选择收货地址");
					 return false;
				};
				
//				//地区联动选择-选择最后一级（区）时自动设置邮编
//				RegionsSelect.regionChange=function(regionid,name,zipcode){
//					$(".addr1").val(zipcode);
//				};
				
				//判断收货详细地址
				if($(".addr").val()== ""){
					alert("请输入详细地址");
					 return false;
				};
				
				//判断邮编格式
				 if($(".addr1").val()!=""){
					 var tel = $(".addr1").val();
					 var reg =/^[0-9]{6}$/;
					 if(reg.test(tel)==false){
						 alert("邮编格式错误");
						 return false;
					 }
				 }
				//校验用户名
				var membername = $(".addr2").val();
				if(membername.indexOf(" ")!=-1){
					alert("请确保会员名称前、后或名称中有空格");
					return false;
				}
				if($(".addr2").val() == ""){
					alert("请输入联系人姓名");
					return false;
				}
				
				//校验手机
				if($(".addr3").val()==""){
					alert("手机未填写");
					return false;
				}		
				//判断手机格式
				 if($(".addr3").val()!=""){
					 var tel = $(".addr3").val();
					 var reg = /^1[3|4|5|7|8]\d{9}$/;
					 if(reg.test(tel)==false){
						 alert("手机格式错误");
						 return false;
					 }
				 }
				var options = {
					url :ctx+"/api/shop/member-address/add-new-address.do",
					type : "POST",
					dataType : 'json',
					success : function(result) {
						if (result.result == 1) {
							location.href="checkout.html";
						}
						if (result.result == 0) {
							alert(result.message);
							 
						}
					},
					error : function(e) {
						alert("出现错误，请重试");
					}
				};
				$("#checkoutform").ajaxSubmit(options);
			})
		},
		
		
		editAddress:function(){
			var self = this;
			$("#deit_btn").click(function(){
				
				self.newListAddress();
				//判断收货地址
				if($("#region_id").val()== "0"){
					alert("请选择收货地址");
					 return false;
				};
				
//				//地区联动选择-选择最后一级（区）时自动设置邮编
//				RegionsSelect.regionChange=function(regionid,name,zipcode){
//					$(".addr1").val(zipcode);
//				};
				
				//判断收货详细地址
				if($(".addr").val()== ""){
					alert("请输入详细地址");
					 return false;
				};
				
				//校验邮编
				if($(".addr3").val()==""){
					alert("手机未填写");
					return false;
				}	
				
				//判断邮编格式
				 if($(".addr1").val()!=""){
					 var tel = $(".addr1").val();
					 var reg = /^[1-9][0-9]{5}$/;
					 if(reg.test(tel)==false){
						 alert("邮编格式错误");
						 return false;
					 }
				 }
				//校验用户名
				var membername = $(".addr2").val();
				if(membername.indexOf(" ")!=-1){
					alert("请确保会员名称前、后或名称中有空格");
					return false;
				}
				if($(".addr2").val() == ""){
					alert("请输入联系人姓名");
					return false;
				}
				
				//校验手机
				if($(".addr3").val()==""){
					alert("手机未填写");
					return false;
				}		
				//判断手机格式
				 if($(".addr3").val()!=""){
					 var tel = $(".addr3").val();
					 var reg = /^0?1[3|4|5|8][0-9]\d{8}$/;
					 if(reg.test(tel)==false){
						 alert("手机格式错误");
						 return false;
					 }
				 }
				 
				//判断是不是用新地址作为默认模板，1为时，不选为空null 
				var editid = $(this).attr("rel");
				var defAddr = parseInt($('input[name="def_addr"]:checked ').val()); 
				//判断默认地址是否为空，如果为空，添加值为0
				if(isNaN(defAddr)){
					var defAddr = parseInt(0);
				}
				var options = {
					url :ctx+"/api/shop/member-address/edit-new-address.do?addr_id="+editid+"&def_addr="+defAddr,
					type : "POST",
					dataType : 'json',
					success : function(result) {
						if (result.result == 1) {
							location .href="checkout.html";
						}
						if (result.result == 0) {
							alert(result.message);
							 
						}
					},
					error : function(e) {
						$.Loading.error("出现错误，请重试");
					}
				};
				$("#checkoutform").ajaxSubmit(options);
			})
		},
		
		//改变收货地址时，要重新加载各种信息，包括配送方式，优惠券、促销信息以及订单价格计算	add_by DMRain 2016-8-5
		refreshDlyList:function(){
			var self = this;
			var typeId = $(".hidden_dly").val();
			var activity_id = $("#isActivity").val();
			var regionid = $(".current").find("input").attr("region_id");
			
			//选择地址时，要重新加载配送方式信息
			$(".dly_list").load(ctx + "/checkout/dlytype_list.html?regionid="+regionid);
			
			//重新加载赠送的赠品和优惠券
			$(".gift_bonus").load(ctx + "/checkout/gift_bonus.html?regionId="+regionid+"&typeId="+typeId+"&activityId="+activity_id);
			
			//重新加载优惠券
			var bonusShow = $(".checkout-bonus-use").attr("rel");
			if(bonusShow == "show"){
				$(".checkout-bonus-list").load(ctx + "/checkout/bonus.html?regionid="+regionid+"&typeid="+typeId+"&activityId="+activity_id);
			}
			
			//重新加载购物车价格
			$(".total").load(ctx + "/checkout/checkout_total.html?regionId="+regionid+"&typeId="+typeId+"&activityId="+activity_id); 
		}
		
}