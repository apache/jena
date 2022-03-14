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

import static org.apache.jena.dboe.base.record.Record.keyGT;
import static org.apache.jena.dboe.base.record.Record.keyLT;
import static org.apache.jena.dboe.base.record.Record.keyNE;
import static org.apache.jena.dboe.trans.bplustree.BPT.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.dboe.base.block.Block;
import org.apache.jena.dboe.base.block.BlockMgr;
import org.apache.jena.dboe.base.buffer.PtrBuffer;
import org.apache.jena.dboe.base.buffer.RecordBuffer;
import org.apache.jena.dboe.base.page.PageBlockMgr;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.sys.SystemIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BPTreeNode extends BPTreePage
{
    private static Logger log = LoggerFactory.getLogger(BPTreeNode.class);
    @Override protected Logger getLogger() { return log; }

    /*package*/ Block block;
    /** Page id.
     *  Pages are addressed by ints (a page ref does in on-disk blocks)
     *  although blocks are addressed in longs.
     *  1k pages => 2Tbyte file limit.
     */
    /*package*/int id;             // Or block.getId()

    // Parent is a debugging aid. It records how we go to this BPTreeNode and also whether this is a root.
    private int parent;
    private int count;             // Number of records.  Number of pointers is +1

    // "Leaf" of the BPTree is the lowest level of ptr/key splits, not the data blocks.
    // We need to know this to know which block manager the block pointers refer to.
    private boolean isLeaf;

    private RecordBuffer records;
    /*package*/ void setRecordBuffer(RecordBuffer r) { records = r; }

    // Accessed by BPlusTreeFactory
    /*package*/ PtrBuffer ptrs;
    /*package*/ void setPtrBuffer(PtrBuffer pb) { ptrs = pb; }

    /* B+Tree
     *
     * Two block managers :
     *   one for Nodes (BPlusTreePages => BPlusTreeNode)
     *   one for Leaves (RecordBufferPages)
     * The split key is the held in the highest in the block
     *
     * A "leaf" node is a leaf of the B+Tree part, and points to
     * highest record in a RecordBuffer
     *
     * Example for N = 2
     *  2*N:      MaxRec = 4, MaxPtr = 5,
     *  Max-1:    HighRec = 3, HighPtr = 4
     *  N-1:      MinRec = 1, MinPtr = 2
     *
     * which is why a tree of order <2 does not make sense.
     *
     *
     * BPTreeNode:
     *
     *      +------------------------+
     *      |-| K0 | K1 | K2 | K3 |--|
     *      +------------------------+
     *      | P0 | P1 | P2 | P3 | P4 |
     *      +------------------------+
     *
     *      +------------------------+
     *      | | K0 | K1 | ** | ** |--|
     *      +------------------------+
     *      | P0 | P1 | P2 | ** | ** |
     *      +------------------------+
     *
     * BPTreeRecords -> RecordBuffer:
     *
     *      +------------------------+
     *      | K0 | K1 | K2 | ** | ** |
     *      +------------------------+
     *
     * The size of records blocks and size of tree nodes don't have to be the same.
     * They use different page managers, and are in different files.
     *
     * The minimal tree is one, leaf, root BPTreeNode and one BPTreeRecords page.
     *
     * Pictures:
     *      /--\ \--\
     * means a block with free space introduced between records[i] and records[i+1], ptrs[i+1]/ptrs[i+2]
     * Lower half is a valid structure (except for overall size)
     *
     *      /--/ /--\
     * means a block with free space introduced between records[i] and records[i+1], ptrs[i]/ptrs[i+1]
     * Upper half is a valid structure (except for overall size)
     */

    // Branch nodes only need create branch nodes (splitting sideways)
    // Leaf nodes only create leaf nodes.
    // The root is an exception.

    private BPTreeNode create(int parent, boolean isLeaf) {
        return create(bpTree, parent, isLeaf);
    }

    private static BPTreeNode create(BPlusTree bpTree, int parent, boolean isLeaf) {
        BPTreeNode n = bpTree.getNodeManager().createNode(parent);
        n.isLeaf = isLeaf;
        return n;
    }

    /*package*/ BPTreeNode(BPlusTree bpTree) {
        super(bpTree);
        // Other set by BPTreeNodeMgr.formatBPTreeNode
    }

    @Override
    public void reset(Block block) {
        this.block = block;
        // reformat block (sets record and pointer buffers)
        BPTreeNodeMgr.formatBPTreeNode(this, bpTree, block, isLeaf, parent, count);
    }

    /** Get the page at slot idx - switch between B+Tree and records files */
    /*package*/ BPTreePage get(int idx) {
        if ( false && logging(log) ) {
            String leafOrNode = isLeaf ? "L" : "N";
            log(log, "%d[%s].get(%d)", id, leafOrNode, idx);
        }
        int subId = ptrs.get(idx);
        PageBlockMgr<? extends BPTreePage> pbm = getPageBlockMgr();
        // Always get a "read" block - it must be promoted to a "write" block
        // to update it.  Getting a write block is unhelpful as many blocks
        // during a write operation are only read.
        return pbm.getRead(subId, this.getId());
    }

    private PageBlockMgr<? extends BPTreePage> getPageBlockMgr() {
        if ( isLeaf )
            return bpTree.getRecordsMgr();
        else
            return bpTree.getNodeManager();
    }

    // ---------- Public calls.
    // None of these are called recursively.

    /** Find a record, using the active comparator */
    public static Record search(BPTreeNode root, Record rec) {
        root.internalCheckNodeDeep();
        if ( ! root.isRoot() )
            throw new BPTreeException("Search not starting from the root: " + root);
        AccessPath path = new AccessPath(root);
        Record r = root.internalSearch(path, rec);
        return r;
    }

    /** Insert a record - return existing value if any, else null */
    public static Record insert(BPTreeNode root, Record record) {
        if ( logging(log) ) {
            log(log, "** insert(%s) / root=%d", record, root.getId());
            if ( DumpTree )
                root.dump();
        }

        if ( !root.isRoot() )
            throw new BPTreeException("Insert begins but this is not the root");

        if ( root.isFull() ) {
            // Root full - root split is a special case.
            splitRoot(root);
            if ( DumpTree )
                root.dump();
        }

        AccessPath path = new AccessPath(root);
        // Root ready - call insert proper.
        Record result = root.internalInsert(path, record);

        root.internalCheckNodeDeep();

        if ( logging(log) ) {
            log(log, "** insert(%s) / finish", record);
            if ( DumpTree )
                root.dump();
        }
        return result;
    }

    /** Delete a record - return the old value if there was one, else null */
    public static Record delete(BPTreeNode root, Record rec) {
        if ( logging(log) ) {
            log(log, "** delete(%s) / start", rec);
            if ( BPT.DumpTree )
                root.dump();
        }
        if ( !root.isRoot() )
            throw new BPTreeException("Delete begins but this is not the root");

        AccessPath path = new AccessPath(root);

        if ( root.isLeaf && root.count == 0 ) {
            // Special case. Just a records block. Allow that to go too small.
            BPTreePage page = root.get(0);
            if ( BPT.CheckingNode && !(page instanceof BPTreeRecords) )
                BPT.error("Zero size leaf root but not pointing to a records block");
            trackPath(path, root, 0, page);
            Record r = page.internalDelete(path, rec);
            page.release();
            if ( r != null )
                root.write();
            if ( BPT.DumpTree )
                root.dump();
            return r;
        }

        // Entry: checkNodeDeep();
        Record v = root.internalDelete(path, rec);
        // Fix the root in case it became empty in deletion process.
        if ( !root.isLeaf && root.count == 0 ) {
            reduceRoot(root);
            root.bpTree.newRoot(root);
            root.internalCheckNodeDeep();
        }

        if ( logging(log) ) {
            log(log, "** delete(%s) / finish", rec);
            if ( BPT.DumpTree )
                root.dump();
        }
        return v;
    }

    /** Iterator over the pages below that have records between minRec (inclusive) and maxRec(exclusive).
     *  There may be other records as well.
     * @param minRec
     * @param maxRec
     * @return Iterator&lt;BPTreePage&gt;
     */
    Iterator<BPTreePage> iterator(Record minRec, Record maxRec) {
        if ( minRec != null && maxRec != null && Record.keyGE(minRec, maxRec) )
            return null;//throw new IllegalArgumentException("minRec >= maxRec: "+minRec+" >= "+maxRec ) ;

        int x1 = 0;
        if ( minRec != null ) {
            x1 = findSlot(minRec);
            // If there is an exact match, we still need to go down that place to get the max.
            // If there is no match, it returns -(i+1) and we need to go down the tree at i.
            // Same effect - start at x1
            // TODO Optimization - get max record on min side once and for all if exact match.
            x1 = apply(x1);
        }

        int x2 = this.getCount();    // Highest place to look. Inclusive.
        if ( maxRec != null ) {
            // If there is an exact match, we need to go down that place to get the record upto max.
            // If there is no match, it returns -(i+1) and we need to go down the tree at i to get from last key to max (exclusive).
            // Same effect - start at x2 inclusive.
            x2 = findSlot(maxRec);
            x2 = apply(x2);
        }
        // Pages from pointer slots x1 to x2 (inc because while we exclude maxRec,
        // keys are only a max of the subtree they mark out.

        // XXX Just grab them now - later, keep indexes and fetch on next().
        // XXX Epoch tracking

        List<BPTreePage> x = new ArrayList<>(x2-x1+1);
        for ( int i = x1; i <= x2 ; i++ )
            x.add(get(i));

        return x.iterator();
    }

//    // OUT OF DATE WITH MVCC
//    /**
//     * Returns the id of the records buffer page for this record. Records Buffer
//     * Page NOT read; record may not exist
//     */
//    static int recordsPageId(BPTreeNode node, Record fromRec) {
//        // Used by BPlusTree.iterator
//        // Walk down the B+tree part of the structure ...
//        while (!node.isLeaf) {
//            BPTreePage page = (fromRec == null) ? node.get(0) : node.findHere(null, fromRec);
//            // Not a leaf so we can cast safely.
//            BPTreeNode n = (BPTreeNode)page;
//            // Release if not root.
//            if ( !node.isRoot() )
//                node.release();
//            node = n;
//        }
//        // ... then find the id of the next step down,
//        // but do not touch the records buffer page.
//        int id;
//        if ( fromRec == null ) {
//            // Just get the lowest starting place.
//            id = node.getPtrBuffer().getLow();
//        } else {
//            // Get the right id based on starting record.
//            int idx = node.findSlot(fromRec);
//            idx = apply(idx);
//            id = node.getPtrBuffer().get(idx);
//        }
//        if ( !node.isRoot() )
//            node.release();
//        return id;
//    }

    final static Record minRecord(BPTreeNode root) {
        AccessPath path = new AccessPath(root);
        return root.internalMinRecord(path);
    }

    final static Record maxRecord(BPTreeNode root) {
        AccessPath path = new AccessPath(root);
        return root.internalMaxRecord(path);
    }

    @Override
    protected Record internalMaxRecord(AccessPath path) {
        BPTreePage page = get(count);
        trackPath(path, this, count, page);
        Record r = page.internalMaxRecord(path);
        page.release();
        return r;
    }

    @Override
    protected Record internalMinRecord(AccessPath path) {
        BPTreePage page = get(0);
        trackPath(path, this, 0, page);
        Record r = page.internalMinRecord(path);
        page.release();
        return r;
    }

    @Override
    final Record getLowRecord() {
        return records.getLow();
    }

    @Override
    final Record getHighRecord() {
        return records.getHigh();
    }

    // count is the number of pointers.

    @Override
    public final int getMaxSize()          { return bpTree.getParams().getOrder(); }

    @Override
    public final int getCount()            { return count; }

    @Override
    public final void setCount(int count)  { this.count = count; }

    @Override
  public Block getBackingBlock()           { return block; }

    @Override
    public BlockMgr getBlockMgr()               { return bpTree.getNodeManager().getBlockMgr(); }

    /** Do not use without great care */
    public final RecordBuffer getRecordBuffer()  { return records; }
    /** Do not use without great care */
    public final PtrBuffer getPtrBuffer()        { return ptrs; }

    final void setParent(int parentId)           { this.parent = parentId; }
    final int getParent()                        { return parent; }

    public final void setIsLeaf(boolean isLeaf)  { this.isLeaf = isLeaf; }
    public final boolean isLeaf()                { return this.isLeaf; }

    @Override
    public final int getId()        { return id; }

    @Override
    public String getRefStr() {
        return String.format("BPTNode[id=%d]", getBackingBlock().getId());
    }

    @Override
    final void write()          { bpTree.getNodeManager().write(this); }

    final private static void trackPath(AccessPath path, BPTreeNode node, int idx, BPTreePage page) {
        if ( path != null )
            path.add(node, idx, page);
    }

    final private static void resetTrackPath(AccessPath path, BPTreeNode node, int idx, BPTreePage page) {
        if ( path != null ) {
            path.reset(node, idx, page);
        }
    }

    @Override
    final boolean promote() {
        if ( bpTree.getNodeManager().isWritable(this.getId()) )
            return false;
        // This calls reset is needed.
        //   The id, records buffer and pointer buffers need resetting if the block changed.
        boolean promoteInPlace = bpTree.state().modifiableNodeBlock(getId());
        if ( promoteInPlace ) {
            bpTree.getNodeManager().promoteInPlace(this);
            return false;
        } else {
            Block oldBlock = block;
            boolean b = bpTree.getNodeManager().promoteDuplicate(this);
            if ( b ) {
                bpTree.getNodeManager().getBlockMgr().release(oldBlock);
            }
            return b;
        }
    }

    @Override
    final void release()        { bpTree.getNodeManager().release(this); }

    @Override
    final void free()           { bpTree.getNodeManager().free(this); }

    // ============ SEARCH

    /*
     * Do a (binary) search of the node to find the record.
     *   Returns:
     *     +ve or 0 => the index of the record
     *     -ve => The insertion point : the immediate higher record or length as (-i-1)
     *  Convert to +ve and decend to find the RecordBuffer with the record in it.
     */

    @Override
    final Record internalSearch(AccessPath path, Record rec) {
        if ( BPT.CheckingNode )
            internalCheckNode();
        BPTreePage page = findHere(path, rec);
        Record r = page.internalSearch(path, rec);
        page.release();
        return r;
    }

    /** Find the next page to look at as we walk down the tree */
    private final BPTreePage findHere(AccessPath path, Record rec) {
        int idx = findSlot(rec);
        idx = apply(idx);
        // Find index, or insertion point (immediate higher slot) as (-i-1)
        // A key is the highest element of the records up to this point
        // so we search down at slot idx (between something smaller and
        // something larger).
        BPTreePage page = get(idx);
        trackPath(path, this, idx, page);
        return page;
    }

    // ============ INSERT

    /*
     * Traverse this page, ensuring the node below is not full before
     * descending. Therefore there is always space to do the actual insert.
     */

    @Override
    final Record internalInsert(AccessPath path, Record record) {
        if ( logging(log) )
            log(log, "internalInsert: %s [%s]", record, this);

        internalCheckNode();

        int idx = findSlot(record);

        if ( logging(log) )
            log(log, "internalInsert: idx=%d (=>%d)", idx, apply(idx));

        idx = apply(idx);
        BPTreePage page = get(idx);
        trackPath(path, this, idx, page);

        if ( logging(log) )
            log(log, "internalInsert: next: %s", page);

        if ( page.isFull() ) {
            // Need to split the page before descending.
            split(path, idx, page);
            // Did it shift the insert index?
            // Examine the record we pulled up in the split.
            if ( Record.keyGT(record, records.get(idx)) ) {
                page.release();
                // Yes. Get the new (upper) page
                idx = idx + 1;
                page = get(idx);
                path.reset(this, idx, page);
            }
            internalCheckNode();
        }

        Record r = page.internalInsert(path, record);
        page.release();
        return r;
    }

    /*
     * Split a non-root node y, held at slot idx in its parent,
     * which is 'this'and is large enough for a new entry without
     * another split because we split full blocks on the way down.
     * Do this by splitting the node in two (call to BPTree.split)
     * and inserting the new key/pointer pair.
     * WRITE(y)
     * WRITE(z)
     * WRITE(this)
     */
    private void split(AccessPath path, int idx, BPTreePage y) {
        // logging = true;
        if ( logging(log) ) {
            log(log, "split >> y=%s  this=%s idx=%d", y.getRefStr(), this.getRefStr(), idx);
            log(log, "split --   %s", y);
        }

        internalCheckNode();
        if ( BPT.CheckingNode ) {
            if ( !y.isFull() )
                BPT.error("Node is not full");
            if ( this.ptrs.get(idx) != y.getId() ) {
                int a = this.ptrs.get(idx);
                int b = y.getId();
                BPT.error("Node to be split isn't in right place [%d/%d]", a, b);
            }
        }
        internalCheckNodeDeep();

        // Either-Or
//        promotePage(path, this);
//        promote1(y, this, idx);
        // Includes promote "this" because "this" is in the path.
        promotePage(path, y);

        Record splitKey = y.getSplitKey();
        splitKey = keyRecord(splitKey);

        if ( logging(log) )
            log(log, "Split key: %s", splitKey);

        BPTreePage z = y.split();
        if ( logging(log) ) {
            log(log, "Split: %s", y);
            log(log, "Split: %s", z);
        }

        // Key only.
        if ( splitKey.hasSeparateValue() )
            splitKey = keyRecord(splitKey);

        // Insert new node. "add" shuffle's up as well.
        records.add(idx, splitKey);
        ptrs.add(idx + 1, z.getId());
        count++;

        if ( logging(log) ) {
            log(log, "split <<   %s", this);
            log(log, "split <<   %s", y);
            log(log, "split <<   %s", z);
        }

        y.write();
        z.write();
        z.release();
        // y.release(); y release management done by caller.
        this.write();
        if ( BPT.CheckingNode ) {
            if ( Record.keyNE(splitKey, y.maxRecord()) )
                BPT.error("Split key %s but max subtree %s", splitKey, y.maxRecord());
            internalCheckNodeDeep();
        }
    }

    @Override
    final Record getSplitKey() {
        int ix = params.SplitIndex;
        Record split = records.get(ix);
        return split;
    }

    /** Split this block - return the split record (key only needed) */
    @Override
    final BPTreePage split() {
        // Median record : will go in parent.
        int ix = params.SplitIndex;

        // New block.
        BPTreeNode z = create(this.parent, isLeaf);

        // Leave the low end untouched and copy, and clear the high end.
        // z becomes the new upper node, not the lower node.
        // 'this' is the lower block.

        int maxRec = maxRecords();
        // Copy from top of y into z.
        records.copy(ix + 1, z.records, 0, maxRec - (ix + 1));
        records.clear(ix, maxRec - ix); // Clear copied and median slot
        records.setSize(ix); // Reset size

        ptrs.copy(ix + 1, z.ptrs, 0, params.MaxPtr - (ix + 1));
        ptrs.clear(ix + 1, params.MaxPtr - (ix + 1));
        ptrs.setSize(ix + 1);

        // Set sizes of subnodes
        setCount(ix); // Median is ix
        internalCheckNode(); // y finished

        z.isLeaf = isLeaf;
        z.setCount(maxRec - (ix + 1)); // Number copied into z

        // Caller puts the blocks in split(int, BTreePage)
        z.internalCheckNode();
        return z;
    }

    /*
     * Split the root and leave the root block as the root.
     * This is the only point the height of the tree increases.
     *
     * Allocate new blocks.
     * Copy root low into left
     * Copy root high into right
     * Set counts.
     * Create new root settings (two pointers, one key record)
     * WRITE(left)
     * WRITE(right)
     * WRITE(root)
     */
    private static void splitRoot(BPTreeNode root) {
        BPlusTree bpTree = root.bpTree;

        if ( BPT.CheckingNode )
            if ( ! root.isRoot() )
                BPT.error("Not root: %d (root is id zero)", root.getId());
        root.internalCheckNode();
        promoteRoot(root);

        // Median record
        int splitIdx = root.params.SplitIndex;
        Record rec = root.records.get(splitIdx);

        if ( logging(log) ) {
            log(log, "** Split root %d (%s)", splitIdx, rec);
            log(log, "splitRoot >>   %s", root);
        }

        // New blocks.
        BPTreeNode left = create(bpTree, root.getId(), root.isLeaf);
        BPTreeNode right = create(bpTree, root.getId(), root.isLeaf);

        // int maxRecords = maxRecords();

        // New left
        root.records.copy(0, left.records, 0, splitIdx);
        root.ptrs.copy(0, left.ptrs, 0, splitIdx + 1);
        left.count = splitIdx;

        // New right
        root.records.copy(splitIdx + 1, right.records, 0, root.maxRecords() - (splitIdx + 1));
        root.ptrs.copy(splitIdx + 1, right.ptrs, 0, root.params.MaxPtr - (splitIdx + 1));
        right.count = root.maxRecords() - (splitIdx + 1);

        if ( logging(log) ) {
            log(log, "splitRoot -- left:   %s", left);
            log(log, "splitRoot -- right:  %s", right);
        }

        // So left.count+right.count = bTree.NumRec-1

        // Clear root by reformatting. New root not a leaf. Has count of 1 after
        // formatting.
        BPTreeNodeMgr.formatForRoot(root, false);
        // Make a non-leaf.

        // Insert two subnodes, divided by the median record
        root.count = 1;

        root.records.add(0, rec);
        root.ptrs.setSize(2);
        root.ptrs.set(0, left.getId()); // slot 0
        root.ptrs.set(1, right.getId()); // slot 1

        if ( logging(log) ) {
            log(log, "splitRoot <<   %s", root);
            log(log, "splitRoot <<   %s", left);
            log(log, "splitRoot <<   %s", right);
        }

        left.write();
        right.write();
        left.release();
        right.release();
        root.write();

        if ( BPT.CheckingNode ) {
            root.internalCheckNode();
            left.internalCheckNode();
            right.internalCheckNode();
        }
    }

    // ============ DELETE

    /*
     * Delete
     * Descend, making sure that the node is not minimum size at each descend.
     * If it is, rebalenace.
     */

    @Override
    final Record internalDelete(AccessPath path, Record rec) {
        if ( logging(log) )
            log(log, ">> internalDelete(%s) : %s", rec, this);
        internalCheckNode();

        int x = findSlot(rec);
        int y = apply(x);
        BPTreePage page = get(y);
        trackPath(path, this, y, page);

        if ( page.isMinSize() ) {
            // Ensure that a node is at least min+1 so a delete can happen.
            // Can't be the root - we descended in the get().
            rebalance(path, page, y);  // Ignore return - need to re-find.
            page.release(); // TODO But rebalance may have freed this?
            // Rebalance may have moved the record due to shuffling.
            x = findSlot(rec);
            y = apply(x);
            page = get(y);
            promote1(page, this, y);
            resetTrackPath(path, this, y, page);
            if ( BPT.CheckingNode ) {
                internalCheckNode();
                page.checkNode();
            }
            this.write();
            // Needed in case the record being deleted is not in the
            // tree, which mean there is no work done and
            // this page is not written.
            page.write();
        }

        // Go to bottom
        // Need to return the deleted key/value.
        Record r2 = page.internalDelete(path, rec);
        if ( x >= 0 ) {
            // And hence r2 != null.
            // The deleted key was in the tree as well as the records.
            // Change to the new key for the subtree.
            // Path is already promoted by the delete.
            // promote1(this, ??, ??);
            Record mx = page.maxRecord();
            records.set(x, keyRecord(mx));
            this.write();
        }
        if ( logging(log) )
            log(log, "<< internalDelete(%s) : %s", rec, this);

        page.release();
        return r2;
    }

    /*
     * Reduce the root when it has only one pointer and no records.
     * WRITE(root)
     * RELEASE(old child)
     * This is the only point the height of the tree decreases.
     */

    private static void reduceRoot(BPTreeNode root) {
        if ( logging(log) )
            log(log, "reduceRoot >> %s", root);

        if ( BPT.CheckingNode && (!root.isRoot() || root.count != 0) )
            BPT.error("Not an empty root");

        if ( root.isLeaf ) {
            if ( logging(log) )
                log(log, "reduceRoot << leaf root");
            return;
        }

        // Must have been cloned due to delete lower down
        promoteRoot(root);

        BPTreePage sub = root.get(0);
        promote1(sub, root, 0);
        BPTreeNode n = cast(sub);
        // Can pull up into the root.
        // Leave root node in same block (rather than swap to new root).
        BPTreeNodeMgr.formatForRoot(root, n.isLeaf);
        n.records.copy(0, root.records, 0, n.count);
        n.ptrs.copy(0, root.ptrs, 0, n.count + 1);
        root.isLeaf = n.isLeaf;
        root.count = n.count;
        root.write();
        // Free up.
        n.free();
        if ( BPT.CheckingNode )
            root.internalCheckNodeDeep();

        if ( logging(log) )
            log(log, "reduceRoot << %s", root);
    }

    /*
     * Rebalance "node" at slot idx in parent (this)
     * The node will then be greater than the minimum size
     * and one-pass delete is then possible.
     *
     * try to shift right, from the left sibling (if exists)
     * WRITE(left)
     * WRITE(n)
     * WRITE(this)
     * try to shift left, from the right sibling (if exists)
     * WRITE(right)
     * WRITE(n)
     * WRITE(this)
     * else
     * merge with left or right sibling
     * Suboperations do all the write-back of nodes.
     *
     * return the page which might be coalesced from left or right..
     */
    private BPTreePage rebalance(AccessPath path, BPTreePage node, int idx) {
        if ( logging(log) ) {
            log(log, "rebalance(id=%d, idx=%d)", node.getId(), idx);
            log(log, ">> this: %s", this);
            log(log, ">> node: %s", node);
        }
        internalCheckNode();
//        promotePage(path, this);
//        promote1(node, this, idx);
        // Includes promote "this"
        promotePage(path, node);

        // Try left first
        BPTreePage left = null;
        if ( idx > 0 )
            left = get(idx - 1);

        // *** SHIFTING : need to change the marker record in the parent.
        // *** getHighRecord of lower block.

        if ( left != null && !left.isMinSize() ) {
            if ( logging(log) )
                log(log, "rebalance/shiftRight");
            // Move elements around.
            promote1(left, this, idx-1);

            shiftRight(left, node, idx - 1);

            if ( logging(log) )
                log(log, "<< rebalance: %s", this);
            if ( BPT.CheckingNode ) {
                left.checkNode();
                node.checkNode();
                this.internalCheckNode();
            }
            left.release();
            return node;
        }

        BPTreePage right = null;
        if ( idx < count )
            right = get(idx + 1);

        if ( right != null && !right.isMinSize() ) {
            if ( logging(log) )
                log(log, "rebalance/shiftLeft");

            promote1(right, this, idx+1);

            shiftLeft(node, right, idx);

            if ( logging(log) )
                log(log, "<< rebalance: %s", this);
            if ( BPT.CheckingNode ) {
                right.checkNode();
                node.checkNode();
                this.internalCheckNode();
            }
            if ( left != null )
                left.release();
            right.release();
            return node;
        }

        // Couldn't shift. Collapse two pages.
        if ( BPT.CheckingNode && left == null && right == null )
            BPT.error("No siblings");

        if ( left != null ) {
            promote1(left, this, idx-1 );
            if ( logging(log) )
                log(log, "rebalance/merge/left: left=%d n=%d [%d]", left.getId(), node.getId(), idx - 1);
            if ( BPT.CheckingNode && left.getId() == node.getId() )
                BPT.error("Left and n the same: %s", left);
            BPTreePage page = merge(left, node, idx - 1);
            if ( right != null )
                // HACK : We didn't use it.
                right.release();
            //left release?
            return page;
        } else {
            // left == null
            // rigth != null
            promote1(right, this, idx+1 );
            // TODO ** HERE is it tracked correctly? Turmn tracking on, test_clear_02 and enable read checking in   Lifecycle track.free.
            if ( logging(log) )
                log(log, "rebalance/merge/right: n=%d right=%d [%d]", node.getId(), right.getId(), idx);
            if ( BPT.CheckingNode && right.getId() == node.getId() )
                BPT.error("N and right the same: %s", right);
            BPTreePage page = merge(node, right, idx);
            return page;
        }
    }

    /** Merge left with right; fills left, frees right */
    private BPTreePage merge(BPTreePage left, BPTreePage right, int dividingSlot) {
        if ( logging(log) ) {
            log(log, ">> merge(@%d): %s", dividingSlot, this);
            log(log, ">> left:  %s", left);
            log(log, ">> right: %s", right);
        }

        // /==\ + key + /==\ ==> /====\
        Record splitKey = records.get(dividingSlot);
        BPTreePage page = left.merge(right, splitKey);
        // Must release right (not done in merge)
        if ( logging(log) )
            log(log, "-- merge: %s", page);

        left.write();
        left.release();
        right.free();

        if ( page == right )
            BPT.error("Returned page is not the left");

        if ( BPT.CheckingNode ) {
            if ( isLeaf ) {
                // If two data blocks, then the split key is not included
                // (it's already there, with its value).
                // Size is N+N and max could be odd so N+N and N+N+1 are
                // possible.
                if ( left.getCount() + 1 != left.getMaxSize() && left.getCount() != left.getMaxSize() )
                    BPT.error("Inconsistent data node size: %d/%d", left.getCount(), left.getMaxSize());
            } else if ( !left.isFull() ) {
                // If not two data blocks, the left side should now be full
                // (N+N+split)
                BPT.error("Inconsistent node size: %d/%d", left.getCount(), left.getMaxSize());
            }
        }

        // Remove from parent (which is "this")
        shuffleDown(dividingSlot);
        this.write();
        internalCheckNodeDeep();
        if ( logging(log) ) {
            log(log, "<< merge: %s", this);
            log(log, "<< left:  %s", left);
        }
        return left;
    }

    @Override
    BPTreePage merge(BPTreePage right, Record splitKey) {
        return merge(this, splitKey, cast(right));
    }

    private static BPTreeNode merge(BPTreeNode left, Record splitKey, BPTreeNode right) {
        // Merge blocks - does not adjust the parent.
        // Copy right to top of left.
        // Caller releases 'right' (needed for testing code).

        left.records.add(splitKey);

        // Copy over right to top of left.
        right.records.copyToTop(left.records);
        right.ptrs.copyToTop(left.ptrs);

        // Update count
        left.count = left.count + right.count + 1;
        left.internalCheckNode();

        right.records.clear();
        right.ptrs.clear();
        return left;
    }

    private void shiftRight(BPTreePage left, BPTreePage right, int i) {
        if ( logging(log) ) {
            log(log, ">> shiftRight: this:  %s", this);
            log(log, ">> shiftRight: left:  %s", left);
            log(log, ">> shiftRight: right: %s", right);
        }
        Record r1 = records.get(i);
        Record r2 = left.shiftRight(right, r1);
        r2 = keyRecord(r2);
        this.records.set(i, r2);

        left.write();
        right.write();
        // Do later -- this.put();
        if ( logging(log) ) {
            log(log, "<< shiftRight: this:  %s", this);
            log(log, "<< shiftRight: left:  %s", left);
            log(log, "<< shiftRight: right: %s", right);
        }
    }

    private void shiftLeft(BPTreePage left, BPTreePage right, int i) {
        if ( logging(log) ) {
            log(log, ">> shiftLeft: this:  %s", this);
            log(log, ">> shiftLeft: left:  %s", left);
            log(log, ">> shiftLeft: right: %s", right);
        }
        Record r1 = records.get(i);
        Record r2 = left.shiftLeft(right, r1);
        r2 = keyRecord(r2);
        this.records.set(i, r2);

        left.write();
        right.write();
        // Do this later - this.put();
        if ( logging(log) ) {
            log(log, "<< shiftLeft: this:  %s", this);
            log(log, "<< shiftLeft: left:  %s", left);
            log(log, "<< shiftLeft: right: %s", right);
        }
    }

    @Override
    Record shiftRight(BPTreePage other, Record splitKey) {
        BPTreeNode node = cast(other);
        if ( BPT.CheckingNode ) {
            if ( count == 0 )
                BPT.error("Node is empty - can't shift a slot out");
            if ( node.isFull() )
                BPT.error("Destination node is full");
        }
        // Records: promote moving element, replace with splitKey
        Record r = this.records.getHigh();
        this.records.removeTop();
        node.records.add(0, splitKey);

        // Pointers just shift
        this.ptrs.shiftRight(node.ptrs);

        this.count--;
        node.count++;
        this.internalCheckNode();
        node.internalCheckNode();
        return r;
    }

    @Override
    Record shiftLeft(BPTreePage other, Record splitKey) {
        BPTreeNode node = cast(other);
        if ( BPT.CheckingNode ) {
            if ( count == 0 )
                BPT.error("Node is empty - can't shift a slot out");
            if ( isFull() )
                BPT.error("Destination node is full");
        }
        Record r = node.records.getLow();
        // Records: promote moving element, replace with splitKey
        this.records.add(splitKey);
        node.records.shiftDown(0);

        // Pointers just shift
        this.ptrs.shiftLeft(node.ptrs);

        this.count++;
        node.count--;
        return r;
    }

    private void shuffleDown(int x) {
        // x is the index in the parent and may be on eover the end.
        if ( logging(log) ) {
            log(log, "ShuffleDown: i=%d count=%d MaxRec=%d", x, count, maxRecords());
            log(log, "ShuffleDown >> %s", this);
        }

        if ( BPT.CheckingNode && x >= count )
            BPT.error("shuffleDown out of bounds");

        // Just the top to clear

        if ( x == count - 1 ) {
            records.removeTop();
            ptrs.removeTop();

            count--;
            if ( logging(log) ) {
                log(log, "shuffleDown << Clear top");
                log(log, "shuffleDown << %s", this);
            }
            internalCheckNode();
            return;
        }

        // Shuffle down. Removes key and pointer just above key.

        records.shiftDown(x);
        ptrs.shiftDown(x + 1);
        count--;
        if ( logging(log) )
            log(log, "shuffleDown << %s", this);
        internalCheckNode();
    }

    // ---- Utilities

    private static final BPTreeNode cast(BPTreePage other) {
        try {
            return (BPTreeNode)other;
        }
        catch (ClassCastException ex) {
            BPT.error("Wrong type: " + other);
            return null;
        }
    }

    final int findSlot(Record rec) {
        int x = records.find(rec);
        return x;
    }

    final boolean isRoot() {
        // No BPT remembered root node currently
        // if ( bpTree.root == this ) return true;
        return this.parent == BPlusTreeParams.RootParent;
    }

    private Record keyRecord(Record record) {
        return bpTree.getRecordFactory().createKeyOnly(record);
    }

    // Fixup/remove?
    private final int maxRecords() {
        return params.MaxRec;
    }

    @Override
    final boolean isFull() {
        if ( BPT.CheckingNode && count > maxRecords() )
            BPT.error("isFull: Moby block: %s", this);

        // Count is number of records.
        return count >= maxRecords();
    }

    /** Return true if there are no keys here or below this node */
    @Override
    final boolean hasAnyKeys() {
        if ( this.count > 0 )
            return true;
        if ( !isRoot() )
            return false;

        // The root can be zero size and point to a single data block.
        BPTreePage page = get(0);
        boolean b = page.hasAnyKeys();
        page.release();
        return b;
    }

    @Override
    final boolean isMinSize() {
        int min = params.getMinRec();
        if ( BPT.CheckingNode && count < min )
            BPT.error("isMinSize: Dwarf block: %s", this);

        return count <= min;
    }

    // ========== Other

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if ( isLeaf )
            b.append("LEAF: ");
        else
            b.append("NODE: ");
        String labelStr = "??";
        if ( parent >= 0 )
            labelStr = Integer.toString(parent);
        else if ( parent == BPlusTreeParams.RootParent )
            labelStr = "root";
        if ( isLeaf )
            labelStr = labelStr + "/leaf";

        b.append(String.format("%d [%s] (size %d/%d) -- ", getId(), labelStr, count, getMaxSize()));
        b.append("\n");
        b.append(String.format("    Range [%s .. %s]", minRecord(), maxRecord()));

        if ( getCount() == 0) {
            b .append("Empty");
            return b.toString();
        }

        // All -- int N = maxRecords();
        // in-use
        int N = count;

        for ( int i = 0; i < N ; i++ ) {
            b.append("\n    ");
            b.append(childStr(i));
            b.append(" max=(");
            b.append(recstr(records, i));
            b.append(")");
        }
        b.append("\n    ");
        b.append(childStr(N));
        b.append(" top=[");
        b.append(maxRecord().toString());
        b.append("]");
        return b.toString();
    }

    @Override
    protected String typeMark() {
        String mark = isLeaf() ? "Leaf" : "Node";
        if ( isRoot() )
            mark = mark+"/Root";
        return mark;
    }

    private final String recstr(RecordBuffer records, int idx) {
        if ( records.isClear(idx) )
            return "----";

        Record r = records._get(idx);
        return r.toString();
    }

    public void dump() {
        boolean b = BPT.Logging;
        BPT.Logging = false;
        try {
            dump(IndentedWriter.stdout);
            IndentedWriter.stdout.flush();
            System.out.flush();
        } finally {
            BPT.Logging = b;
        }
    }

    public void dump(IndentedWriter out) {
        output(out);
        out.ensureStartOfLine();
        out.flush();
    }

    public String dumpToString() {
        IndentedLineBuffer buff = new IndentedLineBuffer();
        output(buff);
        return buff.asString();
    }

    @Override
    public void output(IndentedWriter out) {
        out.print(toString());
        out.incIndent();
        for ( int i = 0; i < count + 1 ; i++ ) {
            out.println();
            BPTreePage page = get(i);
            page.output(out);
            page.release();

        }
        out.decIndent();
    }

    private String childStr(int i) {
        if ( i >= ptrs.size() )
            return "*";
        int x = ptrs.get(i);
        return Integer.toString(x);
    }

    // =========== Checking
    // internal checks - only if checking

    // Check node does not assume a valid tree - may be in mid-operation.
    private final void internalCheckNode() {
        if ( BPT.CheckingNode )
            checkNode(null, null);
    }

    private final void internalCheckNodeDeep() {
        // This disturbs tracking operations and blocks.
        // Hooks left but switched off.
        if ( false )
            checkNodeDeep();
    }

    @Override
    final void checkNode() {
        checkNode(null, null);
    }

    @Override
    final void checkNodeDeep() {
        if ( isRoot() ) {
            // if ( !isLeaf && count == 0 )
            // error("Root is of size zero (one pointer) but not a leaf");
            if ( parent != BPlusTreeParams.RootParent )
                BPT.error("Root parent is wrong");
            // if ( count == 0 )
            // return;
        }
        checkNodeDeep(null, null);
    }

    // Checks of a single node - no looking at children
    // min - inclusive; max - inclusive (allows for duplicates)
    final private void checkNode(Record min, Record max) {
        int id = getId();
        if ( count != records.size() )
            BPT.error("Inconsistent: id=%d, count=%d, records.size()=%d : %s", id, count, records.size(), this);

        if ( !isLeaf && count + 1 != ptrs.size() )
            BPT.error("Inconsistent: id=%d, count+1=%d, ptrs.size()=%d; %s", id, count + 1, ptrs.size(), this) ;

        // No BPT remembered root node currently
        // if ( bpTree.root != null && !isRoot() && count < params.MinRec)
        if ( !isRoot() && count < params.MinRec ) {
            // warning("Runt node: %s", this);
            BPT.error("Runt node: %s", this);
        }
        if ( !isRoot() && count > maxRecords() )
            BPT.error("Over full node: %s", this);
        if ( !isLeaf && parent == id )
            BPT.error("Parent same as id: %s", this);
        Record k = min;

        // Test records in the allocated area
        for ( int i = 0; i < count ; i++ ) {
            if ( records.get(i) == null )
                BPT.error("Node: %d : Invalid record @%d :: %s", id, i, this);
            if ( k != null && keyGT(k, records.get(i)) ) {
                Record r = records.get(i);
                // keyGT(k, r);
                BPT.error("Node: %d: Not sorted (%d) (%s, %s) :: %s ", id, i, k, r, this);
            }
            k = records.get(i);
        }

        if ( k != null && max != null && keyGT(k, max) )
            BPT.error("Node: %d - Record is too high (max=%s):: %s", id, max, this);

        if ( SystemIndex.getNullOut() ) {
            // Test records in the free area
            for ( int i = count; i < maxRecords() ; i++ ) {
                if ( !records.isClear(i) )
                    BPT.error("Node: %d - not clear (idx=%d) :: %s", id, i, this);
            }
        }

        // Pointer checks.
        int i = 0;
        // Check not empty at bottom.
        for (; i < count + 1 ; i++ ) {
            if ( ptrs.get(i) < 0 )
                BPT.error("Node: %d: Invalid child pointer @%d :: %s", id, i, this);
        }

        // Check empty is empty
        if ( SystemIndex.getNullOut() ) {
            int x = params.MaxPtr;
            for (; i < x ; i++ ) {
                if ( !ptrs.isClear(i) )
                    BPT.error("Node: %d: Unexpected pointer @%d :: %s", id, i, this);
            }
        }
    }

    private void checkNodeDeep(Record min, Record max) {
        checkNode(min, max);
        int id = getId();
        // Check pointers.
        int limit = (count == 0) ? 0 : count + 1;

        for ( int i = 0; i < limit ; i++ ) {
            Record min1 = min;
            Record max1 = max;
            BPTreePage n = get(i);

            if ( i != count ) {
                Record keySubTree = n.getHighRecord(); // high key in immediate child
                Record keyHere = records.get(i);       // key in this node

                if ( keySubTree == null )
                    BPT.error("Node: %d: Can't get high record from %d", id, n.getId());

                if ( keySubTree.getKey() == null )
                    BPT.error("Node: %d: Can't get high record is missing it's key from %d", id, n.getId());

                if ( keyHere == null )
                    BPT.error("Node: %d: record is null", id);

                if ( keyHere.getKey() == null )
                    BPT.error("Node: %d: Record key is null", id);

                if ( keyGT(keySubTree, keyHere) )
                    BPT.error("Node: %d: Child key %s is greater than this key %s", id, keySubTree, keyHere);

                Record keyMax = n.maxRecord(); // max key in subTree
                Record keyMin = n.internalMinRecord(null);

                if ( keyNE(keyHere, keyMax) )
                    BPT.error("Node: %d: Key %s is not the max [%s] of the sub-tree idx=%d", id, keyHere, keyMax, i);

                if ( min != null && keyGT(min, keyMin) )
                    BPT.error("Node: %d: Minimun for this node should be %s but it's %s", id, min, keyMin);
                if ( max != null && keyLT(max, keyMax) )
                    BPT.error("Node: %d: Maximum for this node should be %s but it's %s", id, max, keyMax);
                if ( min != null && keyGT(min, keyHere) )
                    BPT.error("Node: %d: Key too small: %s - min should be %s", id, keyHere, min);
                // keyHere == keyMax ??
                if ( max != null && keyLT(max, keyHere) )
                    BPT.error("Node: %d: Key too large: %s - max should be %s", id, keyHere, max);
            }

            // Look deeper.
            if ( !(n instanceof BPTreeNode) ) {
                // Records.
                n.checkNodeDeep();
                n.release();
                continue;
            }

            // Valid pointer?
            if ( isLeaf ) {
                if ( !bpTree.getRecordsMgr().getBlockMgr().valid(ptrs.get(i)) )
                    BPT.error("Node: %d: Dangling ptr (records) in block @%d :: %s", id, i, this);
            } else {
                if ( !bpTree.getNodeManager().valid(ptrs.get(i)) )
                    BPT.error("Node: %d: Dangling ptr in block @%d :: %s", id, i, this);
            }

            // Calc new min/max.
            if ( i == 0 )
                max1 = records.get(0);
            else if ( i == count ) {
                min1 = records.get(count - 1);
                max1 = null;
            } else {
                min1 = records.get(i - 1);
                max1 = records.get(i);
            }

            ((BPTreeNode)n).checkNodeDeep(min1, max1);
            n.release();
        }
    }
}
