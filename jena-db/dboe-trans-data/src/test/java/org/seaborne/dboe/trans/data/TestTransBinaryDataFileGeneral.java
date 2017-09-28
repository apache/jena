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

package org.seaborne.dboe.trans.data;

import org.apache.jena.query.ReadWrite ;
import org.seaborne.dboe.base.file.* ;
import org.seaborne.dboe.transaction.Transactional ;
import org.seaborne.dboe.transaction.TransactionalFactory ;
import org.seaborne.dboe.transaction.txn.ComponentId ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;

public class TestTransBinaryDataFileGeneral extends AbstractTestBinaryDataFile {
    private Journal journal ;
    private BinaryDataFile baseBinData ;
    private TransBinaryDataFile transBinData ;
    private Transactional transactional ;

    @Override
    protected BinaryDataFile createBinaryDataFile() {
        // XXX Builder.
        journal = Journal.create(Location.mem()) ;
        baseBinData = new BinaryDataFileMem() ;
        BufferChannel chan = FileFactory.createBufferChannelMem() ;
        ComponentId cid = ComponentId.allocLocal() ;
        transBinData = new TransBinaryDataFile(baseBinData, cid, chan) ;
        transBinData.open();
        transactional = TransactionalFactory.createTransactional(journal, transBinData) ;
        //Non-transactional usage of a disposed file. 
        transactional.begin(ReadWrite.WRITE) ;
        return transBinData ; 
        
    }
    
    @Override
    protected void releaseBinaryDataFile(BinaryDataFile file) {
        if ( file.isOpen() ) {
            transactional.commit() ;
            transactional.end() ;
            baseBinData.close() ;
        }
    }
}

