package com.ruoyi.web.api;

import com.alibaba.fastjson.JSONObject;
import com.ruoyi.base.bo.FactoryWorkBO;
import com.ruoyi.base.domain.ManFactory;
import com.ruoyi.base.interact.HlkFaceCheckUtil;
import com.ruoyi.base.mapper.ManFactoryMapper;
import com.ruoyi.base.service.IManFactoryService;
import com.ruoyi.base.service.impl.ApiService;
import com.ruoyi.base.service.impl.ManFactoryServiceImpl;
import com.ruoyi.base.utils.HttpUtils;
import com.ruoyi.base.utils.IDcard;
import com.ruoyi.base.utils.ZJFConverter;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.ImgFileTools;
import com.ruoyi.framework.config.ThreadPoolConfig;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.web.api.basic.Response;
import com.ruoyi.web.api.bo.EmployeeBO;
import com.ruoyi.web.api.dto.IdCardDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author shiva   2022/3/5 14:34
 */
@RestController
@RequestMapping("/api/wechat/faceData")
public class ApiFaceDataController {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private ManFactoryMapper factoryMapper;
    @Autowired
    private ManFactoryServiceImpl factoryService;
    @Autowired
    private ApiService apiService;
    @Autowired
    private ThreadPoolConfig pool;
    @Autowired
    private ISysConfigService sysConfigService;
    @Autowired
    private HlkFaceCheckUtil hlkFaceCheckUtil;

    @Autowired
    private IManFactoryService manFactoryService;

    /**
     * 中心库地址
     */
    @Value("${cent.host}")
    private String centHost;

    //通过员工编号查询员工信息，注意员工编号唯一
    @ResponseBody
    @GetMapping("/getByEmployeeNo")
    public Response getByEmployeeNo(String employeeNo) {
        try {
            if (StringUtils.isBlank(employeeNo)) {
                return Response.error("員工編號不能為空！");
            }
            SysUser user = sysUserMapper.getByUserNo(employeeNo);
            if (user == null) {
                return Response.error("未查詢到該編號，請確認後重新輸入！");
            }
            EmployeeBO employeeBO = new EmployeeBO();
            BeanUtils.copyProperties(user, employeeBO);


            return Response.builder().code(0).data(employeeBO).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.error("查詢出錯，請稍後再試！");
    }

    //上傳照片，返回照片地址
    @ResponseBody
    @RequestMapping("/uploadFaceImg")
    public Response uploadFaceImg(MultipartFile file) {
        try {
            if (file.getSize() == 0) {
                return Response.error("圖片不存在，請重新上傳");
            }
            // 返回：/profile/face/2022/03/06/原文件名_20220306102949A001.png
            // 图片全路径为： http://localhost:8080 + fileUrl
            String fileUrl = FileUploadUtils.upload(RuoYiConfig.getProfile() + "/face", file);
            //先进行人脸校验
            String key = sysConfigService.selectConfigByKey("sys.face.check");
            if (StringUtils.isBlank(key)) {
                key = "0";
            }
            int type = Integer.parseInt(key);
            if (type == 0) {

                fileUrl = ImgFileTools.compressionLessByGoogle(RuoYiConfig.getProfile(), fileUrl, 300);
                String face = hlkFaceCheckUtil.hikFaceJudge(fileUrl);
                if (face != null) {
                    return Response.builder().code(1).data(face).build();
                }
            }
            //循环压缩
            if (file.getSize() > 1024 * 100) {
                fileUrl = ImgFileTools.compressionLessByGoogle(RuoYiConfig.getProfile(), fileUrl, 100);
            }
            System.out.println("profile:++" + fileUrl);

            return Response.builder().code(0).data(fileUrl).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.error("上傳錯誤，請稍後再試。");
    }

    // 根据员工 id,設置人臉照片
    @ResponseBody
    @GetMapping("/facePicForEmployee")
    public Response facePicForEmployee(Long id, String facePicUrl, String idCard) {
        try {
            if (id == null || StringUtils.isBlank(facePicUrl)) {
                return Response.error("資料不全，請稍後再試。");
            }
            SysUser sysUser = sysUserMapper.selectUserById(id);
            if (sysUser == null) {
                return Response.error("用戶不存在，請稍後再試！");
            }
            // 設置人臉照片，重新保存
            IDcard.checkIdCard(idCard);
            //身份证校验通过，塞进去
            sysUser.setIdCard(idCard);
            IDcard.competeUserByIdcard(sysUser);
            sysUser.setFace(facePicUrl);
            sysUserMapper.updateUser(sysUser);
            //保存照片
            pool.threadPoolTaskExecutor().execute(() -> apiService.userBindHlk(sysUserMapper.selectUserById(sysUser.getUserId())));
            return Response.builder().code(0).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error(e.getMessage());
        }
    }

    // 根据工单号，查询厂商人员列表，如果
    // 0-普通工单，1-危化品工单
    @ResponseBody
    @GetMapping("/listPersonByWorkNo")
    public Response listPersonByWorkNo(String workNo, Integer workType) {
        try {
            if (StringUtils.isBlank(workNo)) {
                return Response.error("工單號不允許為空！");
            }
            //根据工单号，查询厂商列表
            List<FactoryWorkBO> list = factoryService.listByWorkNoAndDate(workNo, DateUtils.getDate(), workType);

            //如果没有头像，从中心库取头像
            List<String> noFaceIdCardList = list.stream().filter(x -> StringUtils.isEmpty(x.getFace()))
                    .map(FactoryWorkBO::getIdCard).collect(Collectors.toList());
            if (noFaceIdCardList.size() > 0) {
                String mm = "noFaceIdCards=" + StringUtils.join(noFaceIdCardList, ",");
                String res = HttpUtils.sendGet(centHost+"/api/wechat/faceDataCent/getListByIdCards", mm);
                if (!StringUtils.isEmpty(res)) {
                    JSONObject resObj = JSONObject.parseObject(res);
                    Map<String, JSONObject> noFaceManFactoryMap = (Map<String, JSONObject>) resObj.get("data");

                    //更新没有头像的数据，并返回数据
                    if (noFaceManFactoryMap.size() > 0) {
                        factoryService.saveForNoFace(list, noFaceManFactoryMap);
                    }
                }
            }

            if (list.size() > 0) {
                list.forEach(factoryWorkBO -> {
                    StringBuilder stringBuffer = new StringBuilder();
                    if (StringUtils.isNotBlank(factoryWorkBO.getIdCard())) {
                        if (factoryWorkBO.getIdCard().length() > 10) {
                            stringBuffer.append(factoryWorkBO.getIdCard().substring(0, 4)).append("********").append(factoryWorkBO.getIdCard().substring(factoryWorkBO.getIdCard().length() - 4, factoryWorkBO.getIdCard().length()));
                        } else {
                            stringBuffer.append(factoryWorkBO.getIdCard().substring(0, 2)).append("******").append(factoryWorkBO.getIdCard().substring(factoryWorkBO.getIdCard().length() - 2, factoryWorkBO.getIdCard().length()));
                        }
                    }
                    factoryWorkBO.setIdCard(stringBuffer.toString());
                });
            }
            return Response.builder().code(0).data(list).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.error("設置出錯，請稍後再試！");
    }

    // 根据厂商人员ID，上传人脸照片，并设置人脸
    //@ResponseBody
    //@GetMapping("/facePicForSupplier")
    public Response facePicForSupplier(Long id, String facePicUrl, String phone, String address, Long sex) {
        try {
            if (id == null || StringUtils.isBlank(facePicUrl)) {
                return Response.error("資料不全，請稍後再試。");
            }
            ManFactory manFactory = factoryMapper.selectManFactoryByFactoryId(id);
            if (manFactory == null) {
                return Response.error("人員不存在，請稍後再試！");
            }
            // 設置人臉照片，重新保存
            manFactory.setFace(facePicUrl);
            manFactory.setPhone(phone);
            manFactory.setAddress(ZJFConverter.SimToTra(address));
            manFactory.setSex(sex);
            //manFactory.setPicInsertTime(new Date());
            factoryMapper.updateManFactory(manFactory);

            //同时更新中心库-修改by-sunlj
            HttpUtils.sendJsonPost(centHost+"/api/wechat/faceDataCent/saveFaceForCenter", JSONObject.toJSONString(manFactory));

            //最后调用下厂商人脸下发的方法
            pool.threadPoolTaskExecutor().execute(() -> {
                Long[] ids = new Long[]{manFactory.getFactoryId()};
                apiService.sendFactoryMsgList(ids);
            });
            return Response.builder().code(0).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.error("設置出錯，請稍後再試！");
    }

    @PostMapping("/deleteFaceCenter")
    public AjaxResult deleteFaceCenter(@RequestBody IdCardDTO idcard) {
        try {
            manFactoryService.deleteFaceCenterByIdcard(idcard.getIdCard());

            return AjaxResult.success();
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("操作失敗，請稍後再試。");
        }
    }

    @PostMapping("/deleteFaceCenterUser")
    public AjaxResult deleteFaceCenterUser(@RequestBody IdCardDTO idcard) {
        try {
            sysUserMapper.deleteFaceByIdCard(idcard.getIdCard());

            return AjaxResult.success();
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("操作失敗，請稍後再試。");
        }
    }
}
