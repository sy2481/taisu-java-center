package com.ruoyi.base.service.impl;

import com.ruoyi.base.domain.HcUser;
import com.ruoyi.base.domain.ManFactory;
import com.ruoyi.base.mapper.HcUserMapper;
import com.ruoyi.base.service.IHcUserService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 危化人員Service业务层处理
 *
 * @author ruoyi
 * @date 2023-02-02
 */
@Service
public class HcUserServiceImpl implements IHcUserService
{
    @Autowired
    private HcUserMapper hcUserMapper;

    /**
     * 查询危化人員
     *
     * @param id 危化人員主键
     * @return 危化人員
     */
    @Override
    public HcUser selectHcUserById(Long id)
    {
        return hcUserMapper.selectHcUserById(id);
    }

    /**
     * 查询危化人員列表
     *
     * @param hcUser 危化人員
     * @return 危化人員
     */
    @Override
    public List<HcUser> selectHcUserList(HcUser hcUser)
    {
        return hcUserMapper.selectHcUserList(hcUser);
    }

    /**
     * 新增危化人員
     *
     * @param hcUser 危化人員
     * @return 结果
     */
    @Override
    public int insertHcUser(HcUser hcUser)
    {
        hcUser.setCreateTime(DateUtils.getNowDate());
        return hcUserMapper.insertHcUser(hcUser);
    }

    /**
     * 修改危化人員
     *
     * @param hcUser 危化人員
     * @return 结果
     */
    @Override
    public int updateHcUser(HcUser hcUser)
    {
        hcUser.setUpdateTime(DateUtils.getNowDate());
        return hcUserMapper.updateHcUser(hcUser);
    }

    /**
     * 批量删除危化人員
     *
     * @param ids 需要删除的危化人員主键
     * @return 结果
     */
    @Override
    public int deleteHcUserByIds(Long[] ids)
    {
        return hcUserMapper.deleteHcUserByIds(ids);
    }

    /**
     * 删除危化人員信息
     *
     * @param id 危化人員主键
     * @return 结果
     */
    @Override
    public int deleteHcUserById(Long id)
    {
        return hcUserMapper.deleteHcUserById(id);
    }

    /**
     * 獲取實體map
     *
     * @return
     */
    @Override
    public Map<String, HcUser> getEntityMap(List<String> idNoList) {
        Map<String, HcUser> result = new HashMap<String, HcUser>();
        List<HcUser> list = new ArrayList<HcUser>();
        if (idNoList == null) {
            list = hcUserMapper.selectHcUserList(new HcUser());
        } else {
            if (idNoList.size() > 0) {
                list = hcUserMapper.selectHcUserListByIdNos(idNoList.toArray(new String[0]));
            }
        }

        for (HcUser item : list) {
            result.put(item.getIdNo(), item);
        }
        return result;
    }

    /**
     * 保存中心库人脸
     *
     * @param hcUser
     */
    public void saveFaceForCenter(HcUser hcUser) {
        //通过身份证查找人员
        HcUser hcUserCenter = hcUserMapper.selectHcUserByIdNo(hcUser.getIdNo());
        if (hcUserCenter == null) {//无数据则新增
            hcUserCenter = new HcUser();
            BeanUtils.copyProperties(hcUser, hcUserCenter);
            hcUserCenter.setId(null);
            hcUserCenter.setCreateBy("system");
            hcUserCenter.setCreateTime(DateUtils.getNowDate());
            hcUserMapper.insertHcUser(hcUserCenter);

        }else{//有数据
            hcUserCenter.setFace(hcUser.getFace());
            hcUserCenter.setPhone(hcUser.getPhone());
            hcUserCenter.setAddress(hcUser.getAddress());
            hcUserCenter.setSex(hcUser.getSex());
            hcUserCenter.setNm(hcUser.getNm());
            hcUserCenter.setBirthday(hcUser.getBirthday());
            hcUserCenter.setCompany(hcUser.getCompany());
            hcUserCenter.setDriverLicense(hcUser.getDriverLicense());
            hcUserCenter.setEscortLicense(hcUser.getEscortLicense());
            hcUserCenter.setUpdateBy("system");
            hcUserCenter.setUpdateTime(DateUtils.getNowDate());
            hcUserMapper.updateHcUser(hcUserCenter);
        }
    }


}
