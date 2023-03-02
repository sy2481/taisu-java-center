package com.ruoyi.base.service;

import com.ruoyi.base.domain.HcUser;
import com.ruoyi.base.domain.ManFactory;

import java.util.List;
import java.util.Map;

/**
 * 危化人員Service接口
 *
 * @author ruoyi
 * @date 2023-02-02
 */
public interface IHcUserService
{
    /**
     * 查询危化人員
     *
     * @param id 危化人員主键
     * @return 危化人員
     */
    public HcUser selectHcUserById(Long id);

    /**
     * 查询危化人員列表
     *
     * @param hcUser 危化人員
     * @return 危化人員集合
     */
    public List<HcUser> selectHcUserList(HcUser hcUser);

    /**
     * 新增危化人員
     *
     * @param hcUser 危化人員
     * @return 结果
     */
    public int insertHcUser(HcUser hcUser);

    /**
     * 修改危化人員
     *
     * @param hcUser 危化人員
     * @return 结果
     */
    public int updateHcUser(HcUser hcUser);

    /**
     * 批量删除危化人員
     *
     * @param ids 需要删除的危化人員主键集合
     * @return 结果
     */
    public int deleteHcUserByIds(Long[] ids);

    /**
     * 删除危化人員信息
     *
     * @param id 危化人員主键
     * @return 结果
     */
    public int deleteHcUserById(Long id);

    /**
     * 獲取實體map
     *
     * @return
     */
    public Map<String, HcUser> getEntityMap(List<String> idNoList);

    /**
     * 保存中心库人脸
     *
     * @param hcUser
     */
    void saveFaceForCenter(HcUser hcUser);
}
