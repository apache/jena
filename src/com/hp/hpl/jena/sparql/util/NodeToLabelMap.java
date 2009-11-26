/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;

/** Map nodes to blank node representations.
 * 
 * @author Andy Seaborne
 */ 
public class NodeToLabelMap
{
    // Could abstract again as a node -> label cache + cache miss handler.
    int bNodeCounter = 0 ;
    Map<Node, String> bNodeStrings = new HashMap<Node, String>() ;
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

    protected String mapNode(Node n)
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
//            ALog.fatal(this,"Prefix string is null") ;
//            throw new ARQInternalErrorException("Prefix string is null") ;
//        }
//        if ( prefix.equals("") )
//        {
//            ALog.fatal(this,"Prefix string is the empty string") ;
//            throw new ARQInternalErrorException("Prefix string is the empty string") ;
//        }
//            
//        this.prefixString = prefix ;
//    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */