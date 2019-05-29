
package com.xkynar.harossl.util;

public enum ActivityType {
    // pamap2
    lying, // 0
    sitting, // 1
    standing, // 2
    walking, // 3
    running, // 4
    cycling, // 5
    nordic_walking, // 6
    watching_tv, // 7
    computer_work, // 8
    car_driving, // 9
    ascending_stairs, // 10
    descending_stairs, // 11
    vacuum_cleaning, // 12
    ironing, // 13
    folding_laundry, // 14
    house_cleaning, // 15
    playing_soccer, // 16
    rope_jumping // 17
    ;

    public static String[] toStringArray() {
        ActivityType[] values = ActivityType.values();
        int length = values.length;
        String[] stringA = new String[length];
        for (int i = 0; i < length; i++) {
            stringA[i] = values[i].name();
        }
        return stringA;
    }

    // HUGO'S DATASET
    // WALKING, // 0
    // WALKING_UPSTAIRS, // 1
    // WALKING_DOWNSTAIRS, // 2
    // SITTING, // 3
    // STANDING, // 4
    // LAYING, // 5
    // STAND_TO_SIT, // 6
    // SIT_TO_STAND, // 7
    // SIT_TO_LIE, // 8
    // LIE_TO_SIT, // 9
    // STAND_TO_LIE, // 10
    // LIE_TO_STAND // 11

    // LEIDEN DATASET
    // cycling, //0
    // dishwashing, //1
    // lyingDownLeft, //2
    // lyingDownRight, //3
    // sittingChair, //4
    // sittingCouch, //5
    // sittingSofa, //6
    // stakingShelves, //7
    // standing, //8
    // step, //9
    // syncJumping, //10
    // vacuumCleaning, //11
    // walkingFast, //12
    // walkingNormal, //13
    // walkingSlow, //14
    // walkingStairsUp //15

}
