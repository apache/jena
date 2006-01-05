/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: ModelAssemblerTestBase.java,v 1.1 2006-01-05 13:40:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.ReificationStyle;

/**
    Test base for ModelAssemblers - provides parameterised method for
    reification testing.
    
    @author kers
*/
public abstract class ModelAssemblerTestBase extends AssemblerTestBase
    {
    public ModelAssemblerTestBase( String name )
        { super( name ); }

    /**
         Assert that the assembler <code>a</code> will create models with all
         the specified reification styles on the specification <code>base</code>.
    */
    protected final void testCreatesWithStyle( Assembler a, String base )
        {
        testCreateWithStyle( a, base, "ja:minimal", ReificationStyle.Minimal );
        testCreateWithStyle( a, base, "ja:standard", ReificationStyle.Standard );
        testCreateWithStyle( a, base, "ja:convenient", ReificationStyle.Convenient );
        }

    protected final void testCreateWithStyle( Assembler a, String base, String styleString, ReificationStyle style )
        {
        Resource root = resourceInModel( base );
        root.addProperty( JA.reificationMode, resource( root.getModel(), styleString ) );
        Model m = a.createModel( root );
        assertEquals( style, m.getGraph().getReifier().getStyle() );
        }
    }


/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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