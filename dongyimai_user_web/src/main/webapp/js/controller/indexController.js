app.controller('indexController', function ($scope, userService) {
    $scope.getLoginName=function() {
        userService.getLoginName().success(
            function (response) {
            $scope.loginName = response.name;
        });
    }
});