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

package com.hp.hpl.jena.rdfxml.xmlinput.states;


import java.util.HashMap ;

import org.junit.Assert ;

import com.hp.hpl.jena.rdfxml.xmlinput.impl.ANode ;
import com.hp.hpl.jena.rdfxml.xmlinput.impl.Taint ;
import com.hp.hpl.jena.rdfxml.xmlinput.impl.XMLHandler ;

class TestHandler extends XMLHandler {
    public void wrong(String msg) {
        wrong = true;
        if (failOnWarning)
            Assert.fail("unexpected warning: "+msg);
    }

    @Override
    public void warning(Taint taintMe,int i, String s) {
        if (i<100)
            return;
        wrong = true;
        if (failOnWarning)
            Assert.fail("unexpected warning: "+s);
    }
    @Override
    public void endLocalScope(ANode v) {
        scope ++;
    }
    @Override
    public void triple(ANode s, ANode p, ANode o) {
        triples++;
    }
    boolean wrong;
    int triples;
    int scope;
    boolean failOnWarning;
    public void clear(boolean failOnWarning_) {
        wrong = false;
        triples = 0;
        scope = 0;
        this.failOnWarning = failOnWarning_;
        idsUsed = new HashMap<>();
        idsUsedCount = 0;
    }

    public String info() {
        return wrong?"?":
            (
            (triples==0?"":("T"+triples))
            +
            (scope==0?"":(" E"+scope)) );
    }

    public void check(EventRecord r) {
        r.initCounts();
        Assert.assertEquals("triple count",r.triples,triples);
        Assert.assertEquals("end bnode scope count",r.scope,scope);
        
    }
}
