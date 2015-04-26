/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package dev;

import java.util.Collections ;
import java.util.Comparator ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Tuple ;

import org.apache.jena.tdb.store.NodeId ;

/** Sort operations specific to TDB */
public class SortTDB
{
    static int compare(NodeId nodeId1, NodeId nodeId2) {
        return Long.compare(nodeId1.getId(), nodeId2.getId()) ;
    }
    
    public static Iterator<Tuple<NodeId>> sort(Iterator<Tuple<NodeId>> input, final int...order) {
        Comparator<Tuple<NodeId>> comparator = new Comparator<Tuple<NodeId>>() {
            @Override
            public int compare(Tuple<NodeId> tuple1, Tuple<NodeId> tuple2) {
                for ( int x : order ) {
                    NodeId nodeId1 = tuple1.get(x) ;
                    NodeId nodeId2 = tuple2.get(x) ;
                    int cmp = SortTDB.compare(nodeId1, nodeId2) ;
                    if ( cmp != 0 )
                        return cmp ;
                }
                return 0 ;
            }
        } ;
        return sort(input, comparator) ;
    }
    
    /** Sort tuples of NodeIds */
    public static Iterator<Tuple<NodeId>> sort(Iterator<Tuple<NodeId>> input, Comparator<Tuple<NodeId>> comparator) {
        List<Tuple<NodeId>> x = Iter.toList(input) ;
        Collections.sort(x, comparator) ;
        return x.iterator() ; 
    }

}
