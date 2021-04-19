app.controller('payController', function ($scope,$location, payService) {
    $scope.createNative=function () {
        payService.createNative().success(
            function (response) {
                $scope.outTradeNo = response.outTradeNo;//订单编号
                $scope.totalAmount = (response.totalAmount/100).toFixed(2);//支付金额
                $scope.totalFee=response.totalFee;
                //生成二维码
                var qrcode = new QRious({
                    'element': document.getElementById("erweima"),
                    'level':'H',
                    'size':'250',
                    'value':response.qrCode
                })
                $scope.queryPayStatus($scope.outTradeNo);
            });
    }

    $scope.queryPayStatus = function (outTradeNo) {
        payService.queryPayStatus(outTradeNo).success(
            function (response) {
                if (response.success) {
                    //交易成功
                    location.href = "paysuccess.html#?money="+$scope.totalAmount;
                } else {
                    if (response.message="二维码超时") {
                        document.getElementById("timeout").innerHTML = "二维码超时,请刷新页面重新获取二维码!!";
                    } else {
                        location.href = "payfail.html";
                    }
                    //location.href = "payfail.html";
                }
        });
    };
    $scope.getMoney = function () {
        return $location.search()['money'];
    };
});