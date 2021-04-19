package com.offcn.sellergoods.service;

import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.group.Goods;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbItem;

import java.util.List;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	//public void add(TbGoods goods);
	public void add(Goods goods);
	
	
	/**
	 * 修改
	 */
	//public void update(TbGoods goods);
	public void update(Goods goods);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	//public TbGoods findOne(Long id);
	public Goods findOne(Long id);
	
	
	/**
	 * 批量删除(逻辑删除)
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize);

	/**
	 * 审核商品
	 * @param ids
	 * @param auditStatus
	 */
	public void updateAuditStatus(Long[] ids, String auditStatus);

	/**
	 * 上架商品
	 * @param ids
	 */
	public Result upGoods(Long[] ids);

	public Result downGoods(Long[] ids);
	
	/**
	 * 通过SPU的ID和审核状态查询SKU列表
	 *
	 * @param ids
	 * @param status
	 * @return
	 */
	public List<TbItem> findItemListByGoodsIdsAndStatus(Long[] ids, String status);
}
