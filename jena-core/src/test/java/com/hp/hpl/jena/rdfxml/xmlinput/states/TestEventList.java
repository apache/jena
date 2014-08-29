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

import com.hp.hpl.jena.rdfxml.xmlinput.states.FrameI ;

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
        Class< ? extends FrameI> f = TestData.toState(start);
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

    @Override
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
