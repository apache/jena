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

package com.hp.hpl.jena.sparql.sse.builders;

import java.util.HashMap ;
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

    protected Map<String, Build> dispatch = new HashMap<>() ;
    
    private BuilderPath()
    {
        dispatch.put(Tags.tagPath,    buildPath) ;
        dispatch.put(Tags.tagPathSeq, buildSeq) ;
        dispatch.put(Tags.tagPathAlt, buildAlt) ;
        dispatch.put(Tags.tagPathMod, buildMod) ;
        
        dispatch.put(Tags.tagPathFixedLength,   buildFixedLength) ;
        dispatch.put(Tags.tagPathDistinct,      buildDistinct) ;
        dispatch.put(Tags.tagPathMulti,         buildMulti) ;
        dispatch.put(Tags.tagPathShortest,      buildShortest) ;
        dispatch.put(Tags.tagPathZeroOrOne,     buildZeroOrOne) ;
        dispatch.put(Tags.tagPathZeroOrMore1,   buildZeroOrMore1) ;
        dispatch.put(Tags.tagPathZeroOrMoreN,   buildZeroOrMoreN) ;
        dispatch.put(Tags.tagPathOneOrMore1,    buildOneOrMore1) ;
        dispatch.put(Tags.tagPathOneOrMoreN,    buildOneOrMoreN) ;
        
        dispatch.put(Tags.tagPathReverse,  buildReverse) ;
        dispatch.put(Tags.tagPathRev,      buildRev) ;
        dispatch.put(Tags.tagPathLink,     buildLink) ;   // Completeness.
        dispatch.put(Tags.tagPathNotOneOf, buildNotOneOf) ;
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
        for ( String key : dispatch.keySet() )
        {
            if ( str.equalsIgnoreCase( key ) )
            {
                return dispatch.get( key );
            }
        }
        return null ;
    }
    
    protected Path build(ItemList list, int idx)
    {
        return build(list.get(idx)) ;
    }
    
    final protected Build buildPath = new Build()
    {
        @Override
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "path: Exactly one element") ;
            Item item = list.get(1) ;
            return build(item) ;
        }
    } ;
    
    final protected Build buildNotOneOf = new Build()
    {
        @Override
        public Path make(ItemList list)
        {
            BuilderLib.checkLengthAtLeast(1, list, "path: negative property set: must be at least one element") ;
            P_NegPropSet pNegClass = new P_NegPropSet() ;
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
        @Override
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
        @Override
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
        @Override
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
        if ( "_".equals(item.getSymbol()) ) return P_Mod.UNSET ;
        return BuilderNode.buildLong(item) ;
    }

    final protected Build buildFixedLength = new Build()
    {
        @Override
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(3, 3, list, "path fixed repeat: wanted 2 arguments") ;
            long count = modInt(list.get(1)) ;
            Path path  = build(list, 2) ;
            return new P_FixedLength(path, count) ;
        }
    } ;

    final protected Build buildDistinct = new Build()
    {
        @Override
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(2, 2, list, "path distinct: wanted 1 argument") ;
            Path path  = build(list, 1) ;
            return new P_Distinct(path) ;
        }
    } ;

    final protected Build buildMulti = new Build()
    {
        @Override
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(2, 2, list, "path multi : wanted 1 argument") ;
            Path path  = build(list, 1) ;
            return new P_Multi(path) ;
        }
    } ;

    final protected Build buildShortest = new Build()
    {
        @Override
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(2, 2, list, "path shortest : wanted 1 argument") ;
            Path path  = build(list, 1) ;
            return new P_Shortest(path) ;
        }
    } ;

    final protected Build buildZeroOrMore1 = new Build()
    {
        @Override
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(2, 2, list, "path ZeroOrMore1: wanted 1 argument") ;
            Path path  = build(list, 1) ;
            return new P_ZeroOrMore1(path) ;
        }
    } ;
    
    final protected Build buildZeroOrMoreN = new Build()
    {
        @Override
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(2, 2, list, "path ZeroOrMoreN: wanted 1 argument") ;
            Path path  = build(list, 1) ;
            return new P_ZeroOrMoreN(path) ;
        }
    } ;
    

    final protected Build buildZeroOrOne = new Build()
    {
        @Override
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(2, 2, list, "path ZeroOrOne: wanted 1 argument") ;
            Path path  = build(list, 1) ;
            return new P_ZeroOrOne(path) ;
        }
    } ;
    
    final protected Build buildOneOrMore1 = new Build()
    {
        @Override
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(2, 2, list, "path OneOrMore: wanted 1 argument") ;
            Path path  = build(list, 1) ;
            return new P_OneOrMore1(path) ;
        }
    } ;
    
    final protected Build buildOneOrMoreN = new Build()
    {
        @Override
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(2, 2, list, "path OneOrMore: wanted 1 argument") ;
            Path path  = build(list, 1) ;
            return new P_OneOrMoreN(path) ;
        }
    } ;
    
    final protected Build buildReverse = new Build()
    {
        @Override
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(2, 2, list, "path reverse: wanted 1 argument") ;
            Path path = build(list, 1) ;
            return new P_Inverse(path) ;
        }
    };
    
    final protected Build buildLink = new Build()
    {
        @Override
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(2, 2, list, "path link: wanted 1 argument") ;
            return new P_Link(list.get(1).getNode()) ;
        }
    };

    final protected Build buildRev = new Build()
    {
        @Override
        public Path make(ItemList list)
        {
            BuilderLib.checkLength(2, 2, list, "path reverse link: wanted 1 argument") ;
            return new P_ReverseLink(list.get(1).getNode()) ;
        }
    };

}
