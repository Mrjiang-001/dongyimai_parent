package com.offcn.page.service.impl;


import com.offcn.mapper.TbGoodsDescMapper;
import com.offcn.mapper.TbGoodsMapper;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.mapper.TbItemMapper;
import com.offcn.page.service.ItemPageService;
import com.offcn.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ItemPageServiceImpl implements ItemPageService {
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    
    @Autowired
    private TbGoodsMapper goodsMapper;
    
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    
    @Autowired
    private TbItemMapper itemMapper;
    
    @Autowired
    private TbItemCatMapper itemCatMapper;
    
    @Value("${path}")
    private String path;
    
    /**
     * 生产商品详情页
     *
     * @param goodsId
     * @return
     */
    @Override
    public boolean createItemPage(Long goodsId) {
        try {
            //1.创建Freemarker的配置对象
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            //2.创建模板对象
            Template template = configuration.getTemplate("item.ftl");
            //3.查询SPU对象信息
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
            //4.查询商品扩展信息
            TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            
            //查询三级分类信息
            TbItemCat category1 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id());
            TbItemCat category2 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id());
            TbItemCat category3 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
            
            //5.查询SKU列表信息
            TbItemExample tbItemExample = new TbItemExample();
            TbItemExample.Criteria criteria = tbItemExample.createCriteria();
            //设置查询条件
            criteria.andGoodsIdEqualTo(goodsId);
            criteria.andStatusEqualTo("1");
            tbItemExample.setOrderByClause("is_default desc");//根据默认值倒序排序
            List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);
            //6.构建数据元对象
            
            Map<String, Object> dataSource = new HashMap<String, Object>();
            dataSource.put("goods", tbGoods);
            dataSource.put("goodsDesc", tbGoodsDesc);
            dataSource.put("itemList", itemList);
            dataSource.put("category1", category1);
            dataSource.put("category2", category2);
            dataSource.put("category3", category3);
            
            //7.生成静态页面
            
            FileWriter out = new FileWriter(new File(path + goodsId + ".html"));
            template.process(dataSource, out);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
    }
    
    /**
     * 删除商品详情页
     *
     * @param ids
     */
    @Override
    public void deleteItemPage(Long[] ids) {
        for (Long id : ids) {
            new File(path + id + ".html").delete();
        }
    }
    
    
}
