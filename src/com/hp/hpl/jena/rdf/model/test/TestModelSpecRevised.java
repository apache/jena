/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestModelSpecRevised.java,v 1.1 2004-07-28 13:31:14 chris-dollin Exp $
*/
package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerFactory;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.JMS;

import junit.framework.TestSuite;

/**
 TestModelSpecRevised
 @author kers
 */
public class TestModelSpecRevised extends ModelTestBase
    {
    public TestModelSpecRevised( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestModelSpecRevised.class ); }
    
    /*
     * 
        Statement factStatement = description.getProperty( root, JMS.reasonsWith );
        if (factStatement == null) return null;
        Statement reStatement = description.getProperty( factStatement.getResource(), JMS.reasoner );
        String factoryURI = reStatement.getResource().getURI();
        return ReasonerRegistry.theRegistry().getFactory( factoryURI );
     */
    public void testReasonerSupplied()
        { 
        Model rs = modelWithStatements( "_a rdf:type jms:ReasonerSpec" );
        try { createReasoner( rs ); fail( "should catch missing reasoner" ); }
        catch (NoReasonerSuppliedException e) { pass(); }
        }

    private static class NoReasonerSuppliedException extends JenaException
        {
        
        }
    
    private static class NoSuchReasonerException extends JenaException
        {
        
        }
    
    private ReasonerFactory createReasoner( Model rs )
        {
        if (true) throw new NoReasonerSuppliedException();
        return ReasonerRegistry.theRegistry().getFactory( "foo" );
        }
    }


/*
    (c) Copyright 2004, Hewlett-Packard Development Company, LP
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