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

package org.seaborne.dboe.trans.bplustree.iterator;

import static org.apache.jena.atlas.lib.Alg.decodeIndex ;

import java.util.Iterator ;

import org.seaborne.dboe.base.record.Record ;
import org.seaborne.dboe.trans.bplustree.BPTreeNode ;
import org.seaborne.dboe.trans.bplustree.BPTreePage ;
import org.seaborne.dboe.trans.bplustree.BPTreeRecords ;

/** Iterator that does not depend on Records "link" pointers */
public class BPTreeIterator implements Iterator<Record>
{
    public static Iterator<Record> create(BPTreeNode node, Record minRec, Record maxRec) {
//        int idxMin = 0 ;
//        if ( minRec != null ) {
//            idxMin = node.findSlot(minRec) ;
//            idxMin = convert(idxMin) ;
//        }
//        int idxMax = 0 ;
//        if ( maxRec != null ) {
//            idxMax = node.findSlot(maxRec) ;
//            idxMax = convert(idxMax) ;
//        }
//        if ( idxMin == idxMax ) {
//            // All in a single sub-tree below this node.
//            BPTreePage page = node.get(idxMin) ;
//            if ( node.isLeaf() ) {
//                BPTreeRecords records = (BPTreeRecords)page ;
//                // records.iterator(minRec, maxRec) ;
//                return null ;
//            }
//            BPTreeNode node2 = (BPTreeNode)page ;
//            // Rewrite to eliminate the tail recursion.
//            return create(node2, minRec, maxRec) ;
//        }
//        
//        // Range here.
//        
//        new BPTreeIterator1(node, idxMin, idxMax, minRec, maxRec) ;
        
        
        return null ;
    }



    @Override
    public boolean hasNext() {
        return false ;
    }

    @Override
    public Record next() {
        return null ;
    }
    
    
    private static int convert(int idx) {
        if ( idx >= 0 )
            return idx ;
        return decodeIndex(idx) ;
    }
}
    
//   // Could use parent pointers to do the walk but 
//   // (1) still need to find a node in a pointer list each time
//   // (2) to avoid that, need a stack of idx.
//   
//   /** Iterate over a range of fromRec (inclusive) to toRec (exclusive) */ 
//   public static Iterator<Record> iterator(BPTreeNode node, Record fromRec, Record toRec)
//   {
//       if ( toRec != null && fromRec != null && Record.keyLE(toRec, fromRec) )
//           return Iter.nullIter() ;
//       
//       // Find the starting node.
//       int idxMin = -1 ; 
//           
//       if ( fromRec != null )
//           idxMin = node.findSlot(fromRec) ;
//
//       int idxMax = -node.getCount()-1 ;
//       
//       if ( toRec != null )
//           idxMax = node.findSlot(toRec) ;
//       
//       if ( idxMin < 0 && idxMin == idxMax && !node.isLeaf() )
//       {
//           // Fully contained in a subtree - the child behind the records insertion point.
//           int x = -(idxMin+1) ;
//           int subId = node.ptrs.get(x) ;
//           BPTreeNode n = node.pageMgr.get(subId, node.id) ; 
//           return n.iterator(fromRec, toRec) ;
//       }
//       
//       // Subtree.  Start here.
//       return new BPTreeIterator(node, fromRec, idxMin, toRec, idxMax) ;
//   }
//   
//   public static Iterator<Record> iterator(BPTreeNode node)
//   {
//       return new BPTreeIterator(node, null, null) ;
//   }
//
//   private BPTreeNode node ;
//
//   // Index of record in this node to next yield.
//   // idx == -1 => doing left subtree.
//   // idx points to the next record of this node to yield (after sub-iteraror, if any, exhaused)
//   
//   private int idx ; 
//
//   // Iterator over subtree
//   private BPTreeIterator sub ;
//
//   // Next thing to yield.
//   private Record slot ;
//
//   // Just above the last thing to yield.
//   private Record upperLimitRec = null ;
//
//   private BPTreeIterator(BPTreeNode node, Record fromRec, int idxMin, Record toRec, int idxMax)
//   {
//       init(node, fromRec, idxMin, toRec, idxMax) ;
//   }
//
//   private BPTreeIterator(BPTreeNode node, Record fromRec, Record toRec)
//   {
//       // Find the starting node.
//       int idxMin = -1 ; 
//
//       if ( fromRec != null )
//           idxMin = node.findSlot(fromRec) ;
//
//       int idxMax = -node.count-1 ;
//
//       if ( toRec != null )
//           idxMax = node.findSlot(toRec) ;
//       init(node, fromRec, idxMin, toRec, idxMax) ;
//   }
//
//   private BPTreeIterator(BPTreeNode node)
//   {
//       this.node = node ;
//       idx = 0 ;
//       slot = null ;
//       if ( ! node.isLeaf )
//           sub = makeSub(idx) ;
//       upperLimitRec = null ;
//   }
//
//   private void init(BPTreeNode node, Record fromRec, int idxMin, Record toRec, int idxMax)
//   {
//       this.node = node ;
//
//       if ( idxMin >= 0 )
//       {
//           slot = null ;
//           idx = idxMin ;
//       }
//       else
//       {
//           slot = null ;
//           idx = -(idxMin+1) ;             // Insertion point - which is the next record in this node to yield.
//
//           if ( ! node.isLeaf)
//           {
//               // Start at subtree to the immediate left.
//               // Due to child ptr staggering, this is idx.
//               // Not "makeSub(idx)" - need to pass down fromRec
//               BPTreeNode sn = node.pageMgr.get(node.ptrs.get(idx), node.id) ;
//               sub = new BTreeRangeIterator(sn, fromRec, toRec) ;
//           }
//       }
//
//       upperLimitRec = toRec ;
//   }
//
//   //@Override
//   public boolean hasNext()
//   {
//       // Leaf?
//       if ( slot != null )
//           // Something waiting.
//           return true ;
//
//       // sub iterator active.
//       if ( sub != null )
//       {
//           advanceSub() ;
//           if ( slot != null )
//               return true ; 
//           // sub ended
//           sub = null ;
//       }
//
//       // slot == null and sub == null
//
//       if ( node.isLeaf )
//       {
//           if ( idx < node.count )
//           {                 
//               Record r = node.records.get(idx) ;
//               setSlot(r) ;
//           }
//           idx++ ; 
//           return (slot!=null) ;
//       }
//
//       // sub iterator ended.  Internal node.
//       // Do one separator by setting slot and then move to the next one sub tree above. 
//
//       if ( idx < node.count )
//       {
//           // Load the separator so this gets yielded next.
//           // idx maybe -1 meaning do the subtree with no preceeding record from this node.
//           if ( idx >= 0 )
//           {
//               Record r = node.records.get(idx) ; 
//               setSlot(r) ;
//               // Continue to set up sub
//           }
//
//           idx++ ;
//           // Move the sub iterator to the next subtree.
//           sub = makeSub(idx) ;
//           
//           // If the slot was not set, then set it from the subIterator.
//           
//           if ( slot != null )
//               return true ;
//           advanceSub() ;
//           return ( slot != null ) ; 
//       }
//
//       // idx >= node.count
//       return false ;
//   }
//
//   private void advanceSub()
//   {
//       if  ( sub.hasNext() )
//           setSlot(sub.next()) ;
//       else
//           sub = null ;
//   }
//   
//   private BTreeRangeIterator makeSub(int i)
//   {
//       BPTreeNode sn = node.pageMgr.get(node.ptrs.get(i), node.id) ;
//       return new BTreeRangeIterator(sn, null, upperLimitRec) ;
//   }
//   
//   private void setSlot(Record rec)
//   {
//       if ( upperLimitRec != null && Record.keyGE(rec, upperLimitRec ) )
//       {
//           slot = null ;
//           return ;
//       }
//       slot = rec ;
//   }
//
//   //@Override
//   public Record next()
//   {
//       if ( ! hasNext() )
//           throw new NoSuchElementException() ;
//       Record x = slot ;
//       slot = null ;
//       return x ;
//   }
//
//   //@Override
//   public void remove()
//   { throw new UnsupportedOperationException("remove") ; }



    //   // Could use parent pointers to do the walk but 
    //   // (1) still need to find a node in a pointer list each time
    //   // (2) to avoid that, need a stack of idx.
    //   
    //   /** Iterate over a range of fromRec (inclusive) to toRec (exclusive) */ 
    //   public static Iterator<Record> iterator(BPTreeNode node, Record fromRec, Record toRec)
    //   {
    //       if ( toRec != null && fromRec != null && Record.keyLE(toRec, fromRec) )
    //           return Iter.nullIter() ;
    //       
    //       // Find the starting node.
    //       int idxMin = -1 ; 
    //           
    //       if ( fromRec != null )
    //           idxMin = node.findSlot(fromRec) ;
    //
    //       int idxMax = -node.getCount()-1 ;
    //       
    //       if ( toRec != null )
    //           idxMax = node.findSlot(toRec) ;
    //       
    //       if ( idxMin < 0 && idxMin == idxMax && !node.isLeaf() )
    //       {
    //           // Fully contained in a subtree - the child behind the records insertion point.
    //           int x = -(idxMin+1) ;
    //           int subId = node.ptrs.get(x) ;
    //           BPTreeNode n = node.pageMgr.get(subId, node.id) ; 
    //           return n.iterator(fromRec, toRec) ;
    //       }
    //       
    //       // Subtree.  Start here.
    //       return new BPTreeIterator(node, fromRec, idxMin, toRec, idxMax) ;
    //   }
    //   
    //   public static Iterator<Record> iterator(BPTreeNode node)
    //   {
    //       return new BPTreeIterator(node, null, null) ;
    //   }
    //
    //   private BPTreeNode node ;
    //
    //   // Index of record in this node to next yield.
    //   // idx == -1 => doing left subtree.
    //   // idx points to the next record of this node to yield (after sub-iteraror, if any, exhaused)
    //   
    //   private int idx ; 
    //
    //   // Iterator over subtree
    //   private BPTreeIterator sub ;
    //
    //   // Next thing to yield.
    //   private Record slot ;
    //
    //   // Just above the last thing to yield.
    //   private Record upperLimitRec = null ;
    //
    //   private BPTreeIterator(BPTreeNode node, Record fromRec, int idxMin, Record toRec, int idxMax)
    //   {
    //       init(node, fromRec, idxMin, toRec, idxMax) ;
    //   }
    //
    //   private BPTreeIterator(BPTreeNode node, Record fromRec, Record toRec)
    //   {
    //       // Find the starting node.
    //       int idxMin = -1 ; 
    //
    //       if ( fromRec != null )
    //           idxMin = node.findSlot(fromRec) ;
    //
    //       int idxMax = -node.count-1 ;
    //
    //       if ( toRec != null )
    //           idxMax = node.findSlot(toRec) ;
    //       init(node, fromRec, idxMin, toRec, idxMax) ;
    //   }
    //
    //   private BPTreeIterator(BPTreeNode node)
    //   {
    //       this.node = node ;
    //       idx = 0 ;
    //       slot = null ;
    //       if ( ! node.isLeaf )
    //           sub = makeSub(idx) ;
    //       upperLimitRec = null ;
    //   }
    //
    //   private void init(BPTreeNode node, Record fromRec, int idxMin, Record toRec, int idxMax)
    //   {
    //       this.node = node ;
    //
    //       if ( idxMin >= 0 )
    //       {
    //           slot = null ;
    //           idx = idxMin ;
    //       }
    //       else
    //       {
    //           slot = null ;
    //           idx = -(idxMin+1) ;             // Insertion point - which is the next record in this node to yield.
    //
    //           if ( ! node.isLeaf)
    //           {
    //               // Start at subtree to the immediate left.
    //               // Due to child ptr staggering, this is idx.
    //               // Not "makeSub(idx)" - need to pass down fromRec
    //               BPTreeNode sn = node.pageMgr.get(node.ptrs.get(idx), node.id) ;
    //               sub = new BTreeRangeIterator(sn, fromRec, toRec) ;
    //           }
    //       }
    //
    //       upperLimitRec = toRec ;
    //   }
    //
    //   //@Override
    //   public boolean hasNext()
    //   {
    //       // Leaf?
    //       if ( slot != null )
    //           // Something waiting.
    //           return true ;
    //
    //       // sub iterator active.
    //       if ( sub != null )
    //       {
    //           advanceSub() ;
    //           if ( slot != null )
    //               return true ; 
    //           // sub ended
    //           sub = null ;
    //       }
    //
    //       // slot == null and sub == null
    //
    //       if ( node.isLeaf )
    //       {
    //           if ( idx < node.count )
    //           {                 
    //               Record r = node.records.get(idx) ;
    //               setSlot(r) ;
    //           }
    //           idx++ ; 
    //           return (slot!=null) ;
    //       }
    //
    //       // sub iterator ended.  Internal node.
    //       // Do one separator by setting slot and then move to the next one sub tree above. 
    //
    //       if ( idx < node.count )
    //       {
    //           // Load the separator so this gets yielded next.
    //           // idx maybe -1 meaning do the subtree with no preceeding record from this node.
    //           if ( idx >= 0 )
    //           {
    //               Record r = node.records.get(idx) ; 
    //               setSlot(r) ;
    //               // Continue to set up sub
    //           }
    //
    //           idx++ ;
    //           // Move the sub iterator to the next subtree.
    //           sub = makeSub(idx) ;
    //           
    //           // If the slot was not set, then set it from the subIterator.
    //           
    //           if ( slot != null )
    //               return true ;
    //           advanceSub() ;
    //           return ( slot != null ) ; 
    //       }
    //
    //       // idx >= node.count
    //       return false ;
    //   }
    //
    //   private void advanceSub()
    //   {
    //       if  ( sub.hasNext() )
    //           setSlot(sub.next()) ;
    //       else
    //           sub = null ;
    //   }
    //   
    //   private BTreeRangeIterator makeSub(int i)
    //   {
    //       BPTreeNode sn = node.pageMgr.get(node.ptrs.get(i), node.id) ;
    //       return new BTreeRangeIterator(sn, null, upperLimitRec) ;
    //   }
    //   
    //   private void setSlot(Record rec)
    //   {
    //       if ( upperLimitRec != null && Record.keyGE(rec, upperLimitRec ) )
    //       {
    //           slot = null ;
    //           return ;
    //       }
    //       slot = rec ;
    //   }
    //
    //   //@Override
    //   public Record next()
    //   {
    //       if ( ! hasNext() )
    //           throw new NoSuchElementException() ;
    //       Record x = slot ;
    //       slot = null ;
    //       return x ;
    //   }
    //
    //   //@Override
    //   public void remove()
    //   { throw new UnsupportedOperationException("remove") ; }
    
    