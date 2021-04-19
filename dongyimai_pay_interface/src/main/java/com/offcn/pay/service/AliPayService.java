package com.offcn.pay.service;

import java.util.Map;

/**
 * 支付宝支付接口
 */

public interface AliPayService {
    /**
     * 生成支付宝二维码
     *
     * @param out_trade_no 订单编号  不允许重复
     * @param total_fee    支付金额,单位为分
     * @return
     */
    public Map<String, Object> createNative(String out_trade_no, String total_fee);
    
    /**
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    public Map<String, Object> queryPayStatus(String out_trade_no);
}
