package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.mapper.TbTypeTemplateMapper;
import com.offcn.entity.PageResult;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import com.offcn.pojo.TbTypeTemplate;
import com.offcn.pojo.TbTypeTemplateExample;
import com.offcn.pojo.TbTypeTemplateExample.Criteria;
import com.offcn.sellergoods.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TbTypeTemplateMapper typeTemplateMapper;

    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbTypeTemplate> findAll() {
        return typeTemplateMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.insert(typeTemplate);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.updateByPrimaryKey(typeTemplate);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbTypeTemplate findOne(Long id) {
        return typeTemplateMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            typeTemplateMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbTypeTemplateExample example = new TbTypeTemplateExample();
        Criteria criteria = example.createCriteria();

        if (typeTemplate != null) {
            if (typeTemplate.getName() != null && typeTemplate.getName().length() > 0) {
                criteria.andNameLike("%" + typeTemplate.getName() + "%");
            }
            if (typeTemplate.getSpecIds() != null && typeTemplate.getSpecIds().length() > 0) {
                criteria.andSpecIdsLike("%" + typeTemplate.getSpecIds() + "%");
            }
            if (typeTemplate.getBrandIds() != null && typeTemplate.getBrandIds().length() > 0) {
                criteria.andBrandIdsLike("%" + typeTemplate.getBrandIds() + "%");
            }
            if (typeTemplate.getCustomAttributeItems() != null && typeTemplate.getCustomAttributeItems().length() > 0) {
                criteria.andCustomAttributeItemsLike("%" + typeTemplate.getCustomAttributeItems() + "%");
            }
        }

        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(example);
        //放入缓存  20210323
        this.saveToRedis();
        System.out.println("品牌列表和规格列表放入缓存成功");
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据模板ID查询规格列表
     *
     * @param id
     * @return
     */
    public List<Map> findSpecList(Long id) {
        //1.根据ID查询模板对象
        TbTypeTemplate typeTemplate = this.findOne(id);
        //2.规格JSON结构的字符串转换为JSON对象   {"id":27,"text":"网络"}
        List<Map> specList = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
        //3.遍历规格集合
        if (!CollectionUtils.isEmpty(specList)) {
            for (Map map : specList) {
                //4.根据规格ID关联查询规格选项集合
                Long specId = new Long((Integer) map.get("id"));     //Map在存储整型数据  int
                TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
                TbSpecificationOptionExample.Criteria criteria = tbSpecificationOptionExample.createCriteria();
                //根据specId查询
                criteria.andSpecIdEqualTo(specId);
                List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.selectByExample(tbSpecificationOptionExample);
                //5.将规格集合重新放入原有MAP中，重新构建JSON内容   {"id":27,"text":"网络","options":[{},{}]}
                map.put("options", specificationOptionList);
            }
        }
        return specList;
    }

    private void saveToRedis() {
        List<TbTypeTemplate> typeTemplates = this.findAll();
        for (TbTypeTemplate typeTemplate : typeTemplates) {
            //根据模板ID获取品牌列表
            List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(), Map.class);
            //将品牌列表放入缓存  typeId:brandList
            redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(), brandList);
            //根据模板ID获取规格列表
            List<Map> specList = this.findSpecList(typeTemplate.getId());
            //将规格列表放入缓存  typeId:specList
            redisTemplate.boundHashOps("specList").put(typeTemplate.getId(),specList);

        }
    }

}
