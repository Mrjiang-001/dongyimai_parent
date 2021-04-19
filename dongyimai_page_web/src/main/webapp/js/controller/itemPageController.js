app.controller('itemPageController',function ($scope,$http) {
    //购物车加减数量
    $scope.addNum=function (num) {
        $scope.num+=num;
        if ($scope.num < 1) {
            $scope.num = 1;
        }
    }
    //初始化规格对象的数据结构
    $scope.specification = {};
    //初始化选择规格选项
    $scope.selectedSpecification=function (key,value) {
        $scope.specification[key]=value;
        searchSku();
    }
    //是否选中
    $scope.isSelected = function (key,value)  {
        if ($scope.specification[key] == value) {
            return true;
        } else {
            return false;
        }

    };
    //加载SKU数据
    $scope.loadSku = function () {
        $scope.sku = skuList[0];
        //深克隆
        $scope.specification = JSON.parse(JSON.stringify($scope.sku.spec));
    };

    //选中规格，比较规格信息
    searchSku = function () {
        for (var i = 0; i < skuList.length; i++) {
            //比较选择规格
            if (matchObject($scope.specification, skuList[i].spec)) {
                //赋值给SKU
                $scope.sku = skuList[i];
                return;
            }
        }
        //如果规格没有匹配信息
        $scope.sku = {'id': 0, 'title': '----', 'price': 0, 'spec': {}};

    };

    //比较MAP是否一致
    matchObject = function (map1,map2) {
        for (var key in map1) {
            if (map1[key] != map2[key]) {
                return false;
            }
        }
        for (var key in map2) {
            if (map2[key] != map1[key]) {
                return false;
            }
        }
        return true;
    };

    //加入购物车
    $scope.addToCart = function () {
        //alert('ID' + $scope.sku.id);
        $http.get('http://localhost:9108/cart/addGoodsToCartList.do?itemId=' + $scope.sku.id + '&num=' + $scope.num,{'withCredentials':true}).success(
            function (response) {
                if (response.success) {
                    location.href = 'http://localhost:9108/cart.html';
                }
            });
    };





















})