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


import org.junit.Assert;
import org.xml.sax.Attributes;

import com.hp.hpl.jena.rdfxml.xmlinput.impl.TaintImpl ;
import com.hp.hpl.jena.rdfxml.xmlinput.states.FrameI ;


class EventList implements Attributes, Cloneable {
    Event events[] = new Event[20];
    int size = 0;
    int pos = 0;
    boolean testException;
    boolean testFailure;
    FrameI testResult;
    boolean hasNext() {
        return pos < size;
    }
    Event next() {
        return events[pos++];
    }
    Event peek() {
        return events[pos];
    }

    Event last() {
        return events[size-1];
    }
    void clear() {
        size = 0;
        pos = 0;
    }
    void pop() {
        size--;
        events[size]=null;
    }
    void rewind() {
        pos = 0;
    }
    void skipAttrs() {
        pos += getLength();
    }
    void add(Event e) {
        events[size++] = e;
    }
    public EventList() {
    }
    public EventList(String[] ev) {
        for ( String anEv : ev )
        {
            add( TestData.short2Event.get( anEv ) );
        }
    }
    public EventList copy() {
        try {
        EventList rslt = (EventList)clone();
        rslt.events = rslt.events.clone();
        return rslt;
        }
        catch (CloneNotSupportedException e){
            return null;
        }
        
    }
    @Override
    public int getLength() {
        int sz = 0;
        while (pos+sz < size && events[pos+sz].isAttribute())
            sz++;
        return sz;
    }
    private QName q(int i) {
        return ((AttrEvent)events[pos+i]).q;
    }
    @Override
    public String getURI(int index) {
        return q(index).uri;
    }
    @Override
    public String getLocalName(int index) {
        return q(index).localName;
    }
    @Override
    public String getQName(int index) {
        return q(index).qName;
    }
    @Override
    public String getType(int index) {
        return null;
    }
    @Override
    public String getValue(int index) {
        return ((AttrEvent)events[pos+index]).value;
    }
    @Override
    public int getIndex(String uri, String localName) {
        return -1;
    }
    @Override
    public int getIndex(String qName) {
        return -1;
    }
    @Override
    public String getType(String uri, String localName) {
        return null;
    }
    @Override
    public String getType(String qName) {
        return null;
    }
    @Override
    public String getValue(String uri, String localName) {
        return null;
    }
    @Override
    public String getValue(String qName) {
        return null;
    }
    public void delete(int i) {
        System.arraycopy(events,i+1,events,i,size-1-i);
        size--;
        
    }
    boolean test(Class<? extends FrameI> cl) {
        try {
            testException = false;
            testFailure = true;
            FrameI frame = TestData.create(cl);
            TestData.xmlHandler.clear(failOnError);
            TestData.testFrame.clear();
            rewind();
            if (frame  == null)
                Assert.fail("Frame is null");
            frame.getXMLContext().getLang(new TaintImpl());
            while (hasNext()) {
                Event ev = next();
                frame = ev.apply(frame, this);
                skipAttrs();
                if (TestData.xmlHandler.wrong)
                    return false;
                if (frame == TestData.testFrame)
                    return false;
            }
            testResult = frame;
            testFailure = false;
            return true;
        }

        catch (RuntimeException e) {
            testException = true;
            if (rethrowException)
                throw  e;
            return false;
        } catch (Exception e) {
            testException = true;
            if (rethrowException)
                throw  new RuntimeException(e);
            e.printStackTrace();
            return false;
        }
    }
    boolean rethrowException;
    public void expectAnError(boolean b) {
       failOnError = !b;
        
    }
    boolean failOnError;
    public void expectAnException(boolean b) {
        rethrowException = !b;
    }

}
