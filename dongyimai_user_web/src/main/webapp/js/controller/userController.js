 //用户表控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		userService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		userService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		userService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=userService.update( $scope.entity ); //修改  
		}else{
			serviceObject=userService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		userService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		userService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}


	//$scope.entity = {};
	//注册用户
	$scope.register=function () {
		//格式验证
		if ($scope.entity.username == null || $scope.entity.username == '') {
			alert("用户名不能为空");
			return;
		}
		if ($scope.entity.password == null || $scope.entity.password == '') {
			alert("密码不能为空");
			return;
		}
		if ($scope.entity.password != $scope.repassword) {
			alert("密码与确认密码不一致");
			return;
		}
		if ($scope.smsCode == null || $scope.smsCode == '') {
			alert('验证码不能为空!');
			return;
		}
		if ($scope.entity.email == null || $scope.entity.email == '') {
			alert('邮箱不能为空')
			return;
		}
		var reg = /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/;
		if (!reg.test($scope.entity.email)){
			alert("邮箱格式不正确");
			return ;
		}
		userService.add($scope.entity,$scope.smsCode).success(
			function(response){
				if (response.success) {
					alert("注册成功");
				} else {
					alert(response.message);
				}
		});
	}
    //获取短信验证码
	$scope.createSmsCode=function () {
		if ($scope.entity.phone==null||$scope.entity.phone=='') {
			alert('手机号不能为空');
			return;
		}
		userService.createSmsCode($scope.entity.phone).success(
			function (response) {
				alert(response.message);
		});
	}
});	