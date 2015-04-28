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

package org.apache.jena.atlas.io;

import java.io.ByteArrayOutputStream ;
import java.io.IOException ;
import java.io.OutputStreamWriter ;
import java.io.Writer ;

import org.apache.jena.atlas.io.OutputUtils ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

public class TestPrintUtils extends BaseTest
{
    @Test public void hex1()
    {
        String s = test(0,4) ;
        assertEquals("0000", s) ;
    }
    
    @Test public void hex2()
    {
        String s = test(1,8) ;
        assertEquals("00000001", s) ;
    }

    @Test public void hex3()
    {
        String s = test(0xFF,2) ;
        assertEquals("FF", s) ;
    }

    private static String test(int value, int width)
    {
        ByteArrayOutputStream x = new ByteArrayOutputStream() ;
        Writer out = new OutputStreamWriter(x) ;
        OutputUtils.printHex(out, value, width) ;
        try { out.flush() ; } catch (IOException ex) {}
        String s = x.toString() ;
        return s ;
    }
}
