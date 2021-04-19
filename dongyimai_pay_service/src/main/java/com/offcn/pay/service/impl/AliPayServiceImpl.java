package com.offcn.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.offcn.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class AliPayServiceImpl implements AliPayService {
    
    @Autowired
    private AlipayClient alipayClient;
    /**
     * 生成支付宝二维码
     *
     * @param out_trade_no 订单编号  不允许重复
     * @param total_fee    支付金额,单位为分
     * @return
     */
    @Override
    public Map<String, Object> createNative(String out_trade_no, String total_fee) {
        Map resultMap = new HashMap();
        //单位转换  分转元
        long total_fee_long = Long.parseLong(total_fee);
        BigDecimal total_fee_big = new BigDecimal(total_fee_long);
        BigDecimal cs = new BigDecimal(100L);
        BigDecimal total_amount = total_fee_big.divide(cs);
        //创建预下单请求对象
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"total_amount\":\""+total_amount.doubleValue()+"\"," +
                "    \"subject\":\"测试购买商品001\"," +
                "    \"store_id\":\"xa_001\"," +
                "    \"timeout_express\":\"90m\"}");//设置业务参数
        //发出预下单业务请求
        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            System.out.println(response.getBody());
            //根据response中的结果继续业务逻辑处理
            String code = response.getCode();    //返回的响应状态码
            if (code.equals("10000")) {
                resultMap.put("outTradeNo", response.getOutTradeNo());//订单编号
                resultMap.put("totalAmount", total_fee);              //支付金额
                resultMap.put("qrCode", response.getQrCode());        //二维码链接
            } else {
                System.out.println("与调用支付接口失败"+code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }
    
    /**
     * 查询支付状态
     *
     * @param out_trade_no
     * @return
     */
    @Override
    public Map<String, Object> queryPayStatus(String out_trade_no) {
        Map resultMap = new HashMap();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"trade_no\":\"\"}");
        
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            System.out.println(response.getBody());
            String code = response.getCode();
            if (code.equals("10000")) {
                resultMap.put("outTradeNo", response.getOutTradeNo());//订单编号
                resultMap.put("status", response.getTradeStatus());//支付宝平台返回的交易流水号
                resultMap.put("tradeNo", response.getTradeNo());//
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }
}
