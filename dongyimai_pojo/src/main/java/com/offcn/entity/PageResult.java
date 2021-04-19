package com.offcn.entity;

import java.io.Serializable;
import java.util.List;

/**
 * @Auther: lhq
 * @Date: 2021/3/10 10:12
 * @Description:  分页结果的封装类
 */
public class PageResult implements Serializable {

    private long total;  //总记录数
    private List rows;   //分页查询后的集合

    //注意：如果封装类中有 有参的构造方法，则一定要定义一个无参的构造方法
    public PageResult(){

    }

    public PageResult(long total, List rows) {
        this.total = total;
        this.rows = rows;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }
}
