package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 搜索商品
     *
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map searchMap) {
        Map resultMap=new HashMap();
       /* //1.创建查询对象
        Query query = new SimpleQuery();
        //2.创建查询条件选择器,并设置查询条件   is根据分词分析器分词查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //3.执行分页查询
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);*/
        //4获得查询结果
        //resultMap.put("rows", page.getContent());
        
        //将关键字中的空格处理掉   20210324  14:10
        String keywords = (String) searchMap.get("keywords");
        if (StringUtils.isNotEmpty(keywords) && keywords.indexOf(" ") > -1) {
            keywords = keywords.replace(" ", "");
            searchMap.put("keywords", keywords);
        }
        
        resultMap.putAll(this.searchList(searchMap));
        List<String> categoryList = this.findCategoryList(searchMap);
        resultMap.put("categoryList", categoryList);

        // 取得分类的查询条件
        String category = (String) searchMap.get("category");
        //点击分类是,根据所选分类进行查询,否则默认根据分类列表的第一个值进行查询
        if (StringUtils.isNotEmpty(category)) {
            resultMap.putAll(this.findBrandAndSpecList(category));
        }else {
            if (!CollectionUtils.isEmpty(categoryList)) {
                resultMap.putAll(this.findBrandAndSpecList(categoryList.get(0)));
            }
        }
        return resultMap;
    }
    
    /**
     * 导入SKU数据
     *
     * @param itemList
     */
    @Override
    public void importItem(List<TbItem> itemList) {
        
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
    
    /**
     * 删除SKU数据
     *
     * @param goodsIds
     */
    @Override
    public void deleteByGoodsIds(List<Long> goodsIds) {
        //创建查询对象
        Query query = new SimpleQuery();
        //创建查询容器
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);
        //执行删除
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
    
    /**
     * 高亮查询
     * @param searchMap
     * @return
     */
    private Map<String, Object> searchList(Map searchMap) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        //1.1创建高亮查询对象
        HighlightQuery highlightQuery = new SimpleHighlightQuery();
        //1.2.设置高亮字段
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        //1.3.设置高亮查询属性
        highlightOptions.setSimplePrefix("<em style='color: red;'>");//前缀
        highlightOptions.setSimplePostfix("</em>");//后缀
        highlightQuery.setHighlightOptions(highlightOptions);
        //1.4.设置查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        highlightQuery.addCriteria(criteria);

        /*添加筛选条件*/
        //2.筛选分类条件
        if (StringUtils.isNotEmpty((String)searchMap.get("category"))) {
            //过滤查询
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria categoryCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(categoryCriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }
        //3.筛选品牌条件
        if (StringUtils.isNotEmpty((String) searchMap.get("brand"))) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria brandCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(brandCriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }
        //4.筛选规格条件
        if (null != searchMap.get("spec")) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Map<String, String> specMap= (Map<String, String>)searchMap.get("spec");
            //将规格名称转换为拼音
            //获取key值
            for (String key : specMap.keySet()) {
                Criteria specCriteria = new Criteria("item_spec_" + Pinyin.toPinyin(key, "").toLowerCase()).is(specMap.get(key));
                filterQuery.addCriteria(specCriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
        }
        //5.根据价格筛选
        if (StringUtils.isNotEmpty((String) searchMap.get("price"))) {
            //拆分字符串 500-1000   str[0]=500  str[1]=1000
            String[] str = ((String) searchMap.get("price")).split("-");
            if (!str[0].equals("0")) {
                //创建过滤查询对象
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria priceCriteria = new Criteria("item_price").greaterThan(str[0]);
                filterQuery.addCriteria(priceCriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
            if (!str[1].equals("*")) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria priceCriteria = new Criteria("item_price").lessThan(str[1]);
                filterQuery.addCriteria(priceCriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
          
        }
        
        //6.排序
        String sortValue =(String) searchMap.get("sort");// 排序规则  升序  降序
        String sortFiled = (String) searchMap.get("sortField");  //排序字段
        if (StringUtils.isNotEmpty(sortFiled)) {
            //升序
            if (sortValue.equals("ASC")) {
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortFiled);
                highlightQuery.addSort(sort);
            }
            //降序
            if (sortValue.equals("DESC")) {
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortFiled);
                highlightQuery.addSort(sort);
            }
        }
        /**
         * 20210324 10:15
         */
        //分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");//当前页码
        if (pageNo == null) {
            pageNo = 1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");//每页显示记录数
        if (pageSize == null) {
            pageSize = 10;
        }
        highlightQuery.setOffset((pageNo-1)*pageSize);//分页查询的起始位置
        highlightQuery.setRows(pageSize);  //分页查询记录数
        
        
        
        //1.5.执行高亮查询
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(highlightQuery, TbItem.class);
        //1.6.获得高亮查询的入口结果集
        List<HighlightEntry<TbItem>> highlightEntryList = highlightPage.getHighlighted();
        //1.7.将高亮查询结果设置会SKU实体中
        for (HighlightEntry<TbItem> highlightEntry : highlightEntryList) {
            TbItem tbItem = highlightEntry.getEntity();
            //下遍历带高亮集合之前,需要判空
            if (highlightEntry.getHighlights().size()>0&&highlightEntry.getHighlights().get(0).getSnipplets().size()>0) {
                List<HighlightEntry.Highlight> highlightList = highlightEntry.getHighlights();
                List<String> snipplets = highlightList.get(0).getSnipplets();
                tbItem.setTitle(snipplets.get(0));
            }
        }

        resultMap.put("rows", highlightPage.getContent()); //分页查询的结果集
        resultMap.put("total", highlightPage.getTotalElements());//总记录数
        resultMap.put("totalPages",highlightPage.getTotalPages()); //总页数
        //1.8.返回结果
        return resultMap;
    }

    /**
     * 分组查询分类列表
     * @param searchMap
     * @return
     */
    private List<String> findCategoryList(Map searchMap) {
        List<String> categoryList = new ArrayList<String>();
        //1.创建查询对象
        Query query = new SimpleQuery();
        //2.设置查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //3.设置分组条件
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //4.执行分组查询
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        //5.获得分组结果集入口
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
        //6.设置分组结果到cateList中
        List<GroupEntry<TbItem>> group = groupResult.getGroupEntries().getContent();
        for (GroupEntry<TbItem> entry : group) {
            categoryList.add(entry.getGroupValue());
        }
        return categoryList;
    }

    //从缓存中查询品牌和规格列表
    private Map<String, Object> findBrandAndSpecList(String category) {
        Map<String, Object> resultMap=new HashMap<String, Object>();
        //1.根据分类名称在缓存中查询模板ID
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (typeId != null) {
            //2.根据模板ID在缓存中查询品牌列表
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
            //3.根据模板ID在缓存中查询规格列表
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
            resultMap.put("brandList", brandList);
            resultMap.put("specList", specList);
        }
        return resultMap;
    }
}
