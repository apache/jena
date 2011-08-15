/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.transaction;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.errlog ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.syslog ;
import static java.lang.String.format ;

import java.io.File ;
import java.util.Iterator ;
import java.util.Map ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.FileOps ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.tdb.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.IndexMap ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.FileRef ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class JournalControl
{
    private static Logger log = LoggerFactory.getLogger(JournalControl.class) ;

    public static void print(Journal journal)
    {
        System.out.println("Size: "+journal.size()) ;
        
        for ( JournalEntry e : journal )
        {
            System.out.println(JournalEntry.format(e)) ;
            System.out.println("Posn: "+journal.position()+" : ("+(journal.size()-journal.position())+")") ;
            
        }
    }

    public static void recovery(DatasetGraphTDB dsg)
    {
        if ( dsg instanceof DatasetGraphTxn )
            throw new TDBTransactionException("Reocery works on the base dataset, not a transactional one") ; 
        
        if ( dsg.getLocation().isMem() )
            return ;
        
        // Do we need to recover?
        Journal journal = findJournal(dsg) ;
        if ( journal == null )
            return ;
        
        for ( FileRef fileRef : dsg.getConfig().nodeTables.keySet() )
            recoverNodeDat(dsg, fileRef) ;
        recoverSystemJournal(journal, dsg) ;
        
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

    // New recovery - scan to commit, enact, scan, ....
    
    /** Recovery from the system journal.
     *  Find if there is a commit record; if so, reply the journal to that point.
     *  Try to see if there is another commit record ...
     */
    private static void recoverSystemJournal(Journal jrnl, DatasetGraphTDB dsg)
    {
        long posn = 0 ;
        for ( ;; )
        {
            long x = scanForCommit(jrnl, posn) ;
            if ( x == -1 ) break ;
            recoverSegment(jrnl, posn, x, dsg) ;
            posn = x ;
        }

        // We have replayed the journals - clean up.
        jrnl.truncate(0) ;
        jrnl.close();
        dsg.sync() ;
    }

    /** Scan to a commit entry, starting at a given position in the journal.
     * Return addrss of entry after commit if found, else -1.
     *  
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
     *  Scan to see if theer is a commit; if found, play the
     *  journal from the start point to the commit.
     *  Return true is a commit was found.
     *  Leave journal positioned just after commit or at end if none found.
     */
    private static void recoverSegment(Journal jrnl, long startPosn, long endPosn, DatasetGraphTDB dsg)
    {
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
                replay(e, dsg) ;
            }
        } finally { Iter.close(iter) ; }
    }
    
    /** Recovery from the system journal.
     *  Find is there is a commit record; if so, reply the journal.
     */
    private static void recoverSystemJournal_0(DatasetGraphTDB dsg)
    {
        Location loc = dsg.getLocation() ;
        String journalFilename = loc.absolute(Names.journalFile) ;
        File f = new File(journalFilename) ;
        //if ( FileOps.exists(journalFilename)
        if ( f.exists() && f.isFile() && f.length() > 0 )
        {
            Journal jrnl = Journal.create(loc) ;
            // Scan for commit.
            boolean committed = false ;
            for ( JournalEntry e : jrnl )
            {
                if ( e.getType() == JournalEntryType.Commit )
                    committed = true ;
                else
                {
                    if ( committed )
                    {
                        errlog.warn("Extra journal entries ("+loc+")") ;
                        break ;
                    }
                }
            }
            if ( committed )
            {
                syslog.info("Recovering committed transaction") ;
                // The NodeTable Journal has already been done!
                JournalControl.replay(jrnl, dsg) ;
            }
            jrnl.truncate(0) ;
            jrnl.close();
            dsg.sync() ;
        }
        
        if ( f.exists() )
            FileOps.delete(journalFilename) ;
    }
    
    /** Recover a node data file (".dat").
     *  Node data files are append-only so recovering, then not using the data is safe.
     *  Node data file is a precursor for ful lrecovery that works from the master journal.
     */
    private static void recoverNodeDat(DatasetGraphTDB dsg, FileRef fileRef)
    {
        // See DatasetBuilderTxn - same name generation code.
        // [TxTDB:TODO]
        
        RecordFactory recordFactory = new RecordFactory(SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId) ;
        NodeTable baseNodeTable = dsg.getConfig().nodeTables.get(fileRef) ;
        String objFilename = fileRef.getFilename()+"-"+Names.extJournal ;
        objFilename = dsg.getLocation().absolute(objFilename) ;
        File jrnlFile = new File(objFilename) ;
        if ( jrnlFile.exists() && jrnlFile.length() > 0 )
        {
            syslog.info("Recovering node data: "+fileRef.getFilename()) ;
            ObjectFile dataJrnl = FileFactory.createObjectFileDisk(objFilename) ;
            NodeTableTrans ntt = new NodeTableTrans(objFilename, baseNodeTable, new IndexMap(recordFactory), dataJrnl) ;
            ntt.append() ;
            ntt.close() ;
            dataJrnl.close() ;
            baseNodeTable.sync() ;
        }
        if ( jrnlFile.exists() )
            FileOps.delete(objFilename) ;
    }
    
    public static void replay(Transaction transaction)
    {
        Journal journal = transaction.getJournal() ;
        DatasetGraphTDB dsg = transaction.getBaseDataset() ;
        replay(journal, dsg) ;
    }
    
    public static void replay(Journal journal, DatasetGraphTDB dsg)
    {
        journal.position(0) ;
        dsg.getLock().enterCriticalSection(Lock.WRITE) ;
        try {
            for ( JournalEntry e : journal )
                replay(e, dsg) ;
        } 
        catch (RuntimeException ex)
        { 
            // Bad news travels fast.
            syslog.error("Exception during journal replay", ex) ;
            throw ex ;
        }
        finally { dsg.getLock().leaveCriticalSection() ; }
        journal.truncate(0) ;
    }

    /** return true for "go on" */
    private static boolean replay(JournalEntry e, DatasetGraphTDB dsg)
    {
        Map<FileRef, BlockMgr> mgrs = dsg.getConfig().blockMgrs ;
    
        switch (e.getType())
        {
            case Block:
                // All-purpose, works for direct and mapped mode, copy of a block.
                // [TxTDB:PATCH-UP]
                // Direct: blkMgr.write(e.getBlock()) would work.
                // Mapped: need to copy over the bytes.
                
                BlockMgr blkMgr = mgrs.get(e.getFileRef()) ;
                Block blk = e.getBlock() ;
                log.debug("Replay: {} {}",e.getFileRef(), blk) ;
                blk.setModified(true) ;
                blkMgr.overwrite(blk) ; 
                
//                Block blk = blkMgr.getWrite(e.getBlock().getId()) ;
//                blk.getByteBuffer().rewind() ;
//                blk.getByteBuffer().put(e.getBlock().getByteBuffer()) ;
//                blkMgr.write(e.getBlock()) ; 
                return true ;
            case Commit:
                return false ;
            case Abort:
            case Buffer:
            case Object:
            case Checkpoint:
                errlog.warn("Unexpected block type: "+e.getType()) ;
        }
        return false ;
    }
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */