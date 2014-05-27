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

package com.hp.hpl.jena.shared.uuid;

import static com.hp.hpl.jena.shared.uuid.UUIDTestSuite.factory1 ;
import static com.hp.hpl.jena.shared.uuid.UUIDTestSuite.factory4 ;

import java.util.UUID ;

import junit.framework.TestCase ;

/** Test comparing Java JRE UUID and Jena's for parsing existing UUIDs. */

public class TestUUID_JRE extends TestCase
{
    public void testU5_1()
    {
        JenaUUID u1 = JenaUUID.generate() ;
        UUID u2 = UUID.fromString(u1.asString()) ;
        assertEquals(u1.getVersion(), u2.version()) ;
        assertEquals(u1.getVariant(), u2.variant()) ;
    }

    public void testU5_2()
    {
        UUID_V1 u1 = factory1.generateV1() ;
        UUID u2 = UUID.fromString(u1.asString()) ;
        assertEquals(u1.getClockSequence(), u2.clockSequence()) ;
        assertEquals(u1.getTimestamp(), u2.timestamp()) ;
        assertEquals(u1.getNode(), u2.node()) ;
        assertEquals(u1.getVariant(), u2.variant()) ;
        assertEquals(u1.getVersion(), u2.version()) ;
        assertEquals(u1.getMostSignificantBits(), u2.getMostSignificantBits()) ;
        assertEquals(u1.getLeastSignificantBits(), u2.getLeastSignificantBits()) ;
    }

    public void testU5_3()
    {
        UUID_V4 u1 = factory4.generateV4() ;
        UUID u2 = UUID.fromString(u1.asString()) ;
        assertEquals(u1.getVariant(), u2.variant()) ;
        assertEquals(u1.getVersion(), u2.version()) ;
        assertEquals(u1.getMostSignificantBits(), u2.getMostSignificantBits()) ;
        assertEquals(u1.getLeastSignificantBits(), u2.getLeastSignificantBits()) ;
    }

    public void testU5_4()
    {
        UUID u1 = UUID.randomUUID() ;
        JenaUUID u2 = JenaUUID.parse(u1.toString()) ;
        assertEquals(u1.version(), u2.getVersion()) ;
        assertEquals(u1.variant(), u2.getVariant()) ;
        assertEquals(u1.getMostSignificantBits(), u2.getMostSignificantBits()) ;
        assertEquals(u1.getLeastSignificantBits(), u2.getLeastSignificantBits()) ;
        assertEquals(u1.toString(), u2.toString()) ;
    }

    public void testU5_5()
    {
        JenaUUID u1 = factory1.generate() ;
        UUID u2 = new UUID(u1.getMostSignificantBits(), u1.getLeastSignificantBits()) ;

        assertEquals(u1.getVersion(), u2.version()) ;
        assertEquals(u1.getVariant(), u2.variant()) ;
        assertEquals(u1.getMostSignificantBits(), u2.getMostSignificantBits()) ;
        assertEquals(u1.getLeastSignificantBits(), u2.getLeastSignificantBits()) ;
        assertEquals(u1.toString(), u2.toString()) ;
    }

    public void testU5_6()
    {
        JenaUUID u1 = factory4.generate() ;
        UUID u2 = new UUID(u1.getMostSignificantBits(), u1.getLeastSignificantBits()) ;

        assertEquals(u1.getVersion(), u2.version()) ;
        assertEquals(u1.getVariant(), u2.variant()) ;
        assertEquals(u1.getMostSignificantBits(), u2.getMostSignificantBits()) ;
        assertEquals(u1.getLeastSignificantBits(), u2.getLeastSignificantBits()) ;
        assertEquals(u1.toString(), u2.toString()) ;
    }

    private void check(String uuidString)
    {
        JenaUUID uuid = JenaUUID.parse(uuidString) ;
        // assertFalse(u.equals(JenaUUID.nil())) ;
        String s2 = uuid.asString() ;

        String s = uuidString ;
        if (s.matches("[uU][rR][nN]:")) s = s.substring(4) ;
        if (s.startsWith("[uU][uU][iI][dD]:")) s = s.substring(5) ;

        assertTrue(uuidString.equalsIgnoreCase(s)) ;
    }

    private void check(JenaUUID uuid)
    {
        String s = uuid.asString() ;
        if (uuid.isNil())
        {
            assertEquals(JenaUUID.strNil(), s) ;
            return ;
        }
        JenaUUID uuid2 = JenaUUID.parse(s) ;
        assertTrue(uuid2.equals(uuid)) ;
    }
}
