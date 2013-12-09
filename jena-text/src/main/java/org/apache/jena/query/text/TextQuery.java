/**
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

package org.apache.jena.query.text;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.text.assembler.TextAssembler ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.lib.Metadata ;
import com.hp.hpl.jena.sparql.mgt.ARQMgt ;
import com.hp.hpl.jena.sparql.mgt.SystemInfo ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunction ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionFactory ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionRegistry ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.tdb.TDB ;

public class TextQuery
{
    private static boolean initialized = false ;
    private static Object lock = new Object() ;
    public static String NS = "http://jena.apache.org/text#" ;
    public static String IRI = "http://jena.apache.org/#text" ;
    public static final Symbol textIndex = Symbol.create(NS+"index") ;
    public static final String PATH         = "org.apache.jena.query.text";
    
    static private String metadataLocation  = "org/apache/jena/query/text/properties.xml" ;
    static private Metadata metadata        = new Metadata(metadataLocation) ;
    public static final String NAME         = "ARQ Text Query";
   
    public static final String VERSION      = metadata.get(PATH+".version", "unknown") ;
    public static final String BUILD_DATE   = metadata.get(PATH+".build.datetime", "unset") ;
    
    static { init() ; }
    
    public static void init() 
    {
        if ( initialized ) return ;
        synchronized(lock)
        {
            if ( initialized ) return ;
            initialized = true ;
            TDB.init() ;
            TextAssembler.init() ;
            
            SystemInfo sysInfo = new SystemInfo(IRI, VERSION, BUILD_DATE) ;
            ARQMgt.register(PATH+".system:type=SystemInfo", sysInfo) ;
            SystemARQ.registerSubSystem(sysInfo) ;
            
            PropertyFunctionRegistry.get().put("http://jena.apache.org/text#query", new PropertyFunctionFactory() {
                @Override
                public PropertyFunction create(String uri) {
                    return new TextQueryPF() ;
                }
            });
        }
    }
    
    public static String subjectToString(Node s) {
        if ( s == null )
            throw new IllegalArgumentException("Subject node can not be null") ;
        if ( ! (s.isURI() || s.isBlank() ) )
            throw new TextIndexException("Found a subject that is not a URI nor a blank node: "+s) ; 
        return nodeToString(s) ;
    }
    
    public static String graphNodeToString(Node g) {
        if ( g == null )
            return null ;
        if ( ! (g.isURI() || g.isBlank() ) )
            throw new TextIndexException("Found a graph label that is not a URI nor a blank node: "+g) ; 
        return nodeToString(g) ;
    }
    
    private static String nodeToString(Node n) {
        return (n.isURI() ) ? n.getURI() : "_:" + n.getBlankNodeLabel() ;
    }

    /** Reverse the translation of Nodes to string stored in indexes */
    public static Node stringToNode(String v) {
        if ( v.startsWith("_:") ) {
            v = v.substring("_:".length()) ;
            return NodeFactory.createAnon(new AnonId(v)) ;
        }
        else
            return NodeFactory.createURI(v) ;
    }

    /** Create an Entity from a quad.
     * Returns null if the quad is not a candidate for indexing.
     */
    public static Entity entityFromQuad(EntityDefinition defn , Quad quad ) {
        return entityFromQuad(defn, quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
    }

    /** Create an Entity from a quad (as g/s/p/o).
     * Returns null if the quad is not a candidate for indexing.
     */
    public static Entity entityFromQuad(EntityDefinition defn , Node g , Node s , Node p , Node o ) {
        String field = defn.getField(p) ;
        if ( field == null )
            return null ;

        String x = TextQuery.subjectToString(s) ;
        String graphText = TextQuery.graphNodeToString(g) ;
        Entity entity = new Entity(x, graphText) ;
        String graphField = defn.getGraphField() ;
        if ( defn.getGraphField() != null )
            entity.put(graphField, graphText) ;

        if ( !o.isLiteral() ) {
            Log.warn(TextQuery.class, "Not a literal value for mapped field-predicate: " + field + " :: "
                     + FmtUtils.stringForString(field)) ;
            return null ;
        }
        entity.put(field, o.getLiteralLexicalForm()) ;
        return entity ;
    }
}


