package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.order.service.OrderService;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbPayLog;
import com.offcn.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private IdWorker idWorker;
    @Reference
    private OrderService orderService;
    
    @RequestMapping("/createNative")
    public Map createNative() {
        //获取登录的userID
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog tbPayLog = orderService.searchPayLogFromRedis(userId);
        if (null!=tbPayLog) {
            //String outTradeNo = idWorker.nextId()+"";
            return aliPayService.createNative(tbPayLog.getOutTradeNo(),tbPayLog.getTotalFee()+"");
        }
        return new HashMap();
        

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
                    orderService.updateStatus(outTradeNo,(String)map.get("tradeNo"));
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
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
