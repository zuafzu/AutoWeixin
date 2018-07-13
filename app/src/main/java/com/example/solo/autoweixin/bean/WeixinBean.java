package com.example.solo.autoweixin.bean;

public class WeixinBean {

    private String version_id;
    private String wx_yonghuming;
    private String wx_haoyouliebiao;
    private String wx_gengduo;
    private String wx_xiugaibeizhu;
    private String wx_name1;
    private String wx_name2;

    public WeixinBean() {
        super();
    }

    public String getVersion_id() {
        return version_id;
    }

    public void setVersion_id(String version_id) {
        this.version_id = version_id;
    }

    public String getWx_yonghuming() {
        return wx_yonghuming;
    }

    public void setWx_yonghuming(String wx_yonghuming) {
        this.wx_yonghuming = wx_yonghuming;
    }

    public String getWx_haoyouliebiao() {
        return wx_haoyouliebiao;
    }

    public void setWx_haoyouliebiao(String wx_haoyouliebiao) {
        this.wx_haoyouliebiao = wx_haoyouliebiao;
    }

    public String getWx_gengduo() {
        return wx_gengduo;
    }

    public void setWx_gengduo(String wx_gengduo) {
        this.wx_gengduo = wx_gengduo;
    }

    public String getWx_xiugaibeizhu() {
        return wx_xiugaibeizhu;
    }

    public void setWx_xiugaibeizhu(String wx_xiugaibeizhu) {
        this.wx_xiugaibeizhu = wx_xiugaibeizhu;
    }

    public String getWx_name1() {
        return wx_name1;
    }

    public void setWx_name1(String wx_name1) {
        this.wx_name1 = wx_name1;
    }

    public String getWx_name2() {
        return wx_name2;
    }

    public void setWx_name2(String wx_name2) {
        this.wx_name2 = wx_name2;
    }

    @Override
    public String toString() {
        return "WeixinBean{" +
                "version_id='" + version_id + '\'' +
                ", wx_yonghuming='" + wx_yonghuming + '\'' +
                ", wx_haoyouliebiao='" + wx_haoyouliebiao + '\'' +
                ", wx_gengduo='" + wx_gengduo + '\'' +
                ", wx_xiugaibeizhu='" + wx_xiugaibeizhu + '\'' +
                ", wx_name1='" + wx_name1 + '\'' +
                ", wx_name2='" + wx_name2 + '\'' +
                '}';
    }
}
