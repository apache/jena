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

package org.apache.jena.tdb2.lib;

import static org.apache.jena.tdb2.sys.SystemTDB.LenNodeHash;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.atlas.lib.Pool;
import org.apache.jena.atlas.lib.PoolBase;
import org.apache.jena.atlas.lib.PoolSync;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.util.NodeUtils;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.store.Hash;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdFactory;
import org.apache.jena.tdb2.store.nodetable.NodeTable;

public class NodeLib {
    public static Hash hash(Node n) {
        Hash h = new Hash(LenNodeHash);
        setHash(h, n);
        return h;
    }

    private static String BNODE   = "bnode";
    private static String URI     = "uri";
    private static String LITERAL = "literal";

    public static void setHash(Hash h, Node n) {
        if ( n.isURI() )
            hash(h, n.getURI(), null, null, URI);
        else if ( n.isBlank() )
            hash(h, n.getBlankNodeLabel(), null, null, BNODE);
        else if ( n.isLiteral() ) {
            String dt = n.getLiteralDatatypeURI();
            if ( NodeUtils.isSimpleString(n) || NodeUtils.isLangString(n) ) {
                // RDF 1.1 : No datatype for:
                // xsd:String as simple literals
                // rdf:langString and @
                dt = null;
            }
            hash(h, n.getLiteralLexicalForm(), n.getLiteralLanguage(), dt, LITERAL);
        } else
            throw new TDBException("Attempt to hash something strange: " + n);
    }

    private static int                 InitialPoolSize = 5;
    private static Pool<MessageDigest> digesters       = PoolSync.create(new PoolBase<MessageDigest>());
    static {
        try {
            for ( int i = 0; i < InitialPoolSize ; i++ )
                digesters.put(MessageDigest.getInstance("MD5"));
        }
        catch (NoSuchAlgorithmException e) {
            Log.warn(NodeLib.class, "NoSuchAlgorithmException", e);
            throw new RuntimeException(e);
        }
    }

    private static MessageDigest allocDigest() {
        try {
            MessageDigest disgest = digesters.get();
            if ( disgest == null )
                disgest = MessageDigest.getInstance("MD5");
            return disgest;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deallocDigest(MessageDigest digest) {
        digest.reset();
        digesters.put(digest);
    }

    private static void hash(Hash h, String lex, String lang, String datatype, String nodeName) {
        if ( datatype == null )
            datatype = "";
        if ( lang == null )
            lang = "";
        String toHash = lex + "|" + lang + "|" + datatype + "|" + nodeName;
        MessageDigest digest;
        try {
            // MessageDigest.getInstance("MD5");
            digest = allocDigest();
            digest.update(Bytes.string2bytes(toHash));
            if ( h.getLen() == 16 ) {
                // MD5 is 16 bytes.
                digest.digest(h.getBytes(), 0, 16);
            } else {
                byte b[] = digest.digest(); // 16 bytes.
                System.arraycopy(b, 0, h.getBytes(), 0, h.getLen());
            }
            deallocDigest(digest);
            return;
        }
        catch (DigestException ex) {
            Log.error(NodeLib.class, "DigestException", ex);
        }
    }

    public static NodeId getNodeId(Record r, int idx) {
        return NodeIdFactory.get(r.getKey(), idx);
    }

    public static Node termOrAny(Node node) {
        if ( node == null || node.isVariable() )
            return Node.ANY;
        return node;
    }

    public static Iterator<Node> nodes(final NodeTable nodeTable, Iterator<NodeId> iter) {
        return Iter.map(iter, nodeTable::getNodeForNodeId);
    }
}
