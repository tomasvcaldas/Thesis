/**
 * Copyright 2018 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package com.xkynar.harossl.data;

import com.xkynar.harossl.util.ActivityType;
import com.yahoo.labs.samoa.instances.Instance;

import pt.up.fe.specs.contextwa.data.handlers.sensors.simul.Sensor1D;
import pt.up.fe.specs.contextwa.data.handlers.sensors.simul.Sensor3D;
import pt.up.fe.specs.contextwa.data.handlers.sensors.simul.SensorSimulDataHandler;

public class PamapDataHandler extends SensorSimulDataHandler {

    private static int[] sensorDimension = { 1, 1, 3, 3, 3, 1, 3, 3, 3, 1, 3, 3, 3 };
    public static int[] activeSensor = { 0, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1 };
    public static String[] sensors = { "heart rate",
            "hand temp", "hand acc", "hand gyr", "hand mag",
            "chest temp", "chest acc", "chest gyr", "chest mag",
            "ankle temp", "ankle acc", "ankle gyr", "ankle mag", };
    public static int HAND_ACC_POS = 2;
    public static int HAND_GYR_POS = 3;
    public static int HAND_MAG_POS = 4;
    public static int CHEST_ACC_POS = 6;
    public static int CHEST_GYR_POS = 7;
    public static int CHEST_MAG_POS = 8;
    public static int ANKLE_ACC_POS = 10;
    public static int ANKLE_GYR_POS = 11;
    public static int ANKLE_MAG_POS = 12;

    public PamapDataHandler() {
        super(sensorDimension, activeSensor, sensors, ActivityType.toStringArray());

    }
    
    public PamapDataHandler(String[] Classes) {
        super(sensorDimension, activeSensor, sensors, Classes);

    }

    @Override
    public int addFeatures(Instance instance, int pos, Sensor1D sensor1d) {
        sensor1d.mean();
        sensor1d.sDev();
        instance.setValue(pos, sensor1d.getMean());
        instance.setValue(pos + 1, sensor1d.getSdX());
        return 2;
    }

    @Override
    public int addFeatures(Instance instance, int pos, Sensor3D sensor3d) {
        sensor3d.mean();
        sensor3d.sDev();
        sensor3d.correlation();
        instance.setValue(pos, sensor3d.getMeanX());
        instance.setValue(pos + 1, sensor3d.getMeanY());
        instance.setValue(pos + 2, sensor3d.getMeanZ());
        instance.setValue(pos + 3, sensor3d.getMean());
        instance.setValue(pos + 4, sensor3d.getSdX());
        instance.setValue(pos + 5, sensor3d.getSdY());
        instance.setValue(pos + 6, sensor3d.getSdZ());
        instance.setValue(pos + 7, sensor3d.getCorrXY());
        instance.setValue(pos + 8, sensor3d.getCorrYZ());
        instance.setValue(pos + 9, sensor3d.getCorrXZ());
        return 10;
    }

    public void setActiveSensors(boolean hand, boolean chest, boolean ankle) {
        int handI = hand ? 1 : 0;
        int chestI = chest ? 1 : 0;
        int ankleI = ankle ? 1 : 0;
        activeSensor[HAND_ACC_POS] = handI;
        activeSensor[HAND_GYR_POS] = handI;
        activeSensor[HAND_MAG_POS] = handI;
        activeSensor[CHEST_ACC_POS] = chestI;
        activeSensor[CHEST_GYR_POS] = chestI;
        activeSensor[CHEST_MAG_POS] = chestI;
        activeSensor[ANKLE_ACC_POS] = ankleI;
        activeSensor[ANKLE_GYR_POS] = ankleI;
        activeSensor[ANKLE_MAG_POS] = ankleI;
        super.setActiveSensors(activeSensor);
    }

}
