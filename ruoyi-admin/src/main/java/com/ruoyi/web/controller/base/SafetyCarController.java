package com.ruoyi.web.controller.base;

import com.alibaba.fastjson.JSON;
import com.ruoyi.base.domain.BaseSafetycar;
import com.ruoyi.base.service.SafetycarService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车辆信息Controller
 *
 * @author sm
 * @date 2022-09-21
 */
@RestController
@RequestMapping("/base/safetyCar")
public class SafetyCarController extends BaseController {
    @Autowired
    private SafetycarService safetycarService;

    /**
     * 添加
     */
    @PreAuthorize("@ss.hasPermi('base:safetyCar:add')")
    @Log(title = "车辆信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult addSafetyCar(@RequestBody BaseSafetycar safetyCar) {
        System.out.println("safetyCar = " + safetyCar);
        BaseSafetycar exist = safetycarService.isExist(safetyCar.getIpLtLic());
        int count = 0;
        if (exist == null) {
            count = safetycarService.insertCarlist(safetyCar);
        }
        return toAjax(count);
    }


    @Log(title = "工单车辆", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    public AjaxResult updateSafetyCar(@RequestBody BaseSafetycar safetyCar) {
        System.out.println("safetyCar = " + safetyCar);
        int count = safetycarService.updateSafetyCar(safetyCar);
        return toAjax(count);
    }

    /**
     * 查询车辆信息列表
     */
    @PreAuthorize("@ss.hasPermi('base:safetyCar:list')")
    @GetMapping("/list")
    public TableDataInfo list(BaseSafetycar baseSafetycar) {
        startPage();
        List<BaseSafetycar> list = safetycarService.selectBaseSafetycarList(baseSafetycar);
        return getDataTable(list);
    }


    /**
     * 获取车辆信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('base:safetyCar:query')")
    @GetMapping(value = "/{idno}")
    public AjaxResult getInfo(@PathVariable("idno") String idno) {
        return AjaxResult.success(safetycarService.selectBaseSafetycarByIdno(idno));
    }


    /**
     * 修改车辆信息
     */
    @PreAuthorize("@ss.hasPermi('base:safetyCar:edit')")
    @Log(title = "车辆信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BaseSafetycar baseSafetycar) {
        return toAjax(safetycarService.updateBaseSafetycar(baseSafetycar));
    }

    /**
     * 删除车辆信息
     */
    @PreAuthorize("@ss.hasPermi('base:safetyCar:remove')")
    @Log(title = "车辆信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ipLtLics}")
    public AjaxResult remove(@PathVariable String[] ipLtLics) {
        System.out.println("ipLtLics = " + JSON.toJSON(ipLtLics));
        return toAjax(safetycarService.deleteBaseSafetycarByIdnos(ipLtLics));
    }

}
