package com.offcn.page.service;

/**
 * 商品详情页面
 */
public interface ItemPageService {
    /**
     * 生产商品详情页
     * @param goodsId
     * @return
     */
    public boolean createItemPage(Long goodsId);
    
    /**
     * 删除商品详情页
     * @param ids
     */
    public void deleteItemPage(Long[] ids);
}
