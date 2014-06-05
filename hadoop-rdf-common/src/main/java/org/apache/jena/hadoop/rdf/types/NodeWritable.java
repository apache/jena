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

package org.apache.jena.hadoop.rdf.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;
import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.Writer2;
import org.apache.jena.hadoop.rdf.types.compators.NodeComparator;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterNT;
import org.apache.jena.riot.out.NodeToLabel;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.util.NodeUtils;

/**
 * A writable for {@link Node} instances
 * 
 * 
 * 
 */
public class NodeWritable implements WritableComparable<NodeWritable> {

    protected static final Charset utf8 = Charset.forName("utf-8");
    protected static final NodeFormatter formatter = new NodeFormatterNT();
    protected static final NodeToLabel nodeToLabel = SyntaxLabels.createNodeToLabelRT();
    protected static final LabelToNode labelToNode = SyntaxLabels.createLabelToNodeRT();
    
    static {
        WritableComparator.define(NodeWritable.class, new NodeComparator());
    }

    private Node node;

    /**
     * Creates an empty writable
     */
    public NodeWritable() {
        this(null);
    }

    /**
     * Creates a new instance from the given input
     * 
     * @param input
     *            Input
     * @return New instance
     * @throws IOException
     */
    public static NodeWritable read(DataInput input) throws IOException {
        NodeWritable nw = new NodeWritable();
        nw.readFields(input);
        return nw;
    }

    /**
     * Creates a new writable with the given value
     * 
     * @param n
     *            Node
     */
    public NodeWritable(Node n) {
        // Finally set the node
        this.set(n);
    }

    /**
     * Gets the node
     * 
     * @return Node
     */
    public Node get() {
        return this.node;
    }

    /**
     * Sets the node
     * 
     * @param n
     *            Node
     */
    public void set(Node n) {
        if (n == null || !n.isBlank()) {
            this.node = n;
        } else {
            // Special handling for blank nodes
            this.node = labelToNode.get(null, nodeToLabel.get(null, n));
        }
    }

    @Override
    public void readFields(DataInput input) throws IOException {
        // Read type and ignore, used elsewhere for fast binary comparisons
        WritableUtils.readVInt(input);

        // Read length and use this to read in the bytes that represent the
        // serialized node
        int length = WritableUtils.readVInt(input);
        byte[] bytes = new byte[length];
        input.readFully(bytes, 0, length);

        // Parse in the node
        this.node = this.parseNode(new String(bytes, utf8));
    }

    protected Node parseNode(String nodeString) {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(nodeString);
        if (!tokenizer.hasNext())
            throw new RiotException("Empty RDF term");
        Token token = tokenizer.next();
        Node node;
        if (token.isBNode()) {
            String label = NodeFmtLib.decodeBNodeLabel(token.getImage());
            node = labelToNode.get(null, label);
        } else {
            node = token.asNode(null);
        }
        if (node == null)
            throw new RiotException("Bad RDF Term: " + nodeString);

        if (tokenizer.hasNext())
            throw new RiotException("Trailing characters in string: " + nodeString);
        if (node.isURI()) {
            // Lightly test for bad URIs.
            String x = node.getURI();
            if (x.indexOf(' ') >= 0)
                throw new RiotException("Space(s) in  IRI: " + nodeString);
        }
        return node;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        StringWriter strWriter = new StringWriter();
        AWriter writer = Writer2.wrap(strWriter);
        formatter.format(writer, this.node);
        writer.close();
        String nodeStr = strWriter.toString();
        byte[] bytes = nodeStr.getBytes(utf8);
        // Write out type, length and bytes
        WritableUtils.writeVInt(output, this.getTypeId());
        WritableUtils.writeVInt(output, bytes.length);
        output.write(bytes);
    }

    /**
     * Gets the node type identifier
     * 
     * @return Node type identifier
     */
    protected final int getTypeId() {
        if (node.isVariable())
            return 0;
        if (node.isBlank())
            return 1;
        if (node.isURI())
            return 2;
        if (node.isLiteral())
            return 3;
        throw new RuntimeException("Unknown node type");
    }

    @Override
    public int compareTo(NodeWritable other) {
        return NodeUtils.compareRDFTerms(this.node, other.node);
    }

    @Override
    public String toString() {
        return this.node.toString();
    }

    @Override
    public int hashCode() {
        return this.node.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NodeWritable))
            return false;
        return this.compareTo((NodeWritable) other) == 0;
    }
}
