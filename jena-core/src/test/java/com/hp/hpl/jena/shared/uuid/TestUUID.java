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

import junit.framework.TestCase;
import static com.hp.hpl.jena.shared.uuid.UUIDTestSuite.* ;

public class TestUUID extends TestCase
{
    public void testNilUUID1()
    {
        JenaUUID u = JenaUUID.nil();
        assertTrue(u.getVariant() == 0) ;
        assertTrue(u.getVersion() == 0) ;
        assertTrue(u == UUID_nil.getNil()) ;
    }

    public void testNilUUID2()
    {
        JenaUUID u = JenaUUID.nil();
        assertTrue(u.isNil()) ;
    }
    
    public void testNilUUID3()
    {
        JenaUUID u = JenaUUID.nil();
        String s = u.asString() ;
        assertEquals(s, UUID_nil.getNilString()) ;
    }
    
    public void testTime1()
    {
        JenaUUID u = JenaUUID.generate() ;
        assertTrue(u.getVersion() == UUID_V1.version) ; 
        assertTrue(u.getVariant() == UUID_V1.variant) ;
    }
    
    public void testTime2()
    {
        JenaUUID u = JenaUUID.generate() ;
        check(u) ;
    }

    public void testTime3()
    {
        UUID_V1 u1 = factory1.generateV1() ;
        UUID_V1 u2 = UUID_V1_Gen.generate(u1.getVersion(), u1.getVariant(), u1.getTimestamp(), u1.getClockSequence(), u1.getNode()) ;

        assertEquals(u1.getVersion(),        u2.getVersion() ) ;
        assertEquals(u1.getVariant(),        u2.getVariant() ) ;
        assertEquals(u1.getTimestamp(),      u2.getTimestamp() ) ;
        assertEquals(u1.getClockSequence(),  u2.getClockSequence() ) ;
        assertEquals(u1.getNode(),           u2.getNode() ) ;
    }

    public void testTime4()
    {
        UUID_V1 u1 = factory1.generateV1() ;
        UUID_V1 u2 = UUID_V1_Gen.generate(u1.getVersion(), u1.getVariant(), u1.getTimestamp(), u1.getClockSequence(), u1.getNode()) ;
        assertEquals(u1, u2) ;
        assertEquals(u1.asString(), u2.asString()) ;
    }

    
    public void testTime5()
    {
        UUID_V1 u1 = factory1.generateV1() ;
        UUID_V1 u2 = UUID_V1_Gen.generate(u1.getVersion(), u1.getVariant(), u1.getTimestamp(), u1.getClockSequence(), u1.getNode()) ;
        assertEquals(u1.asString(), u2.asString()) ;
    }

    public void testTime6()
    {
        JenaUUID u1 = JenaUUID.generate() ;
        JenaUUID u2 = JenaUUID.generate() ;
        assertFalse(u1.equals(u2)) ;
    }
    
    public void testRandom1()
    {
        JenaUUID u = factory4.generate() ;
        assertEquals(u.getVersion(), UUID_V4.version) ;
        assertEquals(u.getVariant(), UUID_V4.variant) ;
    }
    

    public void testRandom2()
    {
        JenaUUID u = factory4.generate() ;
        check(u) ;
    }
    
    public void testRandom3()
    {
        JenaUUID u = factory4.generate() ;
        check(u.asString()) ;
    }
    
    public void testRandom4()
    {
        JenaUUID u1 = factory4.generate() ;
        JenaUUID u2 = factory4.generate() ;
        assertFalse(u1.equals(u2)) ;
    }
    
    public void testEquals1()
    {
        JenaUUID u1 = JenaUUID.generate() ;
        JenaUUID u2 = JenaUUID.parse(u1.asString()) ;
        assertNotSame(u1, u2) ;
        assertEquals(u1, u2) ;
        JenaUUID u3 = JenaUUID.generate() ;
        assertFalse(u1.equals(u3)) ;
        assertFalse(u3.equals(u1)) ;
        assertFalse(u2.equals(u3)) ;
        assertFalse(u3.equals(u2)) ;
    }
    
    public void testEquals2()
    {
        JenaUUID u1 = factory4.generate() ;
        JenaUUID u2 = JenaUUID.parse(u1.asString()) ;
        assertNotSame(u1, u2) ;
        assertEquals(u1, u2) ;
        JenaUUID u3 = factory4.generate() ;
        assertFalse(u1.equals(u3)) ;
        assertFalse(u3.equals(u1)) ;
        assertFalse(u2.equals(u3)) ;
        assertFalse(u3.equals(u2)) ;
    }
    
    public void testHash1()
    {
        JenaUUID u1 = JenaUUID.generate() ;
        JenaUUID u2 = JenaUUID.parse(u1.asString()) ;
        assertNotSame(u1, u2) ;
        assertEquals(u1.hashCode(), u2.hashCode()) ;
        JenaUUID u3 = JenaUUID.generate() ;
        // Time/increment based so should be different
        assertFalse(u1.hashCode() == u3.hashCode()) ;
    }
    
    public void testHash2()
    {
        JenaUUID u1 = factory4.generate() ;
        JenaUUID u2 = JenaUUID.parse(u1.asString()) ;
        assertNotSame(u1, u2) ;
        assertEquals(u1.hashCode(), u2.hashCode()) ;
    }
    
    public void testMisc1()
    {
        check("8609C81E-EE1F-4D5A-B202-3EB13AD01823") ;
        check("uuid:DB77450D-9FA8-45D4-A7BC-04411D14E384") ;
        check("UUID:C0B9FE13-179F-413D-8A5B-5004DB8E5BB2") ;
        check("urn:8609C81E-EE1F-4D5A-B202-3EB13AD01823") ;
        check("urn:uuid:70A80F61-77BC-4821-A5E2-2A406ACC35DD") ;
    }

    private void check(String uuidString)
    {
        JenaUUID uuid = JenaUUID.parse(uuidString) ;
        //assertFalse(u.equals(JenaUUID.nil())) ;
        String s2 = uuid.asString() ;
        
        String s = uuidString ;
        if ( s.matches("[uU][rR][nN]:") )
            s = s.substring(4) ;
        if ( s.startsWith("[uU][uU][iI][dD]:") )
            s = s.substring(5) ;
        
        assertTrue(uuidString.equalsIgnoreCase(s)) ;
    }
    
    private void check(JenaUUID uuid)
    {
        String s = uuid.asString() ;
        if ( uuid.isNil() )
        {
            assertEquals(JenaUUID.strNil(), s) ;
            return ;
        }
        JenaUUID uuid2 = JenaUUID.parse(s) ;
        assertTrue(uuid2.equals(uuid)) ;
    }
}
