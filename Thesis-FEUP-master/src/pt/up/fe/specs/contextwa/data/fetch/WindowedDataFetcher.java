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

package pt.up.fe.specs.contextwa.data.fetch;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.yahoo.labs.samoa.instances.Instance;

import pt.up.fe.specs.util.collections.pushingqueue.ArrayPushingQueue;
import pt.up.fe.specs.util.collections.pushingqueue.PushingQueue;

/**
 * Fetches data with a specified window size, maintaining between windows previous data based on an overlap factor
 * 
 * @author tdrc
 *
 */
public abstract class WindowedDataFetcher extends ConditionalDataFetcher {

    private int windowSize;
    private float overlap;
    private int dataSize;
    private PushingQueue<Instance> window;

    public WindowedDataFetcher(int window, float overlap) {
        super();
        init(window, overlap);
    }

    public WindowedDataFetcher(int window, float overlap, Predicate<Instance> condition) {
        super(condition);
        init(window, overlap);
    }

    public void init(int window, float overlap) {
        this.windowSize = (window);
        this.overlap = (overlap);
        this.dataSize = ((int) (window * (1 - overlap)));
        this.window = new ArrayPushingQueue<>(windowSize);
    }

    @Override
    public void restart() {

        window = new ArrayPushingQueue<>(windowSize);
    }

    /**
     * Get a window of values with the specified dataSize. See {@link ConditionalDataFetcher#nextIf(Predicate)}. If you
     * populate the window with the {@link WindowedDataFetcher#hasNext(PushingQueue, int, Predicate)} you don't need to
     * do anything here.
     * 
     * @param dataSize
     * @param condition
     * @return
     */
    protected void nextWindow(PushingQueue<Instance> window, int dataSize, Predicate<Instance> condition) {
    }

    @Override
    protected final List<Instance> nextIf(Predicate<Instance> condition) {
        nextWindow(window, getDataSize(), condition);
        return getWindowAsList();
    }

    /**
     * Verifies if you can retrieve data with the specified size. See {@link ConditionalDataFetcher#hasNext(Predicate)}.
     * You may add the new data in the window provided as first argument to pre-populate before calling the
     * {@link ConditionalDataFetcher#nextWindow()} method.
     * 
     * @param dataSize
     * @param condition
     * @return
     */
    protected abstract boolean hasNext(PushingQueue<Instance> window, int dataSize, Predicate<Instance> condition);

    @Override
    protected boolean hasNext(Predicate<Instance> condition) {

        return hasNext(window, window.currentSize() == 0 ? windowSize : dataSize, condition);
    }

    public void addToWindow(List<Instance> nextWindow) {
        nextWindow.forEach(window::insertElement);
    }

    public List<Instance> getWindowAsList() {
        return window.stream().collect(Collectors.toList());
    }

    public int getWindowSize() {
        return windowSize;
    }

    public float getFactor() {
        return overlap;
    }

    public int getDataSize() {
        return dataSize;
    }
}
