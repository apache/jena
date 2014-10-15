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

package com.hp.hpl.jena.tdb.index.ext;
import static org.apache.jena.atlas.lib.ListUtils.asList ;
import static org.apache.jena.atlas.lib.ListUtils.unique ;
import static org.apache.jena.atlas.lib.RandomLib.random ;
import static org.apache.jena.atlas.test.Gen.permute ;
import static org.apache.jena.atlas.test.Gen.rand ;
import static org.apache.jena.atlas.test.Gen.strings ;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.test.ExecGenerator ;
import org.apache.jena.atlas.test.RepeatExecution ;



import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.ext.ExtHash;

public class ExtHashTestBase
{
    public static final RecordFactory factory = new RecordFactory(4, 4) ;
    
    public static void randTests(int maxValue, int maxNumKeys, int iterations, boolean showProgess)
    {
        ExtHashTest test = new ExtHashTest(maxValue, maxNumKeys) ;
        RepeatExecution.repeatExecutions(test, iterations, showProgess) ;
    }
    
    static class ExtHashTest implements ExecGenerator
    {
        int maxNumKeys ;
        int maxValue ;
        ExtHashTest(int maxValue, int maxNumKeys)
        {
            if ( maxValue <= maxNumKeys )
                throw new IllegalArgumentException("ExtHashTest: Max value less than number of keys") ;
            this.maxValue = maxValue ; 
            this.maxNumKeys = maxNumKeys ;
        }
        
        @Override
        public void executeOneTest()
        {
            int numKeys = random.nextInt(maxNumKeys)+1 ;
            randTest(maxValue, numKeys) ;
        }
    }

    public static void randTest(int maxValue, int numKeys)
    {
//      if ( numKeys >= 3000 )
//      System.err.printf("Warning: too many keys\n") ;

        int[] r1 = rand(numKeys, 0, maxValue) ;
        int[] r2 = permute(r1, 4*numKeys) ;
        runTest(r1, r2) ;
    }
        
    public static void runTest(int[] r1, int[] r2)
    {
        try {
            ExtHash extHash = create(r1) ;
            check(extHash, r1) ;
            delete(extHash, r2) ;
            check(extHash) ;
        } catch (RuntimeException ex)
        {
            System.err.println() ;
            System.err.printf("int[] r1 = {%s} ;\n", strings(r1)) ;
            System.err.printf("int[] r2 = {%s}; \n", strings(r2)) ;
            throw ex ;
        }
    }

    public static ExtHash make()
    {
        return ExtHash.createMem(factory, 128) ;
    }
    
    public static Record intToRecord(int v)
    {
        byte[] key = Bytes.packInt(v) ;
        byte[] val = Bytes.packInt(v+100) ;
        return factory.create(key, val) ;
    }
    
    public static Record intToRecordKey(int v)
    {
        byte[] key = Bytes.packInt(v) ;
        return factory.create(key) ;
    }
    
    public static ExtHash create(int...recs)
    {
        ExtHash extHash = make() ;
        for ( int i : recs )
        {
            Record r = intToRecord(i) ;
            extHash.add(r) ;
            
            if ( false ) extHash.dump() ;
        }
        return extHash ;
    }

    public static ExtHash delete(ExtHash extHash, int...recs)
    {
        for ( int i : recs )
        {
            Record r = intToRecord(i) ;
            extHash.delete(r) ;
        }
        return extHash ;
    }

    
    public static void check(ExtHash extHash, int...recs)
    {
        extHash.check();
        for ( int i : recs )
        {
            Record r = intToRecordKey(i) ;
            assertNotNull(extHash.find(r)) ;
        }
        List<Integer> y = unique(asList(recs)) ;
        int x = (int)extHash.size() ;
        assertEquals(y.size(), x); 
    }

    
    public static void check(Iterator<Integer> iter, int...recs)
    {
        for ( int i : recs )
        {
            assertTrue("Iterator shorter than test answers", iter.hasNext()) ;
            int j = iter.next() ;
            assertEquals(i,j) ;
        }
        assertFalse("Iterator longer than test answers", iter.hasNext()) ;
    }
}
