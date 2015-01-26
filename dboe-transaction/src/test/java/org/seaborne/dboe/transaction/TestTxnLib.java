/**
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

package org.seaborne.dboe.transaction;

import static org.junit.Assert.* ;
import org.junit.Test ;
import org.seaborne.dboe.transaction.Txn ;

import com.hp.hpl.jena.query.ReadWrite ;

public class TestTxnLib extends AbstractTestTxn {

    @Test public void libTxn_1() {
        long v1 = counter1.value() ;
        long v2 = counter2.value() ;
        assertEquals(0, v1) ;
        assertEquals(0, v2) ;
        
        Txn.executeRead(unit, () -> {
            assertEquals(0, counter1.get()) ;
            assertEquals(0, counter2.get()) ;
        }) ;
    }

    @Test public void libTxn_2() {
        assertEquals(0, counter1.value()) ;
        
        Txn.executeWrite(unit, () -> {
            counter1.inc() ;
            assertEquals("In W, value()", 0, counter1.value()) ;
            assertEquals("In W, get()",1, counter1.get()) ;
        }) ;
        
        assertEquals("Direct value()", 1, counter1.value()) ;
        assertEquals("Direct get()", 1, counter1.get()) ;

        Txn.executeRead(unit, () -> {
            assertEquals("In R, value()", 1, counter1.get()) ;
            assertEquals("In R, get()", 1, counter1.value()) ;
        }) ;
    }
    
    @Test public void libTxn_3() {
        Txn.executeRead(unit, () -> {
            assertEquals("In R, value()", 0, counter2.get()) ;
            assertEquals("In R, get()", 0, counter2.value()) ;
        }) ;

        Txn.executeWrite(unit, () -> {
            counter2.inc() ;
            assertEquals("In W, value()", 0, counter2.value()) ;
            assertEquals("In W, get()",1, counter2.get()) ;
        }) ;
        
        assertEquals("Direct value()", 1, counter2.value()) ;
        assertEquals("Direct get()", 1, counter2.get()) ;

        Txn.executeRead(unit, () -> {
            assertEquals("In R, value()", 1, counter2.get()) ;
            assertEquals("In R, get()", 1, counter2.value()) ;
        }) ;
    }

    @Test public void libTxn_4() {
        long v1 = counter1.value() ;
        long v2 = counter2.value() ;
        assertEquals(0, v1) ;
        assertEquals(0, v2) ;
        
        //Txn.executeWrite(unit, () -> {

        unit.begin(ReadWrite.WRITE); 

            counter1.inc() ;
            counter2.inc() ;
            assertEquals("Counter out of step", counter1.get(), counter2.get()); 
            assertNotEquals("Counter 1 can see wrong state", counter1.get(), counter1.value() ) ;
            assertNotEquals("Counter 2 can see wrong state", counter2.get(), counter2.value() ) ;
            counter2.inc() ;
            assertNotEquals("Counter 1 and 2 shoudl differ", counter1.get(), counter2.get() ) ;
        unit.commit() ;
        unit.end() ;
        //}) ;
        assertEquals("Component 1 inconsistent", 1, counter1.value()) ;
        assertEquals("Component 2 inconsistent", 2, counter2.value()) ;
        
        Txn.executeRead(unit, () -> {
            assertEquals("Component 1 inconsistent (R)", 1, counter1.get()) ;
            assertEquals("Component 2 inconsistent (R)", 2, counter2.get()) ;
        }) ;

}
}

