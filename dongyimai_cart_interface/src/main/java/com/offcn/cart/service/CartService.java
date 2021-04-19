package com.offcn.cart.service;

import com.offcn.group.Cart;

import java.util.List;

public interface CartService {
    
    /**
     *想购物车列表中添加商品
     * @param srcCartList 原购物车列表
     * @param itemId 商品id(SKU的ID)
     * @param num 购买数量
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> srcCartList, Long itemId, Integer num);
    
    /**
     * 从缓存中查询购物车列表
     * @param username  当前用户名
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);
    
    /**
     * 向缓存中存放购物车列表
     * @param username 用户名
     * @param cartList 购物车列表
     */
    public void saveCartListToRedis(String username, List<Cart> cartList);
    
    /**
     * 合并购物车
     * @param cartList_cookie cookie中的购物车列表
     * @param cartList_redis redis中的购物车列表
     * @return
     */
    public List<Cart> mergeCartList(List<Cart> cartList_cookie, List<Cart> cartList_redis);
}
