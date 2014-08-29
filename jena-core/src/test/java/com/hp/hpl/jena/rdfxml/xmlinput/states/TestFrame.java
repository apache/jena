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


import org.junit.Assert ;

import com.hp.hpl.jena.rdfxml.xmlinput.impl.ANode ;
import com.hp.hpl.jena.rdfxml.xmlinput.impl.AbsXMLContext ;
import com.hp.hpl.jena.rdfxml.xmlinput.impl.XMLHandler ;
import com.hp.hpl.jena.rdfxml.xmlinput.states.AbsXMLLiteral ;
import com.hp.hpl.jena.rdfxml.xmlinput.states.HasSubjectFrameI ;
import com.hp.hpl.jena.rdfxml.xmlinput.states.WantsObjectFrameI ;

class TestFrame extends AbsXMLLiteral implements WantsObjectFrameI,
        HasSubjectFrameI {

    public TestFrame(XMLHandler h, AbsXMLContext x) {
        super(h,x);
    }
    
    void clear() {
        rslt.setLength(0);
        oCount = 0;
        pCount = 0;
        rCount = 0;
    }

    
    int oCount;
    int pCount;
    int rCount;
    @Override
    public void endElement() {
    }

    @Override
    public void theObject(ANode a) {
        oCount++;

    }

    @Override
    public void aPredAndObj(ANode p, ANode o) {
        pCount++;
    }

    public String info() {
        return (rslt.length()==0?"":("x"+rslt.length()+" "))+
           (oCount==0?"":"O"+oCount+" ")+
           (pCount==0?"":"P"+pCount+" ")+
           (rCount==0?"":"R"+rCount+" ");
           
    }

    public void check(EventRecord r) {
        r.initCounts();
        Assert.assertEquals("object looking for s,p count",r.objects,oCount);
        Assert.assertEquals("p,o looking for s count",r.preds,pCount);
        Assert.assertEquals("reification count",r.reify,rCount);   
    }

    @Override
    public void makeSubjectReificationWith(ANode r) {
        rCount++;
    }

}
