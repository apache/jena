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

package org.apache.jena.tdb1.base.record;

import static org.apache.jena.tdb1.base.record.RecordLib.intToRecord;
import static org.apache.jena.tdb1.base.record.RecordLib.recordToInt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestRecord
{
    static final public int RecLen = 4 ;
    
    @Test public void int1()
    {
        Record r = intToRecord(1234, 4) ;
        int v = recordToInt(r) ;
        assertEquals(v , 1234) ;
    }
    
    @Test public void int2()
    {
        // Negative numbers only work for length 4.
        Record r = intToRecord(-99, 4) ;
        int v = recordToInt(r) ;
        assertEquals(v , -99) ;
    }
    
    @Test public void record1()
    {
        Record r1 = intToRecord(1, RecLen) ;
        Record r2 = intToRecord(1, RecLen) ;
        assertTrue(Record.keyEQ(r1,r2)) ;
        assertTrue(Record.keyGE(r1,r2)) ;
        assertTrue(Record.keyLE(r1,r2)) ;
        assertFalse(Record.keyLT(r1,r2)) ;
        assertFalse(Record.keyGT(r1,r2)) ;
    }
    
    @Test public void record2()
    {
        Record r1 = intToRecord(1000, RecLen) ;
        Record r2 = intToRecord(2222, RecLen) ;
        assertFalse(Record.keyEQ(r1,r2)) ;
        assertFalse(Record.keyGE(r1,r2)) ;
        assertTrue(Record.keyLE(r1,r2)) ;
        assertTrue(Record.keyLT(r1,r2)) ;
        assertFalse(Record.keyGT(r1,r2)) ;
    }

    @Test public void record3()
    {
        Record r1 = intToRecord(1000, RecLen)  ;
        Record r2 = intToRecord(0, RecLen) ;
        assertFalse(Record.keyEQ(r1,r2)) ;
        assertTrue(Record.keyGE(r1,r2)) ;
        assertFalse(Record.keyLE(r1,r2)) ;
        assertFalse(Record.keyLT(r1,r2)) ;
        assertTrue(Record.keyGT(r1,r2)) ;
    }
}
