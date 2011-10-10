/**
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

package test ;
import java.nio.ByteBuffer;

import org.junit.Test;


public class ByteBufferTest {
    //@Test
    public void direct0() {
        testDirect("direct0");
    }
    @Test
    public void heap1() {
        testHeap("heap1");
    }

    @Test
    public void direct1() {
        testDirect("direct1");
    }
    
    @Test
    public void heap2() {
        testHeap("heap2");
    }

    @Test
    public void direct2() {
        testDirect("direct2");
    }

    private void testHeap(String name) {
        ByteBuffer buf = ByteBuffer.allocate(2048);
        long startTime = System.currentTimeMillis();
        for (int i = 1048576; i > 0; i --) {
            buf.clear();
            while (buf.hasRemaining()) {
                buf.getInt(buf.position());
                buf.putInt((byte) 0);
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println(name + ": " + (endTime - startTime));
    }
    
    private void testDirect(String name) {
        ByteBuffer buf = ByteBuffer.allocateDirect(2048);
        long startTime = System.currentTimeMillis();
        for (int i = 1048576; i > 0; i --) {
            buf.clear();
            while (buf.hasRemaining()) {
                buf.getInt(buf.position());
                buf.putInt((byte) 0);
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println(name + ": " + (endTime - startTime));
    }

}
