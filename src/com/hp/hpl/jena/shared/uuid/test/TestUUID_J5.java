/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.shared.uuid.test;

//import junit.framework.TestCase;
//import java.util.UUID ;
//
//import com.hp.hpl.jena.shared.uuid.*;

/** Java5-dependent tests.
 * java.util.UUID only appeared in Java5
 * @author Andy Seaborne
 * @version $Id: TestUUID_J5.java,v 1.4 2008-01-02 12:09:21 andy_seaborne Exp $
 */

public class TestUUID_J5 {}

//public class TestUUID_J5 extends TestCase
//{
//    UUID_V1_Gen factory1 = new UUID_V1_Gen() ;
//    UUID_V4_Gen factory4 = new UUID_V4_Gen() ;
//    
//    public void testU5_1()
//    {
//        JenaUUID u1 = JenaUUID.generate() ;
//        UUID u2 = UUID.fromString(u1.asString()) ;
//        assertEquals(u1.getVersion(), u2.version()) ;
//        assertEquals(u1.getVariant(), u2.variant()) ;
//    }
//    
//    public void testU5_2()
//    {
//        UUID_V1 u1 = factory1.generateV1() ;
//        UUID u2 = UUID.fromString(u1.asString()) ;
//        assertEquals(u1.getClockSequence(), u2.clockSequence()) ; 
//        assertEquals(u1.getTimestamp() ,    u2.timestamp() ) ;
//        assertEquals(u1.getNode(),          u2.node()) ;
//        assertEquals(u1.getVariant(),       u2.variant()) ; 
//        assertEquals(u1.getVersion() ,      u2.version()) ;
//        assertEquals(u1.getMostSignificantBits(), u2.getMostSignificantBits()) ;
//        assertEquals(u1.getLeastSignificantBits(), u2.getLeastSignificantBits()) ;
//    }
//    
//    public void testU5_3()
//    {
//        UUID_V4 u1 = factory4.generateV4() ;
//        UUID u2 = UUID.fromString(u1.asString()) ;
//        assertEquals(u1.getVariant(),       u2.variant()) ; 
//        assertEquals(u1.getVersion() ,      u2.version()) ;
//        assertEquals(u1.getMostSignificantBits(), u2.getMostSignificantBits()) ;
//        assertEquals(u1.getLeastSignificantBits(), u2.getLeastSignificantBits()) ;
//    }
//    
//    public void testU5_4()
//    {
//        UUID u1 = UUID.randomUUID() ;
//        JenaUUID u2 = JenaUUID.parse(u1.toString()) ;
//        assertEquals(u1.version(), u2.getVersion()) ;
//        assertEquals(u1.variant(), u2.getVariant()) ;
//        assertEquals(u1.getMostSignificantBits() , u2.getMostSignificantBits()) ;
//        assertEquals(u1.getLeastSignificantBits() , u2.getLeastSignificantBits()) ;
//        assertEquals(u1.toString() , u2.toString()) ;
//    }
//    
//    public void testU5_5()
//    {
//        JenaUUID u1 = factory1.generate() ;
//        UUID u2 = new UUID(u1.getMostSignificantBits(), u1.getLeastSignificantBits()) ;
//        
//        assertEquals(u1.getVersion(), u2.version()) ;
//        assertEquals(u1.getVariant(), u2.variant()) ;
//        assertEquals(u1.getMostSignificantBits() , u2.getMostSignificantBits()) ;
//        assertEquals(u1.getLeastSignificantBits() , u2.getLeastSignificantBits()) ;
//        assertEquals(u1.toString() , u2.toString()) ;
//    }
//    
//    public void testU5_6()
//    {
//        JenaUUID u1 = factory4.generate() ;
//        UUID u2 = new UUID(u1.getMostSignificantBits(), u1.getLeastSignificantBits()) ;
//        
//        assertEquals(u1.getVersion(), u2.version()) ;
//        assertEquals(u1.getVariant(), u2.variant()) ;
//        assertEquals(u1.getMostSignificantBits() , u2.getMostSignificantBits()) ;
//        assertEquals(u1.getLeastSignificantBits() , u2.getLeastSignificantBits()) ;
//        assertEquals(u1.toString() , u2.toString()) ;
//    }
//    
//    private void check(String uuidString)
//    {
//        JenaUUID uuid = JenaUUID.parse(uuidString) ;
//        //assertFalse(u.equals(JenaUUID.nil())) ;
//        String s2 = uuid.asString() ;
//        
//        String s = uuidString ;
//        if ( s.matches("[uU][rR][nN]:") )
//            s = s.substring(4) ;
//        if ( s.startsWith("[uU][uU][iI][dD]:") )
//            s = s.substring(5) ;
//        
//        assertTrue(uuidString.equalsIgnoreCase(s)) ;
//    }
//    
//    private void check(JenaUUID uuid)
//    {
//        String s = uuid.asString() ;
//        if ( uuid.isNil() )
//        {
//            assertEquals(JenaUUID.strNil(), s) ;
//            return ;
//        }
//        JenaUUID uuid2 = JenaUUID.parse(s) ;
//        assertTrue(uuid2.equals(uuid)) ;
//    }
//}

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
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