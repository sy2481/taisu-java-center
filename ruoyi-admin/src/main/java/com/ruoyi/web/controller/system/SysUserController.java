package com.ruoyi.web.controller.system;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.base.domain.CarCard;
import com.ruoyi.base.domain.CardRecord;
import com.ruoyi.base.interact.CarCardSendService;
import com.ruoyi.base.interact.PersonSendService;
import com.ruoyi.base.interact.PlateSendService;
import com.ruoyi.base.mapper.CarCardMapper;
import com.ruoyi.base.mapper.CardRecordMapper;
import com.ruoyi.base.service.impl.ApiService;
import com.ruoyi.base.utils.IDcard;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.config.ThreadPoolConfig;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysPostService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.web.api.basic.Response;
import com.ruoyi.web.api.dto.FacePhotoDTO;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ????????????
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {


    @Autowired
    private ISysUserService userService;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private ISysRoleService roleService;
    @Autowired
    private ISysPostService postService;
    @Autowired
    private ISysDeptService deptService;
    @Autowired
    private CarCardMapper carCardMapper;
    @Autowired
    private CardRecordMapper cardRecordMapper;
    @Autowired
    private ApiService apiService;
    @Autowired
    private ThreadPoolConfig pool;
    @Autowired
    private CarCardSendService carCardSendService;
    @Autowired
    private PersonSendService personSendService;
    @Autowired
    private PlateSendService plateSendService;

    /**
     * ??????????????????
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user) {
        if (!StringUtils.isEmpty(user.getDeptfStr())) {
            //????????????????????????
            List<SysDept> sysDepts =deptService.selectDeptByName(user.getDeptfStr());
            SysDept sysDept=null;
            if(!CollectionUtils.isEmpty(sysDepts)){
                sysDept=sysDepts.get(0);
            }
//            sysDept = deptService.selectDeptByName(user.getDeptfStr()).get(0);
            if (sysDept != null) {
                Long deptId = sysDept.getDeptId();
                user.setDeptId(deptId);
            } else {
                user.setDeptId(Long.valueOf(99999999L));
            }
        }
        startPage();
        List<SysUser> list = userService.selectUserList(user);

        list.forEach(sysUser -> {
            StringBuffer factoryName = new StringBuffer();
            if (StringUtils.isNotBlank(sysUser.getFactoryId())) {
                String[] split = sysUser.getFactoryId().split(",");
                for (int i = 0; i < split.length; i++) {
                    SysDept factory = deptService.selectDeptById(new Long(split[i]));
                    if (i == 0) {
                        factoryName.append(factory.getDeptName());
                    } else {
                        factoryName.append("," + factory.getDeptName());
                    }

                }
            }
            sysUser.setFactoryName(factoryName.toString());
        });
        return getDataTable(list);
    }

    @GetMapping("/listAll")
    public TableDataInfo listAll(SysUser user) {
        if (!StringUtils.isEmpty(user.getDeptfStr())) {
            //????????????????????????
            SysDept sysDept = deptService.selectDeptByName(user.getDeptfStr()).get(0);
            if (sysDept != null) {
                Long deptId = sysDept.getDeptId();
                user.setDeptId(deptId);
            } else {
                user.setDeptId(Long.valueOf(99999999L));
            }
        }
        //user.setDeptId(Long.valueOf(99999999L));
        startPage();
        List<SysUser> list = userService.selectUserListAll(user);

        list.forEach(sysUser -> {
            StringBuffer factoryName = new StringBuffer();
            if (StringUtils.isNotBlank(sysUser.getFactoryId())) {
                String[] split = sysUser.getFactoryId().split(",");
                for (int i = 0; i < split.length; i++) {
                    SysDept factory = deptService.selectDeptById(new Long(split[i]));
                    if (i == 0) {
                        factoryName.append(factory.getDeptName());
                    } else {
                        factoryName.append("," + factory.getDeptName());
                    }

                }
            }
            sysUser.setFactoryName(factoryName.toString());
        });
        return getDataTable(list);
    }

    /**
     * ??????????????????
     */
//    @PreAuthorize("@ss.hasPermi('system:user:list')")
//    @GetMapping("/list")
//    public TableDataInfo list(SysUser user) {
//        if (!StringUtils.isEmpty(user.getDeptfStr())) {
//            //????????????????????????
//            SysDept sysDept = deptService.selectDeptByName(user.getDeptfStr()).get(0);
//            if (sysDept != null) {
//                Long deptId = sysDept.getDeptId();
//                user.setDeptId(deptId);
//            } else {
//                user.setDeptId(Long.valueOf(99999999L));
//            }
//        }
//        startPage();
//        List<SysUser> list = userService.selectUserList(user);
//
//        list.forEach(sysUser -> {
//            StringBuffer factoryName = new StringBuffer();
//            if (StringUtils.isNotBlank(sysUser.getFactoryId())) {
//                String[] split = sysUser.getFactoryId().split(",");
//                for (int i = 0; i < split.length; i++) {
//                    SysDept factory = deptService.selectDeptById(new Long(split[i]));
//                    if (i == 0) {
//                        factoryName.append(factory.getDeptName());
//                    } else {
//                        factoryName.append("," + factory.getDeptName());
//                    }
//
//                }
//            }
//            sysUser.setFactoryName(factoryName.toString());
//        });
//        return getDataTable(list);
//    }

    @Log(title = "????????????", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:user:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysUser user) {
        List<SysUser> list = userService.selectUserList(user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.exportExcel(response, list, "????????????");

    }

    @Log(title = "????????????", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('system:user:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = userService.importUser(userList, updateSupport, operName);
        return AjaxResult.success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.importTemplateExcel(response, "????????????");
    }

    /**
     * ????????????????????????????????????
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping(value = {"/", "/{userId}"})
    public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        userService.checkUserDataScope(userId);
        AjaxResult ajax = AjaxResult.success();
        List<SysRole> roles = roleService.selectRoleAll();
        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        ajax.put("posts", postService.selectPostAll());
        if (StringUtils.isNotNull(userId)) {
            SysUser sysUser = userService.selectUserById(userId);
            ajax.put(AjaxResult.DATA_TAG, sysUser);
            ajax.put("postIds", postService.selectPostListByUserId(userId));
            ajax.put("roleIds", sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));

            if (StringUtils.isNotBlank(sysUser.getFactoryId())) {
                String[] split = sysUser.getFactoryId().split(",");
                List<Long> longList = new ArrayList<>();
                for (String s : split) {
                    longList.add(new Long(s));
                }
                ajax.put("factoryIdArray", longList);
            }


            if (StringUtils.isNotBlank(sysUser.getPlc())){
                String[] split = sysUser.getPlc().split(",");
                List<Long> list = new ArrayList<>();
                for (String s : split) {
                    list.add(new Long(s));
                }
                ajax.put("plcInfo", list);
            }

        }

        return ajax;
    }

    /**
     * ????????????
     */
    @PreAuthorize("@ss.hasPermi('system:user:add')")
    @Log(title = "????????????", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysUser user) {
        //??????????????????????????????
        SysUser userNo = userService.getByUserNo(user.getEmpNo());
        if (userNo != null) {
            return AjaxResult.error("????????????'" + user.getUserName() + "'??????????????????????????????");
        }
        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(user.getUserName()))) {
            return AjaxResult.error("????????????'" + user.getUserName() + "'??????????????????????????????");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            return AjaxResult.error("????????????'" + user.getUserName() + "'??????????????????????????????");
        } else if (StringUtils.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            return AjaxResult.error("????????????'" + user.getUserName() + "'??????????????????????????????");
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
        user.setCreateBy(getUsername());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        //????????????????????????
        try {
            IDcard.checkIdCard(user.getIdCard());
            IDcard.competeUserByIdcard(user);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }

        user.setSended(0L);

        int userRow = userService.insertUser(user);
        if (userRow > 0) {
            // ??????????????????????????????????????????????????????
//            pool.threadPoolTaskExecutor().execute(() -> plateSendService.userCarDownSend(userService.selectUserById(user.getUserId())));
            return toAjax(userRow);
        }
        return AjaxResult.error(user.getIdCard() + "?????????????????????");
    }





    /**
     * ????????????
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "????????????", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysUser user) throws Exception {
        SysUser oldUser = userService.selectUserById(user.getUserId());
        //??????????????????
        if (!oldUser.getEmpNo().equals(user.getEmpNo())) {
            //??????????????????????????????
            SysUser userNo = userService.getByUserNo(user.getEmpNo());
            if (userNo != null) {
                return AjaxResult.error("????????????'" + user.getUserName() + "'??????????????????????????????");
            }
        }
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
//        if (StringUtils.isNotEmpty(user.getPhonenumber())
//                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
//            return AjaxResult.error("????????????'" + user.getUserName() + "'??????????????????????????????");
//        } else if (StringUtils.isNotEmpty(user.getEmail())
//                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
//            return AjaxResult.error("????????????'" + user.getUserName() + "'??????????????????????????????");
//        }
        user.setUpdateBy(getUsername());

        StringBuilder factory = new StringBuilder();
        if (StringUtils.isNull(user.getFactoryIdArray())) {
            user.setFactoryId(null);
        } else {
            for (int i = 0; i < user.getFactoryIdArray().length; i++) {
                if (i == 0) {
                    factory.append(user.getFactoryIdArray()[i]);
                } else {
                    factory.append(",").append(user.getFactoryIdArray()[i]);
                }
            }
            user.setFactoryId(factory.toString());
        }
        //????????????
//        StringBuffer plc = new StringBuffer();
//        if (StringUtils.isNull(user.getPlcInfo())) {
//            user.setPlc(null);
//        } else {
//            for (int i = 0; i < user.getPlcInfo().length; i++) {
//                if (i == 0) {
//                    plc.append(user.getPlcInfo()[i]);
//                } else {
//                    plc.append(",").append(user.getPlcInfo()[i]);
//                }
//            }
//            user.setPlc(plc.toString());
//        }

        IDcard.competeUserByIdcard(user);
        int userRow = userService.updateUser(user);
        if (userRow > 0) {
            pool.threadPoolTaskExecutor().execute(() -> apiService.userBindHlk(sysUserMapper.selectUserById(user.getUserId())));
            //?????????????????????????????????????????????????????????????????????????????????
            //pool.threadPoolTaskExecutor().execute(() -> plateSendService.userPlateDiffSend(sysUserMapper.selectUserById(user.getUserId()), oldUser.getCarId()));
            return toAjax(userRow);
        }
        return AjaxResult.error(user.getIdCard() + "?????????????????????");
    }

    /**
     * ????????????
     */
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "????????????", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds) {
        if (ArrayUtils.contains(userIds, getUserId())) {
            return error("????????????????????????");
        }
        //?????????????????????????????????????????????
        for (int i = 0; i < userIds.length; i++) {
            SysUser sysUser = userService.selectUserById(userIds[i]);
            if (StringUtils.isNotBlank(sysUser.getCarCard())) {
                return error("??????????????????????????????????????????");
            }
            if (StringUtils.isNotBlank(sysUser.getPositionCardNo())) {
                return error("?????????????????????????????????????????????");
            }
        }
        for (int i = 0; i < userIds.length; i++) {
            //???????????????????????????
            SysUser sysUser = userService.selectUserById(userIds[i]);
            String oldPlateNo = sysUser.getCarId();
            sysUser.setCarId(null);
            plateSendService.userPlateDiffSend(sysUser, oldPlateNo);
            personSendService.downSendDeletePerson(sysUser.getIdCard());
        }
        return toAjax(userService.deleteUserByIds(userIds));
    }

    /**
     * ????????????
     */
    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    @Log(title = "????????????", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUpdateBy(getUsername());
        return toAjax(userService.resetPwd(user));
    }

    /**
     * ????????????
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "????????????", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setUpdateBy(getUsername());
        return toAjax(userService.updateUserStatus(user));
    }

    /**
     * ????????????????????????????????????
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping("/authRole/{userId}")
    public AjaxResult authRole(@PathVariable("userId") Long userId) {
        AjaxResult ajax = AjaxResult.success();
        SysUser user = userService.selectUserById(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        ajax.put("user", user);
        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return ajax;
    }

    /**
     * ??????????????????
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "????????????", businessType = BusinessType.GRANT)
    @PutMapping("/authRole")
    public AjaxResult insertAuthRole(Long userId, Long[] roleIds) {
        userService.checkUserDataScope(userId);
        userService.insertUserAuth(userId, roleIds);
        return success();
    }

    // ??????????????????
    @ResponseBody
    @PostMapping("/batchFaceUp")
    public Response batchFaceUp(@RequestBody String faceList) {
        try {
            JSONArray listArray = JSONArray.parseArray(faceList);
            List<FacePhotoDTO> facePhotoDTOS = JSONArray.parseArray(listArray.toString(), FacePhotoDTO.class);
            Integer upNum = 0;
            for (FacePhotoDTO facePhotoDTO : facePhotoDTOS) {
                //url:/profile/face/2022/03/07/??????_486542589615843551_20220307101350A007.jpg
                String url = facePhotoDTO.getUrl();
                //nameWithIdCard[0] ??????????????? nameWithIdCard[1] ?????????????????????
                String nameWithIdCard[] = url.substring(url.lastIndexOf("/"), url.lastIndexOf("_")).split("_");
                SysUser sysUser = sysUserMapper.selectUserByIdCard(nameWithIdCard[1]);
                if (!StringUtils.isNull(sysUser)) {
                    sysUser.setFace(url);
                    int i = sysUserMapper.updateUser(sysUser);
                    if (i == 1) {
                        upNum = upNum + i;
                    }
                }
            }
            return Response.success("?????????" + facePhotoDTOS.size() + "???," + upNum + " ???????????????");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.error("?????????????????????????????????");
    }


    /**
     * ????????????
     */
    @PostMapping("/delCarCard")
    public Response delCarCard(@RequestBody String requestObj) {
        try {
            JSONObject jsonObject = JSON.parseObject(requestObj);
            //????????????????????????
            String carCarNo = (String) jsonObject.get("carCardNo");
            //?????????????????????
            SysUser empNo = sysUserMapper.getByUserNo((String) jsonObject.get("empNo"));
            String[] split = empNo.getCarCard().split(",");
            List<String> list = Arrays.asList(split);
            int carCardIndex = getCarCardIndex(list, carCarNo);
            List<String> list1 = new ArrayList<>(list);
            //????????????
            list1.remove(carCardIndex);
            String carCardlongStr = "";
            //?????????????????????????????????
            for (int i = 0; i < list1.size(); i++) {
                String cardNo = list1.get(i);
                if (i == 0) {
                    carCardlongStr = cardNo;
                } else {
                    carCardlongStr = carCardlongStr + ("," + cardNo);
                }
            }
            //??????????????????????????????
            SysUser sysUser = new SysUser();
            sysUser.setCarCard(carCardlongStr);
            sysUser.setUserId(empNo.getUserId());
            sysUserMapper.updateUser(sysUser);
            //??????????????????????????????
            CarCard carCard = new CarCard();
            carCard.setCardCarNo(carCarNo);
            carCard.setCardCarStatus("0");
            carCard.setBindPlateNo(null);
            carCard.setCardCarUse("");
            carCard.setReturnName(empNo.getNickName());
            carCard.setReturnTime(new Date());
            carCardMapper.updateCarCardByNo(carCard);
            //??????????????????
            //??????????????????????????????????????????????????????????????????
            CarCard carCardInfo = carCardMapper.selectCarCardByCardCarNo(carCarNo);
            CardRecord cardRecord = new CardRecord();
            cardRecord.setCardType("0");
            cardRecord.setCardId(carCardInfo.getCardCarId());
            cardRecord.setCardNo(carCarNo);
            cardRecord.setCardRecordOperate("1");//??????????????????
            cardRecord.setCardRecordObject(empNo.getNickName());
            cardRecord.setCardRecordTime(new Date());
            cardRecord.setCardRecordName(getUsername());
            cardRecordMapper.insertCardRecord(cardRecord);
            // ??????????????????
            pool.threadPoolTaskExecutor().execute(() -> carCardSendService.downSendUnbindCarCard(carCarNo));
            return Response.success("????????????");
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("????????????");
        }
    }

    /**
     * ????????????
     */
    @PostMapping("/addCarCard")
    public Response addCarCard(@RequestBody String requestObj) {
        try {
            JSONObject jsonObject = JSON.parseObject(requestObj);
            //????????????????????????
            String carCarNo = (String) jsonObject.get("carCardNo");
            //??????????????????????????????
            SysUser empNo = sysUserMapper.getByUserNo((String) jsonObject.get("empNo"));
            String[] split;
            List<String> list;
            List<String> list1;
            if (!StringUtils.isEmpty(empNo.getCarCard())) {
                split = empNo.getCarCard().split(",");
                list = Arrays.asList(split);
                list1 = new ArrayList<>(list);
                list1.add(carCarNo);
            } else {
                //????????????
                list1 = new ArrayList<>();
                list1.add(carCarNo);
            }
            String carCardlongStr = "";
            //?????????????????????????????????
            for (int i = 0; i < list1.size(); i++) {
                String cardNo = list1.get(i);
                if (i == 0) {
                    carCardlongStr = cardNo;
                } else {
                    carCardlongStr = carCardlongStr + ("," + cardNo);
                }
            }
            //??????????????????????????????
            SysUser sysUser = new SysUser();
            sysUser.setCarCard(carCardlongStr);
            sysUser.setUserId(empNo.getUserId());
            sysUserMapper.updateUser(sysUser);
            //??????????????????????????????
            CarCard carCard = new CarCard();
            carCard.setCardCarNo(carCarNo);
            carCard.setCardCarStatus("1");
            carCard.setCardCarUse("1");
            carCard.setBindPlateNo(null);
            carCard.setLeadName(empNo.getNickName());
            carCard.setLeadTime(new Date());
            carCardMapper.updateCarCardByNo(carCard);
            //??????????????????
            //??????T??????????????????????????????????????????
            CarCard carCardInfo = carCardMapper.selectCarCardByCardCarNo(carCarNo);
            CardRecord cardRecord = new CardRecord();
            cardRecord.setCardType("0");
            cardRecord.setCardId(carCardInfo.getCardCarId());
            cardRecord.setCardNo(carCarNo);
            cardRecord.setCardRecordOperate("0");//??????????????????
            cardRecord.setCardRecordObject(empNo.getNickName());
            cardRecord.setCardRecordTime(new Date());
            cardRecord.setCardRecordName(getUsername());
            cardRecordMapper.insertCardRecord(cardRecord);
            //????????????????????????
            pool.threadPoolTaskExecutor().execute(() -> carCardSendService.userCarCardDownSend(sysUserMapper.selectUserById(sysUser.getUserId())));
            return Response.success("????????????");
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("????????????");
        }
    }

    public static int getCarCardIndex(List<String> list, String carCarNo) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(carCarNo)) {
                return i;
            }
        }
        return 0;
    }

    @GetMapping("/deleteFace")
    public AjaxResult deleteFace(Long userId) {
        try {
            SysUser sysUser = userService.selectUserById(userId);
            if (StringUtils.isNotBlank(sysUser.getIdCard())){
                personSendService.downSendDeletePerson(sysUser.getIdCard());
            }
            userService.deleteFaceByUserId(userId);
            return AjaxResult.success();
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("?????????????????????????????????");
        }
    }

    /**
     * ????????????????????????
     */
    @DeleteMapping(value = "sendUserMsgList/{ids}")
    public AjaxResult sendUserMsgList(@PathVariable Long[] ids) {
        apiService.sendUserIds(ids);
        return AjaxResult.success();
    }

    @GetMapping(value = "addUser")
    public AjaxResult addUser() {
        int i = userService.addUser();
        if (i > 0) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error();
        }

    }
}
