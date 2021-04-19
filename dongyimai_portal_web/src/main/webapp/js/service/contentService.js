app.service('contentService',function ($http) {

    this.findByCategroyId=function (categroyId) {
        return $http.get('../content/findByCategroyId.do?categroyId=' + categroyId);
    }
})