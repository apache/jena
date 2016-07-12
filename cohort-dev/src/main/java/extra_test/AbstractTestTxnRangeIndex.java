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

package extra_test;

import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

import static extra_test.RecordLib.r ;
import static org.junit.Assert.* ;
import org.seaborne.dboe.base.record.Record ;
import org.seaborne.dboe.index.RangeIndex ;
import org.seaborne.dboe.transaction.Transactional ;
import org.apache.jena.system.Txn ;

// See TestBPlusTreeTxn
public abstract class AbstractTestTxnRangeIndex {
    public abstract RangeIndex make() ;
    
    protected Transactional transactional() {
        return transactional ;
        
    }
    
    RangeIndex rangeIndex ;
    Transactional transactional ;
    
    @Before public void setup() {
        rangeIndex = make() ;
        
    }
    
    @After public void teardown() {
        
    }

    @Test public void _basic() { }
    
    @Test public void add_01() { 
        Record r1 = r(1) ;
        Txn.execWrite(transactional, ()->rangeIndex.insert(r1)) ;
        Record r2 = r(2) ;
        Txn.execWrite(transactional, ()->rangeIndex.insert(r2)) ;
        
        List<Record> x = Txn.execReadRtn(transactional, ()->Iter.toList(rangeIndex.iterator())); 
        assertEquals(2, x.size()) ;
        assertEquals(r1, x.get(0)) ;
        assertEquals(r2, x.get(1)) ;
    }
    
}

