/*
 	(c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestMode.java,v 1.5 2008-01-02 12:05:55 andy_seaborne Exp $
*/

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestMode extends AssemblerTestBase
    {
    public TestMode( String name )
        { super( name ); }

    public void testConstantsExist()
        {
        Mode a = Mode.CREATE, b = Mode.DEFAULT;
        Mode c = Mode.REUSE, d = Mode.ANY;
        assertDiffer( Mode.CREATE, Mode.DEFAULT );
        assertDiffer( Mode.CREATE, Mode.REUSE );
        assertDiffer( Mode.CREATE, Mode.ANY );
        assertDiffer( Mode.DEFAULT, Mode.REUSE );
        assertDiffer( Mode.DEFAULT, Mode.ANY );
        assertDiffer( Mode.REUSE, Mode.ANY );
        }
    
    static final String someName = "aName";
    static final Resource someRoot = resource( "aRoot" );
    
    public void testCreate()
        {
        assertEquals( true, Mode.CREATE.permitCreateNew( someRoot, someName ) );
        assertEquals( false, Mode.CREATE.permitUseExisting( someRoot, someName ) );
        }    
    
    public void testReuse()
        {
        assertEquals( false, Mode.REUSE.permitCreateNew( someRoot, someName ) );
        assertEquals( true, Mode.REUSE.permitUseExisting( someRoot, someName ) );
        }    
    
    public void testAny()
        {
        assertEquals( true, Mode.ANY.permitCreateNew( someRoot, someName ) );
        assertEquals( true, Mode.ANY.permitUseExisting( someRoot, someName ) );
        }    
    
    public void testDefault()
        {
        assertEquals( false, Mode.DEFAULT.permitCreateNew( someRoot, someName ) );
        assertEquals( true, Mode.DEFAULT.permitUseExisting( someRoot, someName ) );
        }
    }


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