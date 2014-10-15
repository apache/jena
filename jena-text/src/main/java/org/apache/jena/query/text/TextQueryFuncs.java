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

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

/** Functions relateing to text query */
public class TextQueryFuncs {

    /** Create a string to put in a Lucene/Solr index for the subject node */  
    public static String subjectToString(Node s) {
        if ( s == null )
            throw new IllegalArgumentException("Subject node can not be null") ;
        if ( ! (s.isURI() || s.isBlank() ) )
            throw new TextIndexException("Found a subject that is not a URI nor a blank node: "+s) ; 
        return nodeToString(s) ;
    }

    /** Create a string to put in a Lucene/Solr index for a graph node */  
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

    /** Recover a Node from a stored Lucene/Solr string */
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
    
        String x = TextQueryFuncs.subjectToString(s) ;
        String graphText = TextQueryFuncs.graphNodeToString(g) ;
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

