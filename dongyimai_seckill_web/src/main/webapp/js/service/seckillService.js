app.service('seckillService',function ($http) {

    this.findList = function (){
        return $http.get('../seckillGoods/findList.do')
    };

    this.findItem = function (id) {
        return $http.get('../seckillGoods/findItem.do?id='+id);
    };
    this.submitOrder = function (itemId) {
        return $http.get("../seckillOrder/submitOrder.do?itemId=" + itemId);
    };

    this.createNative = function () {
        return $http.post("../aliPay/createNative.do");
    };

    this.queryPayStatus = function (outTradeNo) {
        return $http.get('../aliPay/queryPayStatus.do?outTradeNo='+outTradeNo);
    };

})