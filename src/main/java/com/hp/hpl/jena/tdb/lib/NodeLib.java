/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.lib;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenNodeHash ;

import java.nio.ByteBuffer ;
import java.security.DigestException ;
import java.security.MessageDigest ;
import java.security.NoSuchAlgorithmException ;
import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.Pool ;
import org.openjena.atlas.lib.PoolBase ;
import org.openjena.atlas.lib.PoolSync ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.objectfile.StringFile ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.Nodec ;
import com.hp.hpl.jena.tdb.nodetable.NodecSSE ;
import com.hp.hpl.jena.tdb.store.Hash ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.NodeType ;

public class NodeLib
{
    private static Nodec nodec = new NodecSSE() ;
    
    // Characters in IRIs that are illegal and cause SSE problems, but we wish to keep.
    final private static char MarkerChar = '_' ;
    final private static char[] invalidIRIChars = { MarkerChar , ' ' } ; 
    
    public static long encodeStore(Node node, StringFile file)
    {
        return encodeStore(node, file.getByteBufferFile()) ;
    }

    public static Node fetchDecode(long id, StringFile file)
    {
        return fetchDecode(id, file.getByteBufferFile()) ;
    }

    public static long encodeStore(Node node, ObjectFile file)
    {
        // Buffer pool?
        
        // Nodes can be writtern during reads.
        // Make sure this operation is sync'ed. 
        int maxSize = nodec.maxSize(node) ;
        ByteBuffer bb = file.allocWrite(maxSize) ;
        int len = nodec.encode(node, bb, null) ;
        long x = file.completeWrite(bb) ;
        return x ;
    }
    
    public static Node fetchDecode(long id, ObjectFile file)
    {
        ByteBuffer bb = file.read(id) ;
        return decode(bb) ;
    }
    
    /** Encode a node - pref use encodeStore */
    public static ByteBuffer encode(Node node)
    {
        int maxSize = nodec.maxSize(node) ;
        ByteBuffer bb = ByteBuffer.allocate(maxSize) ;
        int len = nodec.encode(node, bb, null) ;
        bb.limit(len) ;
        bb.position(0) ;
        return bb ;
    }
    
    /** Decode a node - pref use fetchDecode */
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
            b.append(NodeFmtLib.serialize(nodes[i])) ;
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

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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