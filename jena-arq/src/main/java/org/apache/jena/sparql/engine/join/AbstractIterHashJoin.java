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

package org.apache.jena.sparql.engine.join;

import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIter2 ;
import org.apache.jena.sparql.engine.iterator.QueryIterPeek ;

/** Hash join algorithm
 *  
 * This code materializes one input into the probe table
 * then hash joins the other input from the stream side.
 */

public abstract class AbstractIterHashJoin extends QueryIter2 {
    protected long s_countProbe           = 0 ;       // Count of the probe data size
    protected long s_countScan            = 0 ;       // Count of the scan data size
    protected long s_countResults         = 0 ;       // Overall result size.
    protected long s_trailerResults       = 0 ;       // Results from the trailer iterator.
    // See also stats in the probe table.
    
    protected final JoinKey               joinKey ;
    protected final HashProbeTable        hashTable ;

    private QueryIterator               iterStream ;
    private Binding                     rowStream       = null ;
    private Iterator<Binding>           iterCurrent ;
    private boolean                     yielded ;       // Flag to note when current probe causes a result. 
    // Hanlde any "post join" additions.
    private Iterator<Binding>           iterTail        = null ;
    
    enum Phase { INIT, HASH , STREAM, TRAILER, DONE }
    Phase state = Phase.INIT ;
    
    private Binding slot = null ;

    protected AbstractIterHashJoin(JoinKey joinKey, QueryIterator probeIter, QueryIterator streamIter, ExecutionContext execCxt) {
        super(probeIter, streamIter, execCxt) ;
        
        if ( joinKey == null ) {
            QueryIterPeek pProbe = QueryIterPeek.create(probeIter, execCxt) ;
            QueryIterPeek pStream = QueryIterPeek.create(streamIter, execCxt) ;
            
            Binding bLeft = pProbe.peek() ;
            Binding bRight = pStream.peek() ;
            
            List<Var> varsLeft = Iter.toList(bLeft.vars()) ;
            List<Var> varsRight = Iter.toList(bRight.vars()) ;
            joinKey = JoinKey.createVarKey(varsLeft, varsRight) ;
            probeIter = pProbe ;
            streamIter = pStream ;
        }
        
        this.joinKey = joinKey ;
        this.iterStream = streamIter ;
        this.hashTable = new HashProbeTable(joinKey) ;
        this.iterCurrent = null ;
        buildHashTable(probeIter) ;
        
    }
        
    private void buildHashTable(QueryIterator iter1) {
        state = Phase.HASH ;
        for (; iter1.hasNext();) {
            Binding row1 = iter1.next() ;
            s_countProbe ++ ;
            hashTable.put(row1) ;
        }
        iter1.close() ;
        state = Phase.STREAM ;
    }

    @Override
    protected boolean hasNextBinding() {
        if ( isFinished() ) 
            return false ;
        if ( slot == null ) {
            slot = moveToNextBindingOrNull() ;
            if ( slot == null ) {
                close() ;
                return false;
            }
        }
        return true ;
    }

    @Override
    protected Binding moveToNextBinding() {
        Binding r = slot ;
        slot = null ;
        return r ;
    }

    protected Binding moveToNextBindingOrNull() {
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
                    Binding b = noYieldedRows(rowStream) ;
                    if ( b != null ) {
                        s_countScan ++ ;
                        return b ;
                    }
                }
                continue ;
            }

            // Nested loop join, only on less.
            //Iterator<Binding> iter = nestedLoop(iterCurrent, rowStream) ;
            
            Binding rowCurrentProbe = iterCurrent.next() ;
            Binding r = Algebra.merge(rowCurrentProbe, rowStream) ;
            Binding r2 = null ;
            
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
    
    
    private Binding doOneTail() {
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
    protected abstract Binding yieldOneResult(Binding rowCurrentProbe, Binding rowStream, Binding rowResult) ;

    /** Signal a row that yields no matches.
     *  This method can return a binding (the outer join case)
     *  which will then be yielded. {@code yieldOneResult} will <em>not</em> be called. 
     * @param rowStream
     * @return
     */
    protected abstract Binding noYieldedRows(Binding rowStream) ;

    /**
     * Signal the end of the hash join.
     * Outer joins can now add any "no matched" results.
     * @return QueryIterator or null
     */
    protected abstract QueryIterator joinFinished() ;
        
    @Override
    protected void closeSubIterator() {
        if ( JoinLib.JOIN_EXPLAIN ) {
            String x = String.format(
                         "HashJoin: LHS=%d RHS=%d Results=%d RightMisses=%d MaxBucket=%d NoKeyBucket=%d",
                         s_countProbe, s_countScan, s_countResults, 
                         hashTable.s_countScanMiss, hashTable.s_maxBucketSize, hashTable.s_noKeyBucketSize) ;
            System.out.println(x) ;
        }
        // In case it's a peek iterator.
        iterStream.close() ;
        hashTable.clear(); 
    }

    @Override
    protected void requestSubCancel() 
    { }
}


