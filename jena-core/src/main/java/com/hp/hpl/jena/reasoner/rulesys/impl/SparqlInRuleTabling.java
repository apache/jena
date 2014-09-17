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


public class SparqlInRuleTabling {
    ArrayList<Node> subject = new ArrayList<Node>();
    ArrayList<Node> predicate = new ArrayList<Node>();
    ArrayList<Node> object = new ArrayList<Node>();

    class KeyTab {
        int subject;
        int predicate;
        int object;
        
        public KeyTab (int subject, int predicate, int object) {
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
        }
        
        public boolean equals(KeyTab kt) {
            return this.subject == kt.subject && this.predicate == kt.predicate && this.object == kt.object;
        }
    }
    
    HashMap<KeyTab, ResultList> values01 = new HashMap<KeyTab, ResultList>();
    HashMap<String, ResultList> values02 = new HashMap<String, ResultList>();
    
    HashMap<KeyTab, ResultList> values11 = new HashMap<KeyTab, ResultList>();
    HashMap<String, ResultList> values12 = new HashMap<String, ResultList>();
    
    public SparqlInRuleTabling() {
        
    }
    
    
    public void  insertValue(Node subject, Node predicate, Node object, ResultList rl1, ResultList rl2) {
        KeyTab keyTab = getKeyTab(subject, predicate, object);
        values01.put(keyTab, rl1);
        values11.put(keyTab, rl2);
    }
    
    public ResultList getValue0(Node subject, Node predicate, Node object) {
        KeyTab keyTab = getKeyTab(subject, predicate, object);
        if(values01.containsKey(keyTab)) {
            return values01.get(keyTab);
        }
        else {
            return null;
        }
    }

    public ResultList getValue1(Node subject, Node predicate, Node object) {
        KeyTab keyTab = getKeyTab(subject, predicate, object);
        if(values11.containsKey(keyTab)) {
            return values11.get(keyTab);
        }
        else {
            return null;
        }
    }
        
    public void insertValue(String SparqlCmd, ResultList rl1, ResultList rl2) {
        values02.put(SparqlCmd, rl1);
        values12.put(SparqlCmd, rl2);
    }
    
    public ResultList getValue0(String SparqlCmd) {
        if(values02.containsKey(SparqlCmd)) {
            return values02.get(SparqlCmd);
        }
        else {
            return null;
        }
    }
    
    public ResultList getValue1(String SparqlCmd) {
        if(values12.containsKey(SparqlCmd)) {
            return values12.get(SparqlCmd);
        }
        else {
            return null;
        }
    }
        
    private KeyTab getKeyTab(Node subject, Node predicate, Node object) {
        int subj = this.subject.indexOf(subject);
        int pred = this.predicate.indexOf(predicate);
        int obj = this.object.indexOf(object);
        
        return new KeyTab(subj, pred, obj);
    }
}
