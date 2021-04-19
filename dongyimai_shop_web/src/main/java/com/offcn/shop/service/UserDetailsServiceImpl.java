package com.offcn.shop.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.pojo.TbSeller;
import com.offcn.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: lhq
 * @Date: 2021/3/12 15:11
 * @Description: 安全认证框架的自定义认证类
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    @Reference
    private SellerService sellerService;


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //1.创建一个权限集合
        List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
        //2.添加自定义权限
        list.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        //根据登录账号查询商家信息
        TbSeller tbSeller = sellerService.findOne(username);
        if (null != tbSeller) {
            //判断商家的审核状态是 审核通过
            if (tbSeller.getStatus().equals("1")) {
                //3.完成用户名和密码的匹配
                return new User(username, tbSeller.getPassword(), list);
            } else {
                return null;
            }

        } else {
            return null;
        }

    }
}
