package com.ruoyi.base.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 车辆信息对象 base_safetyCar
 * 
 * @author ruoyi
 * @date 2022-09-23
 */
public class BaseSafetycar extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 关联编号 - 车辆(车牌号码) */
    private String idno;

    /** 工作名称 */
    @Excel(name = "工作名称")
    private String workName;

    /** 合约卡号 */
    @Excel(name = "合约卡号")
    private String ipLtLic;

    /** 厂区编号 */
    @Excel(name = "厂区编号")
    private String pz;

    /** 十进制码 */
    @Excel(name = "十进制码")
    private String decimalCode;

    public void setIdno(String idno) 
    {
        this.idno = idno;
    }

    public String getIdno() 
    {
        return idno;
    }
    public void setWorkName(String workName) 
    {
        this.workName = workName;
    }

    public String getWorkName() 
    {
        return workName;
    }
    public void setIpLtLic(String ipLtLic) 
    {
        this.ipLtLic = ipLtLic;
    }

    public String getIpLtLic() 
    {
        return ipLtLic;
    }
    public void setPz(String pz) 
    {
        this.pz = pz;
    }

    public String getPz() 
    {
        return pz;
    }
    public void setDecimalCode(String decimalCode) 
    {
        this.decimalCode = decimalCode;
    }

    public String getDecimalCode() 
    {
        return decimalCode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("idno", getIdno())
            .append("workName", getWorkName())
            .append("ipLtLic", getIpLtLic())
            .append("pz", getPz())
            .append("decimalCode", getDecimalCode())
            .toString();
    }
}
