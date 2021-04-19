package com.offcn;

import com.offcn.utils.SmsUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

@Component
public class SmsMessageListener implements MessageListener {
    @Autowired
    private SmsUtils smsUtils;
    @Override
    public void onMessage(Message message) {
        if (message instanceof MapMessage) {
            MapMessage mapMessage = (MapMessage) message;
            try {
                String mobile = mapMessage.getString("mobile");
                String param = mapMessage.getString("param");
    
                HttpResponse response = smsUtils.sendSms(mobile, param);
                System.out.println(EntityUtils.toString(response.getEntity()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
