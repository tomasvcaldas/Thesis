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

import com.yahoo.labs.samoa.instances.Instance;

/**
 * 
 * @author tdrc
 *
 */
public interface DataFetcher {
    /**
     * Get the next data chunk
     * 
     * @return
     */
    public List<Instance> next();

    /**
     * Verify if it is possible to fetch another chunk. Must be called before {@link DataFetcher#next()}.
     * 
     * @return
     */
    public boolean hasNext();

    /**
     * Restart data (e.g. start reading a CSV file from the beginning or re-attach sensors)
     */
    public abstract void restart();

    /**
     * Return the number of samples read so far
     * 
     * @return
     */
    public abstract long getNumberSamplesRead();

    /**
     * The total number of samples that can be read, or an estimate, or -1 if unknown or infinite
     * 
     * @return
     */
    public abstract long getTotalNumberOfSamples();

    public abstract void close();
}
