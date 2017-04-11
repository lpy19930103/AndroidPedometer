package com.lipy.mylibrary.dao;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class generatePedometerDataBase {
    public static Entity PedometerCard;
    public static Entity calenderCard;


    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1, "com.lipy.mylibrary.dao.core");
        schema.enableKeepSectionsByDefault();

        addPedometerCard(schema);
//        addCalendarCard(schema);
        new DaoGenerator().generateAll(schema, "E:/project/AndroidPedometer/mylibrary/src/main/java/");

    }

    //计步
    private static void addPedometerCard(Schema schema) {
        PedometerCard = schema.addEntity("PedometerEntity");
        PedometerCard.setTableName("PedometerLog");
        //默认的id自主增加
        PedometerCard.addIdProperty().autoincrement();
        //日期，并且是唯一的
//        PedometerCard.addStringProperty("date").notNull().unique();
        PedometerCard.addStringProperty("date").notNull();
        //步数
        PedometerCard.addIntProperty("stepCount");
//        步行的距离
//        PedometerCard.addDoubleProperty("distanceInMeters");
        //是否完成了目标值，也就是打卡了0未打，1就已打
        PedometerCard.addIntProperty("status");
        //打卡完成后的目标的步数
        PedometerCard.addIntProperty("targetStepCount");

    }

//    private static void addCalendarCard(Schema schema) {
//        calenderCard = schema.addEntity("CalendarEntity");
//        calenderCard.setTableName("Calendar");
//        //自动生成id
//        calenderCard.addIdProperty().autoincrement();
//
//        //日期
//        calenderCard.addStringProperty("date").notNull();
//
//        //卡的标题
//        calenderCard.addStringProperty("title").notNull();
//
//        //card id
//        Property cardIdProperty = calenderCard.addLongProperty("cardId").getProperty();
//        calenderCard.addToOne(PedometerCard, cardIdProperty);
//
//        //是否已完成
//        calenderCard.addIntProperty("status").notNull();
//    }

}
