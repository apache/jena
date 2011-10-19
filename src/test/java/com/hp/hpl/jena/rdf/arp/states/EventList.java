/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.states;


import junit.framework.Assert;

import org.xml.sax.Attributes;

import com.hp.hpl.jena.rdf.arp.impl.TaintImpl;
import com.hp.hpl.jena.rdf.arp.states.FrameI;


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
        for (int i=0;i<ev.length;i++)
            add(TestData.short2Event.get(ev[i]));
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


/*
 *  (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
