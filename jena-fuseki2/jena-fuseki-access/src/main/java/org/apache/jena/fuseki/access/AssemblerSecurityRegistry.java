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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.graph.GNode;
import org.apache.jena.sparql.util.graph.GraphList;
import org.apache.jena.sparql.util.graph.GraphUtils;

public class AssemblerSecurityRegistry extends AssemblerBase {

    /**
     * SecurityRegistry.
     * Builds a SecurityRegistry - list the graphs a user has access to.
     * <pre>
     * &lt;#securityRegistry&gt; rdf:type access:SecurityRegistry;
     *    access:entry ("user1" &lt;http://host/graphname1&gt;  &lt;http://host/graphname2&gt; );
     *    access:entry ("user1" &lt;http://host/graphname3&gt; );
     *    access:entry ("user2" &lt;http://host/graphname3&gt; );
     *    .
     * </pre>
     */
    @Override
    public AuthorizationService open(Assembler a, Resource root, Mode mode) {
        SecurityRegistry registry = new SecurityRegistry();
        // Java walking gives better error messages.
        StmtIterator sIter = root.listProperties(VocabSecurity.pEntry);
        if ( ! sIter.hasNext() )
            throw new AssemblerException(root, "No access entries");
        MultiValuedMap<String, Node> map = MultiMapUtils.newListValuedHashMap();

        sIter.forEachRemaining(s->{
            RDFNode n = s.getObject();
            if ( ! n.isResource())
                throw new AssemblerException(root, "Found access:entry with non-resource");

            Resource r = (Resource)n;
            GNode entry = new GNode(root.getModel().getGraph(), n.asNode());
            if ( GraphList.isListNode(entry) ) {
                // Format:: access:entry ("user1" <http://host/graphname1>  <http://host/graphname2> );
                parseList(map, root, entry);
            } else if ( r.hasProperty(VocabSecurity.pUser) || r.hasProperty(VocabSecurity.pGraphs) ) {
                // Format:: access:entry [ :user "user2"; :graphs (<http://host/graphname3> ) ]
                parseStruct(map, root, r);
            } else
                throw new AssemblerException(root, "Found access:entry but failed to parse the object: "+s.getSubject());
        });

        map.keySet().forEach(u->{
            SecurityContext sCxt = new SecurityContextView(map.get(u));
            registry.put(u, sCxt);
        });

        return registry;
    }

    /** Format:: access:entry ("user1" <http://host/graphname1>  <http://host/graphname2> ); */
    private void parseList(MultiValuedMap<String, Node> map, Resource root, GNode entry) {
        List<Node> members = GraphList.members(entry);
        // string, then URIs.
        if ( members.isEmpty() )
            throw new AssemblerException(root, "Found access:entry with an empty list");
        Node userNode = members.get(0);
        if ( !  Util.isSimpleString(userNode) )
            throw new AssemblerException(root, "User name is not a string: "+NodeFmtLib.strTTL(userNode));
        String user = userNode.getLiteralLexicalForm();
        List<Node> graphs = members.subList(1, members.size());
        accessEntries(root, map, user, graphs);
    }

    /** Format:: access:entry [ :user "user2"; :graphs (<http://host/graphname3> ) ] */
    private void parseStruct(MultiValuedMap<String, Node> map, Resource root, Resource r) {
        if ( ! GraphUtils.exactlyOneProperty(r, VocabSecurity.pUser) )
            throw new AssemblerException(root, "Expected exactly one access:user property for "+r);
        if ( ! GraphUtils.exactlyOneProperty(r, VocabSecurity.pGraphs) )
            throw new AssemblerException(root, "Expected exactly one access:graphs property for "+r);

        String user = GraphUtils.getStringValue(r, VocabSecurity.pUser);
        r.listProperties(VocabSecurity.pGraphs).mapWith(s->s.getObject()).forEachRemaining(x->{
            List<Node> graphs = new ArrayList<>();
            if ( x.isURIResource() ) {
                //System.out.printf("S: user %s : access : %s\n", user, x.asNode());
                graphs.add(x.asNode());
            } else {
                // List?
                RDFList list = x.as(RDFList.class);
                    list.iterator().forEachRemaining(rn->{
                        graphs.add(rn.asNode());
                    });
            }
            accessEntries(root, map, user, graphs);
        });
    }

    private Node graphLabel(Node x, Resource root) {
        if ( SecurityContext.allGraphsStr.equals(x) ) x = SecurityContext.allGraphs;
        if ( SecurityContext.allNamedGraphsStr.equals(x) ) x = SecurityContext.allNamedGraphs;
        if ( ! x.isURI() )
            throw new AssemblerException(root, "Not a graph name: "+x);
        return x;
    }

    // Unfinished.
    private final static boolean SKIP_ALLGRAPH = true;

    private void accessEntries(Resource root, MultiValuedMap<String, Node> map, String user, List<Node> _graphs) {
        // Convert string names for graphs to URIs.
        Set<Node> graphs = _graphs.stream().map(n->graphLabel(n, root)).collect(Collectors.toSet());

        if ( graphs.contains(SecurityContext.allGraphs) ) {
            map.remove(user);
            map.put(user, SecurityContext.allGraphs);
            return;
        }
        if ( graphs.contains(SecurityContext.allNamedGraphs) ) {
            boolean dft = dftPresent(graphs);
            Node x = SecurityContext.allNamedGraphs;
            if ( dft )
                // Put in "*" instead.
                x = SecurityContext.allGraphs;
            map.remove(user);
            map.put(user, x);
            return;
        }

        if ( SKIP_ALLGRAPH ) {
            if ( graphs.contains(SecurityContext.allGraphs) ) {
                Log.warn(this, "Graph name '"+SecurityContext.allGraphsStr+"' not supported yet");
                graphs.remove(SecurityContext.allGraphs);
            }
            if ( graphs.contains(SecurityContext.allNamedGraphs) ) {
                Log.warn(this, "Graph name '"+SecurityContext.allNamedGraphsStr+"' not supported yet");
                graphs.remove(SecurityContext.allNamedGraphs);
            }
        }

        map.putAll(user, graphs);
    }

    private boolean dftPresent(Collection<Node> nodes) {
        return nodes.stream().anyMatch(n->Quad.isDefaultGraph(n));
    }

}
