package com.offcn.utils;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;

    public void importItem() {
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        //设置查询条件 审核状态为通过
        criteria.andStatusEqualTo("1");
        List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);
        for (TbItem item : itemList) {
            Map<String, String> specMap = JSON.parseObject(item.getSpec(),Map.class);
            Map<String ,String > pinyinMap = new HashMap<>();
            for (String key : specMap.keySet()) {
                //拼音转换
               pinyinMap.put(Pinyin.toPinyin(key, "").toLowerCase(),specMap.get(key));
               //添加到动态域中
                item.setSpecMap(pinyinMap);
            }
        }
        //执行批量导入
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("导入成功");
    }
    public static void main(String[] args) {
        //加载容器配置文件
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.importItem();
    }
}
