/*
 	(c) Copyright 2007 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestAssemblerGroupTracing.java,v 1.1 2007-07-20 15:15:14 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.*;
import com.hp.hpl.jena.assembler.exceptions.AssemblerException;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.JenaException;

public class TestAssemblerGroupTracing extends AssemblerTestBase
    {
    public TestAssemblerGroupTracing( String name )
        { super( name ); }

    public void testFail() 
        {
        Resource root = resourceInModel( "x rdf:type A" );
        AssemblerGroup g = AssemblerGroup.create();
        g.implementWith( resource( "A" ), new ShantAssemble() );
        try 
            { 
            g.open( root ); 
            fail( "shouldn't get past exception" ); 
            }
        catch (AssemblerException e) 
            {
            AssemblerGroup.Frame frame = new AssemblerGroup.Frame( resource( "x" ), resource( "A" ), ShantAssemble.class );
            assertEquals( listOfOne( frame ), e.getDoing() );
            }
        }
    
    static class ShantAssemble extends AssemblerBase
        {
        public Object open( Assembler a, Resource root, Mode mode )
            {            
            throw new JenaException( "shan't" );
            }
    
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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