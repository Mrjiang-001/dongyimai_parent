package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.group.Goods;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbBrandMapper brandMapper;

    @Autowired
    private TbSellerMapper sellerMapper;

    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        //1.设置商品的审核状态 未审核
        goods.getGoods().setAuditStatus("0");   //未审核
        //2.添加SPU商品信息
        goodsMapper.insert(goods.getGoods());
        //3.获得添加之后的SPU的ID
        //4.添加商品扩展信息
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        goodsDescMapper.insert(goods.getGoodsDesc());
        //5.添加SKU信息
        this.saveItem(goods);


    }


    private void setItemValue(Goods goods, TbItem item) {
        item.setCreateTime(new Date());                                     //创建时间
        item.setUpdateTime(new Date());                                     //更新时间
        item.setCategoryid(goods.getGoods().getCategory3Id());              //分类ID 3级分类
        item.setGoodsId(goods.getGoods().getId());                           //SPU的ID
        item.setSellerId(goods.getGoods().getSellerId());                   //商家ID
        //根据分类ID查询分类信息
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(item.getCategoryid());
        item.setCategory(itemCat.getName());                                //分类名称
        //根据品牌ID查询品牌信息
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());                                     //品牌名称
        //根据商家ID查询商家信息
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        item.setSeller(seller.getNickName());                                //店铺名称

        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (!CollectionUtils.isEmpty(imageList)) {
            String url = (String) imageList.get(0).get("url");
            item.setImage(url);                                                  //图片路径
        }
    }


    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        //1.重置商品的审核状态 未审核
        goods.getGoods().setAuditStatus("0");   //未审核
        //2.修改SPU的对象信息
        goodsMapper.updateByPrimaryKey(goods.getGoods());
        //3.修改商品扩展信息
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
        //4.根据商品ID删除SKU列表
        TbItemExample itemExample = new TbItemExample();
        TbItemExample.Criteria criteria = itemExample.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getGoods().getId());
        //执行删除操作
        itemMapper.deleteByExample(itemExample);
        //5.重新将SKU添加
        this.saveItem(goods);
    }


    private void saveItem(Goods goods){
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            if (!CollectionUtils.isEmpty(goods.getItemList())) {
                for (TbItem item : goods.getItemList()) {
                    //拼接SKU名称  SPU名称+规格选项
                    String title = goods.getGoods().getGoodsName();
                    Map<String, String> specMap = JSON.parseObject(item.getSpec(), Map.class);
                    for (String key : specMap.keySet()) {
                        title += specMap.get(key) + " ";
                    }
                    item.setTitle(title);                                               //SKU名称
                    this.setItemValue(goods, item);
                    itemMapper.insert(item);
                }
            }
        } else {
            //设置SKU数据为默认数据
            TbItem item = new TbItem();
            item.setTitle(goods.getGoods().getGoodsName());     //SPU标题
            item.setPrice(goods.getGoods().getPrice());     //默认使用SPU价格
            item.setNum(9999);                              //库存
            item.setStatus("1");                            //默认状态为启用
            item.setIsDefault("1");                         //默认值
            item.setSpec("{}");
            this.setItemValue(goods, item);
            itemMapper.insert(item);

        }
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        //1.根据ID查询SPU对象信息
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        //2.根据ID查询商品扩展信息
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        //3.根据ID作为查询条件查询SKU列表
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);
        //4.设置复合实体类
        Goods goods = new Goods();
        goods.setGoods(tbGoods);
        goods.setGoodsDesc(tbGoodsDesc);
        goods.setItemList(itemList);
        return goods;
    }

    /**
     * 批量删除(逻辑删除)
     *
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
            for (Long id : ids) {
                //逻辑删除
                TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
                tbGoods.setIsDelete("1");//删除
                //执行操作
                goodsMapper.updateByPrimaryKey(tbGoods);
                
                //根据SPU的ID查询出SKU集合
                List<TbItem> itemList = this.findItemListByGoodsIdsAndStatus(ids, "1");
                
                //设置SKU的状态为0
                if (!CollectionUtils.isEmpty(itemList)) {
                    for (TbItem tbItem : itemList) {
                        tbItem.setStatus("0");
                        //执行修改SKU对象
                        itemMapper.updateByPrimaryKey(tbItem);
                    }
                }
                //执行修改SKU对象
        }
    }

    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                //criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            /*if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }*/
            criteria.andIsDeleteIsNull();
        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 审核商品
     *
     * @param ids
     * @param auditStatus
     */
    @Override
    public void updateAuditStatus(Long[] ids, String auditStatus) {
        for (Long id : ids) {
            //查询商品信息
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            //设置审核状态
            tbGoods.setAuditStatus(auditStatus);
            //执行修改操作
            goodsMapper.updateByPrimaryKey(tbGoods);
            //修改SKU列表的状态
            TbItemExample tbItemExample = new TbItemExample();
            TbItemExample.Criteria criteria = tbItemExample.createCriteria();
            criteria.andGoodsIdEqualTo(id);
            List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);
            for (TbItem item : itemList) {
                item.setStatus(auditStatus);
                itemMapper.updateByPrimaryKey(item);
            }
        }
    }

    /**
     * 上架商品
     *
     * @param ids
     */
    @Override
    public Result upGoods(Long[] ids) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            String auditStatus = tbGoods.getAuditStatus();
            //判断商品是否审核通过
            if ("1".equals(auditStatus)) {
                //审核通过才能上架
                try {
                    tbGoods.setIsMarketable("1");
                    goodsMapper.updateByPrimaryKey(tbGoods);
                    return new Result(true, "上架成功");
                } catch (Exception e) {
                    e.printStackTrace();
                    return new Result(true, "上架失败");
                }
            }
        }
        return new Result(true, "上架失败");
    }

    @Override
    public Result downGoods(Long[] ids) {
        for (Long id : ids) {
            try {
                TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
                tbGoods.setIsMarketable("0");
                goodsMapper.updateByPrimaryKey(tbGoods);
                return new Result(true, "下架成功");
            } catch (Exception e) {
                e.printStackTrace();
                return new Result(true, "下架失败");
            }
        }
        return new Result(true, "下架失败");
    }
    
    /**
     * 通过SPU的ID和审核状态查询SKU列表
     *
     * @param ids
     * @param status
     * @return
     */
    @Override
    public List<TbItem> findItemListByGoodsIdsAndStatus(Long[] ids, String status) {
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        //设置goodsID
        criteria.andGoodsIdIn(Arrays.asList(ids));
        //设置审核状态
        criteria.andStatusEqualTo(status);
        return itemMapper.selectByExample(tbItemExample);
    }
    
    
}
