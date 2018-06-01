/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.tdb2.loader.main;

import org.apache.jena.tdb2.store.NodeId;

/** 
 * A {@code LoaderPlan}
 * <p>
 * For triples and for quads there is a first phase to parse the input, 
 * convert to tuples of {@link NodeId NodeIds}, including allocating the ids,
 * and do at least one tuple index for each of triples quads to capture the input.
 * <p>   
 * After that, a number of phases builds the other indexes. 
 * <p>
 * The {@code mulithreadedInput} flag indicates whether the first phase is
 * done in parallel (threads for parer, node table building and primary indexes)
 * or as a single threaded process.
 */
public class LoaderPlan {
    private final InputStage dataInput;
    private final String[] loadGroup3;
    private final String[] loadGroup4;
    private final String[][] secondaryGroups3;
    private final String[][] secondaryGroups4;
    
    public LoaderPlan(InputStage dataInput,
                      String[] loadGroup3, String[] loadGroup4,
                      String[][] secondaryGroups3, String[][] secondaryGroups4) {
        this.dataInput = dataInput;
        this.loadGroup3 = loadGroup3;
        this.loadGroup4 = loadGroup4;
        this.secondaryGroups3 = secondaryGroups3;
        this.secondaryGroups4 = secondaryGroups4;
    }
    public InputStage dataInputType()       { return dataInput; }
    public String[] primaryLoad3()          { return loadGroup3; }
    public String[] primaryLoad4()          { return loadGroup4; }
    public String[][] secondaryIndex3()     { return secondaryGroups3; }
    public String[][] secondaryIndex4()     { return secondaryGroups4; }
}