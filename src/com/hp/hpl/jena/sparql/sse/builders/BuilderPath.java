/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.builders;

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.path.* ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.Tags ;

public class BuilderPath
{
    public static Path buildPath(Item item)
    {
        BuilderPath bob = new BuilderPath() ;
        return bob.build(item) ;
    }
    
    public static TriplePath buildTriplePath(ItemList list)
    {
        if ( list.size() != 3 && list.size() != 4 )
            BuilderLib.broken(list, "Not a triple path", list) ;
        
        if ( list.size() == 4 )
        {
            if ( ! list.get(0).isSymbol(Tags.tagTriplePath) )
                BuilderLib.broken(list, "Not a triple path") ;
            list = list.cdr() ;
        }
        
        Node s = BuilderNode.buildNode(list.get(0)) ;
        Path p = BuilderPath.buildPath(list.get(1)) ;
        Node o = BuilderNode.buildNode(list.get(2)) ;
        return new TriplePath(s, p, o) ; 
    }

    protected Map<String, Build> dispatch = new HashMap<String, Build>() ;
    
    private BuilderPath()
    {
        dispatch.put(Tags.tagPathSeq, buildSeq) ;
        dispatch.put(Tags.tagPathAlt, buildAlt) ;
        dispatch.put(Tags.tagPathMod, buildMod) ;
        dispatch.put(Tags.tagPathReverse, buildReverse) ;
        dispatch.put(Tags.tagPathRev, buildRev) ;
        dispatch.put(Tags.pathNotOneOf, buildNotOneOf) ;
    }
    
    private Path build(Item item)
    {
        if (item.isNode() )
        {
            if ( item.getNode().isURI() )
                return new P_Link(item.getNode()) ;
            BuilderLib.broken(item, "Attempt to build path from a plain node") ;
        }
        
        if (item.isSymbol() )
            BuilderLib.broken(item, "Attempt to build path from a bare symbol") ;
        return build(item.getList()) ;
    }
    
    private Path build(ItemList list)
    {
        Item head = list.get(0) ;
        if ( !head.isSymbol() )
            return build(head) ;
        String tag = head.getSymbol() ;
        Build bob = findBuild(tag) ;
        if ( bob != null )
            return bob.make(list) ;
        else
            BuilderLib.broken(head, "Unrecognized path operation: "+tag) ;
            
        return null ;
    }
    
    static interface Build { Path make(ItemList list) ; }
    
    protected Build findBuild(String str)
    {
        for ( Iterator<String> iter = dispatch.keySet().iterator() ; iter.hasNext() ; )
        {
            String key = iter.next() ; 
            if ( str.equalsIgnoreCase(key) )
                return dispatch.get(key) ;
        }
        return null ;
    }
    
    protected Path build(ItemList list, int idx)
    {
        return build(list.get(idx)) ;
    }
    
    final protected Build buildNotOneOf = new Build()
    {
        public Path make(ItemList list)
        {
            BuilderLib.checkLengthAtLeast(1, list, "path: negative property set: must be at least one element") ;
            P_NegPropClass pNegClass = new P_NegPropClass() ;
            for ( int i = 1 ; i < list.size() ; i++ )
            {
                Item item = list.get(i) ;
                // Node or reverse?
                Path p = build(item) ;
                if ( ! ( p instanceof P_Path0 ) )
                    BuilderLib.broken(item, "Not a property or reverse property") ;
                pNegClass.add((P_Path0)p) ;
            }
            return pNegClass ;
        }
    } ;
    
    final protected Build buildSeq = new Build()
    {
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(3, 3, list, "path seq: wanted 2 arguments") ;
            Path left = build(list, 1) ;
            Path right  = build(list, 2) ;
            return new P_Seq(left, right) ;
        }
    };

    final protected Build buildAlt = new Build()
    {
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(3, 3, list, "path alt: wanted 2 arguments") ;
            Path left = build(list, 1) ;
            Path right  = build(list, 2) ;
            return new P_Alt(left, right) ;
        }

    };

    final protected Build buildMod = new Build()
    {
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(4, 4, list, "path mod: wanted 3 arguments") ;
            
            long min = modInt(list.get(1)) ;
            long max = modInt(list.get(2)) ;
            Path path  = build(list, 3) ;
            return new P_Mod(path, min, max) ;
        }
    };

    static long modInt(Item item)
    {
        if ( "*".equals(item.getSymbol()) ) return P_Mod.INF ;
        //if ( "****".equals(item.getSymbol()) ) return P_Mod.UNSET ;
        return BuilderNode.buildInt(item) ;
    }
    
    final protected Build buildReverse = new Build()
    {
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(2, 2, list, "path reverse: wanted 1 argument") ;
            Path path = build(list, 1) ;
            return new P_Inverse(path) ;
        }
    };
    
    final protected Build buildRev = new Build()
    {
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(2, 2, list, "path reverse link: wanted 1 argument") ;
            return new P_ReverseLink(list.get(1).getNode()) ;
        }
    };

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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