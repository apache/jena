/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    
    @Override
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
