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

package tx;

import java.nio.ByteBuffer ;

import com.hp.hpl.jena.tdb.base.file.BlockAccess ;
import com.hp.hpl.jena.tdb.sys.FileRef ;
import com.hp.hpl.jena.tdb.transaction.Journal ;
import com.hp.hpl.jena.tdb.transaction.JournalEntryType ;
import com.hp.hpl.jena.tdb.transaction.Transaction ;
import com.hp.hpl.jena.tdb.transaction.TransactionLifecycle ;

/** Write a blob, transactional */
public class TransBlob implements TransactionLifecycle
{
    // And need a block mgr
    
    private ByteBuffer bytes ;
    private final FileRef file ;
    private final BlockAccess access ;

    public TransBlob(BlockAccess blockAccess, FileRef file)
    {
        this.access = blockAccess ;
        // BlockMgrFileAccess.
        this.file = file ;
    }
    
    public void setValue(ByteBuffer bytes)
    {
        // Copy for safety?
        this.bytes= bytes ;
    }

    public ByteBuffer getValue()
    {
        return bytes ;
    }
    
    @Override
    public void begin(Transaction txn)
    {
    }

    @Override
    public void abort(Transaction txn)
    {}

    @Override
    public void commitPrepare(Transaction txn)
    {
        Journal journal = txn.getJournal() ;
        //Block blk = new Block(0, bytes) ;
        //JournalEntry entry = new JournalEntry(file, blk) ;

        // And reply?
        journal.writeJournal(JournalEntryType.Buffer, file, bytes) ;
        
       // replay : fileref -> remembered BlockMgr = BlockMgrFileAccess
        
    }

    @Override
    public void commitEnact(Transaction txn)
    {}

    @Override
    public void commitClearup(Transaction txn)
    {}
}

