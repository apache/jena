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

package org.seaborne.dboe.trans.bplustree;

import static java.lang.String.format ;
import static org.apache.jena.atlas.lib.Alg.decodeIndex ;

import java.util.List ;
import java.util.Optional ;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.logging.FmtLog ;
import org.seaborne.dboe.trans.bplustree.AccessPath.AccessStep ;
import org.slf4j.Logger ;

/** B+Tree assist functions */
public final class BPT {
    public static boolean Logging = false ;                  // Turn on/off logging

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
    static int convert(int idx) {
        if ( idx >= 0 )
            return idx ;
        return decodeIndex(idx) ;
    }

    /** Promote a single page. Assumes the path to this page has been handled in some way elsewhere */  
    static boolean promote1(BPTreePage page, BPTreeNode node, int idx) {
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
        // ---- Logging
        if ( logging(pageLog) ) {
            log(pageLog, "Promote :: Path=%s  Page=%s", path, page) ;
//            if ( path != null ) {
//                // Fix to root.
//                path.getPath().forEach(e -> {
//                    log(pageLog, "  Path: %s->%s[%s]", e.node.label(), e.node.getId(), e.idx) ;
//                } ) ;
//            }
        }
        // ---- Checking if the access path is consistent.
        if ( BPlusTreeParams.CheckingNode && path != null ) {
            if ( path.getPath().size() > 2) {
                // Check every one except the last is not a leaf node.
                List<AccessStep> y = path.getPath().subList(0, path.getPath().size()-2) ;
                Optional<AccessStep> z = y.stream().filter(e -> e.node.isLeaf() ).findFirst() ;
                if ( z.isPresent() )
                    error("promote: Leaf %s found in path but not at the tail: %s") ;
            }
            // Check the page/index linkages.
            Optional<AccessStep> z2 = path.getPath().stream().filter(e -> e.node.ptrs.get(e.idx) != e.page.getId()).findFirst() ;
            if ( z2.isPresent() )
                error("promote: path error: %s in %s", z2.get(), path) ;
        }
        
        // ---- Clone the access path nodes.
        // Path is the route to this page - it does not include this page. 
        // Work from the bottom to the top, the reverse order of AccessPath
        if ( logging(pageLog) )
            log(pageLog, "   page>> %s", page.label()) ;
        boolean changed = page.promote();
        if ( logging(pageLog) )
            log(pageLog, "   page<< %s", page.label()) ;
        
        if ( changed ) {
            if ( path != null ) {
                // Sequence of promote1 calls? + root.
                
                List<AccessStep> steps = path.getPath() ;

                int newPtr = page.getId() ;
                BPTreePage newPage = null ;

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

                    if ( ! changed ) {
                        if ( logging(pageLog) )
                            log(pageLog, "    .. no change", n) ;
                        continue ;
                    }
                    // Reset from the duplicated below.
                    // newPtr == s.page.getId() ??
//                    if ( page != s.node && newPtr != s.page.getId() ) {
//                        System.out.flush() ;
//                        System.err.println("  Promotion: newPtr != s.page.getId(): "+newPtr+" != "+s.page.getId()) ;
//                        throw new InternalErrorException() ;
                    n.ptrs.set(s.idx, newPtr) ;
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
                    if ( logging(pageLog) )
                        log(pageLog, "  new root %s", newRoot) ;
                    page.bpTree.newRoot(newRoot) ;
                }
            } // changed.
            page.write() ;  // Necessary?
        }
    }
}

