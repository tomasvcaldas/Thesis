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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.yahoo.labs.samoa.instances.Instance;

import moa.core.InstanceExample;
import moa.streams.ArffFileStream;
import pt.up.fe.specs.contextwa.data.fetch.WindowedDataFetcher;

import pt.up.fe.specs.util.collections.pushingqueue.PushingQueue;

public class PamapDataFetcher extends WindowedDataFetcher {
    private static final String dataDir = "src/pamap2/";
    private static final String dataFile = dataDir + "pamap2.arff";
    private static final String userFileName = "pamap2_user";
    private static final String arffExt = ".arff";

    private ArffFileStream dataStream;
    private Integer numInstances = null;
    private int readInstances;
    private String file;
    private List<Integer> ignoredPersons = new ArrayList<>();
    // private int samplingJump;

    public static PamapDataFetcher fetchAll(int window, float factor) {
        return new PamapDataFetcher(dataFile, window, factor, data -> true, 0);
    }

    public static PamapDataFetcher fetchAll(int window, float factor, int samplingJump) {
        return new PamapDataFetcher(dataFile, window, factor, data -> true, samplingJump);
    }

    public static PamapDataFetcher fetchIgnore(int personToIgnore, int window, float factor) {
        return new PamapDataFetcher(dataFile, window, factor, data -> (((int) data.value(1)) != personToIgnore && ((int) data.value(1)) != 6), 0);
     //   return new PamapDataFetcher(dataFile, window, factor, data -> ((int) data.value(1)) != personToIgnore, 0);

    }

    public static PamapDataFetcher fetchIgnore(int personToIgnore, int window, float factor, int samplingJump) {
        return new PamapDataFetcher(dataFile, window, factor, data -> ((int) data.value(1)) != personToIgnore,
                samplingJump);
    }

    public static PamapDataFetcher fetchOnly(int personToUse, int window, float factor) {
        return new PamapDataFetcher(getPersonFileName(personToUse), window, factor,
                data -> ((int) data.value(1)) == personToUse, 0);
    }

    public static PamapDataFetcher fetchOnly(int personToUse, int window, float factor, int samplingJump) {
        return new PamapDataFetcher(getPersonFileName(personToUse), window, factor,
                data -> ((int) data.value(1)) == personToUse, samplingJump);
    }

    public static String getPersonFileName(int personToUse) {
        return dataDir + userFileName + personToUse + arffExt;
    }

    private PamapDataFetcher(String fileName, int windowSize, float overlap, Predicate<Instance> condition,
            int samplingJump) {
        super(windowSize, overlap, condition);
        this.file = fileName;
        // this.samplingJump = samplingJump;
        dataStream = new ArffFileStream(fileName, 0);
    }

    public Set<Integer> getUsers() {
        restart();
        Set<Integer> data = new HashSet<>();

        while (dataStream.hasMoreInstances()) {
            Instance inst = dataStream.nextInstance().getData();

            double value = inst.value(1);
            data.add((int) value);
        }
        if (numInstances == null) {
            numInstances = data.size();
        }
        return data;
    }

    // public int getNumInstances() {
    // if (numInstances == null) {
    // recalculateNumInstances();
    // }
    // return numInstances;
    // }

    public long estimatedRemainingInstances() {
        return this.dataStream.estimatedRemainingInstances();
    }

    @Override
    public void restart() {
        super.restart();
        readInstances = 0;
        dataStream.prepareForUse();
    }

    private void recalculateNumInstances() {
        restart();
        countWithBufferedReader();
        // countWithDataStream();
    }

    public void countWithDataStream() {
        numInstances = 0;
        while (dataStream.hasMoreInstances()) {
            dataStream.nextInstance();
            numInstances++;
        }
    }

    private void countWithBufferedReader() {
        try (BufferedReader reader = new BufferedReader(new FileReader(this.file));) {
            // ignore until a
            numInstances = 0;
            String readLine = null;
            while ((readLine = reader.readLine()) != null) {
                if (readLine.contains("@data")) {
                    break;
                }
            }
            while ((readLine = reader.readLine()) != null) {
                if (!readLine.isEmpty()) {
                    numInstances++;
                }
            }
        } catch (Exception e) {
            new RuntimeException("Could not count the number of samples.", e);
        }
    }

    /**
     * Must be called prior to {@link WindowedPAMAP2Fetcher#next()}
     * 
     * @return
     */
    @Override
    public boolean hasNext(PushingQueue<Instance> window, int dataSize, Predicate<Instance> condition) {
        return addSamples(window, dataSize, condition);
    }

    private boolean addSamples(PushingQueue<Instance> window, int numSamples, Predicate<Instance> condition) {
        // int sampledInstances = 0;
        while (dataStream.hasMoreInstances() && numSamples > 0) {
            InstanceExample nextInstance = dataStream.nextInstance();
            // if (samplingJump != 0 && sampledInstances == samplingJump) {
            // sampledInstances = 0;
            // continue;
            // }
            // sampledInstances++;
            Instance data = nextInstance.getData();
            if (condition.test(data)) {
                window.insertElement(data);
                numSamples--;
                readInstances++;
            }
        }
        return numSamples == 0;
    }

    // @Override
    // public void nextWindow(PushingQueue<Instance> window, int numSamples, Predicate<Instance> condition {
    // // already filtered in "addSamples" // }

    public long getNumInstancesRead() {
        return readInstances;
    }

    public void addIgnoredPerson(int person) {
        this.ignoredPersons.add(person);
    }

    @Override
    public long getNumberSamplesRead() {
        return readInstances;
    }

    @Override
    public long getTotalNumberOfSamples() {
        if (numInstances == null) {
            recalculateNumInstances();
        }
        return numInstances;
    }

    @Override
    public void close() {

    }

}
