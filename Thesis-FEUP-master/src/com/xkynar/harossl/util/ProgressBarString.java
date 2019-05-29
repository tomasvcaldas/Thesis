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

package com.xkynar.harossl.util;

import org.apache.commons.lang3.StringUtils;

public class ProgressBarString {

    private static int BAR_STEP = 2;
    private static final int BAR_LENGTH = 100 / BAR_STEP;

    /**
     * prints the bar if target percentage is reached and returns new target percentage
     * 
     * @param targetPercentage
     * @param current
     * @param total
     * @return
     */
    public static int printBar(int targetPercentage, long current, long total) {
        int progress = (int) ((current / (float) (total)) * 100);
        // System.out.println(targetPercentage + "vs " + progress + ": " + current + "/" + total);
        if (progress > targetPercentage) {
            printBar(targetPercentage / BAR_STEP, BAR_LENGTH);
            targetPercentage += BAR_STEP;
        }
        return targetPercentage;
    }

    public static void printBar(int numBars, int barLength) {
        String bar = "|";
        for (int i = 0; i < barLength; i++) {
            if (i < numBars) {
                bar += "=";
            } else {
                bar += " ";
            }
        }
        bar += "|\r";
        System.out.print(bar);
    }

    public static void printFullBar() {
        System.out.print("|" + StringUtils.repeat("=", BAR_LENGTH) + "|\n");
    }
}
