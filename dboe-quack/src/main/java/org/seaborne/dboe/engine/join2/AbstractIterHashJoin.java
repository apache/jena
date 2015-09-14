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

package org.seaborne.dboe.engine.join2;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.IteratorSlotted ;
import org.seaborne.dboe.engine.Join ;
import org.seaborne.dboe.engine.JoinKey ;
import org.seaborne.dboe.engine.Row ;
import org.seaborne.dboe.engine.RowBuilder ;
import org.seaborne.dboe.engine.RowList ;
import org.seaborne.dboe.engine.join.HashProbeTable ;
import org.seaborne.dboe.engine.join.Hasher ;

/** Hash join algorithm
 *  
 * This code materializes one input into the probe table
 * then hash joins the other input from the stream side.
 */

public abstract class AbstractIterHashJoin<X> extends IteratorSlotted<Row<X>> {
    protected long s_countProbe           = 0 ;       // Count of the probe data size
    protected long s_countScan            = 0 ;       // Count of the scan data size
    protected long s_countResults         = 0 ;       // Overall result size.
    protected long s_trailerResults       = 0 ;       // Results from the trailer iterator.
    // See also stats in the probe table.
    
    protected final JoinKey               joinKey ;
    protected final HashProbeTable<X>     hashTable ;

    private Iterator<Row<X>>           iterStream ;
    private Row<X>                     rowStream       = null ;
    private Iterator<Row<X>>           iterCurrent ;
    private boolean                     yielded ;       // Flag to note when current probe causes a result. 
    // Hanlde any "post join" additions.
    private Iterator<Row<X>>           iterTail        = null ;
    
    enum Phase { INIT, HASH , STREAM, TRAILER, DONE }
    protected Phase state = Phase.INIT ;
    private final RowBuilder<X> builder;
    
    protected AbstractIterHashJoin(JoinKey joinKey, RowList<X> probe, RowList<X> stream, Hasher<X> hasher, RowBuilder<X> builder) {
        Iterator<Row<X>> probeIter = probe.iterator() ;
        Iterator<Row<X>> streamIter = stream.iterator() ;
        if ( joinKey == null )
            joinKey = JoinKey.createVarKey(probe.vars(), stream.vars()) ;
        
        this.joinKey = joinKey ;
        this.iterStream = streamIter ;
        this.hashTable = new HashProbeTable<>(hasher, joinKey) ;
        this.iterCurrent = null ;
        this.builder = builder ;
        buildHashTable(probeIter) ;
        
    }
        
    private void buildHashTable(Iterator<Row<X>> iter1) {
        state = Phase.HASH ;
        for (; iter1.hasNext();) {
            Row<X> row1 = iter1.next() ;
            s_countProbe ++ ;
            hashTable.put(row1) ;
        }
        Iter.close(iter1) ;
        state = Phase.STREAM ;
    }

    @Override
    protected boolean hasMore() {
        if ( isFinished() ) 
            return false ;
        return true ;
    }

    @Override
    protected Row<X> moveToNext() {
        // iterCurrent is the iterator of entries in the
        // probe hashed table for the current stream row.     
        // iterStream is the stream of incoming rows.
        
        switch ( state ) {
            case DONE : return null ;
            case HASH : 
            case INIT :
                throw new IllegalStateException() ;
            case TRAILER :
                return doOneTail() ;
            case STREAM :
        }
        
        for(;;) {
            // Ensure we are processing a row. 
            while ( iterCurrent == null ) {
                // Move on to the next row from the right.
                if ( ! iterStream.hasNext() ) {
                    state = Phase.TRAILER ;
                    iterTail = joinFinished() ;
                    if ( iterTail != null )
                        return doOneTail() ;
                    return null ;
                }
                rowStream = iterStream.next() ;
                s_countScan ++ ;
                iterCurrent = hashTable.getCandidates(rowStream) ;
                yielded = false ;
            }
            
            // Emit one row using the rightRow and the current matched left rows. 
            if ( ! iterCurrent.hasNext() ) {
                iterCurrent = null ;
                if ( ! yielded ) {
                    Row<X> b = noYieldedRows(rowStream) ;
                    if ( b != null ) {
                        s_countScan ++ ;
                        return b ;
                    }
                }
                continue ;
            }

            // Nested loop join, only on less.
            //Iterator<Row<X>> iter = nestedLoop(iterCurrent, rowStream) ;
            
            builder.reset() ;
            Row<X> rowCurrentProbe = iterCurrent.next() ;
            Row<X> r = Join.merge(rowCurrentProbe, rowStream, builder) ;
            Row<X> r2 = null ;
            
            if (r != null)
                r2 = yieldOneResult(rowCurrentProbe, rowStream, r) ;
            if ( r2 == null ) {
                // Reject
            } else {
                yielded = true ;
                s_countResults ++ ;
                return r2 ;
            }
        }
    }    
    
    
    private Row<X> doOneTail() {
        // Only in TRAILING
        if ( iterTail.hasNext() ) {
            s_countResults ++ ;
            s_trailerResults ++ ;
            return iterTail.next() ;
        }
        state = Phase.DONE ;
        // Completely finished now.
        iterTail = null ;
        return null ;
    }
    
    /**
     * Signal about to return a result.
     * @param rowCurrentProbe
     * @param rowStream
     * @param rowResult
     * @return 
     */
    protected abstract Row<X> yieldOneResult(Row<X> rowCurrentProbe, Row<X> rowStream, Row<X> rowResult) ;

    /** Signal a row that yields no matches.
     *  This method can return a Row<X> (the outer join case)
     *  which will then be yielded. {@code yieldOneResult} will <em>not</em> be called. 
     * @param rowStream
     * @return
     */
    protected abstract Row<X> noYieldedRows(Row<X> rowStream) ;

    /**
     * Signal the end of the hash join.
     * Outer joins can now add any "no matche" results.
     * @return QueryIterator or null
     */
    protected abstract Iterator<Row<X>> joinFinished() ;
        
    @Override
    protected void closeIterator() {
        if ( JoinLib.JOIN_EXPLAIN ) {
            String x = String.format(
                         "HashJoin: LHS=%d RHS=%d Results=%d RightMisses=%d MaxBucket=%d NoKeyBucket=%d",
                         s_countProbe, s_countScan, s_countResults, 
                         hashTable.s_countScanMiss, hashTable.s_maxBucketSize, hashTable.s_noKeyBucketSize) ;
            System.out.println(x) ;
        }
        hashTable.clear(); 
    }

//    @Override
//    protected void requestSubCancel() {
//        hashTable.clear(); 
//    }
}


