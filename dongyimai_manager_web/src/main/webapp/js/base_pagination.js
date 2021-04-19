//自定义模块
var app = angular.module('dongyimai', ['pagination']);   //参数二：  引入分页模块

app.filter('trustHtml',['$sce',function ($sce) {
    return function (data){
        return $sce.trustAsHtml(data);
    }
}])