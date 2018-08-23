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

package org.apache.jena.fuseki.access;

import java.util.List;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.ext.com.google.common.collect.ArrayListMultimap;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.sparql.util.graph.GNode;
import org.apache.jena.sparql.util.graph.GraphList;
import org.apache.jena.sparql.util.graph.GraphUtils;

public class AssemblerSecurityRegistry extends AssemblerBase {

    /**
     * SecurityRegistry.
     * Builds a SecurityRegistry - a map fron user name to 
     * 
     * <#securityRegistry> rdf:type access:SecurityRegistry ;
     *    access:entry ("user1" <http://host/graphname1>  <http://host/graphname2> ) ;) ;
     *    access:entry ("user1" <http://host/graphname3> ) ;
     *    access:entry ("user2" <http://host/graphname3> ) ;
     *    .
     * 
     * access:entry [ :user "user2" ; :graphs (<http://host/graphname3> ) ] ;
     */
    
    @Override
    public SecurityRegistry open(Assembler a, Resource root, Mode mode) {
        SecurityRegistry registry = new SecurityRegistry();
        // Java walking gives better error messages.
        StmtIterator sIter = root.listProperties(VocabSecurity.pEntry);
        if ( ! sIter.hasNext() )
            throw new AssemblerException(root, "No access entries");
        Multimap<String, Node> map = ArrayListMultimap.create();
        
        sIter.forEachRemaining(s->{
            RDFNode n = s.getObject(); 
            if ( ! n.isResource()) 
                throw new AssemblerException(root, "Found access:entry with non-resource"); 
            Resource r = (Resource)n;
            GNode entry = new GNode(root.getModel().getGraph(), n.asNode());
            if ( GraphList.isListNode(entry) ) {
                // Format:: access:entry ("user1" <http://host/graphname1>  <http://host/graphname2> ) ;
                parseList(map, root, entry);
            } else if ( r.hasProperty(VocabSecurity.pUser) || r.hasProperty(VocabSecurity.pGraphs) ) {
                // Format:: access:entry [ :user "user2" ; :graphs (<http://host/graphname3> ) ]
                parseStruct(map, root, r);
            } else
                throw new AssemblerException(root, "Found access:entry but failed to parse the object: "+s.getSubject());
        });
        
        map.keySet().forEach(u->{
            SecurityPolicy policy = new SecurityPolicy(map.get(u));
            registry.put(u, policy);
        });
        
        return registry;
    }

    /**Format:: access:entry ("user1" <http://host/graphname1>  <http://host/graphname2> ) ; */
    private void parseList(Multimap<String, Node> map, Resource root, GNode entry) {
        List<Node> members = GraphList.members(entry);
        // string, then URIs.
        if ( members.isEmpty() )
            throw new AssemblerException(root, "Found access:entry with an empty list");
        Node userNode = members.get(0);
        if ( !  Util.isSimpleString(userNode) ) {}
        String user =  userNode.getLiteralLexicalForm();
        for ( int i = 1 ; i < members.size() ; i++ ) {
            Node gn = members.get(i);
            //if ( gn.isBlank() )
            if ( ! gn.isURI() ) { }
            //System.out.printf("L: user %s : access : %s\n", user, gn);
            map.put(user, gn);
        }
    }

    /** Format:: access:entry [ :user "user2" ; :graphs (<http://host/graphname3> ) ] */
    private void parseStruct(Multimap<String, Node> map, Resource root, Resource r) {
        if ( ! GraphUtils.exactlyOneProperty(r, VocabSecurity.pUser) )
            throw new AssemblerException(root, "Expected exactly one access:user property for "+r); 
        if ( ! GraphUtils.exactlyOneProperty(r, VocabSecurity.pGraphs) )
            throw new AssemblerException(root, "Expected exactly one access:graphs property for "+r); 
        
        String user = GraphUtils.getStringValue(r, VocabSecurity.pUser);
        r.listProperties(VocabSecurity.pGraphs).mapWith(s->s.getObject()).forEachRemaining(x->{
            if ( x.isURIResource() ) {
                //System.out.printf("S: user %s : access : %s\n", user, x.asNode());
                map.put(user, x.asNode());
            } else {
                // List?
                RDFList list = x.as(RDFList.class);
                list.iterator().forEachRemaining(rn->{
                    if ( ! rn.isURIResource() )
                        throw new AssemblerException(root, "Not a graph name: "+rn);
                    //System.out.printf("S: user %s : access : %s\n", user, rn.asNode());
                    map.put(user, rn.asNode());
                });
            }
        });
    }

}
