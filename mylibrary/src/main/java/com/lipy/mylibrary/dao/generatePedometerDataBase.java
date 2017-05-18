package com.lipy.mylibrary.dao;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class generatePedometerDataBase {
    public static Entity PedometerCard;


    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1, "com.lipy.mylibrary.dao.core");
        schema.enableKeepSectionsByDefault();

        addPedometer(schema);
//        new DaoGenerator().generateAll(schema, "E:/project/AndroidPedometer/mylibrary/src/main/java/");
        new DaoGenerator().generateAll(schema, "/Users/lipy/Documents/AndroidPedometer/mylibrary/src/main/java/");

    }

    //计步
    private static void addPedometer(Schema schema) {
        PedometerCard = schema.addEntity("PedometerEntity");
        PedometerCard.setTableName("PedometerLog");
        //默认的id自主增加
        PedometerCard.addIdProperty().autoincrement();
        //日期，并且是唯一的
//        PedometerCard.addStringProperty("date").notNull().unique();
        PedometerCard.addStringProperty("date").notNull();
        //每日步数
        PedometerCard.addIntProperty("dailyStep");
//        步行的距离
//        PedometerCard.addDoubleProperty("distanceInMeters");
        //开机后总步数
        PedometerCard.addIntProperty("totalSteps");

        PedometerCard.addIntProperty("tagStep");
        PedometerCard.addIntProperty("lastSystemSteps");
        //是否重启手机重置，0重置，1未重置
        PedometerCard.addBooleanProperty("reStart");

        //打卡
        PedometerCard.addBooleanProperty("punchCard");

    }



}
