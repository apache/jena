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

package com.hp.hpl.jena.tdb.lib;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenNodeHash ;

import java.nio.ByteBuffer ;
import java.security.DigestException ;
import java.security.MessageDigest ;
import java.security.NoSuchAlgorithmException ;
import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.Pool ;
import org.apache.jena.atlas.lib.PoolBase ;
import org.apache.jena.atlas.lib.PoolSync ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.riot.out.NodeFmtLib ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.store.Hash ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.NodeType ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.nodetable.Nodec ;
import com.hp.hpl.jena.tdb.store.nodetable.NodecSSE ;

public class NodeLib
{
    private static Nodec nodec = new NodecSSE() ;
    
    // Characters in IRIs that are illegal and cause SSE problems, but we wish to keep.
    final private static char MarkerChar = '_' ;
    final private static char[] invalidIRIChars = { MarkerChar , ' ' } ; 
    
    public static long encodeStore(Node node, ObjectFile file)
    {
        // Buffer pool?
        
        // Nodes can be writtern during reads.
        // Make sure this operation is sync'ed. 
        int maxSize = nodec.maxSize(node) ;
        Block block = file.allocWrite(maxSize) ;
        try {
            int len = nodec.encode(node, block.getByteBuffer(), null) ;
            file.completeWrite(block) ;
            return block.getId() ;
        } catch (TDBException ex)
        {
            file.abortWrite(block) ;
            throw ex ;
        }
    }
    
    public static Node fetchDecode(long id, ObjectFile file)
    {
        ByteBuffer bb = file.read(id) ;
        if ( bb == null )
            return null ;
        return decode(bb) ;
    }
    
    /**
     * Encode a node - it is better to use encodeStore which may avoid
     * anadditional copy in getting the node into the ObjectFile
     */
    public static ByteBuffer encode(Node node)
    {
        int maxSize = nodec.maxSize(node) ;
        ByteBuffer bb = ByteBuffer.allocate(maxSize) ;
        int len = nodec.encode(node, bb, null) ;
        bb.limit(len) ;
        bb.position(0) ;
        return bb ;
    }
    
    /**
     * Decode a node - it is better to use fetchDecode which may avoid an
     * additional copy in getting the node from the ObjectFile.
     */
    public static Node decode(ByteBuffer bb)
    {
        bb.position(0) ;
        Node n = nodec.decode(bb, null) ;
        return n ;
    }

    public static Hash hash(Node n)
    { 
        Hash h = new Hash(LenNodeHash) ;
        setHash(h, n) ;
        return h ;
    }
    
    public static void setHash(Hash h, Node n) 
    {
        NodeType nt = NodeType.lookup(n) ;
        switch(nt) 
        {
            case URI:
                hash(h, n.getURI(), null, null, nt) ;
                return ;
            case BNODE:
                hash(h, n.getBlankNodeLabel(), null, null, nt) ;
                return ;
            case LITERAL:
                hash(h,
                     n.getLiteralLexicalForm(),
                     n.getLiteralLanguage(),
                     n.getLiteralDatatypeURI(), nt) ;
                return  ;
            case OTHER:
                throw new TDBException("Attempt to hash something strange: "+n) ; 
        }
        throw new TDBException("NodeType broken: "+n) ; 
    }
    
    /** This pattern is common - abstract */ 
    private static int InitialPoolSize = 5 ;
    private static Pool<MessageDigest> digesters = PoolSync.create(new PoolBase<MessageDigest>()) ;
    static {
        try {
            for ( int i = 0 ; i < InitialPoolSize ; i++ )
                digesters.put(MessageDigest.getInstance("MD5"));
        }
        catch (NoSuchAlgorithmException e)
        { e.printStackTrace(); }
    }
    
    private static MessageDigest allocDigest()
    {
        try {
            MessageDigest disgest = digesters.get() ;
            if ( disgest == null ) 
                disgest = MessageDigest.getInstance("MD5");
            return disgest ;
        }
        catch (NoSuchAlgorithmException e)
        { e.printStackTrace(); return null ; }
    }

    private static void deallocDigest(MessageDigest digest) { digest.reset() ; digesters.put(digest) ; }
    
    
    private static void hash(Hash h, String lex, String lang, String datatype, NodeType nodeType)
    {
        if ( datatype == null )
            datatype = "" ;
        if ( lang == null )
            lang = "" ;
        String toHash = lex + "|" + lang + "|" + datatype+"|"+nodeType.getName() ;
        MessageDigest digest;
        try
        {
            digest = allocDigest() ; //MessageDigest.getInstance("MD5");
            digest.update(Bytes.string2bytes(toHash)); //digest.update(toHash.getBytes("UTF8"));
            if ( h.getLen() == 16 )
                // MD5 is 16 bytes.
                digest.digest(h.getBytes(), 0, 16) ;
            else
            {
                byte b[] = digest.digest(); // 16 bytes.
                // Avoid the copy? If length is 16.  digest.digest(bytes, 0, length) needs 16 bytes
                System.arraycopy(b, 0, h.getBytes(), 0, h.getLen()) ;
            }
            deallocDigest(digest) ;
            return ;
        }
        catch (DigestException ex) { Log.fatal(NodeLib.class, "DigestException", ex); } 
    }
    
    public static NodeId getNodeId(Record r, int idx)
    {
        return NodeId.create(Bytes.getLong(r.getKey(), idx)) ;
    }
    
    public static Node termOrAny(Node node)
    {
        if ( node == null || node.isVariable() )
            return Node.ANY ;
        return node ;
    }
    
    public static String format(String sep, Node[] nodes)
    {
        // Sigh ...
        StringBuilder b = new StringBuilder() ;
        for ( int i = 0 ; i < nodes.length ; i++ )
        {
            if ( i != 0 ) 
                b.append(sep) ;
            b.append(NodeFmtLib.str(nodes[i])) ;
        }
        return b.toString() ;
    }
    
    public static Iterator<Node> nodes(final NodeTable nodeTable, Iterator<NodeId> iter)
    {
        return Iter.map(iter, new Transform<NodeId, Node>(){
            @Override
            public Node convert(NodeId item)
            {
                return nodeTable.getNodeForNodeId(item) ;
            }
        }) ;
    }
}
