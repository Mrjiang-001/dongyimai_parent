app.controller('contentController',function ($scope,contentService) {


    $scope.contentList = [];
    $scope.findByCategroyId=function (categroyId) {
        contentService.findByCategroyId(categroyId).success(
            function (response) {
                $scope.contentList[categroyId] = response;
        })
    }
    
    //ๆ็ดขๅๅ
    $scope.search=function () {
        location.href = "http://localhost:9104/search.html#?keywords=" + $scope.keywords;
    }
    
})