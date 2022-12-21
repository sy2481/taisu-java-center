package com.ruoyi.base.service.impl;

import ch.qos.logback.core.util.TimeUtil;
import com.ruoyi.base.bo.FactoryBo;
import com.ruoyi.base.bo.WorkBo;
import com.ruoyi.base.domain.*;
import com.ruoyi.base.mapper.ManFactoryMapper;
import com.ruoyi.base.mapper.ManWorkFactoryMapper;
import com.ruoyi.base.mapper.ManWorkMapper;
import com.ruoyi.base.service.IManBlackInfoService;
import com.ruoyi.base.service.IManWorkService;
import com.ruoyi.base.service.IOaMaxInfoService;
import com.ruoyi.base.utils.OldIpMap;
import com.ruoyi.base.utils.PlcRedisUtils;
import com.ruoyi.base.utils.ZJFConverter;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.system.service.ISysDeptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author shiva   2022-04-07 21:49
 */
@Slf4j
@Service
public class WorkDataService {

    @Autowired
    private ManWorkMapper manWorkMapper;
    @Autowired
    private ManFactoryMapper manFactoryMapper;
    @Autowired
    private ManWorkFactoryMapper manWorkFactoryMapper;
    @Autowired
    private ISysDeptService deptService;
    @Autowired
    private IManBlackInfoService manBlackInfoService;
    @Autowired
    private PlcRedisUtils plcRedisUtils;
    @Autowired
    private IManWorkService manWorkService;
    @Autowired
    private IOaMaxInfoService oaMaxInfoService;
    // 厂商工单保存 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 针对每一个工单关联对象进行保存
     */
    //存工单，存厂商人员，存关联表
    //判断工单里必要条件：单号（单号是不是重复，重复就跳过）
    //保存工单
    //存厂商人员，判断：身份证号
    //存关联表
    @Transactional(rollbackFor = {Exception.class}, readOnly = false)
    public void saveWorkBo(WorkBo workBo) {
        //添加工单,先补全数据
        ManWork manWork = instanceWork(workBo);
        Long deptId = null;
        // 根據IP拿到廠區, 查詢plc
        if (StringUtils.isNotBlank(manWork.getIp())) {
            String[] split = manWork.getIp().split(",");

            PlcEquipment equipment = plcRedisUtils.getPlcEquipment(split[0]);

            if (equipment != null && equipment.getPlantAreaId() != null) {
                deptId = equipment.getPlantAreaId();
                manWork.setDeptId(equipment.getPlantAreaId());
                log.info("1s，工单号：{}, 对应厂区id:{}", manWork.getWorkNo(), deptId);
            }
        }
        //判断是否存在重复单号，存在重复就不需要继续运行了
        ManWork oldWork = manWorkMapper.selectManWorkByworkNo(manWork.getWorkNo());
        if (oldWork != null) {
            manWork.setWorkId(oldWork.getWorkId());
            oldWork.setDeptId(manWork.getDeptId());
            oldWork.setStartTime(manWork.getStartTime());
            oldWork.setEndTime(manWork.getEndTime());
            oldWork.setExtendStartTime(manWork.getExtendStartTime());
            oldWork.setExtendEndTime(manWork.getExtendEndTime());
            oldWork.setFactoryDoorName(manWork.getFactoryDoorName());
            updateWorkIp(workBo, oldWork);
            updateWorkCar(workBo, oldWork);
        } else {
            //不存在的话，先保存了。
            //重新将全部IP再放回
            HashSet<String> ips = new HashSet<>();
            String[] split = workBo.getIP().split(",");
            for (String ip : split) {
                //遍历查找对应的ip
                String equipIp = OldIpMap.IP_MAP.get(ip + workBo.getFctdornm());
                if (StringUtils.isNotBlank(equipIp)) {
                    ips.add(equipIp);
                }
            }
            ips.remove("null");
            if (ips.size() > 0) {
                manWork.setIp(String.join(",", ips));
            }
//            manWork.setIp(workBo.getIP());
            manWorkMapper.insertManWork(manWork);
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////

        //添加厂商人员
        List<FactoryBo> factoryBoList = workBo.getFactoryBoList();
        //厂商人员没有，那就不用循环，也不用继续插入中间表了
        if (factoryBoList.size() == 0) {
            return;
        }
        //开始处理厂商人员
        Long finalDeptId = deptId;
        factoryBoList.forEach(factoryBo -> {
            ManFactory manFactory = instanceFactory(factoryBo);
            manFactory.setDeptId(finalDeptId);
            //厂商人员的身份证判重
            //存在两种情况，有身份证，没有身份证
            //没有身份证的直接保存。
            //有身份证的需要审核
            //判断是否在黑名单(身份证)
            if (StringUtils.isNotBlank(manFactory.getIdCard())) {
                ManBlackInfo blackInfo = manBlackInfoService.getBlackInfoByCard(manFactory.getIdCard());
                if (blackInfo != null) {
                    return;
                }
            }
            if (StringUtils.isBlank(manFactory.getIdCard())) {
                insertNewFactory(manFactory, factoryBo);
            } else {
                //根据身份证号查询，查询到数据 就修改，查询不到就添加
                ManFactory factory = manFactoryMapper.selectManFactoryByIdCard(manFactory.getIdCard());

                if (factory != null) {
                    //身份证存在;更新的话，不能把原数据覆盖了
                    manFactory.setFactoryId(factory.getFactoryId());
                    manFactory.setSended(factory.getSended());
                    manFactory.setEntered(factory.getEntered());
                    manFactoryMapper.updateManFactory(manFactory);
                    log.info("3s，厂商人员：{}, 对应厂区id:{}", manFactory.getIdCard(), manFactory.getDeptId());
                } else {
                    insertNewFactory(manFactory, factoryBo);
                }
            }

            //添加关联表
            ManWorkFactory manWorkFactory = new ManWorkFactory();

            //manWorkFactory.setWorkId(manWork.getWorkId());
            BeanUtils.copyProperties(manWork,manWorkFactory);
            manWorkFactory.setFactoryId(manFactory.getFactoryId());
            manWorkFactory.setEffectiveTime(manWork.getWorkTime());
            //查询中间表，有将不插入，没有进行插入
            if (manWorkFactoryMapper.selectManWorkInfo(manWorkFactory).size() == 0) {
                manWorkFactoryMapper.insertManWorkFactory(manWorkFactory);
            }

        });

    }

    private void updateWorkIp(WorkBo workBo, ManWork oldWork) {
        HashSet<String> ips = new HashSet<>();
        //如果原本不是空的
        if (StringUtils.isNotBlank(oldWork.getIp())) {
            String[] split = oldWork.getIp().split(",");
            ips.addAll(Arrays.asList(split));
        }
        //单号存在了，直接把ip拼接进去
        //现在已经是不重复的了
        String[] split = workBo.getIP().split(",");
        for (String ip : split) {
            //遍历查找对应的ip
            String equipIp = OldIpMap.IP_MAP.get(ip + workBo.getFctdornm());
            if (StringUtils.isNotBlank(equipIp)) {
                ips.add(equipIp);
            }
        }
        ips.remove("null");
        if (ips.size() > 0) {
            oldWork.setIp(String.join(",", ips));
        }
        //只要是发过来的工单，都是当天有效的
        oldWork.setWorkTime(workBo.getDatetime());
        log.info("2s，工单号：{}, 对应厂区id:{}", oldWork.getWorkNo(), oldWork.getDeptId());
        manWorkMapper.updateManWork(oldWork);
    }

    private void updateWorkCar(WorkBo workBo, ManWork oldWork) {
        //继续验证车牌，验证车牌的话，需要
        String plateNos = ZJFConverter.TraToSim(workBo.getCarId());
        if (StringUtils.isBlank(plateNos)) {
            //没有车牌号，直接返回
            return;
        }
        if (StringUtils.isBlank(oldWork.getCarId())) {
            //如果原本没有车牌号，直接保存
            oldWork.setCarId(plateNos);
        } else {
            //有车牌号，需要对比是不是旧车牌
            HashSet<String> carIds = new HashSet<>(Arrays.asList(oldWork.getCarId().split(",")));
            String[] plateSpilt = plateNos.split(",");
            carIds.addAll(Arrays.asList(plateSpilt));
            oldWork.setCarId(String.join(",", carIds));
        }
        manWorkMapper.updateManWork(oldWork);
    }

    private ManWork instanceWork(WorkBo workBo) {
        ManWork manWork = new ManWork();
        manWork.setXtInNum(0);
        manWork.setComInNum(0);
        //车牌号
        manWork.setCarId(ZJFConverter.TraToSim(workBo.getCarId()));
        //工程编号
        manWork.setProjectNo(workBo.getEgno());
        //工单号
        manWork.setWorkNo(workBo.getWorkNumber());
        //项目名称
        manWork.setProjectName(workBo.getEgnm());
        //作业类别
        manWork.setJobCategory(workBo.getOprEnvt21());
        //安全督导员
        manWork.setInspector(workBo.getInspector());
        //恰工同意
        manWork.setInspector2(workBo.getInspector2());
        //工单有效期限
        manWork.setWorkTime(workBo.getDatetime());
        manWork.setWorkType(0);
        manWork.setWorkStatus(0);


        if (workBo.getIP() != null) {
            String[] split = workBo.getIP().split(",");
            //循环遍历去map里面映射
            for (int i = 0; i < split.length; i++) {
                if (OldIpMap.IP_MAP.get(split[i] + workBo.getFctdornm()) != null) {
                    manWork.setIp(OldIpMap.IP_MAP.get(split[i] + workBo.getFctdornm()));
                    break;
                }
            }
        }
        try {
            if(!StringUtils.isEmpty(workBo.getBegtime())){
                manWork.setStartTime(DateUtils.parseDate(workBo.getBegtime(),"yyyyMMddHHmmss"));
            }
            if(!StringUtils.isEmpty(workBo.getEndtime())) {
                manWork.setEndTime(DateUtils.parseDate(workBo.getEndtime(), "yyyyMMddHHmmss"));
            }
            if(!StringUtils.isEmpty(workBo.getIntime())) {
                manWork.setExtendStartTime(DateUtils.parseDate(workBo.getIntime(), "yyyyMMddHHmmss"));
            }
            if(!StringUtils.isEmpty(workBo.getOuttime())) {
                manWork.setExtendEndTime(DateUtils.parseDate(workBo.getOuttime(), "yyyyMMddHHmmss"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        manWork.setFactoryDoorName(workBo.getFctdornm());

        return manWork;
    }


    private ManFactory instanceFactory(FactoryBo factoryBo) {
        ManFactory manFactory = new ManFactory();
        //工单号
        manFactory.setWorkNo(factoryBo.getWorkNumber());
        //身份证ID
        manFactory.setIdCard(factoryBo.getIdCard().length() > 7 ? factoryBo.getIdCard() : "");
        //车牌
        manFactory.setLcensePlate(factoryBo.getIdCard().length() > 7 ? " " : factoryBo.getIdCard());
        //当前工程编号
        manFactory.setThisNumber(factoryBo.getThisNumber());
        //工头
        manFactory.setLead(factoryBo.getProfsid());
        //合约卡号：lpltlic+pz，到时候回写到旧库
        manFactory.setIpLtLic(factoryBo.getIpltlic() + factoryBo.getFactoryNo());
        //普通厂商人员
        manFactory.setDangerType(0);
        manFactory.setSended(0);
        return manFactory;
    }

    private void insertNewFactory(ManFactory manFactory, FactoryBo factoryBo) {
        //判断名称是否重复,当天(在身份证为空的情况下)
        if (StringUtils.isNotBlank(factoryBo.getName()) && StringUtils.isBlank(manFactory.getIdCard())) {
            List<ManFactory> manFactories = manFactoryMapper.selectManFactoryByName(factoryBo.getName());
            if (StringUtils.isNotNull(manFactories)) {
                return;
            }
        }
        //姓名
        manFactory.setName(factoryBo.getName());
        //入场证
        manFactory.setIpLtLic(factoryBo.getIpltlic());
        //厂商名称
        manFactory.setFactoryName(factoryBo.getFactoryName());
        /*//工单号
        manFactory.setWorkNo(factoryBo.getWorkNumber());*/
        //通过数据中心的厂商视图查询新增用户图片
        if(factoryBo.getIdCard()!=null) {
            String face = manFactoryMapper.selectAllManFactory(factoryBo.getIdCard());
            if(face!=null){
                manFactory.setFace(face);
            }
        }
        manFactoryMapper.insertManFactory(manFactory);

    }



    // 危险品工单保存 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * 保存危险品工单
     */
    @Transactional(rollbackFor = {Exception.class}, readOnly = false)
    public void saveDangerWork(WorkBo workBo) {
        if (workBo == null) {
            return;
        }
        //拿到工单号
        ManWork dangerWork = instanceDangerWork(workBo);
        //判断是否存在重复单号，存在重复就不需要继续运行了
        ManWork manWork = manWorkMapper.selectManWorkByworkNo(dangerWork.getWorkNo());
        if (StringUtils.isNull(manWork)) {
            //不存在的话，先保存工单。
            manWorkMapper.insertManWork(dangerWork);

        }

        //////////////////////////////////////////////////////////////////////////////////////////////////
        //添加危险品的人员
        List<FactoryBo> factoryBoList = workBo.getFactoryBoList();
        //危险品的人员没有，那就不用循环，也不用继续插入中间表了
        if (factoryBoList.size() == 0) {
            return;
        }
        //保存危险品人员
        //开始处理厂商人员
        factoryBoList.forEach(factoryBo -> {
            ManFactory dangerFactory = instanceDangerFactory(factoryBo, workBo);
            //人员的身份证判重
            if (StringUtils.isBlank(dangerFactory.getIdCard())) {
                insertNewFactory(dangerFactory, factoryBo);
            } else {
                //根据身份证号查询，查询到数据 就修改，查询不到就添加
                ManFactory factory = manFactoryMapper.selectManFactoryByIdCard(dangerFactory.getIdCard());
                if (factory == null) {
                    insertNewFactory(dangerFactory, factoryBo);
                } else {
                    dangerFactory.setFactoryId(factory.getFactoryId());
                    dangerFactory.setEntered(factory.getEntered());
                    manFactoryMapper.updateManFactory(dangerFactory);
                    log.info("3s，危险品人员：{}, 对应厂区id:{}", dangerFactory.getIdCard(), dangerFactory.getDeptId());
                }
            }
            //添加关联表
            ManWorkFactory manWorkFactory = new ManWorkFactory();

            manWorkFactory.setWorkId(dangerWork.getWorkId());
            manWorkFactory.setFactoryId(dangerFactory.getFactoryId());
            manWorkFactory.setEffectiveTime(dangerWork.getWorkTime());
            //查询中间表，有将不插入，没有进行插入
            if (manWorkFactoryMapper.selectManWorkInfo(manWorkFactory) == null) {
                manWorkFactoryMapper.insertManWorkFactory(manWorkFactory);
            }
        });
    }

    private ManWork instanceDangerWork(WorkBo workBo) {
        ManWork manWork = new ManWork();
        //危险品
        manWork.setWorkType(1);
        manWork.setWorkStatus(0);
        manWork.setXtInNum(0);
        manWork.setComInNum(0);
        //车牌号
        manWork.setCarId(workBo.getCarId());
        //工单号
        manWork.setWorkNo(workBo.getWorkNumber());
        //工单有效期限
        Date date;
        try {
            date = DateUtils.parseDate(workBo.getDatetime(), DateUtils.YYYYMMDDHHMMSS);
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }
        //工单日期
        manWork.setWorkTime(DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", date));
        //根据危化名称，获得对应的厂区
        //取字典数据
        List<SysDictData> dictCache = DictUtils.getDictCache("chemical_type");
        dictCache.forEach(sysDictData -> {

            //传递的数据在字典的键值中有
            if (workBo.getIP().contains(sysDictData.getDictValue())) {
                List<SysDept> factory = deptService.getFactory(sysDictData.getDictLabel());
                //保存危化品的全称，字典对应value,厂区ID
                if (factory.size() > 0) {
                    manWork.setProjectName(workBo.getIP());
                    manWork.setProjectNo(sysDictData.getDictLabel());
                    manWork.setDeptId(factory.get(0).getDeptId());
                    String ip = manWorkService.getIp(factory.get(0).getDeptId());
                    manWork.setIp(ip);
                }
                return;
            }
        });
        return manWork;
    }

    private ManFactory instanceDangerFactory(FactoryBo factoryBo, WorkBo workBo) {
        ManFactory dangerFactory = new ManFactory();
        //名称
        dangerFactory.setName(factoryBo.getFactoryName());
        //工单号
        dangerFactory.setWorkNo(factoryBo.getWorkNumber());
        //身份证ID
        dangerFactory.setIdCard(factoryBo.getIdCard());
        //车牌
        dangerFactory.setLcensePlate(workBo.getCarId());
        //当前工程编号
        dangerFactory.setThisNumber(workBo.getWorkNumber());
        //危险品人员
        dangerFactory.setDangerType(1);
        dangerFactory.setSended(0);
        return dangerFactory;
    }


}
