/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.transaction;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.errlog ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.syslog ;

import java.io.File ;
import java.util.Map ;

import org.openjena.atlas.lib.FileOps ;

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
    //private static Logger log = LoggerFactory.getLogger(Replay.class) ;
    

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
        
        for ( FileRef fileRef : dsg.getConfig().nodeTables.keySet() )
            recoverNodeDat(dsg, fileRef) ;
        
        recoverSystemJournal(dsg) ;
    }
    
    /** Recovery from the system journal.
     *  Find is there is a commit record; if so, reply the journal.
     */
    private static void recoverSystemJournal(DatasetGraphTDB dsg)
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
            NodeTableTrans ntt = new NodeTableTrans(baseNodeTable, new IndexMap(recordFactory), dataJrnl) ;
            ntt.append() ;
            ntt.close() ;
            baseNodeTable.sync() ;
        }
        if ( jrnlFile.exists() )
            FileOps.delete(objFilename) ;
    }
    
    public static void replay(Transaction transaction)
    {
        // What about the Transactional components of a transation. 
        Journal journal = transaction.getJournal() ;
//        System.out.println(">> REPLAY") ;
//        print(journal) ;
//        System.out.println("<< REPLAY") ;
//        System.out.flush() ;
        
        DatasetGraphTDB dsg = transaction.getBaseDataset() ;
        replay(journal, dsg) ;
//        Iterator<Transactional> iter = transaction.components() ;
//        xxxxxxxxxxxxxxxx
    }
    
    private static void replay(Journal journal, DatasetGraphTDB dsg)
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