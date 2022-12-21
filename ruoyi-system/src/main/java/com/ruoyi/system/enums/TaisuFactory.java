package com.ruoyi.system.enums;

public enum TaisuFactory {
    AE(1, "AE"), SAP(2, "SAP"), PP(3, "PP"), PVC(4, "PVC"), EVA(5, "EVA"), PZ(6, "PZ"), MA(7, "MA");

    private final Integer code;
    private final String factory;

    public Integer getCode() {
        return code;
    }

    public String getFactory() {
        return factory;
    }

    TaisuFactory(Integer code, String factory) {
        this.code = code;
        this.factory = factory;
    }
}

