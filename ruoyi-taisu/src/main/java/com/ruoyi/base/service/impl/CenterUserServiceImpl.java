package com.ruoyi.base.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ruoyi.base.service.CenterUserService;
import com.ruoyi.base.utils.HttpUtils;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.enums.TaisuFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CenterUserServiceImpl implements CenterUserService {

    /**
     * 中心-host
     */
//    @Value("${ae.host}")
//    private String aeHost;
    @Autowired
    private ISysUserService userService;


    @Override
    public int addUser(SysUser user) {
        //修改判斷編號是否重複
        SysUser userNo = userService.getByUserNo(user.getEmpNo());
        if (userNo != null) {
            System.out.println("員工編號已存在");
            return -1;
        }
        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(user.getUserName()))) {
            System.out.println("登录账号已存在");
            return -1;
        } else if (StringUtils.isNotEmpty(user.getPhonenumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            System.out.println("手机号码已存在");
            return -1;
        } else if (StringUtils.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            System.out.println("邮箱账号已存在");
            return -1;
        }
        //提取中心要保存的信息
        SysUser centerUser = new SysUser();
        centerUser.setDeptId(user.getDeptId());

        user.setPassword("");
        user.setSended(1L);

        int userRow = userService.insertUser(user);
        return userRow;
    }

    @Override
    public int syncUser(SysUser sysUser) {
        //过滤字段,这些字段不需要更新
        sysUser.setUserId(null);
        sysUser.setPlc(null);
        sysUser.setFactoryIdArray(null);
        sysUser.setFactoryId(null);
        sysUser.setPlc(null);
        sysUser.setPlcInfo(null);
        sysUser.setCarId(null);
        sysUser.setCarCard(null);
        String userJson = JSONObject.toJSONString(sysUser);
        String httpStr = "aeHost" + "/api/sync/syncUser";
        System.out.println("发送的请求 : " + httpStr);
        String resultStr = HttpUtils.sendJsonPost(httpStr, userJson);
        if (StringUtils.isEmpty(resultStr)){
            return -1;
        }
        JSONObject bindCardResultObj = JSONObject.parseObject(resultStr);
        Integer code = (Integer) bindCardResultObj.get("code");
        return code;
    }

    @Override
    public void syncedUser(String idCard, TaisuFactory factory) {
        if (StringUtils.isEmpty(idCard)){
            return;
        }

        userService.updateSyncedUser(idCard,factory);
    }
}
