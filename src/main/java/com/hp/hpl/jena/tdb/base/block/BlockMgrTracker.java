/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import static com.hp.hpl.jena.tdb.base.block.BlockMgrTracker.Action.Alloc ;
import static com.hp.hpl.jena.tdb.base.block.BlockMgrTracker.Action.Free ;
import static com.hp.hpl.jena.tdb.base.block.BlockMgrTracker.Action.GetRead ;
import static com.hp.hpl.jena.tdb.base.block.BlockMgrTracker.Action.GetWrite ;
import static com.hp.hpl.jena.tdb.base.block.BlockMgrTracker.Action.IterRead ;
import static com.hp.hpl.jena.tdb.base.block.BlockMgrTracker.Action.Promote ;
import static com.hp.hpl.jena.tdb.base.block.BlockMgrTracker.Action.Release ;
import static com.hp.hpl.jena.tdb.base.block.BlockMgrTracker.Action.Write ;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;

import org.openjena.atlas.lib.Pair ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.TDBException ;

public class BlockMgrTracker /*extends BlockMgrWrapper*/ implements BlockMgr
{
    static enum Action { Alloc, Promote, GetRead, GetWrite, Write, Release, Free, IterRead }
    static final Integer NoId = Integer.valueOf(-9) ;
    
    // HACK - needs to be multitread safe over it's own datastructures.
    // But don't sync the underlying blockMgr (so history may be wrong but 
    // we do not want to force inglethread access to the underlying blockMgr).

    // ref counting blocks.
    
    // Don't inherit BlockMgrWrapper to make sure this class caches everything.

    // ---- State for tracking
    // Track the active state of the BlockMgr
    
    // The use of AtomicInteger here isn't for the atomicity but because they are also "refs to ints"
    
    static final class RefLong
    {
        private long value ;
        public RefLong() { this(0) ; }
        public RefLong(long v) { value = v ; }
        public long value()     { return value ; }
        public void inc()       { value++ ; } 
        public void dec()       { --value ; }
        public long getAndInc() { return value++ ; }
        public long incAndGet() { return ++value ; }
        public long getAndDec() { return value-- ; }
        public long decAndGet() { return -value ; }
        public void add(long v) { value += v ; }
        public void set(long v) { value = v ; }
        @Override public String toString() { return "Ref:"+Long.toString(value) ; } 
        // hashCode and equals are Object equality - this is a mutable object
    }

    static class MultiSet<T> implements Iterable<T>
    {
        private Map<T,RefLong> map   = new HashMap<T,RefLong>() ;
        private RefLong get(T obj)
        {
            RefLong z = map.get(obj) ;
            if ( z == null )
            {
                z = new RefLong(0) ;
                map.put(obj, z) ;
            }
            return z ;
        }
        
        public boolean isEmpty()        { return map.isEmpty() ; }
        public boolean contains(T obj)  { return map.containsKey(obj) ; }
        public void add(T obj)          { get(obj).inc(); } 
        public void remove(T obj)
        {
            RefLong x = map.get(obj) ;
            if ( x == null ) return ;
            x.dec() ;
            if ( x.value() == 0 )
                map.remove(obj) ;
        }
        public void clear() { map.clear() ; }
        public long count(T obj)
        {
            if ( ! map.containsKey(obj) ) return 0 ;
            return map.get(obj).value() ;
        }
        
        @Override
        public Iterator<T> iterator()
        {
            // CRUDE
            // TODO Fixme
            List<T> expanded = new ArrayList<T>() ;
            for ( Map.Entry<T, RefLong> e : map.entrySet() )
            {
                for ( int i = 0 ; i < e.getValue().value() ; i++ )
                    expanded.add(e.getKey()) ;
            }
            
            return expanded.iterator() ;
        }
        
        @Override public String toString()
        {
            StringBuilder sb = new StringBuilder() ;
            String sep = "" ;
            for ( Map.Entry<T, RefLong> e : map.entrySet() )
            {
                sb.append(sep) ;
                sep = ", " ;
                sb.append(e.getKey().toString()) ;
                sb.append("=") ;
                sb.append(Long.toString(e.getValue().value())) ;
            }
            
            return sb.toString() ;
        }
        
    }    
    private static boolean contains(Map<Integer,RefLong> x, Integer id) { return x.containsKey(id) ; } 
    
    protected final MultiSet<Integer> activeReadBlocks   = new MultiSet<Integer>() ;
    protected final MultiSet<Integer> activeWriteBlocks  = new MultiSet<Integer>() ;
    protected final MultiSet<Integer> activeIterBlocks   = new MultiSet<Integer>() ;
    // Track the operations 
    protected final List<Pair<Action, Integer>> actions = new ArrayList<Pair<Action, Integer>>() ;
    protected final List<Iterator<?>> activeIterators = new ArrayList<Iterator<?>>() ;
    // ---- State for tracking
    
    protected final BlockMgr blockMgr ;
    
    private void clearInternalRW()
    {
        activeReadBlocks.clear() ;
        activeWriteBlocks.clear() ;
        actions.clear() ;
    }
    
    private void clearInternalIter()
    {
        clearInternalRW() ;
        activeIterators.clear() ;
        activeIterBlocks.clear() ;
    }

    private int inRead = 0 ;
    private boolean inUpdate = false ;
    private final Logger log ;
    
    public static BlockMgr track(String label, BlockMgr blkMgr)
    {
        return new BlockMgrTracker(label, blkMgr) ;
    }

    public BlockMgrTracker(BlockMgr blockMgr)
    {
        this(LoggerFactory.getLogger(BlockMgrTracker.class), blockMgr) ;
    }
    
    public BlockMgrTracker(String label, BlockMgr blockMgr)
    {
        this(LoggerFactory.getLogger(label), blockMgr) ;
    }
    
    public BlockMgrTracker(Class<?> label, BlockMgr blockMgr)
    {
        this(LoggerFactory.getLogger(label), blockMgr) ;
    }
    
    public BlockMgrTracker(Logger logger, BlockMgr blockMgr)
    {
        this.blockMgr = blockMgr ;
        this.log = logger ;
    }

    private void add(Action action, Integer id) { actions.add(new Pair<Action, Integer>(action, id)) ; }

    @Override
    public Block allocate(int blockSize)
    {
        Block block ;
        synchronized (this)
        {
            checkUpdate("allocate") ;
            block = blockMgr.allocate(blockSize) ;
            Integer id = block.getId() ;
            activeWriteBlocks.add(id) ;
            add(Alloc, id) ;
        }
        return block ;
    }

    
    @Override
    public Block getRead(int id)
    {
        synchronized (this)
        {
            checkRead("getRead") ;
            add(GetRead, id) ;
            activeReadBlocks.add(id) ;
        }
        return blockMgr.getRead(id) ;
    }
    
    @Override
    public Block getReadIterator(int id)
    {
        synchronized (this)
        {
            checkRead("getReadIterator") ;
            add(IterRead, id) ;
            activeIterBlocks.add(id) ;
        }
        return blockMgr.getReadIterator(id) ;
    }    

    @Override
    public Block getWrite(int id)
    {
        synchronized (this)
        {
            checkUpdate("getWrite") ;
            add(GetWrite, id) ;
            activeWriteBlocks.add(id) ;
        }
        return blockMgr.getWrite(id) ;
    }
    
    @Override
    public Block promote(Block block)
    {
        synchronized (this)
        {
            Integer id = block.getId() ;
            checkUpdate("promote") ;
            add(Promote, id) ;

            if ( ! activeWriteBlocks.contains(id) && ! activeReadBlocks.contains(id) )
                error(Promote, id+" is not an active block") ;

            activeReadBlocks.remove(id) ;
            // Still on read blocks?
            activeWriteBlocks.add(id) ;
        }
        return blockMgr.promote(block) ;
    }
    
    @Override
    public void release(Block block)
    {
        synchronized (this)
        {
            Integer id = block.getId() ;
            checkRead("release") ;
            add(Release, id) ;

            if ( ! activeReadBlocks.contains(id) && ! activeIterBlocks.contains(id) && ! activeWriteBlocks.contains(id) )
                error(Release, id+" is not an active block") ;

            // ???????????????????????
            activeReadBlocks.remove(block.getId()) ;
            activeIterBlocks.remove(block.getId()) ;
            activeWriteBlocks.remove(block.getId()) ;
        }
        blockMgr.release(block) ;
    }

    @Override
    public void write(Block block)
    {
        synchronized (this)
        {
            Integer id = block.getId() ;
            checkUpdate("write") ;
            add(Write, id) ;
            if ( ! activeWriteBlocks.contains(id) )
                error(Write, id+ " is not an active write block") ;
        }
        blockMgr.write(block) ;
    }

    @Override
    public void free(Block block)
    {
        synchronized (this)
        {
            Integer id = block.getId() ;
            checkUpdate("freeBlock") ;
            add(Free, id) ;
            if ( activeReadBlocks.contains(id) )
                error(Free, id+" is a read block") ;
            else if ( ! activeWriteBlocks.contains(id) )
                error(Free, id+" is not a write block") ;
            activeWriteBlocks.remove(id) ;
        }
        blockMgr.free(block) ;
    }

    @Override
    public void sync()
    {
        blockMgr.sync() ;
    }
    
    @Override
    public void close()
    { blockMgr.close() ; }

    @Override
    public boolean isEmpty()
    {
        return blockMgr.isEmpty() ;
    }

    @Override
    public boolean valid(int id)
    {
        return blockMgr.valid(id) ;
    }

    @Override
    public boolean isClosed()
    {
        return blockMgr.isClosed() ;
    }

    @Override
    public void beginIterator(Iterator<?> iter)
    {
        synchronized (this)
        {
            if ( activeIterators.contains(iter) )
                log.warn("Iterator already active: "+iter) ;
            activeIterators.add(iter) ;
        }
        blockMgr.beginIterator(iter) ;
    }

    @Override
    public void endIterator(Iterator<?> iter)
    {
        synchronized (this)
        {
            if ( ! activeIterators.contains(iter) )
                log.warn("Iterator not active: "+iter) ;
            activeIterators.remove(iter) ;
            if ( activeIterators.size() == 0 )
                checkEmpty("Outstanding iterator read blocks", activeIterBlocks) ;
        }
        blockMgr.endIterator(iter) ;
    }
    
    @Override
    synchronized
    public void beginRead()
    {
        synchronized (this)
        {
            if ( inUpdate )
                log.warn("beginRead when already in update") ;
            inRead++ ;
            inUpdate = false ;
        }
        blockMgr.beginRead() ;
    }

    @Override
    synchronized
    public void endRead()
    {
        synchronized (this)
        {
            if ( inRead == 0 )
                log.error("endRead but not in read") ;
            if ( inUpdate )
                log.error("endRead when in update") ;

            checkEmpty("Outstanding write blocks at end of read operations!",
                       activeWriteBlocks) ;

            if ( inRead == 0 )
            {
                // Check at end of multiple reads or a write
                checkEmpty("Outstanding read blocks at end of read operations",
                           activeReadBlocks) ;
                clearInternalRW() ;
            }

            inUpdate = false ;
            inRead-- ;
        }
        blockMgr.endRead() ;
    }

    @Override
    public void beginUpdate()
    {
        synchronized (this)
        {
            if ( inRead > 0 )
                log.error("beginUpdate when already in read") ;
            if ( inUpdate )
                log.error("beginUpdate when already in update") ;
            inUpdate = true ;
        }
        blockMgr.beginUpdate() ;
    }

    @Override
    public void endUpdate()
    {
        synchronized (this)
        {
            if ( ! inUpdate )
                log.warn("endUpdate but not in update") ;
            if ( inRead > 0 )
                log.warn("endUpdate when in read") ;

            checkEmpty("Outstanding read blocks at end of update operations",
                       activeReadBlocks) ;

            checkEmpty("Outstanding write blocks at end of update operations",
                       activeWriteBlocks) ;

            inUpdate = false ;
            inRead = 0 ;
            clearInternalRW() ;
        }
        blockMgr.endUpdate() ;
    }

    private void checkUpdate(String method)
    {
        if ( ! inUpdate )
            log.error(method+" called outside update") ;
    }

    private void checkRead(String method)
    {
        // [TxTDB:PATCH-UP] Iterator trip this.
//        if ( ! inUpdate && ! inRead )
//            log.error(method+" called outside update and read") ;
    }

    private void checkEmpty(String string, MultiSet<Integer> blocks)
    {
        if ( ! blocks.isEmpty() )
        {
            log.error(string) ;
            for ( Integer id : blocks )
                log.info("    Block: "+id) ;
            history() ;
            throw new TDBException() ;
            //debugPoint() ;
        }
    }
    
    private void error(Action action, String string)
    {
        log.error(action+": "+string) ;
        history() ;
        throw new TDBException() ;
        //debugPoint() ;
    }

    // Do nothing - but use a a breakpoint point.
    private void debugPoint() {}

    private void warn(Action action, String string)
    { log.warn(action+": "+string) ; }

    private void history()
    {
        log.info("History") ;
        for ( Pair<Action, Integer> p : actions )
        {
            if ( p.getRight() != NoId )
                log.info(String.format("    %-12s  %d", p.getLeft(), p.getRight())) ;
            else
                log.info(String.format("    %-12s", p.getLeft())) ;
            
        }
    }
    
    @Override
    public String toString() { return "BlockMgrTracker: "+log.getName() ; } 
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