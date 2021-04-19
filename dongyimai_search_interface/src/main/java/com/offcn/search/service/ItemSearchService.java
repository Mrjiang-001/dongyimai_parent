package com.offcn.search.service;

import com.offcn.pojo.TbItem;

import java.util.List;
import java.util.Map;

/**
 * 搜索模块查询接口
 */
public interface ItemSearchService {
    /**
     * 搜索商品
     *
     * @param searchMap
     * @return
     */
    public Map<String, Object> search(Map searchMap);
    
    /**
     * 导入SKU数据
     * @param itemList
     */
    public void importItem(List<TbItem> itemList);
    
    /**
     * 删除SKU数据
     * @param goodsIds
     */
    public void deleteByGoodsIds(List<Long> goodsIds);
}
