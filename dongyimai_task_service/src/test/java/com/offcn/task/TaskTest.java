package com.offcn.task;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
public class TaskTest {
    /**
     * cron 表达式
     * 第一个表示 秒 * 任意时间(0-59)   5,15,25,35 中","表示枚举时间      0/5  表示0秒开始,每五秒执行    /表示步长
     *   "-"  15-35  表示范围
     *
     * 第二个参数表示 分钟
     * 第三个参数表示 小时 *任意时间(0-23) ;  , 表示枚举 ;  / 步长  ;  -  范围  ;
     * 第四个参数表示 天  *任意时间(1-31)  ;  ,  表示枚举时间  ;  -范围  ;  ?  占位符
     * 第五个参数表示 月份 * 任意时间      ;  ,
     */
    @Scheduled(cron ="0/300 0-50 14,16 * * ?" )
    public void printTime() {
        System.out.println(new Date().toLocaleString());
    }
    
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-service.xml");
        TaskTest taskTest =(TaskTest) context.getBean("taskTest");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
