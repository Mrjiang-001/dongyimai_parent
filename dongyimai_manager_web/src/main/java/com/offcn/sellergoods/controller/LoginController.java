package com.offcn.sellergoods.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: lhq
 * @Date: 2021/3/12 11:07
 * @Description:  登录的控制器
 */
@RestController
public class LoginController {

    @RequestMapping("/getLoginName")
    public Map getLoginName(){
        //从安全验证框架中获得登录的用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap();
        map.put("name",name);
        return map;
    }
}
