package com.offcn.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.cart.service.CartService;
import com.offcn.group.Cart;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     *想购物车列表中添加商品
     * @param srcCartList 原购物车列表
     * @param itemId 商品id(SKU的ID)
     * @param num 购买数量
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> srcCartList, Long itemId, Integer num) {
        //1.sku的id查询SKU信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        //判断商品是否存在
        if(item==null){
            throw new RuntimeException("该商品不存在");
        }
        //判断商品的审核状态
        if(!item.getStatus().equals("1")){
            throw new RuntimeException("该商品未审核,不能购买");
        }
        //取得商家信息
        String sellerId = item.getSellerId();
        String sellerName = item.getSeller();
        //3.组装购物车对象,需要根据sellerID判断购物车列表是否有该商家
        Cart cart = this.searchCartBySellerId(srcCartList, sellerId);
        //4.如果购物车位空,则新创建购物车对象
        if (cart == null) {
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(sellerName);
            //4.1构建订单详情列表
            List<TbOrderItem> orderItemList = new ArrayList<>();
            //4.2将订单详情列表放入购物车
            cart.setOrderItemList(orderItemList);
            //4.3将购物车对象放入到购物车列表
            srcCartList.add(cart);
        } else {//5.如果购物车存在,向购物车中添加商品
            //6.判断商品是否在订单详情中存在
            TbOrderItem orderItem = this.searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (orderItem == null) {//6.1如果不存在,则从新创建订单详情
                orderItem = this.createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            } else {//6.2如果存在,则修改购买数量
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
                //7.判断订单详情的购买数量是否为0
                if (orderItem.getNum() == 0) {
                    cart.getOrderItemList().remove(orderItem);
                }
                //8.判断购物车中订单详情列表的元素个数是否为0
                if (cart.getOrderItemList().size() == 0) {
                    srcCartList.remove(cart);
                }
            }
        }
        return srcCartList;
    }
    
    /**
     * 从缓存中查询购物车列表
     *
     * @param username 当前用户名
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart> cartList =(List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (CollectionUtils.isEmpty(cartList)) {
            return new ArrayList<Cart>();
        }
        return cartList;
    }
    
    /**
     * 向缓存中存放购物车列表
     *
     * @param username 用户名
     * @param cartList 购物车列表
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向缓存中保存购物车列表");
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }
    
    /**
     * 合并购物车
     *
     * @param cartList_cookie cookie中的购物车列表
     * @param cartList_redis  redis中的购物车列表
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList_cookie, List<Cart> cartList_redis) {
        //遍历cookie购物车列表
        for (Cart cart : cartList_cookie) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartList_redis=this.addGoodsToCartList(cartList_redis, orderItem.getItemId(), orderItem.getNum());
            }
        }
            return cartList_redis;
    }
    
    
    //判断商家是否存在于购物车列表
    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){
        for(Cart cart:cartList){
            if(sellerId.equals(cart.getSellerId())){
                return cart;
            }
        }
        return null;
    }
    //构建订单详情信息
    private TbOrderItem createOrderItem(TbItem item,Integer num){
        if(num<1){
            throw new RuntimeException("购买的商品数目不合法");
        }
        TbOrderItem orderItem=new TbOrderItem();
        orderItem.setItemId(item.getId());//sku的 ID
        orderItem.setGoodsId(item.getGoodsId()); //SPU的id
        orderItem.setTitle(item.getTitle());  //商品名称
        orderItem.setPrice(item.getPrice());   //商品单价
        orderItem.setNum(num);     //购买数量
        orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*num));//总金额
        orderItem.setPicPath(item.getImage());//商品图片
        orderItem.setSellerId(item.getSellerId());
        return orderItem;
    }
    //根据SKU的ID判断订单详情是否存在
    private  TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
        for(TbOrderItem orderItem:orderItemList){
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return orderItem;
            }
        }
        return null;
    }
}
