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

package com.hp.hpl.jena.reasoner.sparqlinrules.test;

import java.util.ArrayList;
import java.util.HashMap;


public class ResultRow extends Result {
        ArrayList<ResultField> result = new ArrayList();
        HashMap<String, Integer> resultMap = new HashMap<>();
        
        public ResultRow () {

        }
        
        public ResultRow (ResultField [] resultField) {
         }
        
        public ResultRow (ArrayList<ResultField> resultField) {
            for(ResultField rf : resultField) {
                addField(rf);
            }
        }

        public void addField(ResultField rf) {
            if(resultMap.containsKey(rf.var)) {
                result.remove(resultMap.get(rf.var));
            }
            result.add(rf);
            resultMap.put(rf.var, result.size()-1);
        }
        
        public void print() {
            for(ResultField rf : result) {
                System.out.print(rf.var + " -> " +rf.result+"; ");
            }
        }
        

        public boolean sameResult(Result presult) {
            ArrayList<ResultField> r1 = new ArrayList<>();
            r1.addAll(result);
            ArrayList<ResultField> r2 = new ArrayList<>();
            r2.addAll(((ResultRow) presult).result);
            
            return CompareResults.sameResult(r1, r2);
        }
        
    }
