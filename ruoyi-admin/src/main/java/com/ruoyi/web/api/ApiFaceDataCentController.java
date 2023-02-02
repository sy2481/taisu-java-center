package com.ruoyi.web.api;


import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.ruoyi.base.domain.ManFactory;
import com.ruoyi.base.service.IManFactoryService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.web.api.basic.Response;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author shiva   2022/3/5 14:34
 */
@Api(tags = "FaceDataCent")
@RestController
@RequestMapping("/api/wechat/faceDataCent")
public class ApiFaceDataCentController {

    @Autowired
    private IManFactoryService manFactoryService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private SysUserMapper sysUserMapper;



    @ResponseBody
    @PostMapping("/getListByIdCardsForSupplier")
    public Response getListByIdCardsForSupplier(@RequestBody JSONObject param) {
        try {
            List<String> noFaceIdCardList = Arrays.asList(param.getString("noFaceIdCards").split(","));
            Map<String, ManFactory> noFaceManFactoryMap = manFactoryService.getListByIdCards(noFaceIdCardList);

            return Response.builder().code(0).data(noFaceManFactoryMap).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.error("查詢出錯，請稍後再試！");
    }

    @ResponseBody
    @PostMapping(value = "/saveFaceForSupplier")
    public Response saveFaceForSupplier(@RequestBody ManFactory manFactory) {
        try {
            manFactoryService.saveFaceForCenter(manFactory);
            return Response.success("保存成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.error("查詢出錯，請稍後再試！");
    }

    @ResponseBody
    @PostMapping("/getListByIdCardsForEmployee")
    public Response getListByIdCardsForEmployee(@RequestBody JSONObject param) {
        try {
            List<String> noFaceIdCardList = Arrays.asList(param.getString("noFaceIdCards").split(","));
            Map<String, SysUser> noFaceSysUserMap = sysUserService.getListByIdCards(noFaceIdCardList);

            return Response.builder().code(0).data(noFaceSysUserMap).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.error("查詢出錯，請稍後再試！");
    }

    @ResponseBody
    @PostMapping("/getAllListByIdCardsForEmployee")
    public Response getAllListByIdCardsForEmployee() {
        try {
            //List<String> noFaceIdCardList = Arrays.asList(param.getString("noFaceIdCards").split(","));
            Map<String, SysUser> noFaceSysUserMap = sysUserService.getAllListByIdCards();

            return Response.builder().code(0).data(noFaceSysUserMap).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.error("查詢出錯，請稍後再試！");
    }

    @ResponseBody
    @PostMapping(value = "/saveFaceForEmployee")
    public Response saveFaceForEmployee(@RequestBody SysUser sysUser) {
        try {
            sysUserService.saveFaceForCenter(sysUser);
            return Response.success("保存成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.error("查詢出錯，請稍後再試！");
    }
    @ResponseBody
    @PostMapping(value = "/saveEmployee")
    public Response saveEmployee(@RequestBody SysUser sysUser) {
        try {
            sysUserService.saveEmployeeForCenter(sysUser);
            return Response.success("保存成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.error("查詢出錯，請稍後再試！");
    }

    @ResponseBody
    @PostMapping(value = "/deleteFace")
    public Response deleteFace(@RequestBody SysUser sysUser1) {
        try {
            SysUser sysUser = sysUserService.selectUserByIdCard(sysUser1.getIdCard());

            sysUserService.deleteFaceByUserId(sysUser.getUserId());
            return Response.success("操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("操作失敗，請稍後再試。");
        }
    }

    @ResponseBody
    @PostMapping("/listUser")
    public Response listUser(@RequestBody JSONObject param) {
        try {
            int pageNum = param.getInteger("pageNum");
            int pageSize = param.getInteger("pageSize");
            String orderBy = param.getString("orderBy");
            String paramEmpNos = param.getString("empNos");
            String[] empNos = !StringUtils.isEmpty(paramEmpNos)
                    ? paramEmpNos.split(",") : null;
            PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(true);
            SysUser paramUser=new SysUser();
            paramUser.setEmpNos(empNos);
            List<SysUser> list = sysUserMapper.selectUserListAll(paramUser);

            return Response.builder().code(0).data(list).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.error("查詢出錯，請稍後再試！");
    }

}
