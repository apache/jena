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

package com.hp.hpl.jena.sparql.util;

import java.util.HashMap ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Node ;

/** Map nodes to blank node representations. */ 
public class NodeToLabelMap
{
    // Could abstract again as a node -> label cache + cache miss handler.
    int bNodeCounter = 0 ;
    Map<Node, String> bNodeStrings = new HashMap<>() ;
    boolean bNodesAsFakeURIs = false ;
    String prefixString = "b" ;
    
    public NodeToLabelMap() { this("b") ; }
    
    public NodeToLabelMap(String prefix) { this(prefix, false) ; }
    
    public NodeToLabelMap(String prefix, boolean bNodesAsFakeURIs)
    {
        if ( prefix == null || prefix.equals("") )
            throw new IllegalArgumentException("Must provide a prefix") ;
        this.prefixString = "_:"+prefix ;
        this.bNodesAsFakeURIs = bNodesAsFakeURIs ;
    }
    
    // Null means not mapped
    public String asString(Node n)
    { 
        if ( ! n.isBlank() )
            return null ;
        
        return mapNode(n) ;
    }

    // synchronized because this may be used from a static via FmtUtils
    protected synchronized String mapNode(Node n)
    {
        String s = bNodeStrings.get(n) ;
        if ( s != null )
            return s ;
        
        s = genStringForNode(n) ;
        bNodeStrings.put(n, s) ;
        return s ;
    }

    protected String genStringForNode(Node n)
    {
        if ( bNodesAsFakeURIs && n.isBlank() )
            return "<_:"+n.getBlankNodeId().getLabelString()+">" ;

        return  prefixString+(bNodeCounter++) ;
    }

//    /**
//     * @return Returns the prefix.
//     */
//    public String getPrefixString()
//    {
//        return prefixString ;
//    }
//
//    /**
//     * @param prefix The prefix to set.
//     */
//    public void setPrefixString(String prefix)
//    {
//        if ( prefix == null )
//        {
//            Log.fatal(this,"Prefix string is null") ;
//            throw new ARQInternalErrorException("Prefix string is null") ;
//        }
//        if ( prefix.equals("") )
//        {
//            Log.fatal(this,"Prefix string is the empty string") ;
//            throw new ARQInternalErrorException("Prefix string is the empty string") ;
//        }
//            
//        this.prefixString = prefix ;
//    }
}
