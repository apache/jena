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

import com.hp.hpl.jena.graph.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class ResultRow extends Result{
    HashMap<String, Node> result;
    
    public ResultRow() {
        result = new HashMap<String, Node>();
    }
    
    public ResultRow(HashMap<String, Node> result) {
        setResult(result);
    }
    
    public void setResult(HashMap<String, Node> result) {
        this.result = result;
    }   
    
    public void addResult(String var, Node n) {
        result.put(var, n);
    }
    
    public void addResult(HashMap<String, Node> ln) {
        if(ln!= null) {
            result.putAll(ln);
        }
    }
    
    public void addResult(ResultRow ln) {
        if(ln!= null) {
            result.putAll(ln.getResult());
        }
    }
    public int size() {
        return result.size();
    }
    
    public Node getInstanceValue(String fieldName) {
        Node retN = null;
        
        if(result.containsKey(fieldName)) {
            retN = result.get(fieldName);
        }

        return retN;
    }
    
    
    public HashMap<String, Node> getResult() {
        return result;
    }
        
    public String toString() {
        String msg = "";
        
        for(String s : result.keySet()) {
            msg += s + " -> "+result.get(s)+"; ";
        }
        
        return msg;
    }
    
    
    public Set<String> getAllFieldsName() {
        return result.keySet();
    }

    public boolean existField(String field){
        return result.containsKey(field);
    }

    public boolean sameResult(ResultRow r) {
        boolean retV = false;
        if(result.size() == r.getResult().size()) {
            Set<String> fields = r.getAllFieldsName();
            boolean allEq = true;
            for(String field : fields) {
                if(!getInstanceValue(field).matches(r.getInstanceValue(field))) {
                    allEq = false;
                    break;
                }
            }
            retV = allEq;
        }
        
        return retV;
    }

    @Override
    public boolean sameResult(Result r) {
        return sameResult((ResultRow) r);
    }
    
}
