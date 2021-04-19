app.controller('baseController', function ($scope) {
    //设置分页参数
    $scope.paginationConf = {
        'currentPage': 1,   //当前页码
        'itemsPerPage': 10, //每页显示条数
        'totalItems': 10,  //总记录数
        'perPageOptions': [10, 20, 30, 40, 50],    //每页显示条数选择器
        onChange: function () {
            //执行分页查询
            //$scope.findPage($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
            $scope.reloadList();
        }
    }
    $scope.reloadList = function () {
        //$scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }
    $scope.selectIds = [];   //初始化选中ID集合的数据结构

    //选中
    $scope.updateSelection = function ($event, id) {
        //判断复选框是选中还是反选
        if ($event.target.checked) {
            //选中，则向数组中放入ID
            $scope.selectIds.push(id);
        } else {
            //反选，则从数组中移除元素
            var index = $scope.selectIds.indexOf(id);   //返回该元素的索引位置
            $scope.selectIds.splice(index, 1);   //参数一：元素的索引位置  参数二：移除的个数
        }
    }


    //JSON字符串转换
    $scope.jsonToString = function (jsonString, key) {
        //1.将JSON结构的字符串转换成JSON对象
        var json = JSON.parse(jsonString);
        var value = "";
        //2.遍历JSON集合
        for (var i = 0; i < json.length; i++) {
            if (i > 0) {
                value += ",";
            }
            //3.取得key值，完成字符串拼接
            value += json[i][key]
        }
        return value;
    }


    /**
     * 判断对象在集合中是否存在
     * @param list    待判断的集合
     * @param key           集合中的属性
     * @param value         属性值
     */
    $scope.searchObjectByKey = function (list,key,value){
        for(var i = 0;i<list.length;i++){
            if(list[i][key]==value){
                return list[i];
            }
        }
        return null;
    }
})