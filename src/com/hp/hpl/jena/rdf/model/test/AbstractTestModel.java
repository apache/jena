/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: AbstractTestModel.java,v 1.4 2003-07-10 09:01:33 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.graph.*;

/**
 	@author kers
*/
public abstract class AbstractTestModel extends ModelTestBase
    {
    public AbstractTestModel( String name )
        { super(name); }

    public abstract Model getModel();
    
    private Model model;
    
    public void setUp()
        {
        model = getModel();
        }
        
    public void tearDown()
        {
        model.close();
        } 
       
    public void testTransactions()
        { 
        Command cmd = null;
        if (model.supportsTransactions()) model.executeInTransaction( cmd );
        }
        
    public void testCreateResourceFromNode()
        {
        RDFNode S = model.getRDFNode( Node.create( "spoo:S" ) ); 
        assertTrue( S instanceof Resource );
        assertEquals( "spoo:S", ((Resource) S).getURI() );
        }
        
    public void testCreateLiteralFromNode()
        {
        RDFNode S = model.getRDFNode( Node.create( "42" ) ); 
        assertTrue( S instanceof Literal );
        assertEquals( "42", ((Literal) S).getLexicalForm() );
        }    
            
   public void testCreateBlankFromNode()
        {
        RDFNode S = model.getRDFNode( Node.create( "_Blank" ) ); 
        assertTrue( S instanceof Resource );
        assertEquals( new AnonId( "_Blank" ), ((Resource) S).getId() );
        }
        
    public void testIsEmpty()
        {
        Statement S1 = statement( model, "model rdf:type nonEmpty" );
        Statement S2 = statement( model, "pinky rdf:type Pig" );
        assertTrue( model.isEmpty() );
        model.add( S1 );
        assertFalse( model.isEmpty() );
        model.add( S2 );
        assertFalse( model.isEmpty() );
        model.remove( S1 );
        assertFalse( model.isEmpty() );
        model.remove( S2 );
        assertTrue( model.isEmpty() );
        }
        
    public void testContainsResource()
        {
        modelAdd( model, "x R y; _a P _b" );
        assertTrue( model.containsResource( resource( model, "x" ) ) );
        assertTrue( model.containsResource( resource( model, "R" ) ) );
        assertTrue( model.containsResource( resource( model, "y" ) ) );
        assertTrue( model.containsResource( resource( model, "_a" ) ) );
        assertTrue( model.containsResource( resource( model, "P" ) ) );
        assertTrue( model.containsResource( resource( model, "_b" ) ) );
        assertFalse( model.containsResource( resource( model, "i" ) ) );
        assertFalse( model.containsResource( resource( model, "_j" ) ) );
        }
    }


/*
    (c) Copyright Hewlett-Packard Company 2003
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