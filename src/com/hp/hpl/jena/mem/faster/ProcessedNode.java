/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: ProcessedNode.java,v 1.2 2005-07-08 15:41:34 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.faster;

import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.shared.*;

/**
    Processed nodes for Query: a ProcessedNode knows its Domain index, if
    relevant, and what to do for (a) delivering the Node value to use in the
    query's called for <code>find[Faster]</code>, and (b) if and how to match
    a possible triple answer.
     
    @author kers
 */
public class ProcessedNode
    {    
    public final Node node;
    public final int index;
    
    public ProcessedNode( Node node )
        { this( node, -1 ); }
    
    public ProcessedNode( Node node, int index )
        { this.node = node; this.index = index; }
    
    public boolean mustMatch()
        { return false; }
    
    public boolean match( Domain d, Node X )
        { throw new BrokenException( "ARGH" ); }
    
    public Node finder( Domain d )
        { return Node.ANY; }
    
    public String toString()
        { return node.toString() + "[" + index + "]"; }
    
    public static ProcessedNode allocateBindings( Mapping map, Set local, Node X )
        {
        if (X.equals( Node.ANY ))
            return new Any();
        if (X.isVariable())
            {
            if (map.hasBound( X ))
                {
                if (local.contains( X ))
                    return new JBound( X, map.indexOf( X ) );
                else
                    return new Bound( X, map.indexOf( X ) );
                }
            else
                {
                local.add( X );
                return new Bind( X, map.newIndex( X ) );
                }
            }
        return 
            new Fixed( X );
        }

    public static class Fixed extends ProcessedNode
        {
        public Fixed( Node node ) 
            { super( node ); }
        
        public Node finder( Domain d )
            { return node; }
        }
    
    public static class Bind extends ProcessedNode
        {
        public Bind( Node node, int index )
            { super( node, index ); }
        
        public boolean mustMatch()
            { return true; }
        
        public boolean match( Domain d, Node X )
            {
            d.setElement( index, X );
            return true;
            }
        }
    
    public static class Bound extends ProcessedNode
        {
        public Bound( Node node, int index )
            { super( node, index ); }
        
        public Node finder( Domain d )
            { return d.getElement( index ); }
        }
    
    public static class JBound extends ProcessedNode
        {
        public JBound( Node node, int index )
            { super( node, index ); }
        
        public boolean mustMatch()
            { return true; }
        
        public boolean match( Domain d, Node X )
            { return X.matches( d.getElement( index ) ); }
        }
    
    public static class Any extends ProcessedNode
        {
        public Any()
            { super( Node.ANY ); }
        }
    }

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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