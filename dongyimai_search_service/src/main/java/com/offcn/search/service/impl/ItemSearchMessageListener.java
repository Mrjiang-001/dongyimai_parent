package com.offcn.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.search.service.ItemSearchService;
import com.offcn.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class ItemSearchMessageListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
       
        if (message instanceof TextMessage) {
            //1.消息类型转换
            TextMessage textMessage = (TextMessage) message;
            try {
                String jsonStr = textMessage.getText();
                List<TbItem> itemList = JSON.parseArray(jsonStr, TbItem.class);
                //2.消息导入solr的方法
                itemSearchService.importItem(itemList);
                System.out.println("消息队列接收信息成功,导入solr");
            } catch (JMSException e) {
                e.printStackTrace();
            }
            
        }
        
    
    }
}
