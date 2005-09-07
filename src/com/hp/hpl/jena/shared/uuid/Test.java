/*
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */



/**
 * @author   Andy Seaborne
 * @version  $Id: Test.java,v 1.1 2005-09-07 17:26:22 andy_seaborne Exp $
 */

package com.hp.hpl.jena.shared.uuid;

import junit.framework.*;

public class Test extends TestCase
{
    
    
    
    static public void Main(String args[])
    {
    	// Version 1
    	System.out.println("Version 1 testing") ;
    	UUID.useSecureRandom = true ;
    	
        UUID u = UUID.create() ;

		System.out.println("Sequentially allocated UUIDs") ;
        System.out.println(u.toString()) ;
        
        System.out.println(UUID.create().toString()) ;
        System.out.println(UUID.create().toString()) ;
        
        System.out.println() ;
        System.out.println("Force reset UUID_V1") ;
        UUID_V1.reset() ;

		System.out.println("Sequentially allocated UUIDs") ;
        System.out.println(UUID.create().toString()) ;
        System.out.println(UUID.create().toString()) ;

        System.out.println() ;
        UUID u2 = UUID.create() ;
        UUID u3 = UUID.create(u.toString()) ;
        System.out.println("u:  "+u) ;
        System.out.println("u2: "+u2) ;
        System.out.println("u3: "+u3) ;
        System.out.println() ;
	
		System.out.println("Should be false") ;
        System.out.println("  u equals u2: "+u.equals(u2));
		System.out.println("Should be true") ;
        System.out.println("  u equals u3: "+u.equals(u3));
        
        // Version 4
        System.out.println() ;
    	System.out.println("Version 4 UUIDing") ;

		UUID id1 = UUID.createV4() ;
		System.out.println("Sequentially allocated UUIDs") ;
        System.out.println(u.toString()) ;
        
        System.out.println(UUID.createV4().toString()) ;
        System.out.println(UUID.createV4().toString()) ;

        System.out.println() ;
		UUID id2 = UUID.create() ;
		UUID id3 = UUID.create(id1.toString()) ;
		System.out.println("Should be false") ;
        System.out.println("  id1 equals id2: "+id1.equals(id2));
		System.out.println("Should be true") ;
        System.out.println("  id1 equals id3: "+id1.equals(id3));
		
		
        System.out.println() ;
    	System.out.println("Misc UUIDs") ;

        // Actually the UUID for the D-U-N-S tModel in UDDI
        System.out.println() ;
        UUID1("8609C81E-EE1F-4D5A-B202-3EB13AD01823") ;

        System.out.println() ;
        UUID1("uuid:DB77450D-9FA8-45D4-A7BC-04411D14E384") ;

        System.out.println() ;
        UUID1("UUID:C0B9FE13-179F-413D-8A5B-5004DB8E5BB2") ;

        System.out.println() ;
        UUID1("urn:uuid:70A80F61-77BC-4821-A5E2-2A406ACC35DD") ;
    }

    static private void UUID1(String s)
    {
        UUID u = UUID.create() ;
        System.out.println("In:  "+s) ;
        System.out.println("Out: "+u) ;
    }
}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
