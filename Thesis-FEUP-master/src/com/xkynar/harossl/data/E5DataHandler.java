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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

import com.xkynar.harossl.util.ActivityType;
import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

import moa.core.Utils;
import pt.up.fe.specs.contextwa.data.handlers.DataHandler;
import pt.up.fe.specs.contextwa.data.handlers.sensors.simul.Sensor1D;
import pt.up.fe.specs.contextwa.data.handlers.sensors.simul.Sensor3D;

@Deprecated
public class E5DataHandler implements DataHandler {
    private int[] sensorDimension = { 1, 1, 3, 3, 3, 1, 3, 3, 3, 1, 3, 3, 3 };
    private Instances headInfo;
    public int[] activeSensor = { 0, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1 };
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

    @Override
    public Instance extractFeatures(List<Instance> dataPoints) {

        int numPoints = dataPoints.size();
        ArrayList<Sensor1D> sensors1D = new ArrayList<>();
        ArrayList<Sensor3D> sensors3D = new ArrayList<>();

        int[] activityCounter = new int[ActivityType.values().length];

        int instanceValuePos = 2;
        // int countAtt = 0;
        for (int i = 0; i < getSensorDimension().length; i++) {
            int numValues = getSensorDimension()[i];
            if (activeSensor[i] == 0) {
                instanceValuePos += numValues;
                continue;
            }
            // for (int numValues : sens) {
            // aux = 0;
            if (numValues == 1) {
                // countAtt += 2;
                Sensor1D s1D = new Sensor1D(numPoints);
                double[] vectorX = new double[numPoints];
                for (int j = 0; j < numPoints; j++) {
                    Instance instance = dataPoints.get(j);
                    int classValue = (int) instance.classValue();
                    activityCounter[classValue]++;
                }
                s1D.setCoordX(vectorX);
                s1D.mean();
                s1D.sDev();

                sensors1D.add(s1D);
            }
            if (numValues == 3) {
                // countAtt += 10;
                Sensor3D s3D = new Sensor3D(numPoints);
                double[] vectorX = new double[numPoints];
                double[] vectorY = new double[numPoints];
                double[] vectorZ = new double[numPoints];
                for (int j = 0; j < numPoints; j++) {
                    activityCounter[(int) dataPoints.get(j).classValue()]++;
                    vectorX[j] = dataPoints.get(j).value(instanceValuePos);
                    vectorY[j] = dataPoints.get(j).value(instanceValuePos + 1);
                    vectorZ[j] = dataPoints.get(j).value(instanceValuePos + 2);
                }
                s3D.setCoordX(vectorX);
                s3D.setCoordY(vectorY);
                s3D.setCoordZ(vectorZ);

                s3D.mean();
                s3D.sDev();
                s3D.correlation();

                sensors3D.add(s3D);
            }
            instanceValuePos += numValues;

        }

        ActivityType activity = ActivityType.values()[Utils.maxIndex(activityCounter)];
        Instance instance = new DenseInstance((sensors1D.size()) * 2 + (sensors3D.size()) * 10 + 1);
        headInfo = createDataInfo((sensors1D.size()) * 2 + (sensors3D.size()) * 10 + 1);
        instance.setDataset(headInfo);

        int pos = 0;

        instance.setValue(pos++, activity.ordinal());

        for (int i = 0; i < sensors1D.size(); i++) {
            instance.setValue(pos++, sensors1D.get(i).getMean());
            instance.setValue(pos++, sensors1D.get(i).getSdX());
        }
        for (int i = 0; i < sensors3D.size(); i++) {
            instance.setValue(pos++, sensors3D.get(i).getMeanX());
            instance.setValue(pos++, sensors3D.get(i).getMeanY());
            instance.setValue(pos++, sensors3D.get(i).getMeanZ());
            instance.setValue(pos++, sensors3D.get(i).getMean());
            instance.setValue(pos++, sensors3D.get(i).getSdX());
            instance.setValue(pos++, sensors3D.get(i).getSdY());
            instance.setValue(pos++, sensors3D.get(i).getSdZ());
            instance.setValue(pos++, sensors3D.get(i).getCorrXY());
            instance.setValue(pos++, sensors3D.get(i).getCorrYZ());
            instance.setValue(pos++, sensors3D.get(i).getCorrXZ());

        }
        return instance;
    }

    // method that creates standard deviation
    private static boolean isBiasCorrected = true;

    public static double sd(double[] accXList, double accAxis1Mean, int numPoints) {
        double sd1 = DoubleStream.of(accXList).map(x -> Math.abs(x - accAxis1Mean)).reduce(0,
                (acc, current) -> acc + (current) * (current));
        return Math.sqrt(sd1 / (numPoints - (isBiasCorrected ? 1 : 0))); // same as in R and Apache Math3
    }

    public String activeSensors() {
        List<String> activeSensors = new ArrayList<>();
        for (int i = 0; i < sensors.length; i++) {
            if (activeSensor[i] != 0) {
                activeSensors.add(sensors[i]);
            }
        }
        return activeSensors.toString();
    }

    public static String activeSensors(int[] activeSensor) {
        List<String> activeSensors = new ArrayList<>();
        for (int i = 0; i < sensors.length; i++) {
            if (activeSensor[i] != 0) {
                activeSensors.add(sensors[i]);
            }
        }
        return activeSensors.toString();
    }

    @Override
    public Instances createDataInfo() {
        int countAttr = 1;// 21;
        for (int i = 0; i < getSensorDimension().length; i++) {
            countAttr += getSensorDimension()[i] * activeSensor[i];
        }
        return createDataInfo(countAttr);
    }

    // head of arff dataset
    private static Instances createDataInfo(int countAttr) {

        // Create dataset info
        List<String> activityList = new ArrayList<String>();
        for (ActivityType activityType : ActivityType.values()) {
            activityList.add(activityType.name());
        }

        List<Attribute> attributes = new ArrayList<Attribute>();

        attributes.add(new Attribute("class", activityList));
        for (int i = 0; i < (countAttr - 1); i++) {
            attributes.add(new Attribute("attr_" + i));
        }

        Instances dataInfo = new Instances("har", attributes, 0);
        dataInfo.setClassIndex(0);

        return dataInfo;
    }

    @Override
    public String getClassification(int index) {
        return ActivityType.values()[index].name();
    }

    public void setActiveSensors(boolean hand, boolean chest, boolean ankle) {
        int handI = hand ? 1 : 0;
        int chestI = hand ? 1 : 0;
        int ankleI = hand ? 1 : 0;
        activeSensor[E5DataHandler.HAND_ACC_POS] = handI;
        activeSensor[E5DataHandler.HAND_GYR_POS] = handI;
        activeSensor[E5DataHandler.HAND_MAG_POS] = handI;
        activeSensor[E5DataHandler.CHEST_ACC_POS] = chestI;
        activeSensor[E5DataHandler.CHEST_GYR_POS] = chestI;
        activeSensor[E5DataHandler.CHEST_MAG_POS] = chestI;
        activeSensor[E5DataHandler.ANKLE_ACC_POS] = ankleI;
        activeSensor[E5DataHandler.ANKLE_GYR_POS] = ankleI;
        activeSensor[E5DataHandler.ANKLE_MAG_POS] = ankleI;
    }

    public void setActiveSensors(int[] sensors) {
        if (sensors.length != sensorDimension.length) {
            throw new IndexOutOfBoundsException("The array of sensors must be of size " + sensorDimension.length
                    + "(the same as 'sensorDimension') but an array of size " + sensors.length + " was given.");
        }
        activeSensor = sensors;
    }

    public int[] getSensorDimension() {
        return sensorDimension;
    }

    public void setSensorDimension(int[] sensorDimension) {
        this.sensorDimension = sensorDimension;
    }

    @Override
    public int getNumClasses() {
        return ActivityType.values().length;
    }

	@Override
	public String getRealClass() {
		// TODO Auto-generated method stub
		return null;
	}
}
