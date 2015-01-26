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

package org.seaborne.dboe.engine.join;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.MultiMap ;
import org.seaborne.dboe.engine.JoinKey ;
import org.seaborne.dboe.engine.Row ;
import org.seaborne.dboe.engine.join.HashJoin.Hasher ;

// Use in Hashjoin.
class HashProbeTable<X> {
    private long s_count                = 0 ;
    private long s_bucketCount          = 0 ;
    private long s_maxBucketSize        = 0 ;
    private long s_noKeyBucketSize      = 0 ;
    private long s_maxMatchGroup        = 0 ;

    private final List <Row<X>> noKeyBucket = new ArrayList<>() ;
    private final MultiMap<Object, Row<X>> buckets ;
    private final Hasher<X> hasher ;
    private final JoinKey   joinKey ;
    
    HashProbeTable(Hasher<X> hasher, JoinKey joinKey) {
        this.hasher = hasher ;
        this.joinKey = joinKey ;
        buckets = MultiMap.createMapList() ;
    }
    
    public void put(Row<X> row) {
        s_count++ ;
        Object longHash = PipelineHashJoin.hash(hasher, joinKey, row) ;
        if ( longHash == PipelineHashJoin.noKeyHash ) {
            noKeyBucket.add(row) ;
            return ;
        }
        buckets.put(longHash, row) ;
    }
    
    public Iterator<Row<X>> getCandidates(Row<X> row) {
        Iterator<Row<X>> iter = null ;
        Object longHash = PipelineHashJoin.hash(hasher, joinKey, row) ;
        if ( longHash == PipelineHashJoin.noKeyHash )
            iter = buckets.flatten() ;
        else {
            Collection<Row<X>> x = buckets.get(longHash) ;
            if ( x != null ) {
                s_maxMatchGroup = Math.max(s_maxMatchGroup, x.size()) ;
                iter = x.iterator() ;
            }
        }
        // And the rows with no common hash key
        if ( noKeyBucket != null )
            iter = Iter.concat(iter, noKeyBucket.iterator()) ;
        return iter ;
    }                        

    public void stats() {
        long max = 0 ;
        for ( Object key : buckets.keys() ) {
            long s = buckets.get(key).size() ;
            max = Math.max(max, s) ;
        }
        s_maxBucketSize = max ;
        s_bucketCount = buckets.keys().size() ;
        s_noKeyBucketSize = (noKeyBucket == null ) ? 0 : noKeyBucket.size() ;
        //s_count
        //s_maxMatchGroup
        // What to do with them?
    }
    
    // Should not need these operations.
    public Collection<Row<X>> getNoKey$() {
        if ( noKeyBucket == null )
            return null ;
        return noKeyBucket ;
    }
    
    public Collection<Row<X>> getHashMatch$(Row<X> row) {
        Object longHash = PipelineHashJoin.hash(hasher, joinKey, row) ;
        if ( longHash == PipelineHashJoin.noKeyHash )
            return noKeyBucket ;
        Collection<Row<X>> list = buckets.get(longHash) ;
        return list ;
    }
    
    public Iterator<Row<X>> flatten$() { return buckets.flatten() ; } 
    
    public void clear() {
        buckets.clear() ;
    }
}
