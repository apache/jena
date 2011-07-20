/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.graph;


import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Reifier ;
import com.hp.hpl.jena.graph.test.AbstractTestReifier ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.shared.ReificationStyle ;

public class TestReifier2 extends AbstractTestReifier
{
    public TestReifier2()
    {
        super("Reifier2") ;
    }

    @Override
    public Graph getGraph()
    {
        return new GraphMemSimple2() ;
    }

    @Override
    public Graph getGraph(ReificationStyle style)
    {
        if ( style != ReificationStyle.Standard )
        {}
        return new GraphMemSimple2() ;
    }

    // Standard only.
    @Override public void testStyle() { assertSame( ReificationStyle.Standard, 
                                                    getGraph( ReificationStyle.Standard ).getReifier().getStyle() ); }
    
    // These are tests on other styles.
    @Override public void testIntercept() {}              // "Convenient"
    @Override public void testMinimalExplode() {}         // "Minimal"
    @Override public void testDynamicHiddenTriples() {}   // "Minimal"

//    @Override public void testBulkClearReificationTriples() {}
//    @Override public void testBulkClearReificationTriples2() {}
    
    /*@Test*/ public void testRemoveReification()
    {
        // Test from Benson Margulies : JENA-82
        Model model= ModelFactory.createModelForGraph(getGraph()) ;
        Resource per1 = model.createResource("urn:x:global#per1");
        Resource per2 = model.createResource("urn:x:global#per2");
        Property pred1 = model.createProperty("http://example/ns#prop1");
        Property pred2 = model.createProperty("http://example/ns#prop2") ;
        Statement s1 = model.createStatement(per1, pred1, per2);
        Statement s2 = model.createStatement(per2, pred2, per2);
        
        s1.createReifiedStatement();
        s2.createReifiedStatement();
        
        assertEquals(2, model.listReifiedStatements().toList().size());
        
        Reifier r = new Reifier2(model.getGraph()) ;
        //r = model.getGraph().getReifier() ;
        r.remove(s2.asTriple()) ;
        assertEquals(1, model.listReifiedStatements().toList().size());
    }
    
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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