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

package com.hp.hpl.jena.sparql.syntax;

import java.util.Collection ;
import java.util.List ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.modify.TemplateLib ;
import com.hp.hpl.jena.sparql.serializer.FormatterTemplate ;
import com.hp.hpl.jena.sparql.util.Iso ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** Triples template. */

public class Template 
{
    static final int HashTemplateGroup     = 0xB1 ;
    private final BasicPattern bgp ;
    
    public Template(BasicPattern bgp)
    { 
        this.bgp = bgp ;
    }

//    public void addTriple(Triple t) { quads.addTriple(t) ; }
//    public int mark() { return quads.mark() ; }
//    public void addTriple(int index, Triple t) { quads.addTriple(index, t) ; }
//    public void addTriplePath(TriplePath path)
//    { throw new ARQException("Triples-only collector") ; }
//
//    public void addTriplePath(int index, TriplePath path)
//    { throw new ARQException("Triples-only collector") ; }


    public BasicPattern getBGP()        { return bgp ; }
    public List<Triple> getTriples()    { return bgp.getList() ; }
    // -------------------------

    public void subst(Collection<Triple> acc, Map<Node, Node> bNodeMap, Binding b)
    {
        for ( Triple t : bgp.getList() )
        {
            t = TemplateLib.subst(t, b, bNodeMap) ;
            acc.add(t) ;
        }
    }

    private int calcHashCode = -1 ;  
    @Override
    public int hashCode()
    { 
        // BNode invariant hashCode. 
        int calcHashCode = Template.HashTemplateGroup ;
        for ( Triple t : bgp.getList() )
            calcHashCode ^=  hash(t) ^ calcHashCode<<1 ; 
        return calcHashCode ;
    }
    
    private static int hash(Triple triple)
    {
        int hash = 0 ;
        hash = hashNode(triple.getSubject())   ^ hash<<1 ;
        hash = hashNode(triple.getPredicate()) ^ hash<<1 ;
        hash = hashNode(triple.getObject())    ^ hash<<1 ;
        return hash ;
    }

    private static int hashNode(Node node)
    {
        if ( node.isBlank() ) return 59 ;
        return node.hashCode() ;
    }
    
    public boolean equalIso(Object temp2, NodeIsomorphismMap labelMap)
    {
        if ( ! ( temp2 instanceof Template) ) return false ;
        Template tg2 = (Template)temp2 ;
        List<Triple> list1 = this.bgp.getList() ;
        List<Triple> list2 = tg2.bgp.getList() ;
        if ( list1.size() != list2.size() ) return false ;
        
        for ( int i = 0 ; i < list1.size() ; i++ )
        {
            Triple t1 = list1.get(i) ;
            Triple t2 = list2.get(i) ;
            Iso.tripleIso(t1, t2, labelMap) ;
        }
        return true ;
    }
    
    public void format(FormatterTemplate fmtTemplate)
    {
        fmtTemplate.format(this) ;
    }
}
