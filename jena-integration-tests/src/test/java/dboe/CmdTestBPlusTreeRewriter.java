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

package dboe;

import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.sys.SystemIndex;
import org.apache.jena.dboe.trans.bplustree.BPT;
import org.apache.jena.dboe.trans.bplustree.rewriter.TestBPlusTreeRewriterNonTxn;

public class CmdTestBPlusTreeRewriter extends BaseSoakTest
{
    static public void main(String... argv) {
        new CmdTestBPlusTreeRewriter(argv).mainRun() ;
    }

    protected CmdTestBPlusTreeRewriter(String[] argv) {
        super(argv) ;
    }
    
    static int KeySize     = 4 ;
    static int ValueSize   = 8 ;
    
    @Override
    protected void before() {
        SystemIndex.setNullOut(true) ;
        // Forced mode
        if ( false ) {
            BPT.forcePromoteModes = true ;
            BPT.promoteDuplicateNodes = true ;
            BPT.promoteDuplicateRecords  = true ;
        }
        if ( false ) {
            // Transactions.
        }
    }
    
    @Override
    protected void after() { }

    @Override
    protected void runOneTest(int testCount, int order, int size, boolean debug) {
        runOneTest(testCount, order, size) ;
    }

    @Override
    protected void runOneTest(int testCount, int order, int size) {
        RecordFactory recordFactory = new RecordFactory(KeySize, ValueSize) ;
        TestBPlusTreeRewriterNonTxn.runOneTest(order, size, recordFactory, false) ;
    }

}
