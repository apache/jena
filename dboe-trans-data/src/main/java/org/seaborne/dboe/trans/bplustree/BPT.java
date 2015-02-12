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

import static org.apache.jena.atlas.lib.Alg.decodeIndex ;

import java.util.List ;
import java.util.Optional ;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.logging.FmtLog ;
import org.seaborne.dboe.trans.bplustree.AccessPath.AccessStep ;
import org.slf4j.Logger ;

/** B+Tree assist functions */
public class BPT {
    public static boolean Logging = false ;                  // Turn on/off logging

    protected final static boolean logging(Logger log) {
        return Logging && log.isDebugEnabled() ;
    }

    protected final static void log(Logger log, String fmt, Object... args) {
        if ( logging(log) ) {
            FmtLog.debug(log, fmt, args);
        }
    }

    /** Convert a find index return to the insert location in the array */ 
    static int convert(int idx) {
        if ( idx >= 0 )
            return idx ;
        return decodeIndex(idx) ;
    }

    /** Promote a B+Tree page */ 
    /*package*/ static void promotePage(AccessPath path, BPTreePage page) {
        Logger pageLog = page.getLogger() ;
        // ---- Logging
        if ( logging(pageLog) ) {
            log(pageLog, "Promote :: Path=%s  Page=%s", path, page) ;
            if ( path != null ) {
                // Fix to root.
                path.getPath().forEach(e -> {
                    log(pageLog, "  Path: %s->%s[%s]", e.node.label(), e.node.getId(), e.idx) ;
                    //n.duplicate() ;
                } ) ;
            }
            //log(pageLog, "  Path -- %s", path) ;
        }
        // ---- Checking if the access path is consistent.
        if ( BPlusTreeParams.CheckingNode && path != null ) {
            if ( path.getPath().size() > 2) {
                try {

                    // Check every one except the last is not a leaf node.
                    List<AccessStep> y = path.getPath().subList(0, path.getPath().size()-2) ;
                    Optional<?> z = y.stream().filter(e -> e.node.isLeaf() ).findFirst() ;
                    if ( z.isPresent() )
                        throw new InternalErrorException("promote: Leaf "+z.get()+" found in path not at the tail: "+path) ;
                    z = y.stream().filter(e -> e.node.ptrs.get(e.idx) != e.page.getId()).findFirst() ;
                    if ( z.isPresent() )
                        throw new InternalErrorException("promote: path error: "+path) ;
                } catch (Throwable th) { 
                    System.err.println(path) ;
                    throw th ;
                }
            }
        }

        // ---- Clone the access path nodes.
        // Path is the route to this page - it does not include this page. 
        // Work from the bottom to the top, the reverse order of AccessPath
        boolean changed = page.promote();
        if ( changed ) {
            if ( path != null ) {
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
                    BPTreeNode n = s.node ; //** NOTE THAT WE NEED TO FIX UP page AS WELL if Pages don't mutate.
                    if ( logging(pageLog) )
                        log(pageLog, "    >> %s", n) ;

                    changed = n.promote() ; // TODO Reuses java datastructure.  Copy better? 

                    if ( logging(pageLog) )
                        log(pageLog, "    << %s", n) ;

                    if ( ! changed )
                        continue ;
                    if ( n.isRoot() ) {
                        if ( newRoot != null)
                            throw new InternalErrorException("New root already found") ;
                        newRoot = n ;
                    }
                    // Reset from the duplicated below.
                    // newPtr == s.page.getId() ??
//                    if ( page != s.node && newPtr != s.page.getId() ) {
//                        System.out.flush() ;
//                        System.err.println("  Promotion: newPtr != s.page.getId(): "+newPtr+" != "+s.page.getId()) ;
//                        throw new InternalErrorException() ;
                    n.ptrs.set(s.idx, newPtr) ;
                    newPtr = n.getId() ;

                }
                if ( newRoot != null ) {
                    if ( logging(pageLog) )
                        log(pageLog, "  new root %s", newRoot) ;
                    page.bpTree.newRoot(newRoot) ;
                }
            }
        }        
    }
}

