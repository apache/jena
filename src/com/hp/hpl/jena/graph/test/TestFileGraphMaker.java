/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestFileGraphMaker.java,v 1.7 2003-09-10 15:31:42 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.shared.*;
import junit.framework.*;

/**
    Test a FileGraphMaker; use the abstract tests, plus specialised ones for the name
    conversion routines.
    
 	@author hedgehog
*/
public class TestFileGraphMaker extends AbstractTestGraphMaker
    {
    public TestFileGraphMaker( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestFileGraphMaker.class ); }

    public GraphMaker getGraphMaker()
        { String scratch = getScratchDirectory( "jena-test-FileGraphMaker" ).toString();
        return new FileGraphMaker( scratch, ReificationStyle.Minimal, true ); } 
        
    public void testToFilename()
        { assertEquals( "plain", FileGraphMaker.toFilename( "plain" ) );   
        assertEquals( "with_Sslash", FileGraphMaker.toFilename( "with/slash" ) );
        assertEquals( "with_Ccolon", FileGraphMaker.toFilename( "with:colon" ) );
        assertEquals( "with_Uunderbar", FileGraphMaker.toFilename( "with_underbar" ) );
        assertEquals( "with_Stwo_Sslashes", FileGraphMaker.toFilename( "with/two/slashes" ) );
        assertEquals( "with_Sa_Cmixture_U...", FileGraphMaker.toFilename( "with/a:mixture_..." ) ); }
        
    public void testToGraphname()
        { assertEquals( "plain", FileGraphMaker.toGraphname( "plain" ) );
        assertEquals( "with/slash", FileGraphMaker.toGraphname( "with_Sslash" ) );    
        assertEquals( "with:colon", FileGraphMaker.toGraphname( "with_Ccolon" ) );    
        assertEquals( "with_underbar", FileGraphMaker.toGraphname( "with_Uunderbar" ) );    
        assertEquals( "a/mixture_of:things", FileGraphMaker.toGraphname( "a_Smixture_Uof_Cthings" ) );    
        assertEquals( "with/two/slashes", FileGraphMaker.toGraphname( "with_Stwo_Sslashes" ) ); }
    }


/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/