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

package com.hp.hpl.jena.sparql.algebra;

import java.util.Map ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph ;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;

public class OpPrefixesUsed
{
    static public PrefixMapping used(Op op, PrefixMapping pmap)
    {
        PrefixMapping pmap2 = new PrefixMappingImpl() ;
        PrefixFinder finder = new PrefixFinder(pmap2, pmap.getNsPrefixMap()) ;
        OpWalker.walk(op, finder) ;
        return pmap2 ;
    }
    
    
    static class PrefixFinder extends OpVisitorBase
    {
        Map<String, String> pmap ;
        PrefixMapping usedPMap ;
        
        public PrefixFinder(PrefixMapping pmap2, Map<String, String> pmap)
        {
            this.pmap = pmap ;
            this.usedPMap = pmap2 ;
        }
        
        @Override
        public void visit(OpGraph opGraph)
        {
            node(opGraph.getNode()) ;
        }
        
        @Override
        public void visit(OpQuadPattern quadPattern)
        {
            node(quadPattern.getGraphNode()) ;
            visit(quadPattern.getBasicPattern()) ;
        }
        
        @Override
        public void visit(OpBGP opBGP)
        {
            BasicPattern p = opBGP.getPattern() ;
            visit(opBGP.getPattern()) ;
        }
        
        private void visit(BasicPattern pattern)
        {
            for ( Triple t : pattern )
            {
                node(t.getSubject()) ;
                node(t.getPredicate()) ;
                node(t.getObject()) ;
            }
        }
        
        @Override
        public void visit(OpFilter opFilter)
        {
            // Do more
        }
        
        private void node(Node n)
        {
            if ( ! n.isURI() ) return ;
            String uri = n.getURI() ;
            
            if ( usedPMap.shortForm(uri) != uri )
                return ;

            for ( Map.Entry<String, String> e : pmap.entrySet() )
            {
                String k = e.getKey();
                String v = e.getValue();

                if ( uri.startsWith( v ) )
                {
                    usedPMap.setNsPrefix( k, v );
                    return;
                }
            }
        }
    }
}
