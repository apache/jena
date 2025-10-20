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

package org.apache.jena.fuseki.build;

import static org.apache.jena.fuseki.build.FusekiPrefixes.PREFIXES;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.graph.GNode;
import org.apache.jena.sparql.util.graph.GraphList;
import org.apache.jena.system.G;
import org.apache.jena.vocabulary.RDFS;

/**
 * Library code for operations related to building Fuseki servers and services.
 */
/*package*/ class BuildLib {

    private BuildLib() {}

    /*package*/ static RowSet query(String string, Graph graph) {
        return query(string, graph, null, null);
    }

    /*package*/ static RowSet query(String string, Graph graph, String varName, Node value) {
        Query query = QueryFactory.create(PREFIXES + string);
        QueryExecBuilder qExec = QueryExec.graph(graph).query(query);
        if ( varName != null && value != null )
            qExec.substitution(varName, value);
        return qExec.build().select().materialize();
    }

    private static Binding querySubstitution(String varName, Node value) {
        Var var = Var.alloc(varName);
        return BindingFactory.binding(var, value);
    }

    /*package*/ static Node getOne(Graph graph, Node subject, Node property) {
        List<Node> x = G.listSP(graph, subject, property);
        if ( x.isEmpty() )
            throw new FusekiConfigException("No property '" + BuildLib.displayStr(graph, property) + "' for service " + BuildLib.displayStr(graph, subject));
        if ( x.size() > 1 )
            throw new FusekiConfigException("Multiple properties '" + BuildLib.displayStr(graph, property) + "' for service " + BuildLib.displayStr(graph, subject));
        return x.get(0);
    }

    /*package*/ static Node getZeroOrOne(Graph graph, Node subject, Node property) {
        List<Node> x = G.listSP(graph, subject, property);
        if ( x.isEmpty() )
            return null;
        if ( x.size() > 1 )
            throw new FusekiConfigException("Multiple triples for "+displayStr(graph, subject)+" "+displayStr(graph, property));
        return x.get(0);
    }

    /**
     * Get all object of a subject/property, whether repeated triples or RDF Lists or a
     * mixture. If the subject/property isn't present, return null, so a caller can tell
     * the difference between "not present" and an empty list value.
     */
    /*package*/ static Collection<Node> getMultiple(Graph graph, Node resource, Node property) {
        List<Node> nodes = G.listSP(graph, resource, property);
        if ( nodes.isEmpty() )
            return null;
        // For mall lists (one level) - expand by members.
        List<Node> results = new ArrayList<>();

        nodes.forEach(node->{
            List<Node> members = listMembers(graph, node);
            if ( members != null )
                results.addAll(members);
            else
                results.add(node);
        });
        return results;
    }

    private static List<Node> listMembers(Graph graph, Node node) {
        GNode gnode = new GNode(graph, node);
        if ( ! GraphList.isListNode(gnode) )
            return null;
        List<Node> list = GraphList.members(gnode);
        return list;
    }

    // Node presentation

    /*package*/ static String displayStr(Graph graph, Node n) {
        if ( n == null )
            return "NULL";
        if ( graph == null )
            return NodeFmtLib.str(n, null);

        PrefixMap prefixMap = Prefixes.adapt(graph);
        return NodeFmtLib.str(n, null, prefixMap);
    }

    /*package*/ static String strForResource(Graph graph, Node node) {
        if ( node == null )
            return "NULL";
        if ( G.hasProperty(graph, node, RDFS.Nodes.label) ) {

            Node label = G.getOneSP(graph, node, RDFS.Nodes.label);
            if ( label.isLiteral() )
                return label.getLiteralLexicalForm();
        }

        if ( node.isBlank() )
            return "<<blank node>>";

        if ( node.isURI() )
            return strForURI(graph, node.getURI());
        throw notSupported(node);
    }

    private static RuntimeException notSupported(Node node) {
        return new JenaException("Not supported: "+node);
    }

    /*package*/ static String strForURI(Graph graph, String uri) {
        if ( graph != null ) {
            PrefixMap prefixMap = Prefixes.adapt(graph);
            String x = graph.getPrefixMapping().qnameFor(uri);
            if ( x != null )
                return x;
        }
        return "<" + uri + ">";
    }

    /** Load a class (an {@link ActionService}) and create an {@link Operation} for it. */
    /*package*/ static Pair<Operation, ActionService> loadOperationActionService(Graph graph, Node implementation) {
        String classURI = implementation.isLiteral()
            ? implementation.getLiteralLexicalForm()
            : implementation.getURI();
        String javaScheme = "java:";
        String scheme = null;
        if ( classURI.startsWith(javaScheme) ) {
            scheme = javaScheme;
        } else {
            Fuseki.configLog.error("Class to load is not 'java:' " + classURI);
            throw new FusekiConfigException("Not a 'java:' class reference: "+classURI);
        }
        String className = classURI.substring(scheme.length());

        ActionService action = null;
        try {
            Class<?> cls = Class.forName(className);
            Constructor<?> x = cls.getConstructor();
            action = (ActionService)x.newInstance();
        } catch (ClassNotFoundException ex) {
            throw new FusekiConfigException("Class not found: " + className);
        } catch (Exception ex) {
            throw new FusekiConfigException("Can't create object from " + className);
        }
        Operation op = Operation.alloc(NodeFactory.createBlankNode(), classURI, classURI);
        return Pair.create(op, action);
    }

    // With file: file loading ...
//    /** Load a class (an {@link ActionService}) and create an {@link Operation} for it. */
//    /*package*/ static Pair<Operation, ActionService> loadOperationActionService(Graph graph, Node implementation) {
//        String classURI = implementation.isLiteral()
//            ? implementation.getLiteralLexicalForm()
//            : implementation.getURI();
//        String javaScheme = "java:";
//        String fileScheme = "file:";
//        String scheme = null;
//        if ( classURI.startsWith(javaScheme) ) {
//            scheme = javaScheme;
//        } else if ( classURI.startsWith(fileScheme) ) {
//            scheme = fileScheme;
//        } else {
//            Fuseki.configLog.error("Class to load is not 'java:' or 'file:': " + classURI);
//            throw new FusekiConfigException("Not a 'java:' or 'file:' class reference: "+classURI);
//        }
//        String className = classURI.substring(scheme.length());
//
//        ActionService action = null;
//        try {
//            Class<?> cls;
//            if ( Objects.equals(scheme, fileScheme) ) {
//                try ( URLClassLoader urlClassLoader = new URLClassLoader(new URL[] {new URL(classURI)}) ){
//                    cls = Class.forName(className, true, urlClassLoader);
//                }
//            } else {
//                cls = Class.forName(className);
//            }
//            Constructor<?> x = cls.getConstructor();
//            action = (ActionService)x.newInstance();
//        } catch (ClassNotFoundException ex) {
//            throw new FusekiConfigException("Class not found: " + className);
//        } catch (Exception ex) {
//            throw new FusekiConfigException("Can't create object from " + className);
//        }
//        Operation op = Operation.alloc(NodeFactory.createBlankNode(), classURI, classURI);
//        return Pair.create(op, action);
//    }
}
