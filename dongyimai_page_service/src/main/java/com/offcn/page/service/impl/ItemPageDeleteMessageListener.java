package com.offcn.page.service.impl;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;
@Component
public class ItemPageDeleteMessageListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            try {
                Long[] ids = (Long[]) objectMessage.getObject();
                itemPageService.deleteItemPage(ids);
                System.out.println("消息队列接收消息成功,删除静态页面成功");
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
