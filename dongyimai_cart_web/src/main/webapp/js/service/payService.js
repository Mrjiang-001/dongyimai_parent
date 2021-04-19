app.service('payService', function ($http) {

    this.createNative=function () {
        return $http.get('../aliPay/createNative.do');
    }
    this.queryPayStatus = function (otTradeNo) {
        return $http.get('../aliPay/queryPayStatus.do?outTradeNo='+otTradeNo);
    };
});