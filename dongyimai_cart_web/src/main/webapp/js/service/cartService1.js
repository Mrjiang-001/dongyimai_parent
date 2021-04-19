app.service('cartService1',function ($http) {
    this.findCartList=function () {
        return $http.get('../cart/findCartList.do');
    }
    this.addGoodsToCartList = function (itemID,num) {
        return $http.get('../cart/addGoodsToCartList.do?itemId='+itemID+'&num='+num);
    };




    //计算总金额
    this.addNum = function(cartList){
        var total = {'totalNum':0,'totalMoney':0.00};
        for (var i=0;i<cartList.length;i++) {
            var cart = cartList[i];
            for (var j=0;j<cart.orderItemList.length;j++) {
                var orderItem = cart.orderItemList[j];
                total.totalNum += orderItem.num;     //总数量
                total.totalMoney += orderItem.totalFee;  //总金额
            }
        }
        return total;
    }

    this.findAddressListByUserId = function (){
        return $http.get('../address/findAddressListByUserId.do')
    }


    //提交订单
    this.submitOrder = function (order) {
        return $http.post('../order/add.do',order)
    };
})