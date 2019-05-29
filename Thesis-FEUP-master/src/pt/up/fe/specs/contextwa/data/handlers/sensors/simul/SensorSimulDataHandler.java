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

package pt.up.fe.specs.contextwa.data.handlers.sensors.simul;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;

import com.xkynar.harossl.util.ActivityType;
import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

import moa.core.Utils;
import pt.up.fe.specs.contextwa.data.handlers.DataHandler;

/**
 * Data handler simulator that uses {@link Sensor1D} and {@link Sensor3D} to simulate sensors with one or three axes,
 * respectively.
 * 
 * @author tdrc
 *
 */
public abstract class SensorSimulDataHandler implements DataHandler {
    /**
     * Defines if the sensor of position X is a 1D or 3D sensor (use 1 and 3 to define)
     */
    private int[] sensorDimension;
    private Instances headInfo;
    public int[] activeSensors;
    public String[] sensorsNames;
    private String[] classes;
    private int numClasses;
    private List<String> classesList;
    private String RealClass;

    public SensorSimulDataHandler(int[] sensorDimension, int[] activeSensors, String[] sensorsNames,
            String[] classes) {
        this.sensorDimension = sensorDimension;
        this.activeSensors = activeSensors;
        this.sensorsNames = sensorsNames;
        this.classes = classes;
        this.numClasses = classes.length;
    }

    @Override
    public Instance extractFeatures(List<Instance> dataPoints) {
    	

        int numPoints = dataPoints.size();
        ArrayList<Sensor1D> sensors1D = new ArrayList<>();
        ArrayList<Sensor3D> sensors3D = new ArrayList<>();

        int[] activityCounter = new int[numClasses];
        int instanceValuePos = 2;
        for (int i = 0; i < getSensorDimension().length; i++) {
            int numValues = getSensorDimension()[i];
            if (activeSensors[i] == 0) {
                instanceValuePos += numValues;
                continue;
            }

            if (numValues == 1) {
                Sensor1D s1D = new Sensor1D(numPoints);
                double[] vectorX = new double[numPoints];
                for (int j = 0; j < numPoints; j++) {
                    Instance instance = dataPoints.get(j);
                    int classValue = searchIndex(this.classesList,dataPoints.get(j));
                    RealClass = ActivityType.values()[(int) instance.classValue()].toString();
                    //int classValue = (int) instance.classValue();
                    activityCounter[classValue]++;
                }
                s1D.setCoordX(vectorX);

                sensors1D.add(s1D);
            }
            if (numValues == 3) {
                Sensor3D s3D = new Sensor3D(numPoints);
                double[] vectorX = new double[numPoints];
                double[] vectorY = new double[numPoints];
                double[] vectorZ = new double[numPoints];
                for (int j = 0; j < numPoints; j++) {
                	int classValue = 0;
                	//if(this.classesList != null)
                	 classValue = searchIndex(getClassesList(),dataPoints.get(j));
                     RealClass = ActivityType.values()[(int) dataPoints.get(j).classValue()].toString();

                	if(classValue == -1)
                		return null;
                	
                    activityCounter[classValue]++;
                    vectorX[j] = dataPoints.get(j).value(instanceValuePos);
                    vectorY[j] = dataPoints.get(j).value(instanceValuePos + 1);
                    vectorZ[j] = dataPoints.get(j).value(instanceValuePos + 2);
                }
                s3D.setCoordX(vectorX);
                s3D.setCoordY(vectorY);
                s3D.setCoordZ(vectorZ);
                sensors3D.add(s3D);
            }
            instanceValuePos += numValues;
        }

        // ActivityType activity = ActivityType.values()[Utils.maxIndex(activityCounter)];
        int activityOrdinal = Utils.maxIndex(activityCounter);
        Instance instance = new DenseInstance((sensors1D.size()) * 2 + (sensors3D.size()) * 10 + 1);
        headInfo = createDataInfo((sensors1D.size()) * 2 + (sensors3D.size()) * 10 + 1);
        instance.setDataset(headInfo);
        

        int pos = 0;

        // int activityOrdinal = activity.ordinal();
        instance.setValue(pos++, activityOrdinal);

        for (int i = 0; i < sensors1D.size(); i++) {
            pos += addFeatures(instance, pos, sensors1D.get(i));
        }
        for (int i = 0; i < sensors3D.size(); i++) {
            pos += addFeatures(instance, pos, sensors3D.get(i));
        }
        return instance;
    }

    private int searchIndex(List<String> classesList2, Instance instance) {
		for(int i = 0;i < classesList2.size();i++)
		{
			String temp = instance.toString();
			String temp2[] = temp.split(",");
			String vec[] = classesList2.get(i).split("-");
			
			for(int j = 0;j < vec.length; j++) {
			//if(temp.contains(vec[j])) {
			//	return i;
			//}
				
				if(vec[j].equals(temp2[0]))
					return i;
			}
		}
		return -1;
	}
    
    public String getRealClass() {
    	return RealClass;
    }

	/**
     * 
     * @param sensors1D
     * @param instance
     * @param pos
     *            first position to set the
     * @param i
     * @return the number of features added to the instance
     */
    public abstract int addFeatures(Instance instance, int pos, Sensor1D sensor1d);

    /**
     * 
     * @param sensors1D
     * @param instance
     * @param pos
     *            first position to set the
     * @param i
     * @return the number of features added to the instance
     */
    public abstract int addFeatures(Instance instance, int pos, Sensor3D sensor3d);

    public String activeSensors() {
        List<String> activeSensorsList = new ArrayList<>();
        for (int i = 0; i < sensorsNames.length; i++) {
            if (activeSensors[i] != 0) {
                activeSensorsList.add(sensorsNames[i]);
            }
        }
        return activeSensorsList.toString();
    }

    // public static String activeSensors(int[] activeSensor) {
    // List<String> activeSensors = new ArrayList<>();
    // for (int i = 0; i < sensors.length; i++) {
    // if (activeSensor[i] != 0) {
    // activeSensors.add(sensors[i]);
    // }
    // }
    // return activeSensors.toString();
    // }

    @Override
    public Instances createDataInfo() {
        int countAttr = 1;// 21;
        for (int i = 0; i < getSensorDimension().length; i++) {
            countAttr += getSensorDimension()[i] * activeSensors[i];
        }
        return createDataInfo(countAttr);
    }

    // head of arff dataset
    private Instances createDataInfo(int countAttr) {

        // Create dataset info

        List<Attribute> attributes = new ArrayList<Attribute>();

        attributes.add(new Attribute("class", getClassesList()));
        for (int i = 0; i < (countAttr - 1); i++) {
            attributes.add(new Attribute("attr_" + i));
        }

        Instances dataInfo = new Instances("har", attributes, 0);
        dataInfo.setClassIndex(0);

        return dataInfo;
    }

    private List<String> getClassesList() {
        if (classesList == null) {
            classesList = Arrays.asList(this.classes);
        }
        return classesList;
    }

    @Override
    public String getClassification(int index) {
        return classes[index];
    }

    public int[] getActiveSensors() {
        return activeSensors;
    }

    public void setActiveSensors(int[] sensors) {
        if (sensors.length != sensorDimension.length) {
            throw new IndexOutOfBoundsException("The array of sensors must be of size " + sensorDimension.length
                    + "(the same as 'sensorDimension') but an array of size " + sensors.length + " was given.");
        }
        activeSensors = sensors;
    }

    public int[] getSensorDimension() {
        return sensorDimension;
    }

    public void setSensorDimension(int[] sensorDimension) {
        this.sensorDimension = sensorDimension;
    }

    // method that creates standard deviation
    @Deprecated
    private static boolean isBiasCorrected = true;

    @Deprecated
    public static double sd(double[] accXList, double accAxis1Mean, int numPoints) {
        double sd1 = DoubleStream.of(accXList).map(x -> Math.abs(x - accAxis1Mean)).reduce(0,
                (acc, current) -> acc + (current) * (current));
        return Math.sqrt(sd1 / (numPoints - (isBiasCorrected ? 1 : 0))); // same as in R and Apache Math3
    }

    @Override
    public int getNumClasses() {
        return numClasses;
    }
}
