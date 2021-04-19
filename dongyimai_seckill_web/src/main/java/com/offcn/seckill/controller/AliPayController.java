package com.offcn.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbSeckillOrder;
import com.offcn.seckill.service.SeckillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/aliPay")
public class AliPayController {
    @Reference
    private AliPayService aliPayService;
    @Reference
    private SeckillOrderService seckillOrderService;
    
    @RequestMapping("/createNative")
    public Map createNative() {
        //获取登录的userID
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //根据当前登录人从缓存中查询订单
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedis(userId);
        if (seckillOrder != null) {
            long money = (long) (seckillOrder.getMoney().doubleValue() * 100);
            return aliPayService.createNative(seckillOrder.getId() + "", money+ "");
        } else {
            return new HashMap();
        }
    }
    
    /**
     * 查询检查结果
     * @param outTradeNo
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String outTradeNo) {
        int i = 0;
        Result result = null;
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        while (true) {
            Map map = null;
            try {
                map = aliPayService.queryPayStatus(outTradeNo);
                //什么也没有查到
                if (map == null) {
                    System.out.println("查询失败");
                }
                //交易成功
                if (map.get("status") != null && map.get("status").equals("TRADE_SUCCESS")) {
                    //orderService.updateStatus(outTradeNo,(String)map.get("tradeNo"));
                    seckillOrderService.saveOrderFromRedisToDb(userId,Long.parseLong(outTradeNo),(String) map.get(("tradeNo")));
                    result = new Result(true, "支付成功");
                    break;
                }
                //交易关闭
                if (map.get("status") != null && map.get("status").equals("TRADE_CLOSED")) {
                    result = new Result(true, "未付款交易超时关闭,或支付完成后全额退款");
                    break;
                }
                //交易结束
                if (map.get("status") != null && map.get("status").equals("TRADE_FINISHED")) {
                    result = new Result(true, "交易结束,不能退款");
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                result = new Result(false, "查询失败");
                break;
            }
            try {
                Thread.sleep(3000);//每三秒轮询一次
                i++;
                if (i>=10) {
                    result= new Result(false, "二维码超时");
                    
                    //TODO  关闭交易接口,取消支付宝预下单方法 (学员实习)
    
                    seckillOrderService.deleteOrderFromRedis(userId, Long.parseLong(outTradeNo));
                    
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
