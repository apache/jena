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

package com.hp.hpl.jena.sparql.util.graph;

import java.util.ArrayList ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QuerySolutionMap ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.rdf.model.StmtIterator ;
import com.hp.hpl.jena.sparql.util.NotUniqueException ;
import com.hp.hpl.jena.sparql.util.PropertyRequiredException ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.sparql.util.TypeNotUniqueException ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.vocabulary.RDF ;

/** Graph utilities.  See also GraphFactory. */ 

public class GraphUtils
{

    public static List<String> multiValueString(Resource r, Property p)
    {
        List<RDFNode> nodes = multiValue(r, p) ;
        List<String> values = new ArrayList<>() ;

        for ( RDFNode n : nodes )
        {
            if ( n.isLiteral() )
            {
                values.add( ( (Literal) n ).getString() );
            }
        }
        return values ;
    }

    public static List<RDFNode> multiValue(Resource r, Property p)
    {
        List<RDFNode> values = new ArrayList<>() ;
        StmtIterator sIter = r.listProperties(p) ;
        while(sIter.hasNext())
        {
            Statement s = sIter.nextStatement() ;
            values.add(s.getObject()) ;
        }
        return values;
    }
    
    public static List<Resource> multiValueResource(Resource r, Property p)
    {
        List<RDFNode> nodes = multiValue(r, p) ;
        List<Resource> values = new ArrayList<>() ;

        for ( RDFNode n : nodes )
        {
            if ( n.isResource() )
            {
                values.add( (Resource) n );
            }
        }
        return values ;
    }

    public static List<String> multiValueURI(Resource r, Property p)
    {
        List<RDFNode> nodes = multiValue(r, p) ;
        List<String> values = new ArrayList<>() ;

        for ( RDFNode n : nodes )
        {
            if ( n.isURIResource() )
            {
                values.add( ( (Resource) n ).getURI() );
            }
        }
        return values ;
    }

    public static boolean exactlyOneProperty(Resource r, Property p)
    {
        StmtIterator sIter = r.listProperties(p) ;
        try {
            if ( ! sIter.hasNext() )
                throw new PropertyRequiredException(r, p) ;
            sIter.next() ;
            if ( sIter.hasNext() )
                throw new NotUniqueException(r, p) ;
        } finally { sIter.close() ; } 
        return true ;
    }

    public static boolean atmostOneProperty(Resource r, Property p)
    {
        StmtIterator sIter = r.listProperties(p) ;
        try {
            if ( ! sIter.hasNext() ) return true ;
            sIter.next() ;
            if ( sIter.hasNext() )
                throw new NotUniqueException(r, p) ;
        } finally { sIter.close() ; } 
        return true ;
    }

    public static String getStringValue(Resource r, Property p)
    {
        if ( ! atmostOneProperty(r, p) )
            throw new NotUniqueException(r, p) ;
        Statement s = r.getProperty(p) ;
        if ( s == null )
            return null ;
        return s.getString() ; 
    }

    public static String getAsStringValue(Resource r, Property p)
    {
        if ( ! atmostOneProperty(r, p) )
            throw new NotUniqueException(r, p) ;
        Statement s = r.getProperty(p) ;
        if ( s == null )
            return null ;
        if ( s.getObject().isResource() )
            return s.getResource().getURI() ;
        return s.getString() ; 
    }

    public static Resource getResourceValue(Resource r, Property p)
    {
        if ( ! atmostOneProperty(r, p) )
            throw new NotUniqueException(r, p) ;
        Statement s = r.getProperty(p) ;
        if ( s == null )
            return null ;
        return s.getResource() ;
    }

    public static Resource getResourceByType(Model model, Resource type)
    {
        // See also 
        StmtIterator sIter = model.listStatements(null, RDF.type, type) ;
        if ( ! sIter.hasNext() )
            return null ;
        Resource r = sIter.nextStatement().getSubject() ;
        if ( sIter.hasNext() )
            throw new TypeNotUniqueException(r) ;
        return r ;
    }
    
    public static Resource findRootByType(Model model, Resource atype)
    {
        String s = StrUtils.strjoin("\n", 
            "PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" ,
            "PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
            "SELECT DISTINCT ?root { { ?root rdf:type ?ATYPE } UNION { ?root rdf:type ?t . ?t rdfs:subClassOf ?ATYPE } }") ;
        
        Query q = QueryFactory.create(s) ;
        QuerySolutionMap qsm = new QuerySolutionMap() ;
        qsm.add("ATYPE", atype) ;

        QueryExecution qExec = QueryExecutionFactory.create(q, model, qsm);
        Resource r = (Resource)QueryExecUtils.getOne(qExec, "root") ;
        return r;
    }
    
    
    public static String fmtURI(Resource r)
    { return r.getModel().shortForm(r.getURI()) ;  }
    
    /** All subjects and objects, no duplicates. */
    public static Iterator<Node> allNodes(Graph graph)
    {
        Set<Node> x = new HashSet<>(1000) ;
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
        for ( ; iter.hasNext() ; )
        {
            Triple t = iter.next();
            x.add(t.getSubject()) ;
            x.add(t.getObject()) ;
        }
        iter.close() ;
        return x.iterator() ;
    }
}
