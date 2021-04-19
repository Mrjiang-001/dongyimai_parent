//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function () {
        //地址路由接收参数
        var id = $location.search()["id"];
        if (id == null) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                //富文本编辑器赋值
                editor.html($scope.entity.goodsDesc.introduction);
                //将JSON结构的字符串转换成JSON对象使用
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
                for(var i =0;i<$scope.entity.itemList.length;i++){
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
                }

            }
        );
    }

    //回显规格选中
    $scope.checkAttibuteValue = function (specName, specValue) {
        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(items,"attributeName",specName);
        if(object==null){
            return false;
        }else{
            if(object.attributeValue.indexOf(specValue)>=0){
                return true;
            }else{
                return false;
            }
        }
    }


    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    //$scope.reloadList();//重新加载
                    location.href = "goods.html";
                } else {
                    alert(response.message);
                }
            }
        );
    }

   /* $scope.add = function () {
        //设置富文本编辑器的数据
        $scope.entity.goodsDesc.introduction = editor.html();
        goodsService.add($scope.entity).success(
            function (response) {
                if (response.success) {
                    //清空表单
                    //$scope.entity = {};
                    $scope.entity = {'goods': {}, 'goodsDesc': {'itemImages': [], 'specificationItems': []}};
                    //清空富文本编辑器
                    editor.html('');
                } else {
                    alert(response.message);
                }
            })
    }*/


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }


    $scope.item_image_entity = {'color': '', 'url': ''};    //初始化图片对象的数据结构

    //上传文件
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(
            function (response) {
                if (response.success) {
                    $scope.item_image_entity.url = response.message;
                } else {
                    alert(response.message);
                }
            }).error(function () {
            alert("上传发生异常");
        })
    }

    $scope.entity = {'goods': {}, 'goodsDesc': {'itemImages': [], 'specificationItems': []}}   //初始化商品复合实体的数据结构

    //增加行
    $scope.addTableRow = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.item_image_entity);

    }
    //删除行
    $scope.deleteTableRow = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }

    //页面加载时查询顶级分类列表
    $scope.findCategory1List = function () {
        itemCatService.findByParentId("0").success(
            function (response) {
                $scope.category1List = response;
            })
    }

    //联动查询二级分类列表
    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
        //观察绑定的数据是否发生变化,判断newValue是否有值
        if (newValue) {
            //查询二级分类列表
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.category2List = response;
                })
        }
    })
    //联动查询三级分类
    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
        //观察绑定的数据是否发生变化,判断newValue是否有值
        if (newValue) {
            //查询二级分类列表
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.category3List = response;
                })
        }
    })

    //联动查询模板ID
    $scope.$watch('entity.goods.category3Id', function (newValue, oldValue) {
        if (newValue) {
            itemCatService.findOne(newValue).success(
                function (response) {
                    $scope.entity.goods.typeTemplateId = response.typeId;
                })
        }
    })
    //联动查询模板对象
    $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
        if (newValue) {
            typeTemplateService.findOne(newValue).success(
                function (response) {
                    $scope.typeTemplate = response;
                    //JSON对象转换
                    $scope.brandList = JSON.parse($scope.typeTemplate.brandIds);   //品牌集合
                    if ($location.search()["id"] == null) {
                        $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);//模板下的扩展属性集合
                    }
                })
            //根据模板ID关联查询规格集合
            typeTemplateService.findSpecList(newValue).success(
                function (response) {
                    $scope.specList = response;
                })
        }
    })
    //$event 选中事件对象   name 规格名称  value 规格选项
    $scope.updateSpecAttribute = function ($event, name, value) {
        // 判断specificationItems是否有选中的规格对象
        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', name);
        if (object == null) {
            // a| 如果没有，则需要创建规格对象，并放入到specificationItems
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]});
        } else {
            // b| 如果有，判断用户是选中还是反选
            if ($event.target.checked) {
                // 1）选中
                // 则需要该规格对象下的attributeValue放入选中规格选项
                object.attributeValue.push(value);
            } else {
                // 2）反选
                // 则需要该规格下的attributeValue移除反选的规格选项
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);
                // 判断attributeValue中是否还有元素存在，如果没有元素，则specificationItems移除掉该规格对象
                if (object.attributeValue.length == 0) {
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }
        }
    }

    $scope.createItemList = function () {
        //1.初始化SKU的列表数据结构
        $scope.entity.itemList = [{'price': 0, 'num': 9999, 'status': '0', 'isDefault': '0', 'spec': {}}];
        var items = $scope.entity.goodsDesc.specificationItems;
        //2.遍历specificationItems
        for (var i = 0; i < items.length; i++) {
            //拼接SKU列表
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    }

    /**
     * 增加SKU的列
     * @param itemList    SKU列表
     * @param columnName    规格名称
     * @param columnValues   规格选项集合
     */
    addColumn = function (itemList, columnName, columnValues) {
        var newList = [];
        //1.遍历SKU列表
        for (var i = 0; i < itemList.length; i++) {
            var oldRow = itemList[i];      //之前的行的数据
            for (var j = 0; j < columnValues.length; j++) {
                //深克隆
                var newRow = JSON.parse(JSON.stringify(oldRow));
                //对SKU的spec属性进行赋值   {"机身内存":"16G"}
                newRow.spec[columnName] = columnValues[j];
                //将新构建好的SKU行数据重新添加到新的集合中
                newList.push(newRow);
            }
        }
        return newList;

    }
    //0.未审核  1.审核通过  2.驳回  3.关闭
    $scope.status = ["未审核", "审核通过", "驳回", "关闭"];

    $scope.categoryList = [];
    $scope.findCategoryList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.categoryList[response[i].id] = response[i].name;
                }
            })
    }
    //商品上架
    $scope.upGoods=function () {
        //获取选中的复选框
        goodsService.upGoods($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();
                    alert(response.message);
                } else {
                    alert(response.message);
                }
                $scope.selectIds = [];
            })
        }

    $scope.downGoods=function () {
        //获取选中的复选框
        goodsService.downGoods($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();
                    alert(response.message);
                } else {
                    alert(response.message);
                }
                $scope.selectIds = [];
            })
    }
});	