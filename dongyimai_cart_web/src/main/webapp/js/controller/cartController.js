app.controller('cartController',function ($scope,cartService1) {
    $scope.findCartList = function () {
        cartService1.findCartList().success(
            function (response) {
                $scope.cartList = response;
                $scope.total = cartService1.addNum($scope.cartList);
            });
    };

    $scope.addGoodsToCartList = function (itemId,num) {
        cartService1.addGoodsToCartList(itemId,num).success(
            function (response) {
                if (response.success) {
                    $scope.findCartList();
                } else {
                    alert(response.message);
                }
        });
    };
    
    $scope.findAddressListByUserId=function () {
        cartService1.findAddressListByUserId().success(
            function(response){
                $scope.addressList = response;
                for (var i=0;i<$scope.addressList.length;i++) {
                    $scope.address = $scope.addressList[i];
                    break;
                }
            }
        )
    }

    //选中地址
    $scope.selectAddress = function (address) {
        $scope.address = address;
    };
    //判断是否选中
    $scope.isSelect = function (address) {
        if ($scope.address == address) {
            return true;
        } else {
            return false;
        }
    };

    //初始化订单的数据结构
    $scope.order = {'paymentType': '1'};

    //选中支付方式
    $scope.selectPaymentType = function (type) {
        $scope.order.paymentType = type;
    };
    //提交订单
    $scope.submitOrder = function () {
        $scope.order.receiverAreaName = $scope.address.address;  //收货地址
        $scope.order.receiver=$scope.address.contact;               //收货人
        $scope.order.receiverMobile = $scope.address.mobile; //收货电话

        cartService1.submitOrder($scope.order).success(
            function (response) {
                if (response.success) {
                    if ($scope.order.paymentType == '1') {
                        location.href = 'pay.html';
                    } else {
                        location.href='paysuccess.html'
                    }
                }
        });
    };
})