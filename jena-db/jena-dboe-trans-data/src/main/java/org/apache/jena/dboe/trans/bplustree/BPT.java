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

package org.apache.jena.dboe.trans.bplustree;

import static java.lang.String.format ;
import static org.apache.jena.atlas.lib.Alg.decodeIndex ;

import java.util.List ;
import java.util.Optional ;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.logging.FmtLog ;
import org.apache.jena.dboe.base.block.BlockMgr;
import org.apache.jena.dboe.base.file.BufferChannel;
import org.apache.jena.dboe.sys.SystemIndex;
import org.apache.jena.dboe.trans.bplustree.AccessPath.AccessStep;
import org.slf4j.Logger ;

/** B+Tree assist functions */
public final class BPT {
    public static boolean Logging = false ;                  // Turn on/off logging
    
    public static boolean forcePromoteModes         = false ;
    public static boolean promoteDuplicateRecords   = false ;
    public static boolean promoteDuplicateNodes     = false ;

    // Check within BPTreeNode
    public static boolean CheckingNode = false ;                      
    // Check on exit of B+Tree modifiying operations
    // Not done?
    public static boolean CheckingConcurrency = SystemIndex.Checking ;

    /** Enable detailed internal consistency checking */
    public static void checking(boolean onOrOff) {
        CheckingNode = onOrOff ;
    }

    /** Dump before and after top level update operations **/
    public static boolean DumpTree = false ;

    /** Output a lot of detailed information. */
    public static void infoAll(boolean onOrOff) { 
        DumpTree = true ;
        Logging = true ;
    }

    static boolean logging(Logger log) {
        return Logging && log.isDebugEnabled() ;
    }

    static void log(Logger log, String fmt, Object... args) {
        if ( logging(log) ) {
            FmtLog.debug(log, fmt, args);
        }
    }

    static void warning(String msg, Object... args) {
        msg = format(msg, args) ;
        System.out.println("Warning: " + msg) ;
        System.out.flush() ;
    }

    static void error(String msg, Object... args) {
        msg = format(msg, args) ;
        System.out.println() ;
        System.out.println(msg) ;
        System.out.flush() ;
        throw new BPTreeException(msg) ;
    }

    /** Convert a find index return to the insert location in the array */ 
    static int apply(int idx) {
        if ( idx >= 0 )
            return idx ;
        return decodeIndex(idx) ;
    }
    
    // ---- Promotion of pages.

    /** Promote a single page. Assumes the path to this page has been handled in some way elsewhere */  
    static boolean promote1(BPTreePage page, BPTreeNode node, int idx) {
        //System.out.println("promote1: "+page.getBlockMgr().getLabel()+" "+page.getBackingBlock().getId()) ;
        boolean changed = page.promote() ;
        node.ptrs.set(idx, page.getId()) ;
        return changed ;
    }

    /** Promote a B+Tree root */ 
    static boolean promoteRoot(BPTreeNode root) {
        if ( ! root.isRoot() ) 
            throw new InternalErrorException("Not a root") ;
        boolean changed = root.promote() ;
        root.bpTree.newRoot(root) ;
        return changed ;
    }
    
    /** Promote a B+Tree page */ 
    static void promotePage(AccessPath path, BPTreePage page) {
        Logger pageLog = page.getLogger() ;
        boolean loggingCall = logging(pageLog) ; 
        if ( loggingCall )
            log(pageLog, "Promote :: Path=%s  Page=%s", path, page) ;
        // ---- Checking if the access path is consistent.
        if ( BPT.CheckingNode && path != null ) {
            if ( path.getPath().size() > 2) {
                // Check every one except the last is not a leaf node.
                List<AccessStep> y = path.getPath().subList(0, path.getPath().size()-2) ;
                Optional<AccessStep> z = y.stream().filter(e -> e.node.isLeaf() ).findFirst() ;
                if ( z.isPresent() )
                    error("promote: Leaf %s found in path but not at the tail: %s") ;
            }
            // Check the page/index pointers
            Optional<AccessStep> z2 = path.getPath().stream().filter(e -> e.node.ptrs.get(e.idx) != e.page.getId()).findFirst() ;
            if ( z2.isPresent() )
                error("promote: path error: %s in %s", z2.get(), path) ;
        }
        
        // ---- Clone the access path nodes.
        // Path is the route to this page - it does not include this page. 
        // Work from the bottom to the top, the reverse order of AccessPath
        if ( loggingCall )
            log(pageLog, "   page>> %s", page.label()) ;
        boolean changed = page.promote();
        if ( loggingCall ) {
            if ( changed )
                log(pageLog, "   page<< %s", page.label()) ;
            else
                log(pageLog, "    .. no change") ;
        }
        
        if ( changed )
            page.write() ;  // Being careful.

        // Even if the page did not change, make sure the chain is dealt with.
        // e.g. promote in place policies. 
        //if ( changed ) {
        if ( path != null ) {
            // Sequence of promote1 calls? + root.

            List<AccessStep> steps = path.getPath() ;

            int newPtr = page.getId() ;
            BPTreePage newPage = null ;
            boolean previousChanged = changed ; 
            BPTreeNode newRoot = null ; 

            if ( logging(pageLog) )
                log(pageLog, "Path: %s", path) ;
            // Duplicate from bottom to top.
            for ( int i = steps.size() - 1 ; i >= 0 ; i--  ) {
                AccessStep s = steps.get(i) ;
                // duplicate
                BPTreeNode n = s.node ;
                if ( logging(pageLog) )
                    log(pageLog, "    >> %s", n) ;

                changed = n.promote() ;
                
                if ( previousChanged ) {
                    // Even if n did not change, if it's sub changed, need to update s.idx. 
                    n.ptrs.set(s.idx, newPtr) ;
                } else {
                    if ( ! changed ) {
                        if ( logging(pageLog) )
                            log(pageLog, "    .. no change") ;
                        continue ;
                    }
                }
                
                previousChanged = changed ;

                if ( logging(pageLog) )
                    log(pageLog, "    << %s", n) ;

                if ( n.isRoot() ) {
                    if ( newRoot != null)
                        throw new InternalErrorException("New root already found") ;
                    newRoot = n ;
                }
                newPtr = n.getId() ;
                n.write() ;

            }
            if ( newRoot != null ) {
                if ( loggingCall )
                    log(pageLog, "  new root %s", newRoot) ;
                page.bpTree.newRoot(newRoot) ;
            }
        } // end of "if ( path != null )"
    }
    
    /**
     * The initial tree is a single root node with no records block below it. It
     * is an illegal tree. We make it by creating a real tree, deleting and
     * freeing the records block from the root, then resetting the records block
     * manager. This is to avoid having specialized creating code in
     * BPlusTreeFactory solely for the rewriter.
     */
    public static BPlusTree createRootOnlyBPTree(BPlusTreeParams bptParams, 
                                                 BufferChannel bptState, 
                                                 BlockMgr blkMgrNodes, 
                                                 BlockMgr blkMgrRecords) {
        BPlusTree bpt = BPlusTreeFactory.createNonTxn(bptParams, bptState, blkMgrNodes, blkMgrRecords) ;
        
        BPTreeRecordsMgr recordsMgr = bpt.getRecordsMgr() ;
        
        BPTreeRecords recordsPage = recordsMgr.getWrite(0) ;
//        recordsPage.getRecordBuffer().clear();
//        recordsMgr.write(recordsPage) ;
        recordsMgr.free(recordsPage);
//        recordsMgr.release(recordsPage);
        recordsMgr.resetAlloc(0);
        
        BPTreeNodeMgr nodeMgr = bpt.getNodeManager() ;
        
        // Alter the root node.
        BPTreeNode root = nodeMgr.getWrite(BPlusTreeParams.RootId, BPlusTreeParams.RootParent) ;
        int rootId = root.getId() ;
        if ( rootId != 0 )
            throw new BPTreeException("**** Not the root: " + rootId) ;

        // Undo the records block.
        root.getPtrBuffer().clear() ;
        root.getRecordBuffer().clear() ;
        //root.setCount(-1);
        nodeMgr.write(root);
        nodeMgr.release(root);

        // Now a broken tree of one root block and no records.
        return bpt ;
    }
}

