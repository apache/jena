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

package org.seaborne.tdb2.lib;

import static org.seaborne.tdb2.sys.SystemTDB.LenNodeHash ;

import java.security.DigestException ;
import java.security.MessageDigest ;
import java.security.NoSuchAlgorithmException ;
import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.Pool ;
import org.apache.jena.atlas.lib.PoolBase ;
import org.apache.jena.atlas.lib.PoolSync ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Node ;
import org.apache.jena.riot.out.NodeFmtLib ;
import org.apache.jena.sparql.util.NodeUtils ;
import org.apache.jena.tdb.store.Hash ;
import org.apache.jena.tdb.store.NodeId ;
import org.apache.jena.tdb.store.NodeType ;
import org.seaborne.dboe.base.record.Record ;
import org.seaborne.tdb2.TDBException ;
import org.seaborne.tdb2.store.nodetable.NodeTable ;

public class NodeLib
{
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
                String dt = n.getLiteralDatatypeURI() ;
                if ( NodeUtils.isSimpleString(n) || NodeUtils.isLangString(n) ) {
                    // RDF 1.1 : No datatype for:
                    //   xsd:String as simple literals
                    //   rdf:langString and @ 
                    dt = null ;
                }
                hash(h, n.getLiteralLexicalForm(), n.getLiteralLanguage(), dt, nt) ;
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
        return Iter.map(iter, nodeTable::getNodeForNodeId) ;
    }
}
