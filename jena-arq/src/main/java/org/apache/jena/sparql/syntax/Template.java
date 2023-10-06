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

package org.apache.jena.sparql.syntax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.serializer.FormatterTemplate;
import org.apache.jena.sparql.util.Iso;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

/** Quads/Triples template. */

public class Template
{
    static final int HashTemplateGroup     = 0xB1 ;
    private final QuadAcc qp ;
    private final BasicPattern bgp;

    public Template(QuadAcc qp)
    {
        this.qp = qp ;
        this.bgp = null;
    }

    public Template(BasicPattern bgp)
    {
    	this.bgp = bgp;
    	this.qp = null;
    }

    public boolean containsRealQuad(){
    	for(Quad quad : this.getQuads()){
    		if ( ! Quad.defaultGraphNodeGenerated.equals( quad.getGraph())){
    			return true;
    		}
    	}
    	return false;
    }

    public BasicPattern getBGP()
    {
    	if (this.bgp != null){
    		return this.bgp;
    	}
    	BasicPattern bgp = new BasicPattern();
    	for(Quad q: qp.getQuads()){
    		if (Quad.defaultGraphNodeGenerated.equals(q.getGraph()))
    			bgp.add(q.asTriple());
    	}
    	return bgp;
    }
    public List<Triple> getTriples()
    {
    	if(this.bgp != null){
    		return this.bgp.getList();
    	}
    	List<Triple> triples = new ArrayList<>();
    	for(Quad q: qp.getQuads()) {
    		if (Quad.defaultGraphNodeGenerated.equals(q.getGraph()))
    			triples.add(q.asTriple());
    	}
    	return triples;
    }

    public List<Quad> getQuads() {
    	if( this.bgp != null){
    		List<Quad> quads = new ArrayList<>();
    		for(Triple triple: this.bgp.getList()) {
    			quads.add( new Quad( Quad.defaultGraphNodeGenerated, triple ) );
    		}
    		return quads;
    	}
    	return qp.getQuads() ;
    }

    public Map<Node, BasicPattern> getGraphPattern(){
        List<Quad> quads = getQuads();
        HashMap<Node, BasicPattern> graphs = new HashMap<>();
        for (Quad q: quads){
            BasicPattern bgp = graphs.get(q.getGraph());
            if (bgp == null){
                bgp = new BasicPattern();
                graphs.put(q.getGraph(), bgp);
            }
            bgp.add( q.asTriple() );
        }
        return graphs;
    }

    // -------------------------

    private int calcHashCode = -1 ;
    @Override
    public int hashCode()
    {
        // BNode invariant hashCode.
        int calcHashCode = Template.HashTemplateGroup ;
        for ( Quad q : getQuads() )
            calcHashCode ^=  hash(q) ^ calcHashCode<<1 ;
        return calcHashCode ;
    }

    private static int hash(Quad quad)
    {
        int hash = 0 ;
        hash = hashNode(quad.getSubject())   ^ hash<<1 ;
        hash = hashNode(quad.getPredicate()) ^ hash<<1 ;
        hash = hashNode(quad.getObject())    ^ hash<<1 ;
        hash = hashGraph(quad.getGraph())    ^ hash<<1 ;
        return hash ;
    }

    private static int hashGraph(Node node){
    	if ( node == null ) return Quad.defaultGraphNodeGenerated.hashCode() ;
        if ( node.isBlank() ) return 59 ;
        return node.hashCode() ;
    }

    private static int hashNode(Node node)
    {
    	if ( node == null ) return 37 ;
        if ( node.isBlank() ) return 59 ;
        return node.hashCode() ;
    }

    public boolean equalIso(Object temp2, NodeIsomorphismMap labelMap)
    {
        if ( !(temp2 instanceof Template tg2) )
            return false;
        List<Quad> list1 = this.getQuads() ;
        List<Quad> list2 = tg2.getQuads() ;
        if ( list1.size() != list2.size() ) return false ;

        for ( int i = 0 ; i < list1.size() ; i++ )
        {
            Quad q1 = list1.get(i) ;
            Quad q2 = list2.get(i) ;
            boolean iso = Iso.quadIso(q1, q2, labelMap) ;
            if(!iso){
            	return false;
            }
        }
        return true ;
    }

    public void format(FormatterTemplate fmtTemplate)
    {
        fmtTemplate.format(this) ;
    }
}
