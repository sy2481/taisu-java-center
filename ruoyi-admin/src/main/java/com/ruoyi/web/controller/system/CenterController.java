package com.ruoyi.web.controller.system;

import com.ruoyi.base.service.CenterUserService;
import com.ruoyi.base.service.IPersonBindService;
import com.ruoyi.base.utils.IDcard;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试中心--员工管理 同步接口
 *
 * @author wang
 */

@RestController
@RequestMapping("/center/user")
public class CenterController extends BaseController {

    @Autowired
    private ISysUserService userService;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private IPersonBindService personBindService;


    //----
    @Autowired
    private CenterUserService centerUserService;

    //TODO 10-8 更新中心员工信息

    /**
     *  中心员工更新和添加员工
     *  查询 没有该员工和状态为删除 就添加
     *  有员工数据就 做更新
     *  只做照片和基本信息存储
     *  不做权限
     */
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping("/updateUser")
    public void updateUser(@Validated @RequestBody SysUser user){
        System.out.println("分厂 --> 中心   ---------user = " + user);
        SysUser oldUser = sysUserMapper.selectUserByIdCard(user.getIdCard());
        //如果没有员工数据或者状态为删除 则新增用户
        if (oldUser == null) {
            int status = centerUserService.addUser(user);
            if (status > 0) {
                System.out.println(user.getIdCard() + "-----> 中心添加成功");
            } else {
                System.out.println(user.getIdCard() + "-----> 中心添加失败");
            }
            return;
        }
        //只有检测到图片更新才做数据更新
        if (oldUser.getFace().equals(user.getFace())){
            return;
        }
        //判斷是否修改
        if (!oldUser.getEmpNo().equals(user.getEmpNo())) {
            //修改判斷編號是否重複
            SysUser userNo = userService.getByUserNo(user.getEmpNo());
            if (userNo != null) {
                System.out.println("员工编号已存在");
                return;
            }
        }
        if (StringUtils.isNotEmpty(user.getPhonenumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            System.out.println("手机号码已存在");
            return;
        } else if (StringUtils.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            System.out.println("邮箱账号已存在");
            return;
        }
        user.setUpdateBy(getUsername());

        IDcard.competeUserByIdcard(user);
        userService.updateUser(user);
    }


}
