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
