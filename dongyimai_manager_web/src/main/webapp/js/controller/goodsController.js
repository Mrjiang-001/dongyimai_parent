 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location ,itemCatService ,goodsService){
	
	$controller('baseController',{$scope:$scope});//继承

    //读取列表数据绑定到表单中
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//查询实体
	$scope.findOne = function () {
		//地址路由接收参数
		var id = $location.search()["id"];
		goodsService.findOne(id).success(
			function (response) {
				$scope.entity = response;
				//富文本编辑器赋值
				editor.html($scope.entity.goodsDesc.introduction);
				//将JSON结构的字符串转换成JSON对象使用
				$scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
				for(var i =0;i<$scope.entity.itemList.length;i++){
					$scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
				}

			}
		);
	}
	
	//保存 
	$scope.save=function(){
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
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
		goodsService.dele( $scope.selectIds ).success(
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
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	//0  1  2  3
	$scope.status = ["未审核", "审核通过", "驳回", "关闭"];

	$scope.categoryList = [];
	$scope.findCategoryList = function () {
		itemCatService.findAll().success(
			function (response) {
				for (var i = 0; i < response.length; i++) {
					$scope.categoryList[response[i].id] = response[i].name;
				}
			})
	}

	$scope.updateAuditStatus=function (auditStatus) {
		goodsService.updateAuditStatus($scope.selectIds,auditStatus).success(
			function (response) {
				if (response.success) {
					$scope.reloadList();
				} else {
					alert(response.message);
				}
		})
	}


	//页面加载时查询顶级分类列表
	$scope.findCategory1List = function () {
		itemCatService.findByParentId("0").success(
			function (response) {
				$scope.category1List = response;
			})
	}

	//联动查询二级分类列表
	$scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
		//观察绑定的数据是否发生变化,判断newValue是否有值
		if (newValue) {
			//查询二级分类列表
			itemCatService.findByParentId(newValue).success(
				function (response) {
					$scope.category2List = response;
				})
		}
	})
	//联动查询三级分类
	$scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
		//观察绑定的数据是否发生变化,判断newValue是否有值
		if (newValue) {
			//查询二级分类列表
			itemCatService.findByParentId(newValue).success(
				function (response) {
					$scope.category3List = response;
				})
		}
	})

	//获取所有品牌集合
	$scope.BrandList = [];
	$scope.findBrandList = function () {
		goodsService.findBrandAll().success(
			function (response) {
				$scope.BrandList = response;
			}
		);
	}
	//根据品牌id显示品牌名称
	$scope.findBrandNameById = function(id){
		for (var i = 0; i < $scope.BrandList.length; i++){
			if ($scope.BrandList[i].id===id){
				return $scope.BrandList[i].name;
			}
		}
	}

	//回显规格选中
	$scope.checkAttibuteValue = function (specName, specValue) {
		var items = $scope.entity.goodsDesc.specificationItems;
		var object = $scope.searchObjectByKey(items,"attributeName",specName);
		if(object==null){
			return false;
		}else{
			if(object.attributeValue.indexOf(specValue)>=0){
				return true;
			}else{
				return false;
			}
		}
	}
});	