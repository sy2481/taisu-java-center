package com.ruoyi.base.service;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.enums.TaisuFactory;
import org.apache.ibatis.annotations.Param;

public interface CenterUserService {
    /**
     * 添加员工
     * */
    int addUser(SysUser user);

    /**
     * 同步员工
     * 中心 --> 分厂CenterUserService
     * */
    int syncUser(SysUser sysUser);

    /**
     * 更新员工同步状态
     * 中心 --> 分厂CenterUserService
     * */
    void syncedUser(String idCard, TaisuFactory factory);

}
