package com.offcn.page.service.impl;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class ItemPageMessageListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;
    
    
    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                Long id = Long.parseLong(textMessage.getText());
                itemPageService.createItemPage(id);
                System.out.println("消息队列接受消息成功，生成页面成功");
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
