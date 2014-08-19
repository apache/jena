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


public class ResultField extends Result{
        String var;
        String result;
        
        public ResultField(String pvar, String presult) {
            var = pvar;
            result = presult;
        }
        
        public boolean sameResult(String presult) {
            String vresult = presult.contains("^^") ? presult.substring(0, presult.indexOf("^^")): presult;
            
            return vresult.compareTo(result)==0;
        }
        
        public boolean sameResult(Result presult) {
            return sameResult(((ResultField) presult).result);
        }
        
        public boolean sameResult(String pvar, String presult) {
            return pvar.compareTo(var) == 0 && sameResult(presult);
        }
        
    }
