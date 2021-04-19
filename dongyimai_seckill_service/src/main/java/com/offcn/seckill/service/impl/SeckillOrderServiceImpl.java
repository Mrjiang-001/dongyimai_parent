package com.offcn.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbSeckillGoodsMapper;
import com.offcn.mapper.TbSeckillOrderMapper;
import com.offcn.pojo.TbSeckillGoods;
import com.offcn.pojo.TbSeckillOrder;
import com.offcn.pojo.TbSeckillOrderExample;
import com.offcn.pojo.TbSeckillOrderExample.Criteria;
import com.offcn.seckill.service.SeckillOrderService;
import com.offcn.seckill.util.RedisLock;
import com.offcn.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {
    
    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    @Autowired
    private IdWorker idWorker;
    
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    
    @Autowired
    private RedisLock redisLock;
    
    /**
     * 查询全部
     */
    @Override
    public List<TbSeckillOrder> findAll() {
        return seckillOrderMapper.selectByExample(null);
    }
    
    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }
    
    /**
     * 增加
     */
    @Override
    public void add(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.insert(seckillOrder);
    }
    
    
    /**
     * 修改
     */
    @Override
    public void update(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }
    
    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillOrder findOne(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }
    
    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            seckillOrderMapper.deleteByPrimaryKey(id);
        }
    }
    
    
    @Override
    public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        TbSeckillOrderExample example = new TbSeckillOrderExample();
        Criteria criteria = example.createCriteria();
        
        if (seckillOrder != null) {
            if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + seckillOrder.getUserId() + "%");
            }
            if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + seckillOrder.getSellerId() + "%");
            }
            if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
                criteria.andStatusLike("%" + seckillOrder.getStatus() + "%");
            }
            if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
                criteria.andReceiverAddressLike("%" + seckillOrder.getReceiverAddress() + "%");
            }
            if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + seckillOrder.getReceiverMobile() + "%");
            }
            if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + seckillOrder.getReceiver() + "%");
            }
            if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
                criteria.andTransactionIdLike("%" + seckillOrder.getTransactionId() + "%");
            }
        }
        
        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }
    
    /**
     * 提交订单
     *
     * @param itemId
     * @param userId
     */
    @Override
    public void submitOrder(Long itemId, String userId) {
        String lockName = "createOrderLock";
        long ex = 1 * 1000L;
        String value = String.valueOf(System.currentTimeMillis() + ex);
        boolean lock = redisLock.lock(lockName, value);
        //如果加锁成功,则返回true
        if (lock) {
            //1.根据itemId在缓存中获取商品详情
            TbSeckillGoods tbSeckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(itemId);
            if (tbSeckillGoods == null) {
                return;
                //0408 throw new RuntimeException("改秒杀商品不存在");
            }
            //判断库存是否为0
            if (tbSeckillGoods.getStockCount() == 0) {
                //0408 throw new RuntimeException("该商品已被抢空");
                System.out.println("该商品已被抢空");
                return;
            }
            //2.库存做减一处,并更新到缓存中
            tbSeckillGoods.setStockCount(tbSeckillGoods.getStockCount() - 1);
            redisTemplate.boundHashOps("seckillGoods").put(itemId, tbSeckillGoods);
            //3.设置秒杀订单的属性,并保存订单到缓存中
            TbSeckillOrder seckillOrder = new TbSeckillOrder();
            seckillOrder.setId(idWorker.nextId());                          //秒杀订单编号
            seckillOrder.setSeckillId(itemId);                              //秒杀商品id
            seckillOrder.setMoney(tbSeckillGoods.getCostPrice());           //秒杀金额
            seckillOrder.setUserId(userId);                                //当前登录人
            seckillOrder.setSellerId(tbSeckillGoods.getSellerId());        //商家id
            seckillOrder.setCreateTime(new Date());                        //创建时间
            seckillOrder.setStatus("0");                                //未支付
            redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
            //4.判断秒杀商品的库存,如果为0,则情况缓存中的商品,并同步回到数据库中
            if (tbSeckillGoods.getStockCount() == 0) {
                redisTemplate.boundHashOps("seckillGoods").delete(itemId);
                //更新数据库
                seckillGoodsMapper.updateByPrimaryKey(tbSeckillGoods);
            }
            //解锁
            redisLock.unlock(lockName, value);
        }
    }
    
    /**
     * 从缓存中查询订单对象
     *
     * @param userId
     * @return
     */
    @Override
    public TbSeckillOrder searchOrderFromRedis(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }
    
    /**
     * 保存订单到数据库
     *
     * @param userId        当前登录人id
     * @param orderId       订单编号
     * @param transactionId 支付宝平台返回的交易流水号
     */
    @Override
    public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
        //1.从缓存中查询订单对象
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder == null) {
            throw new RuntimeException("该订单不存在");
        }
        //2.判断订单编号是否和缓存中的订单编号一致
        if (orderId.longValue() == seckillOrder.getId().longValue()) {
            //3.设置订单属性
            seckillOrder.setStatus("1");//已支付
            seckillOrder.setPayTime(new Date());//支付时间
            seckillOrder.setTransactionId(transactionId);
            
            //4.保存订单到数据库
            seckillOrderMapper.insert(seckillOrder);
            //5.清空缓存中的订单信息
            redisTemplate.boundHashOps("seckillOrder").delete(userId);
        }
    }
    
    /**
     * 订单超时删除订单
     *
     * @param userId
     * @param orderId
     */
    @Override
    public void deleteOrderFromRedis(String userId, Long orderId) {
        //1.根据userId从缓存中查询订单
        TbSeckillOrder tbseckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        //2.根据订单编号比较是否一致
        if (null != tbseckillOrder && tbseckillOrder.getId().longValue() == orderId.longValue()) {
            
            //3.从缓存中删除订单信息
            redisTemplate.boundHashOps("seckillOrder").delete(userId);
        }
        //4.查询秒杀商品信息
        TbSeckillGoods tbseckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(tbseckillOrder.getSeckillId());
        if (null == tbseckillGoods) {
            throw new RuntimeException("该秒杀商品不存在");
        }
        //5.对库存加一
        tbseckillGoods.setStockCount(tbseckillGoods.getStockCount() + 1);
        //6.重新将秒杀商品放回到缓存中
        redisTemplate.boundHashOps("seckillGoods").put(tbseckillGoods.getId(), tbseckillGoods);
        
    }
    
}
