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
import static org.apache.jena.riot.out.NodeFmtLib.displayStr;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDFS;

/**
 * Library code for operations related to building Fuseki servers and services.
 */
/*package*/ class BuildLib {

    private BuildLib() {}

    // ---- Helper code
    /*package*/ static ResultSet query(String string, Model m) {
        return query(string, m, null, null);
    }

    /*package*/ static RDFNode queryOne(String string, Model m, String varname) {
        ResultSet rs = query(string, m);
        return getExactlyOne(rs, varname);
    }

    private static RDFNode getExactlyOne(ResultSet rs, String varname) {
        if ( ! rs.hasNext() )
            return null;
        QuerySolution qs = rs.next();
        if ( rs.hasNext() )
            return null;
        return qs.get(varname);
    }

    /*package*/ static ResultSet query(String string, Dataset ds) {
        return query(string, ds, null, null);
    }

    /*package*/ static ResultSet query(String string, Model m, String varName, RDFNode value) {
        Query query = QueryFactory.create(PREFIXES + string);
        QuerySolutionMap initValues = null;
        if ( varName != null && value != null )
            initValues = querySolution(varName, value);
        try ( QueryExecution qExec = QueryExecution.create().query(query).model(m).initialBinding(initValues).build() ) {
            return ResultSetFactory.copyResults(qExec.execSelect());
        }
    }

    /*package*/ static ResultSet query(String string, Dataset ds, String varName, RDFNode value) {
        Query query = QueryFactory.create(PREFIXES + string);
        QuerySolutionMap initValues = null;
        if ( varName != null && value != null )
            initValues = querySolution(varName, value);
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, ds, initValues) ) {
            return ResultSetFactory.copyResults(qExec.execSelect());
        }
    }

    private static QuerySolutionMap querySolution(String varName, RDFNode value) {
        QuerySolutionMap qsm = new QuerySolutionMap();
        querySolution(qsm, varName, value);
        return qsm;
    }

    /*package*/ static QuerySolutionMap querySolution(QuerySolutionMap qsm, String varName, RDFNode value) {
        qsm.add(varName, value);
        return qsm;
    }

    /*package*/ static RDFNode getOne(Resource svc, Property property) {
        ResultSet rs = BuildLib.query("SELECT * { ?svc <" + property.getURI() + "> ?x}", svc.getModel(), "svc", svc);
        if ( !rs.hasNext() )
            throw new FusekiConfigException("No property '" + property + "' for service " + BuildLib.nodeLabel(svc));
        RDFNode x = rs.next().get("x");
        if ( rs.hasNext() )
            throw new FusekiConfigException("Multiple properties '" + property + "' for service " + BuildLib.nodeLabel(svc));
        return x;
    }

    /**
     * Get all object of a subject/property, whether repeated triples or RDF Lists or a
     * mixture. If the subject/property isn't present, return null, so a caller can tell
     * the difference between "not present" and an empty list value.
     */
    /*package*/ static Collection<RDFNode> getAll(Resource resource, String property) {
        ResultSet rs = BuildLib.query("SELECT * { ?subject " + property + " ?x}", resource.getModel(), "subject", resource);
        if ( ! rs.hasNext() )
            return null;
        List<RDFNode> results = new ArrayList<>();
        rs.forEachRemaining(qs->{
            RDFNode n = qs.get("x");
            try {
                RDFList list = n.as(RDFList.class);
                results.addAll(list.asJavaList());
            } catch (JenaException x) {
                // Not a list.
                results.add(n);
            }
        });
        return results;
    }

    // Node presentation
    /*package*/ static String nodeLabel(RDFNode n) {
        if ( n == null )
            return "<null>";
        if ( n instanceof Resource )
            return strForResource((Resource)n);

        Literal lit = (Literal)n;
        return lit.getLexicalForm();
    }

    /*package*/ static String strForResource(Resource r) {
        return strForResource(r, r.getModel());
    }

    /*package*/ static String strForResource(Resource r, PrefixMapping pm) {
        if ( r == null )
            return "NULL ";
        if ( r.hasProperty(RDFS.label) ) {
            RDFNode n = r.getProperty(RDFS.label).getObject();
            if ( n instanceof Literal )
                return ((Literal)n).getString();
        }

        if ( r.isAnon() )
            return "<<blank node>>";

        if ( pm == null )
            pm = r.getModel();

        return strForURI(r.getURI(), pm);
    }

    /*package*/ static String strForURI(String uri, PrefixMapping pm) {
        if ( pm != null ) {
            String x = pm.shortForm(uri);

            if ( !x.equals(uri) )
                return x;
        }
        return "<" + uri + ">";
    }

    /*package*/ static RDFNode getZeroOrOne(Resource ep, Property property) {
        StmtIterator iter = ep.listProperties(property);
        try {
            if ( ! iter.hasNext() )
                return null;
            RDFNode x = iter.next().getObject();
            if ( iter.hasNext() )
                throw new FusekiConfigException("Multiple triples for "+displayStr(ep)+" "+displayStr(property));
            return x;
        } finally { iter.close(); }
    }

    /** Load a class (an {@link ActionService}) and create an {@link Operation} for it. */
    /*package*/ static Pair<Operation, ActionService> loadOperationActionService(RDFNode implementation) {
        String classURI = implementation.isLiteral()
            ? implementation.asLiteral().getLexicalForm()
            : ((Resource)implementation).getURI();
        String javaScheme = "java:";
        String fileScheme = "file:";
        String scheme = null;
        if ( classURI.startsWith(javaScheme) ) {
            scheme = javaScheme;
        } else if ( classURI.startsWith(fileScheme) ) {
            scheme = fileScheme;
        } else {
            Fuseki.configLog.error("Class to load is not 'java:' or 'file:': " + classURI);
            throw new FusekiConfigException("Not a 'java:' or 'file:' class reference: "+classURI);
        }
        String className = classURI.substring(scheme.length());

        ActionService action = null;
        try {
            Class<?> cls;
            if ( Objects.equals(scheme, fileScheme) ) {
                try ( URLClassLoader urlClassLoader = new URLClassLoader(new URL[] {new URL(classURI)}) ){
                    cls = Class.forName(className, true, urlClassLoader);
                }
            } else {
                cls = Class.forName(className);
            }
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
}
