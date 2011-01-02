package dev;

// This file records long term issue with TDB.
public class LongTermIssues
{
    /* [FREC]
     * Files: 
     *   BPlusTreeNode.split (needs further checking e.g. delete).
     *   BPTreeNodeMgr.formatBPTreeNode
     *   BPlusTreeParams.calcOrder
     *   BPTreeNodeBuilder.hasNext
     * When storing (key,value) pairs, for a real value (the hash->offset map for the nodetable)
     * the B+Tree node has a the full record, but the value is not used. 
     * 
     * Found during 0.8.8 development.
     * Left to maximise compatibility.
     * Fix when a format change occurs. 
     */
    
    /*
     * Use 63 bit NodeId, 1 bit for inline/external
     */

    /*  
     * BNodes are written without leading zeros.
     */

    /*  
     * Binary node table.
     */
}

