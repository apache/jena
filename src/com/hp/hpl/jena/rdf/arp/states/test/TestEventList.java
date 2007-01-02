/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.states.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestEventList extends TestCase implements Test {

    final EventRecord record1, record2;

    public TestEventList(String nm, EventRecord r1, EventRecord r2) {
        super(nm);
        record1 = r1;
        record2 = r2;
    }

    private void check(String start, EventRecord first, EventRecord second) {
        Class f = TestData.toState(start);
        String ev[] = new String[first.startEvents.length
                + second.startEvents.length];
        System.arraycopy(first.startEvents, 0, ev, 0, first.startEvents.length);
        System.arraycopy(second.startEvents, 0, ev, first.startEvents.length,
                second.startEvents.length);
        EventList events = new EventList(ev);
        events.expectAnException(second.rsltState.equals("!"));
        events.expectAnError(second.rsltState.equals("?"));
        events.test(f);
        if (second.rsltState.equals("!"))
            assertTrue("expected an exception", events.testException);
        else if (second.rsltState.equals("?"))
            assertTrue("expected an error or warning", events.testFailure);
        else if (events.testFailure) 
            fail("parse had unexpected warning or error");
        else if (events.testException) 
            fail("parse had unexpected exception");
        else {
            assertEquals("end state", events.testResult.getClass(), TestData
                    .toState(second.rsltState));
            TestData.xmlHandler.check(second);
            TestData.testFrame.check(second);
        }
    }

    protected void runTest() {
        String state = record1 instanceof FullEventRecord ? ((FullEventRecord) record1).state
                : ((FullEventRecord) record2).state;
        check(state, record1, record2);
        // for (int i=0;i<record.moreCharacter.length;i++)
        // check(record.state, record, record.moreCharacter[i]);
    }

    static EventRecord dummyEvent = new EventRecord();
    static {
        dummyEvent.startEvents = new String[0];
    }

    public static Test create(String line, String[] fields) {
        FullEventRecord ev = new FullEventRecord(fields);
        if (ev.rsltState.length() == 1) {
            return new TestEventList(line, dummyEvent, ev);
        }
        TestSuite rslt = new TestSuite();
        rslt.setName(ev.state + " " + ev.toEventString());
        rslt.addTest(new TestEventList("$ " + ev.toResultString(), dummyEvent, ev));
        for (int i = 0; i < ev.moreCharacter.length; i++)
            rslt.addTest(new TestEventList(ev.moreCharacter[i]
                    .toEventString()
                    + " $ " + ev.moreCharacter[i].toResultString(), ev,
                    ev.moreCharacter[i]));
        return rslt;
    }

}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

