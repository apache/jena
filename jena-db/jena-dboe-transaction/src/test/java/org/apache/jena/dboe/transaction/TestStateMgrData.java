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

package org.apache.jena.dboe.transaction;
import static org.junit.Assert.* ;

import java.nio.ByteBuffer ;

import org.apache.jena.dboe.base.file.BufferChannel;
import org.apache.jena.dboe.base.file.FileFactory;
import org.apache.jena.dboe.transaction.txn.StateMgrDataIdx;
import org.junit.Test ;

public class TestStateMgrData {
    
    @Test public void state_data_01() {
        BufferChannel x = FileFactory.createBufferChannelMem() ;
        long[] data = {2,3} ; 
        StateMgrDataIdx sm = new StateMgrDataIdx(x, data) ;
        assertEquals(data.length, sm.getData().length) ;
        assertEquals(2L, sm.get(0)) ;
        assertEquals(3L, sm.get(1)) ;
        // Test initial state written
        ByteBuffer bb = ByteBuffer.allocate(2*Long.BYTES) ;
        x.read(bb, 0) ;
        assertEquals(2L, bb.getLong(0)) ;
        assertEquals(3L, bb.getLong(Long.BYTES)) ; 
    }
    
    @Test public void state_data_02() {
        BufferChannel x = FileFactory.createBufferChannelMem() ;
        long[] data = {2,3} ; 
        StateMgrDataIdx sm = new StateMgrDataIdx(x, data) ;
        sm.writeState(); 
        sm.set(1, 99L);
        sm.writeState();
        ByteBuffer bb = ByteBuffer.allocate(2*Long.BYTES) ;
        x.read(bb, 0) ;
        assertEquals(99L, bb.getLong(Long.BYTES)) ; 
    }

    @Test public void state_data_03() {
        BufferChannel x = FileFactory.createBufferChannelMem() ;
        {
            ByteBuffer bb = ByteBuffer.allocate(Long.BYTES) ;
            bb.putLong(0, -8888) ;
            bb.rewind();
            x.write(bb) ;
            bb.putLong(0, -1234) ;
            bb.rewind();
            x.write(bb) ;
            x.sync(); 
        }
        long[] data = {2,3} ; 
        StateMgrDataIdx sm = new StateMgrDataIdx(x, data) ;
        assertEquals(-8888L, sm.get(0)) ;
        assertEquals(-1234L, sm.get(1)) ;
    }
    
}

