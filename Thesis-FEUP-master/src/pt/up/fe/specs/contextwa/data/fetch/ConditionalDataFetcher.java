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

import com.yahoo.labs.samoa.instances.Instance;

/**
 * Fetches data with a provided condition
 * 
 * @author tdrc
 *
 */
public abstract class ConditionalDataFetcher implements DataFetcher {

    private Predicate<Instance> condition;

    /**
     * New data fetcher without condition
     * 
     * @param window
     * @param factor
     */
    public ConditionalDataFetcher() {
        this(i -> true);
    }

    /**
     * New data fetcher with a specified instance condition
     * 
     * @param window
     * @param factor
     */
    public ConditionalDataFetcher(Predicate<Instance> condition) {
        this.setCondition(condition);
    }

    /**
     * Get the next window with the specified condition
     * 
     * @return
     */
    @Override
    public final List<Instance> next() {
        return nextIf(condition);
    }

    /**
     * Called by {@link ConditionalDataFetcher#nextWindow()}
     * 
     * @return
     */
    protected abstract List<Instance> nextIf(Predicate<Instance> condition);

    @Override
    public final boolean hasNext() {
        return hasNext(condition);
    }

    @Override
    public abstract void restart();

    /**
     * Called by {@link ConditionalDataFetcher#hasNext()}
     * 
     * @return
     */
    protected abstract boolean hasNext(Predicate<Instance> condition);

    /**
     * Can be used to filter the instances with a given condition
     * 
     * @return
     */
    public Predicate<Instance> getCondition() {
        return condition;
    }

    public void setCondition(Predicate<Instance> condition) {
        this.condition = condition;
    }

}
