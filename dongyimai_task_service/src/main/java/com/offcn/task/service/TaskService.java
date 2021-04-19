package com.offcn.task.service;

import com.offcn.mapper.TbSeckillGoodsMapper;
import com.offcn.pojo.TbSeckillGoods;
import com.offcn.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
@Component
public class TaskService {
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 每30秒查询数据库
     * 将复合条件的增量同步到数据库
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void refreshSeckillGoods() {
        System.out.println("增量更新秒杀商品任务");
        Set set= redisTemplate.boundHashOps("seckillGoods").keys();
        TbSeckillGoodsExample tbSeckillGoodsExample = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = tbSeckillGoodsExample.createCriteria();
        criteria.andStatusEqualTo("1");        //状态时审核通过的
        criteria.andStockCountGreaterThan(0);  //库存大于0
        criteria.andStartTimeLessThan(new Date());   //在秒杀开始之后
        criteria.andEndTimeGreaterThan(new Date());     //秒杀结束之前
        seckillGoodsMapper.selectByExample(tbSeckillGoodsExample);
        //将存在的id过滤掉
        if (!CollectionUtils.isEmpty(set)) {
            criteria.andIdNotIn(new ArrayList<Long>(set));
        }
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(tbSeckillGoodsExample);
        if (!CollectionUtils.isEmpty(seckillGoodsList)) {
            for (TbSeckillGoods seckillGoods : seckillGoodsList) {
                System.out.println("向缓存中保存秒杀商品"+seckillGoods.getId());
                redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
            }
        }
    }
    
    /**
     * 移除过期秒杀商品
     */
    @Scheduled(cron = "* * * * * ?")
    public void removeSeckillGoods() {
        System.out.println("移除过期秒杀商品开始" );
        List<TbSeckillGoods> list = redisTemplate.boundHashOps("seckillGoods").values();
        if (!CollectionUtils.isEmpty(list)) {
            for (TbSeckillGoods seckillGoods : list) {
                if ( seckillGoods.getEndTime().getTime() < (new Date()).getTime()) {
                    redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());
                    System.out.println("移除过期秒杀商品结束" + seckillGoods.getId());
                }
                
            }
        }
    }
}
