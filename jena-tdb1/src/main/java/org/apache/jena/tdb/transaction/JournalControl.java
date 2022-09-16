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

package org.apache.jena.tdb.transaction;

import static java.lang.String.format ;
import static org.apache.jena.tdb.sys.SystemTDB.errlog ;
import static org.apache.jena.tdb.sys.SystemTDB.syslog ;

import java.io.File ;
import java.nio.ByteBuffer ;
import java.util.Collection ;
import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.tdb.TDBException ;
import org.apache.jena.tdb.base.block.Block ;
import org.apache.jena.tdb.base.block.BlockMgr ;
import org.apache.jena.tdb.base.file.BufferChannel ;
import org.apache.jena.tdb.base.file.BufferChannelFile ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.store.StorageConfig ;
import org.apache.jena.tdb.sys.FileRef ;
import org.apache.jena.tdb.sys.Names ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class JournalControl
{
    private static Logger log = LoggerFactory.getLogger(JournalControl.class) ;

    /** Dump a journal - debug support function - opens the journal specially - inconsistent views possible  */
    public static void print(String filename)
    {
        BufferChannelFile chan = BufferChannelFile.createUnmanaged(filename, "r") ;
        Journal journal = new Journal(chan) ;
        JournalControl.print(journal) ;
        chan.close() ;
    }
    
    public static void print(Journal journal)
    {
        System.out.println("Size: "+journal.size()) ;
        Iterator<JournalEntry> iter = journal.entries() ; 
        
        for ( ; ; )
        {
            long posn0 = journal.position();
            if ( ! iter.hasNext() )
                break;
            JournalEntry e = iter.next() ;
            long posn1 = journal.position();
            long size = posn1 - posn0;
            System.out.printf("Posn: (%d, %d) Len=%d  reverse %d\n", posn0, posn1, size, (journal.size()-journal.position())) ;
            System.out.print("  ");
            System.out.println(JournalEntry.format(e)) ;
        }
    }

    /** Recover a base storage DatasetGraph */
    public static void recovery(DatasetGraphTDB dsg)
    {
        if ( dsg.getLocation().isMem() )
            return ;
        
        // Do we need to recover?
        Journal journal = findJournal(dsg) ;
        if ( journal == null || journal.isEmpty() )
            return ;
        
        recoverFromJournal(dsg.getConfig(), journal) ;
        
        journal.close();
        // Recovery complete.  Tidy up.  Node journal files have already been handled.
        if ( journal.getFilename() != null )  
        {
            if ( FileOps.exists(journal.getFilename()) )
                FileOps.delete(journal.getFilename()) ;
        }
    }
    
    private static Journal findJournal(DatasetGraphTDB dsg)
    {
        Location loc = dsg.getLocation() ;
        String journalFilename = loc.absolute(Names.journalFile) ;
        File f = new File(journalFilename) ;
        //if ( FileOps.exists(journalFilename)
        
        if ( f.exists() && f.isFile() && f.length() > 0 )
            return Journal.create(loc) ;
        else
            return null ;
    }

    /** Recovery from a journal.
     *  Find if there is a commit record; if so, replay the journal to that point.
     *  Try to see if there is another commit record ...
     *  Return true if a recovery was attempted; return false if we decided no work needed.
     */
    public static boolean recoverFromJournal(StorageConfig sConf, Journal jrnl)
    {
        if ( jrnl.isEmpty() )
            return false ;
        
        for ( FileRef fileRef : sConf.objectFiles.keySet() )
            recoverNodeDat(sConf.location, fileRef) ;

        long posn = 0 ;
        for ( ;; )
        {
            // Any errors indicate a partially written journal.
            // A commit was not written properly in the prepare phase.
            // e.g. JVM died half-way though writing the prepare phase data.
            // The valid journal ends at this point. Exit loop and clean up.  

            long x ;
            try { x = scanForCommit(jrnl, posn) ; }
            catch (TDBException ex) { x = -1 ; }
            
            if ( x == -1 ) break ;
            recoverSegment(jrnl, posn, x, sConf) ;
            posn = x ;
        }
        
        // Sync database.
        syncAll(sConf) ;
        // We have replayed the journals - clean up.
        jrnl.truncate(0) ;
        jrnl.sync() ;
        return true ;
    }

    /** Scan to a commit entry, starting at a given position in the journal.
     * Return address of entry after commit if found, else -1.
     */
    private static long scanForCommit(Journal jrnl, long startPosn)
    {
        Iterator<JournalEntry> iter = jrnl.entries(startPosn) ;
        try {
            for ( ; iter.hasNext() ; )
            {
                JournalEntry e = iter.next() ;
                if ( e.getType() == JournalEntryType.Commit )
                    return e.getEndPosition() ;
            }
            return -1 ;
        } finally { Iter.close(iter) ; }
    }
    
    /** Recover one transaction from the start position given.
     *  Scan to see if there is a commit; if found, play the
     *  journal from the start point to the commit.
     *  Return true is a commit was found.
     *  Leave journal positioned just after commit or at end if none found.
     */
    private static void recoverSegment(Journal jrnl, long startPosn, long endPosn, StorageConfig sConf)
    {
        //System.out.printf("Segment: %d %d\n", startPosn, endPosn);
        
        Iterator<JournalEntry> iter = jrnl.entries(startPosn) ;
        iter = jrnl.entries(startPosn) ;
        try {
            for ( ; iter.hasNext() ; )
            {
                JournalEntry e = iter.next() ;
                if ( e.getType() == JournalEntryType.Commit )
                {
                    if ( e.getEndPosition() != endPosn )
                        log.warn(format("Inconsistent: end at %d; expected %d", e.getEndPosition(), endPosn)) ;
                    return ;
                }
                replay(e, sConf) ;
            }
        } finally { Iter.close(iter) ; }
    }
    
    /** Recover a node data file (".dat").
     *  Node data files are append-only so recovering.
     *  This code is only for ObjectFileTransComplex.
     */
    private static void recoverNodeDat(Location loc, FileRef fileRef)
    {
        // See DatasetBuilderTxn (Jena 3.4.0 or earlier) - same name generation code.
        String objFilename = fileRef.getFilename()+"-"+Names.extJournal ;
        objFilename = loc.absolute(objFilename) ;
        File jrnlFile = new File(objFilename) ;
        if ( jrnlFile.exists() ) {
            if ( jrnlFile.length() > 0 ) {
                syslog.info("Found dat-jrnl file : earlier version of Jena"+fileRef.getFilename()) ;
                syslog.info("  To clearup: run TDB from a version of Jena 3.0.0-3.4.0");
                syslog.info("  dat-jrnl should then go away");
                syslog.info("  See https://issues.apache.org/jira/browse/JENA-1379");
                throw new TDBException("Manual recovery required - see log - see JENA-1379 <https://issues.apache.org/jira/browse/JENA-1379>");
            }
            //Empty - nothing to do anyway - clearup.
            FileOps.delete(objFilename) ;
        }
    }
    
    public static void replay(Transaction transaction)
    {
        if ( syslog.isDebugEnabled())
            syslog.debug("Replay "+transaction.getLabel()) ;
        Journal journal = transaction.getJournal() ;
        DatasetGraphTDB dsg = transaction.getBaseDataset() ;
        // Currently, we (crudely) replay the whole journal.
        replay(journal, dsg.getConfig()) ;
    }
    
    /** Replay a journal onto a dataset */
    public static void replay(Journal journal, DatasetGraphTDB dsg)
    {
        replay(journal, dsg.getConfig()) ;
    }
    
    /** Replay a journal onto a store configuration (the file resources) */
    private static void replay(Journal journal, StorageConfig sConf)
    {
        if ( journal.size() == 0 )
            return ;
        
        journal.position(0) ;
        try {
            Iterator<JournalEntry> iter = journal.entries() ; 

            for (  ; iter.hasNext() ; )
            {
                JournalEntry e = iter.next() ;
                replay(e, sConf) ;

                // There is no point sync here.  
                // No writes via the DSG have been done. 
                // so all internal flags "syncNeeded" are false.
                //dsg.sync() ;
            }
        } 
        catch (RuntimeException ex)
        { 
            // Bad news travels fast.
            syslog.error("Exception during journal replay", ex) ;
            throw ex ;
        }
        
        Collection<BlockMgr> x = sConf.blockMgrs.values() ;
        for ( BlockMgr blkMgr : x )
            blkMgr.syncForce() ;
        // Must do a hard sync before this.
        journal.truncate(0) ;
    }

    /** return true for "go on" */
    private static boolean replay(JournalEntry e, StorageConfig sConf)
    {
        switch (e.getType())
        {
            case Block:
            {
                BlockMgr blkMgr = sConf.blockMgrs.get(e.getFileRef()) ;
                Block blk = e.getBlock() ;
                log.debug("Replay: {} {}",e.getFileRef(), blk) ;
                blk.setModified(true) ;
                blkMgr.overwrite(blk) ; 
                return true ;
            }   
            case Buffer:
            {
                BufferChannel chan = sConf.bufferChannels.get(e.getFileRef()) ;
                ByteBuffer bb = e.getByteBuffer() ;
                log.debug("Replay: {} {}",e.getFileRef(), bb) ;
                chan.write(bb, 0) ; // YUK!
                return true ;
            }
                
            case Commit:
                return false ;
            case Abort:
            case Object:
            case Checkpoint:
                errlog.warn("Unexpected block type: "+e.getType()) ;
        }
        return false ;
    }

    private static void syncAll(StorageConfig sConf)
    {
        Collection<BlockMgr> x = sConf.blockMgrs.values() ;
        for ( BlockMgr blkMgr : x )
            blkMgr.syncForce() ;
        Collection<BufferChannel> y = sConf.bufferChannels.values() ;
        for ( BufferChannel bChan : y )
            bChan.sync() ;
        //sConf.nodeTables ;
    }
}
