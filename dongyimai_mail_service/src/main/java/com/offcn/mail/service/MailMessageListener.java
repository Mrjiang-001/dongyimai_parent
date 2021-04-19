package com.offcn.mail.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
@Component
public class MailMessageListener implements MessageListener {
    @Autowired
    private JavaMailSender javaMailSender;
    @Override
    public void onMessage(Message message) {
        if (message instanceof MapMessage) {
            MapMessage mapMessage = (MapMessage) message;
            try {
                String username = mapMessage.getString("username");
                String email = mapMessage.getString("email");
                //创建带有附件的邮箱
                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                //创建一个发送附件的邮件助手
                MimeMessageHelper mimeMessageHelper=new MimeMessageHelper(mimeMessage, true,"GBK");
                //发送邮箱的账号
                mimeMessageHelper.setFrom("muhaoqingfeng@126.com");
                //  结束邮箱的账号
                mimeMessageHelper.setTo(email);
                //主题
                mimeMessageHelper.setSubject("welcome " + username);
                //正文 +加载邮件模板
                String html = IOUtils.toString(new FileInputStream("D:\\dym1123\\home-index.html"), "UTF-8");
                //设定发送邮件内容,支持HTML
                mimeMessageHelper.setText(html, true);
                //发送邮件
                javaMailSender.send(mimeMessage);
                System.out.println("发送邮件成功:"+email);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
