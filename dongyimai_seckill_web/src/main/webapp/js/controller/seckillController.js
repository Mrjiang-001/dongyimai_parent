app.controller('seckillController', function ($scope, $location, $interval, seckillService) {

    $scope.findList = function () {
        seckillService.findList().success(
            function (response) {
                $scope.list = response;
            })
    };
    $scope.findItem = function () {
        var id = $location.search()['id'];
        if (id == null) {
            return;
        }
        seckillService.findItem(id).success(
            function (response) {
                $scope.item = response;
                //console.log(new Date($scope.item.endTime).getTime());
                //console.log(((new Date($scope.item.endTime)).getTime() - (new Date()).getTime())/1000);
                var seconds = Math.floor(((new Date($scope.item.endTime)).getTime() - (new Date()).getTime()) / 1000);
                $interval(function () {
                    if (seconds > 0) {
                        seconds = seconds - 1;
                        $scope.timeString = formatTime(seconds);
                    } else {
                        //结束轮询
                        $interval.cancel();
                    }
                }, 1000)

            });
    };
    /*    $scope.second = 10;
        //十秒倒计时
        $interval(function () {
            if ($scope.second > 0) {
                $scope.second = $scope.second - 1;
            } else {
                //结束轮询
                $interval.cancel();
            }
        },1000)*/

    //格式话日期时间
    formatTime = function (seconds) {
        //秒换算天
        var day = Math.floor(seconds / (60 * 60 * 24));
        var hour = Math.floor(((seconds - day * 60 * 60 * 24) / (60 * 60)));
        var minute = Math.floor((seconds - day * 60 * 60 * 24 - hour * 60 * 60) / 60);
        var sec = seconds - day * 60 * 60 * 24 - hour * 60 * 60 - minute * 60;
        var timeString = "";
        if (day > 0) {
            timeString += day + "天";
        }
        return timeString + hour + ":" + minute + ":" + sec;
    };
    $scope.submitOrder = function () {
        seckillService.submitOrder($scope.item.id).success(
            function (response) {
                if (response.success) {
                    location.href = "pay.html";
                } else {
                    if (response.message == '用户未登录') {
                        //设置挑半夜
                        location.href = "login.html";
                    } else {
                        alert(response.message);
                    }
                }
            });
    };

    $scope.createNative = function () {
        seckillService.createNative().success(
            function (response) {
                $scope.outTradeNo = response.outTradeNo;//订单编号
                $scope.totalAmount = (response.totalAmount / 100).toFixed(2);//支付金额
                $scope.totalFee = response.totalFee;
                //生成二维码
                var qrcode = new QRious({
                    'element': document.getElementById("erweima"),
                    'level': 'H',
                    'size': '250',
                    'value': response.qrCode
                })
                $scope.queryPayStatus($scope.outTradeNo);
            });
    };

    $scope.queryPayStatus = function (outTradeNo) {
        seckillService.queryPayStatus(outTradeNo).success(
            function (response) {
                if (response.success) {
                    //交易成功
                    location.href = "paysuccess.html#?money="+$scope.totalAmount;
                } else {
                    if (response.message="二维码超时") {
                        location.href = "payfail.html";
                    } else {
                        location.href = "payfail.html";
                    }
                }
            });
    };
    $scope.getMoney = function () {
        return $location.search()['money'];
    };
})