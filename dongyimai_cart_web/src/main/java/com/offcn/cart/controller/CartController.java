package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Result;
import com.offcn.group.Cart;
import com.offcn.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;
    
    /**
     * 查询购物车方法
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request, HttpServletResponse response) {
        //获取登录用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String cartListStr = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (StringUtils.isEmpty(cartListStr)) {
            cartListStr = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListStr, Cart.class);
        // 未登录在cookie中查询
        if (username.equals("anonymousUser")) {
            return cartList_cookie;
        } else {//登录之后在Redis 中查询
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            //1.如果cookie中有购物车列表 ,则合并购物车
            if (CollectionUtils.isEmpty(cartList_cookie)) {
                //2.合并购物车
                cartList_redis = cartService.mergeCartList(cartList_cookie,cartList_redis);
                //3.更新缓存中的购物车列表
                cartService.saveCartListToRedis(username,cartList_redis);
                //4.清空cookie
                CookieUtil.deleteCookie(request,response,"cartList");
            }
            return cartList_redis;
        }
    }
    
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, Integer num,HttpServletRequest request, HttpServletResponse response) {
        try {
           /* //配置跨域请求的就解决方案 CORS
            response.setHeader("Access-Control-Allow-Origin","http://localhost:9105");
            //允许携带参数
            response.setHeader("Access-Control-Allow-Credentials","true");
            */
            //1.取得购物车列表
            List<Cart> cartList = this.findCartList(request, response);
            //2.完成添加购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            //获取当前用户名
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            //未登录,则保存到cookie中
            if (username.equals("anonymousUser")) {
                //3.重新存入cookie中
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 60 * 60 * 24, "UTF-8");
            } else {//登录保存到Redis中
                cartService.saveCartListToRedis(username, cartList);
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }
    
    
}
