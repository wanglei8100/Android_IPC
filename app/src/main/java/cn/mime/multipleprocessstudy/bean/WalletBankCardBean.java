/*
 *
 *  * -------------------------------------------------------------------------------------
 *  *    Mi-Me Confidential
 *  *
 *  *    Copyright (C) 2015 Shanghai Mi-Me Financial Information Service Co., Ltd.
 *  *    All rights reserved.
 *  *
 *  *    No part of this file may be reproduced or transmitted in any form or by any means,
 *  *    electronic, mechanical, photocopying, recording, or otherwise, without prior
 *  *    written permission of Shanghai Mi-Me Financial Information Service Co., Ltd.
 *  * -------------------------------------------------------------------------------------
 *
 *
 */

package cn.mime.multipleprocessstudy.bean;

import java.io.Serializable;

/**
 * <p>获取登陆用户最近使用的银行卡信息 主要是姓名和身份证号码字段需要使用
 *
 * @author wangshan
 * @version 2.0.0
 * @since 添加银行卡界面
 */
public class WalletBankCardBean implements Serializable{
    public static final int ITEM_TYPE_NORMAL = 0;
    public static final int ITEM_TYPE_ADD = 1;
    public static final int CARD_DEFAULT = 1;
    private static final long serialVersionUID = 9185994267038356215L;

    private String cardId;//银行卡id
     private String bankNo;
    private String bankName;
    private String bigBankLogoImgUrl;//银行logo图标URL(还款页使用)
    private String smallBankLogoImgUrl;//银行logo图标URL(添加银行卡页使用)
    private String bankBackgroundImgUrl;//银行卡背景图片URL(添加银行卡页使用)
    private String bankCardNo;
    private String cardType;
    private String phone;
    private String name;
    private String idNo;

    private int itemType;//银行卡item类型，银行卡item 或 添加item

    public WalletBankCardBean(String cardId, String bankNo, String bankName, String bigBankLogoImgUrl,
                              String smallBankLogoImgUrl, String bankBackgroundImgUrl, String bankCardNo,
                              String cardType, String phone, String name, String idNo, int itemType) {
        this.cardId = cardId;
        this.bankNo = bankNo;
        this.bankName = bankName;
        this.bigBankLogoImgUrl = bigBankLogoImgUrl;
        this.smallBankLogoImgUrl = smallBankLogoImgUrl;
        this.bankBackgroundImgUrl = bankBackgroundImgUrl;
        this.bankCardNo = bankCardNo;
        this.cardType = cardType;
        this.phone = phone;
        this.name = name;
        this.idNo = idNo;
        this.itemType = itemType;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }


    public String getBankNo() {
        return bankNo;
    }

    public void setBankNo(String bankNo) {
        this.bankNo = bankNo;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBigBankLogoImgUrl() {
        return bigBankLogoImgUrl;
    }

    public void setBigBankLogoImgUrl(String bigBankLogoImgUrl) {
        this.bigBankLogoImgUrl = bigBankLogoImgUrl;
    }

    public String getSmallBankLogoImgUrl() {
        return smallBankLogoImgUrl;
    }

    public void setSmallBankLogoImgUrl(String smallBankLogoImgUrl) {
        this.smallBankLogoImgUrl = smallBankLogoImgUrl;
    }

    public String getBankBackgroundImgUrl() {
        return bankBackgroundImgUrl;
    }

    public void setBankBackgroundImgUrl(String bankBackgroundImgUrl) {
        this.bankBackgroundImgUrl = bankBackgroundImgUrl;
    }

    public String getBankCardNo() {
        return bankCardNo;
    }

    public void setBankCardNo(String bankCardNo) {
        this.bankCardNo = bankCardNo;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }


}
