package com.example.solo.autoweixin.bean;

public class CodeBean {

    private Integer id;
    private String mKey;//激活码
    private Integer isActivated;//是否被激活
    private Integer vipType;//会员类型（0体验会员3天50次，1月度会员30天15000次，2季度会员91天182000次，3半年度会员183天1456000次，4年度会员365天9999999次）
    private String deviceId;//设备Id
    private Long ActivatedDate;//激活时间（毫秒数）
    private Long endDate;//终止时间（毫秒数）
    private Long totalTime;//全部可使用时间（毫秒数）
    private Long totalNum;//全部可使用次数

    public CodeBean() {
        super();
    }

    public String getmKey() {
        return mKey;
    }

    public void setmKey(String mKey) {
        this.mKey = mKey;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIsActivated() {
        return isActivated;
    }

    public void setIsActivated(Integer isActivated) {
        this.isActivated = isActivated;
    }

    public Integer getVipType() {
        return vipType;
    }

    public void setVipType(Integer vipType) {
        this.vipType = vipType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getActivatedDate() {
        return ActivatedDate;
    }

    public void setActivatedDate(Long activatedDate) {
        ActivatedDate = activatedDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public Long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }

    public Long getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(Long totalNum) {
        this.totalNum = totalNum;
    }

    @Override
    public String toString() {
        return "CodeBean{" +
                "id=" + id +
                ", mKey='" + mKey + '\'' +
                ", isActivated=" + isActivated +
                ", vipType=" + vipType +
                ", deviceId='" + deviceId + '\'' +
                ", ActivatedDate=" + ActivatedDate +
                ", endDate=" + endDate +
                ", totalTime=" + totalTime +
                ", totalNum=" + totalNum +
                '}';
    }
}
