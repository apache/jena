/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.BasicPattern;

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
            
            for ( Iterator<Map.Entry<String, String>> iter = pmap.entrySet().iterator() ; iter.hasNext() ; )
            {
                Map.Entry<String, String> e = iter.next();
                String k = e.getKey() ;
                String v = e.getValue() ;
                
                if ( uri.startsWith(v) )
                {
                    usedPMap.setNsPrefix(k, v) ;
                    return ;
                }
            }
        }
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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