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


package com.hp.hpl.jena.reasoner.rulesys.impl;

import java.util.ArrayList;


public class Answer_SparqlInRules {
    boolean answer;
    ResultList result = null;
    
    public Answer_SparqlInRules() {
        
    }
    
    public Answer_SparqlInRules(boolean answer) {
        this.answer = answer;
    }

    public void setAnswer(boolean answer) {
        this.answer = answer;
    }
    
    public void setAnswerTrue() {
        answer = true;
    }
    
    public void setAnswerFalse() {
        answer = false;
    }
    
    public void setResultList(ResultList result){
        this.result = result;
    }
    
    public ResultList getResultList(){
        return result;
    }
    
    public boolean getAnswer() {
        return answer;
    }
}
