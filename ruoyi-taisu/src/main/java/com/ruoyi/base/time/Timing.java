package com.ruoyi.base.time;

import com.ruoyi.base.mapper.CarCardMapper;
import com.ruoyi.base.service.CenterUserService;
import com.ruoyi.base.service.ILocateCardService;
import com.ruoyi.base.service.IManFactoryService;
import com.ruoyi.base.service.impl.ManWorkServiceImpl;
import com.ruoyi.base.utils.ZhenQuUtils;
import com.ruoyi.base.vo.AccessTokenVO;
import com.ruoyi.base.vo.MacSuccessVo;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.SysJobLog;
import com.ruoyi.system.enums.TaisuFactory;
import com.ruoyi.system.mapper.SysJobLogMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import java.util.Date;
import java.util.List;

/**
 * @author 兴跃
 */
@Configuration
@EnableScheduling
@Slf4j
public class Timing {

    @Autowired
    private IManFactoryService manFactoryService;
    @Autowired
    private CarCardMapper carCardMapper;
    @Autowired
    private ManWorkServiceImpl workService;

    @Autowired
    private ZhenQuUtils zhenQuUtils;

    @Autowired
    private ILocateCardService locateCardService;
    @Autowired
    private SysJobLogMapper sysJobLogMapper;

    @Autowired
    private SysUserMapper sysUserMapper;





    @Autowired
    private ISysUserService userService;

    @Autowired
    private CenterUserService centerUserService;
    /**
     * 每天03：00执行
     * 定时去清理工单，厂商的车卡和车牌号
     */
    //@Scheduled(cron = "0 3 0 * * ?")
    public void timingRefresh() {
        try {
            log.info(new Date() + "定时开始");
            manFactoryService.updateCar();
            carCardMapper.dailyClear();
        } catch (Exception e) {
            e.printStackTrace();
            log.info("异常：", e);
        }
    }

    /**
     * 每天凌晨执行；把手工单的日期重置为当日。
     * 顺便要删掉中间表的关联关系
     */
    //@Scheduled(cron = "0 0 0 * * ?")
    public void resetLongTimeWork() {
        workService.resetLongTimeWork();
    }

    /**
     * 定时删除厂商的身份证号为空的
     */
    //@Scheduled(cron = "0 0 0 * * ?")
    public void delFactory() {
        manFactoryService.delFactory();
    }

    /**
     * 定时获取定位卡的电量
     */
    //@Scheduled(cron = "0 */30 * * * ?")
    public void getBattery(){
        locateCardService.updateBySn();
    }


    /**
     * 定时连接数据库
     */


    @Scheduled(cron = "0 */3 * * * ?")
    public void connectDatabase(){
        SysJobLog sysJobLog=new SysJobLog();
        sysJobLogMapper.selectSysJobLogList(sysJobLog);
        System.out.println("鏈接成功");
    }

    //@Scheduled(cron = "0 0 0 * * ?")
    public void addEmployeeAE(){
        sysUserMapper.selectSysuserDoSynchronized(new Date());
    }

    //TODO 10-10 员工信息同步到各厂

    /**
     * AE中心员工定期更新到分厂
     */
    //@Scheduled(cron = "*/90 * * * * ?")
    public void syncUser() {
        System.out.println("ae厂 定时同步员工数据.....");
        List<SysUser> aeUnSyncUserList = userService.selectUserUnSync();
        if (!CollectionUtils.isEmpty(aeUnSyncUserList)) {
            for (SysUser sysUser : aeUnSyncUserList) {
                System.out.println("sysUser = " + JSON.toJSONString(sysUser));
                int result = centerUserService.syncUser(sysUser);
                if (result == 200){
                    System.out.println(sysUser.getIdCard() + "--- 更新成功");
                    centerUserService.syncedUser(sysUser.getIdCard(), TaisuFactory.AE);
                }
            }
        }
    }

}
