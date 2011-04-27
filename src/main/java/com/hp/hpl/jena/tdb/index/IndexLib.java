/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;


import java.util.Iterator;

import org.openjena.atlas.lib.Tuple ;


import com.hp.hpl.jena.tdb.store.NodeId;

public class IndexLib
{
//    @Deprecated
//    public static Iterator<Tuple<NodeId>> tuples(RangeIndex index)
//    {
//        return tuplesRaw(index.iterator()) ;
//    }
//
//    @Deprecated
//    public static void print(RangeIndex index)
//    {
//        print(tuples(index)) ;
//    }


    public static void print(Iterator<Tuple<NodeId>> iter)
    {
        for ( int i = 0 ; iter.hasNext() ; i++ )
        {
            Tuple<NodeId> tuple = iter.next();
            System.out.printf("%2d: %s\n", i, tuple) ;
        } 
    }

//    @Deprecated
//    public static Iterator<Tuple<NodeId>> tuplesRaw(Iterator<Record> iter)
//    {
//        Transform<Record, Tuple<NodeId>> transform = new Transform<Record, Tuple<NodeId>>() {
//            @Override
//            public Tuple<NodeId> convert(Record item)
//            {
//                return tuplesRaw(item) ;
//            }} ; 
//            return Iter.map(iter, transform) ;
//    }
//    // ----
//
//    @Deprecated
//    public static Tuple<NodeId> tuplesRaw(Record e)
//    {
//        // In index native order
//        NodeId x = NodeLib.getNodeId(e, 0) ;
//        NodeId y = NodeLib.getNodeId(e, NodeId.SIZE) ;
//        NodeId z = NodeLib.getNodeId(e, 2*NodeId.SIZE) ;
//        return Tuple.create(x, y, z) ;
//    }

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