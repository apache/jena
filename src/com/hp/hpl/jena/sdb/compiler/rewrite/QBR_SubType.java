/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler.rewrite;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.compiler.QuadBlock;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class QBR_SubType implements QuadBlockRewrite
{
    private static final Node rdfType = RDF.type.asNode() ;
    
    public QuadBlock rewrite(SDBRequest request, QuadBlock quadBlock)
    {
        // Does not consider if the property slot is a variable.
        
        if ( ! quadBlock.contains(null, null, rdfType, null) )
            return quadBlock ;
        
        quadBlock = new QuadBlock(quadBlock) ;
        
        int i = 0 ;
        
        // Better/clearer : do as copy over from one block to another. 
        while ( ( i = quadBlock.findFirst(i, null, null, rdfType, null) ) != -1 ) 
        {
            // { :s rdf:type :C } => { :s rdf:type ?V . ?V rdfs:subClassOf :C } 
            Quad rdfTypeQuad = quadBlock.get(i) ;
            Var var = request.genVar() ;
            Quad q1 = new Quad(rdfTypeQuad.getGraph(), rdfTypeQuad.getSubject(), rdfType, var) ;
            Quad q2 = new Quad(rdfTypeQuad.getGraph(), var, RDFS.subClassOf.asNode(), rdfTypeQuad.getObject()) ;
            quadBlock.set(i, q1) ;      // replace rdf:type statement
            quadBlock.add(i+1, q2) ;    // add subClassOf statement
            i = i+2 ;                   // Skip the two statements.
        }
        return quadBlock ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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