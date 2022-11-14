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

package org.apache.jena.sparql.util.graph ;

import java.util.*;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.util.NotUniqueException ;
import org.apache.jena.sparql.util.PropertyRequiredException ;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.sparql.util.TypeNotUniqueException ;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.vocabulary.RDF ;

/** Graph utilities. See also GraphFactory. */

public class GraphUtils {

    // Only used by DatasetDescriptionAssembler
    // (and DatasetDescriptionAssembler itself is unused)
    /**
     * Get all the literals for a resource-property.
     */
    public static List<String> multiValueString(Resource r, Property p) {
        List<RDFNode> nodes = multiValue(r, p) ;
        List<String> values = new ArrayList<>() ;

        for ( RDFNode n : nodes ) {
            if ( n.isLiteral() ) {
                values.add(((Literal)n).getString()) ;
            }
        }
        return values ;
    }

    /** Get a list of the URIs (as strings) and strings
     *  @see #getAsStringValue
     */
    public static List<String> multiValueAsString(Resource r, Property p) {
        List<RDFNode> nodes = multiValue(r, p) ;
        List<String> values = new ArrayList<>() ;

        for ( RDFNode n : nodes ) {
            if ( n.isLiteral() ) {
                values.add(((Literal)n).getString()) ;
            }
            if ( n.isURIResource() ) {
                values.add(((Resource)n).getURI());
            }
        }
        return values ;
    }

    public static List<RDFNode> multiValue(Resource r, Property p) {
        List<RDFNode> values = new ArrayList<>() ;
        StmtIterator sIter = r.listProperties(p) ;
        while (sIter.hasNext()) {
            Statement s = sIter.nextStatement() ;
            values.add(s.getObject()) ;
        }
        return values ;
    }

    public static List<Resource> multiValueResource(Resource r, Property p) {
        List<RDFNode> nodes = multiValue(r, p) ;
        List<Resource> values = new ArrayList<>() ;

        for ( RDFNode n : nodes ) {
            if ( n.isResource() ) {
                values.add((Resource)n) ;
            }
        }
        return values ;
    }

    public static List<String> multiValueURI(Resource r, Property p) {
        List<RDFNode> nodes = multiValue(r, p) ;
        List<String> values = new ArrayList<>() ;

        for ( RDFNode n : nodes ) {
            if ( n.isURIResource() ) {
                values.add(((Resource)n).getURI()) ;
            }
        }
        return values ;
    }

    public static boolean exactlyOneProperty(Resource r, Property p) {
        StmtIterator sIter = r.listProperties(p) ;
        try {
            if ( !sIter.hasNext() )
                throw new PropertyRequiredException(r, p) ;
            sIter.next() ;
            if ( sIter.hasNext() )
                throw new NotUniqueException(r, p) ;
        }
        finally {
            sIter.close() ;
        }
        return true ;
    }

    public static boolean atmostOneProperty(Resource r, Property p) {
        StmtIterator sIter = r.listProperties(p) ;
        try {
            if ( !sIter.hasNext() )
                return true ;
            sIter.next() ;
            if ( sIter.hasNext() )
                throw new NotUniqueException(r, p) ;
        }
        finally {
            sIter.close() ;
        }
        return true ;
    }

    public static boolean getBooleanValue(Resource r, Property p) {
        if ( !GraphUtils.atmostOneProperty(r, p) )
            throw new NotUniqueException(r, p) ;
        Statement s = r.getProperty(p) ;
        if ( s == null )
            throw new PropertyNotFoundException(p);
        return s.getBoolean();
    }

    /** Get a string literal. */
    public static String getStringValue(Resource r, Property p) {
        RDFNode obj = getAsRDFNode(r, p);
        if ( obj == null )
            return null;
        return obj.asLiteral().getString();
    }

    /** Get a string literal or a URI as a string. */
    public static String getAsStringValue(Resource r, Property p) {
        RDFNode obj = getAsRDFNode(r, p);
        if ( obj == null )
            return null;
        if ( obj.isResource() )
            return obj.asResource().getURI() ;
        if ( obj.isLiteral() )
            return obj.asLiteral().getString();
        throw new UnsupportedOperationException("Not a URI or a string");
    }

    public static RDFNode getAsRDFNode(Resource r, Property p) {
        if ( !atmostOneProperty(r, p) )
            throw new NotUniqueException(r, p) ;
        Statement s = r.getProperty(p) ;
        if ( s == null )
            return null ;
        return s.getObject();
    }

    public static Resource getResourceValue(Resource r, Property p) {
        if ( !atmostOneProperty(r, p) )
            throw new NotUniqueException(r, p) ;
        Statement s = r.getProperty(p) ;
        if ( s == null )
            return null ;
        return s.getResource() ;
    }

    public static List<Resource> listResourcesByType(Model model, Resource type) {
        return Iter.toList(model.listSubjectsWithProperty(RDF.type, type)) ;
    }

    public static Resource getResourceByType(Model model, Resource type) {
        ResIterator sIter = model.listSubjectsWithProperty(RDF.type, type) ;
        if ( !sIter.hasNext() )
            return null ;
        Resource r = sIter.next();
        if ( sIter.hasNext() )
            throw new TypeNotUniqueException(r) ;
        return r ;
    }

    public static Resource findRootByType(Model model, Resource atype) {
        String s = String.join("\n",
            "PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
            "PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
            "SELECT DISTINCT ?root { { ?root rdf:type ?ATYPE } UNION { ?root rdf:type ?t . ?t rdfs:subClassOf ?ATYPE } }") ;
        Query q = QueryFactory.create(s) ;
        QuerySolutionMap qsm = new QuerySolutionMap() ;
        qsm.add("ATYPE", atype) ;

        try(QueryExecution qExec = QueryExecution.model(model).query(q).initialBinding(qsm).build() ) {
            return (Resource)QueryExecUtils.getAtMostOne(qExec, "root") ;
        }
    }

    public static List<Resource> findRootsByType(Model model, Resource atype) {
        String s = String.join("\n",
            "PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
            "PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
            "SELECT DISTINCT ?root { { ?root rdf:type ?ATYPE } UNION { ?root rdf:type ?t . ?t rdfs:subClassOf ?ATYPE } }") ;
        Query q = QueryFactory.create(s) ;
        QuerySolutionMap qsm = new QuerySolutionMap() ;
        qsm.add("ATYPE", atype) ;
        try(QueryExecution qExec = QueryExecution.model(model).query(q).initialBinding(qsm).build() ) {
            return ListUtils.toList(
                    QueryExecUtils.getAll(qExec, "root").stream().map(r->(Resource)r));

        }
    }

    public static String fmtURI(Resource r) {
        return r.getModel().shortForm(r.getURI()) ;
    }

    /** All subjects and objects, no duplicates. */
    public static Iterator<Node> allNodes(Graph graph) {
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
        IterSO iterSO = new IterSO(iter);
        Iterator<Node> distinctIterator = Iter.distinct(iterSO);
        return distinctIterator;
    }

    static class IterSO extends NiceIterator<Node> {
        private ExtendedIterator<Triple> it;
        private boolean tripleConsumed;
        private Triple triple;

        IterSO(ExtendedIterator<Triple> it) {
            this.it = it;
            this.tripleConsumed = true;
        }

        @Override
        public void close() {
            it.close();
        }

        @Override
        public boolean hasNext() {
            return !this.tripleConsumed || it.hasNext();
        }

        @Override
        public Node next() {
            if (this.tripleConsumed) {
                triple = it.next();
                tripleConsumed = false;
                return triple.getSubject();
            } else {
                tripleConsumed = true;
                return triple.getObject();
            }
        }
    }
}
