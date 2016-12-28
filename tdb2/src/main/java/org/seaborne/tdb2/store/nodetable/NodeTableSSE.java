/*
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

package org.seaborne.tdb2.store.nodetable;

import java.nio.ByteBuffer ;

import org.apache.jena.atlas.io.BlockUTF8 ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.out.NodeFmtLib ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapNull ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import org.apache.jena.riot.web.LangTag ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.util.NodeUtils ;
import org.seaborne.dboe.base.block.Block ;
import org.seaborne.dboe.base.file.BufferChannel ;
import org.seaborne.dboe.base.file.BufferChannelFile ;
import org.seaborne.dboe.base.objectfile.ObjectFile ;
import org.seaborne.dboe.base.objectfile.ObjectFileStorage ;
import org.seaborne.dboe.index.Index ;
import org.seaborne.tdb2.TDBException ;
import org.seaborne.tdb2.store.NodeId ;
import org.seaborne.tdb2.store.NodeIdFactory;

// For reference only.
public class NodeTableSSE extends NodeTableNative {

    private long position ;
    private ObjectFile objects ;
    //private final Index nodeToId ;
    
    public NodeTableSSE(Index nodeToId, String filename)
    {
        super(nodeToId);
        BufferChannel file = BufferChannelFile.create(filename) ;
        this.objects = new ObjectFileStorage(file) ;
    }
    
    @Override
    protected final NodeId writeNodeToTable(Node node)
    {
        // Synchronized in accessIndex
        long x = encodeStore(node, objects) ;
        // Paired : [*]
        return NodeIdFactory.createPtr(0, x);
    }

    @Override
    protected final Node readNodeFromTable(NodeId id)
    {
        // Paired : [*]
        long x = id.getPtrLo();
        if ( x >= objects.length() )
            return null ;
        return fetchDecode(x, objects) ;
    }
    
    private static Nodec nodec = new NodecSSE() ;
    
    private static long encodeStore(Node node, ObjectFile file)
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
    
    private static Node fetchDecode(long id, ObjectFile file)
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

    @Override
    protected void syncSub() { objects.sync(); }

    @Override
    protected void closeSub() { objects.close(); }

    /** Encode/decode for Nodes into bytes */
    public interface Nodec {
        /**
         * Calculate the maximum number of bytes needed for a Node. This needs to be
         * an overestimate and is used to ensure there is space in the bytebuffer
         * passed to encode.
         */
        public int maxSize(Node node) ;

        /**
         * Encode the node into the byte buffer, starting at the given offset. The
         * ByteBuffer will have position/limit around the space used on return,
         * <b>without a length code<b>.
         * 
         * @param node Node to encode.
         * @param bb ByteBuffer
         * @param pmap Optional prefix mapping. Can be null.
         * @return Length of byte buffer used for the whole encoding.
         */
        public int encode(Node node, ByteBuffer bb, PrefixMapping pmap) ;

        /**
         * Decode the node from the byte buffer. The ByteBuffer position should be
         * the start of the encoding (no binary length for example)
         * 
         * @param bb ByteBuffer
         * @param pmap Optional prefix mapping. Can be null.
         * @return the decoded Node.
         */
        public Node decode(ByteBuffer bb, PrefixMapping pmap) ;
    }
    
    private static class NodecSSE implements Nodec
    {
        private static boolean SafeChars = false ;
        // Characters in IRIs that are illegal and cause SSE problems, but we wish to keep.
        final private static char MarkerChar = '_' ;
        final private static char[] invalidIRIChars = { MarkerChar , ' ' } ; 
        
        public NodecSSE() {}
        
        @Override
        public int maxSize(Node node)
        {
            return maxLength(node) ;
        }

        private static final PrefixMap pmap0 = PrefixMapNull.empty ;
        private static final boolean onlySafeBNodeLabels = false ;
        @Override
        public int encode(Node node, ByteBuffer bb, PrefixMapping pmap)
        {
            String str = null ;

            if ( node.isURI() ) 
            {
                // Pesky spaces etc
                String x = StrUtils.encodeHex(node.getURI(), MarkerChar, invalidIRIChars) ;
                if ( x != node.getURI() )
                    node = NodeFactory.createURI(x) ; 
            }
            
            if ( node.isLiteral() && NodeUtils.isLangString(node) )
            {
                // Check syntactically valid.
                String lang = node.getLiteralLanguage() ;
                if ( ! LangTag.check(lang) )
                    throw new TDBException("bad language tag: "+node) ;
            }
            
            if ( node.isBlank() && ! onlySafeBNodeLabels ) {
                // Special case.
                str = "_:"+node.getBlankNodeLabel() ;
            }
            
            // Node->String
            if ( str == null )
                str = NodeFmtLib.str(node, (String)null, pmap0) ;
            // String -> bytes ;
            BlockUTF8.fromChars(str, bb) ;
            bb.flip() ;
            return bb.limit() ;
        }

        @Override
        public Node decode(ByteBuffer bb, PrefixMapping pmap)
        {
            // Ideally, this would be straight from the byte buffer.
            // But currently we go bytes -> string -> node 

            // Byte -> String
            String str = BlockUTF8.toString(bb) ;
            //OLD  
            //String str = Bytes.fromByteBuffer(bb) ;
            // String -> Node
            
            // Easy cases.
            if ( str.startsWith("_:") )   
            {
                // Must be done this way.
                // In particular, bnode labels can contain ":" from Jena
                // TokenizerText does not recognize these.
                str = str.substring(2) ;
                return NodeFactory.createBlankNode(str) ;
            }

            if ( str.startsWith("<") )
            {
                // Do directly.
                // (is it quicker?)
                str = str.substring(1,str.length()-1) ;
                str = StrUtils.unescapeString(str) ;
                str = StrUtils.decodeHex(str, MarkerChar) ;
                return NodeFactory.createURI(str) ;
            }

            Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(str) ;
            if ( ! tokenizer.hasNext() )
                throw new TDBException("Failed to tokenise: "+str) ;
            Token t = tokenizer.next() ;

            try {
                Node n = t.asNode() ;
                if ( n == null ) throw new TDBException("Not a node: "+str) ;
                return n ;
            } catch (RiotException ex)
            {
                throw new TDBException("Bad string for node: "+str) ;
            }
        }

        // Over-estimate the length of the encoding.
        private static int maxLength(Node node)
        {
            if ( node.isBlank() )
                // "_:"
                return 2+maxLength(node.getBlankNodeLabel()) ;    
            if ( node.isURI() )
                // "<>"
                return 2+maxLength(node.getURI()) ;
            if ( node.isLiteral() )
            {
                int len = 2+maxLength(node.getLiteralLexicalForm()) ;
                if ( NodeUtils.isLangString(node) )
                    // Space for @ (language tag is ASCII)
                    len = len + 3 + node.getLiteralLanguage().length() ;
                else if ( ! NodeUtils.isSimpleString(node) )
                    // The quotes and also space for ^^<>
                    len = len + 4 + maxLength(node.getLiteralDatatypeURI()) ;
                return len ;
            }
            if ( node.isVariable() )
                // "?"
                return 1+maxLength(node.getName()) ;
            throw new TDBException("Unrecognized node type: "+node) ;
        }

        private static int maxLength(String string)
        {
            // Very worse case for UTF-8 - and then some.
            // Encoding every character as _XX or bad UTF-8 conversion (3 bytes)
            // Max 3 bytes UTF-8 for up to 10FFFF (NB Java treats above 16bites as surrogate pairs only). 
            return string.length()*3 ;
        }
    }
 }