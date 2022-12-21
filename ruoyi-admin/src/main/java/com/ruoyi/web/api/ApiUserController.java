package com.ruoyi.web.api;


import com.ruoyi.base.utils.IDcard;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.shardbatis.builder.ShardConfigParser;
import com.ruoyi.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class ApiUserController {




    @Autowired
    private ISysUserService userService;
//    @Autowired
//    private SysUserMapper sysUserMapper;
//    @Autowired
//    private ISysRoleService roleService;
//    @Autowired
//    private ISysPostService postService;
//    @Autowired
//    private ISysDeptService deptService;
//    @Autowired
//    private CarCardMapper carCardMapper;
//    @Autowired
//    private CardRecordMapper cardRecordMapper;
//    @Autowired
//    private ApiService apiService;
//    @Autowired
//    private ThreadPoolConfig pool;
//    @Autowired
//    private CarCardSendService carCardSendService;
//    @Autowired
//    private PersonSendService personSendService;
//    @Autowired
//    private PlateSendService plateSendService;

    /**
     * 新增用户
     */

    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public void add(@Validated @RequestBody SysUser user) {
        //修改判斷編號是否重複
        SysUser userNo = userService.getByUserNo(user.getEmpNo());
        if (userNo != null) {
            log.info("新增用户'" + user.getUserName() + "'失败，員工編號已存在");
        }

//        if (StringUtils.isNotNull(user.getFactoryIdArray())&&user.getFactoryIdArray().length>0){
//            StringBuilder factory = new StringBuilder();
//            for (int i = 0; i < user.getFactoryIdArray().length; i++) {
//                if (i == 0) {
//                    factory.append(user.getFactoryIdArray()[i]);
//                } else {
//                    factory.append(",").append(user.getFactoryIdArray()[i]);
//                }
//            }
//            user.setFactoryId(factory.toString());
//        }

//        if (StringUtils.isNotNull(user.getPlcInfo())&&user.getPlcInfo().length>0){
//            StringBuilder factory = new StringBuilder();
//            for (int i = 0; i < user.getPlcInfo().length; i++) {
//                if (i == 0) {
//                    factory.append(user.getPlcInfo()[i]);
//                } else {
//                    factory.append(",").append(user.getPlcInfo()[i]);
//                }
//            }
//            user.setPlc(factory.toString());
//        }
        user.setCreateBy("admin");
        user.setPassword("");
        //先判断身份证格式
        try {
            IDcard.checkIdCard(user.getIdCard());
            IDcard.competeUserByIdcard(user);
        } catch (Exception e) {
            //return "error";
        }

        user.setSended(0L);

        int userRow = userService.insertUser(user);
        if (userRow > 0) {
            // 新增用户的时候，可能需要下发车牌权限
//            pool.threadPoolTaskExecutor().execute(() -> plateSendService.userCarDownSend(userService.selectUserById(user.getUserId())));
            //return "success";
        }
        //return ("身份证号已存在");
    }
}
