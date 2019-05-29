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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class PamapOptions {

    protected static final boolean REPORT_PREDICTIONS = true;
    protected static String RESULTS_DIR = System.getProperty("user.home") + "/results";
    // protected static int SAMPLING_JUMP = 2;
    protected static int kValue = 3;
    protected static int WINDOWSIZE = 400;
    protected static float OVERLAPFACTOR = 0.3f;
    protected static List<Integer> USERS_LIST = new ArrayList<>();
    protected static boolean[] ENSEMBLE = { false, true, true };
    protected static boolean[] SENSORS = { true, true, true };
    protected File outputDIR = new File(RESULTS_DIR);

    public static void processArguments(String[] args) {
        if (args.length == 6) {
            try {
                WINDOWSIZE = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Argument " + args[0] + " must be an integer.");
                System.exit(1);
            }
            try {
                OVERLAPFACTOR = Float.parseFloat(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Argument " + args[1] + " must be a float.");
                System.exit(1);
            }

            String ensembleMode = args[2];
            if (ensembleMode.length() != 3) {
                System.err.println("Argument " + ensembleMode
                        + " must be a 0|1 String of three elements:kNN,naive bayes, hoeffding tree");
                System.exit(-1);
            }
            char kNN = ensembleMode.charAt(0);
            char naive = ensembleMode.charAt(1);
            char hoef = ensembleMode.charAt(2);
            ENSEMBLE[0] = kNN != '0';
            ENSEMBLE[1] = naive != '0';
            ENSEMBLE[2] = hoef != '0';

            String sensorsMode = args[3];
            if (sensorsMode.length() != 3) {
                System.err
                        .println("Argument" + sensorsMode
                                + " must be a 0|1 String of three elements: hand, chest, ankle");
                System.exit(-1);
            }
            SENSORS[0] = sensorsMode.charAt(0) != '0';
            SENSORS[1] = sensorsMode.charAt(1) != '0';
            SENSORS[2] = sensorsMode.charAt(2) != '0';

            RESULTS_DIR = args[4];

            try {
                String[] split = args[5].split(",");
                USERS_LIST = Arrays.stream(split).map(s -> Integer.parseInt(s)).collect(Collectors.toList());

            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[5] + " must be numbers separated by commas");
                System.exit(1);
            }
        } else {
            System.out.println("--- Instructions --- " + "\n5 parameters are necessary:\n"
                    + "\n1) Window Size"
                    + "\n2) Window Overlap"
                    + "\n3) Active Classifiers (0|1 String of three elements:kNN,naive bayes, hoeffding tree)"
                    + "\n4) Active Sensors (0|1 String of three elements:hand, chest, anklee)"
                    + "\n5) Output Directory"
                    + "\n6) Users to test (numbers separated by a comma)");
            System.exit(1);
        }
    }

    public PamapOptions() {
        super();
    }

    public void deleteFolder() throws IOException {
        if (!outputDIR.exists()) {
            outputDIR.mkdir();
        } else {
            outputDIR.delete();
            if (!outputDIR.exists()) {
                outputDIR.mkdir();
            }
        }
    }

}