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

package org.apache.jena.query.spatial;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;

public class SpatialQueryFuncs {
    
    /** Create a string to put in a Lucene index for the subject node */  
    public static String subjectToString(Node s) {
        if ( s == null )
            throw new IllegalArgumentException("Subject node can not be null") ;
        if ( ! (s.isURI() || s.isBlank() ) )
            throw new SpatialIndexException("Found a subject that is not a URI nor a blank node: "+s) ; 
        return nodeToString(s) ;
    }

    /** Create a string to put in a Lucene index for a graph node */  
    public static String graphNodeToString(Node g) {
        if ( g == null )
            return null ;
        if ( ! (g.isURI() || g.isBlank() ) )
            throw new SpatialIndexException("Found a graph label that is not a URI nor a blank node: "+g) ; 
        return nodeToString(g) ;
    }

    private static String nodeToString(Node n) {
        return (n.isURI() ) ? n.getURI() : "_:" + n.getBlankNodeLabel() ;
    }

    /** Recover a Node from a stored Lucene string */
    public static Node stringToNode(String v) {
        if ( v.startsWith("_:") ) {
            v = v.substring("_:".length()) ;
            return NodeFactory.createBlankNode(v) ;
        }
        else
            return NodeFactory.createURI(v) ;
    }
}

