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

package org.apache.jena.hadoop.rdf.io.types;

import java.io.* ;

import org.apache.hadoop.io.WritableComparable;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import static org.apache.jena.atlas.lib.tuple.TupleFactory.tuple ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.types.NodeTupleWritable;
import org.apache.jena.hadoop.rdf.types.NodeWritable;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.sparql.core.Quad ;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the various RDF types defined by the
 * {@link org.apache.jena.hadoop.rdf.types} package
 * 
 * 
 * 
 */
public class RdfTypesTest {

    private static final Logger LOG = LoggerFactory.getLogger(RdfTypesTest.class);

    private ByteArrayOutputStream outputStream;
    private ByteArrayInputStream inputStream;

    /**
     * Prepare for output
     * 
     * @return Data output
     */
    private DataOutput prepareOutput() {
        this.outputStream = new ByteArrayOutputStream();
        return new DataOutputStream(this.outputStream);
    }

    /**
     * Prepare for input from the previously written output
     * 
     * @return Data Input
     */
    private DataInput prepareInput() {
        this.inputStream = new ByteArrayInputStream(this.outputStream.toByteArray());
        return new DataInputStream(this.inputStream);
    }

    /**
     * Prepare for input from the given data
     * 
     * @param data
     *            Data
     * @return Data Input
     */
    private DataInput prepareInput(byte[] data) {
        this.inputStream = new ByteArrayInputStream(data);
        return new DataInputStream(this.inputStream);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T extends WritableComparable> void testWriteRead(T writable, T expected) throws IOException, InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        // Write out data
        DataOutput output = this.prepareOutput();
        writable.write(output);

        // Read back in data
        DataInput input = this.prepareInput();
        T actual = (T) Class.forName(writable.getClass().getName()).newInstance();
        actual.readFields(input);

        LOG.debug("Original = " + writable.toString());
        LOG.debug("Round Tripped = " + actual.toString());

        // Check equivalent
        Assert.assertEquals(0, expected.compareTo(actual));
    }
    
    /**
     * Basic node writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void node_writable_null() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Node n = null;
        NodeWritable nw = new NodeWritable(n);
        testWriteRead(nw, nw);
    }
    
    /**
     * Basic node writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    @Ignore
    public void node_writable_variable_01() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Node n = NodeFactory.createVariable("x");
        NodeWritable nw = new NodeWritable(n);
        testWriteRead(nw, nw);
    }
    
    /**
     * Basic node writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    @Ignore
    public void node_writable_variable_02() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Node n = NodeFactory.createVariable("really-log-variable-name-asddsfr4545egfdgdfgfdgdtgvdg-dfgfdgdfgdfgdfg4-dfvdfgdfgdfgfdgfdgdfgdfgfdg");
        NodeWritable nw = new NodeWritable(n);
        testWriteRead(nw, nw);
    }

    /**
     * Basic node writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void node_writable_uri_01() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Node n = NodeFactory.createURI("http://example.org");
        NodeWritable nw = new NodeWritable(n);
        testWriteRead(nw, nw);
    }

    /**
     * Basic node writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void node_writable_uri_02() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Node n = NodeFactory.createURI("http://user:password@example.org/some/path?key=value#id");
        NodeWritable nw = new NodeWritable(n);
        testWriteRead(nw, nw);
    }

    /**
     * Basic node writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void node_writable_literal_01() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Node n = NodeFactory.createLiteral("simple");
        NodeWritable nw = new NodeWritable(n);
        testWriteRead(nw, nw);
    }

    /**
     * Basic node writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void node_writable_literal_02() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Node n = NodeFactory.createLiteral("language", "en", null);
        NodeWritable nw = new NodeWritable(n);
        testWriteRead(nw, nw);
    }

    /**
     * Basic node writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void node_writable_literal_03() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Node n = NodeFactory.createLiteral("string", XSDDatatype.XSDstring);
        NodeWritable nw = new NodeWritable(n);
        testWriteRead(nw, nw);
    }

    /**
     * Basic node writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void node_writable_literal_04() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Node n = NodeFactory.createLiteral("1234", XSDDatatype.XSDinteger);
        NodeWritable nw = new NodeWritable(n);
        testWriteRead(nw, nw);
    }

    /**
     * Basic node writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void node_writable_literal_05() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Node n = NodeFactory.createLiteral("123.4", XSDDatatype.XSDdecimal);
        NodeWritable nw = new NodeWritable(n);
        testWriteRead(nw, nw);
    }

    /**
     * Basic node writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void node_writable_literal_06() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Node n = NodeFactory.createLiteral("12.3e4", XSDDatatype.XSDdouble);
        NodeWritable nw = new NodeWritable(n);
        testWriteRead(nw, nw);
    }

    /**
     * Basic node writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void node_writable_literal_07() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Node n = NodeFactory.createLiteral("true", XSDDatatype.XSDboolean);
        NodeWritable nw = new NodeWritable(n);
        testWriteRead(nw, nw);
    }

    /**
     * Basic node writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void node_writable_bnode_01() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Node n = NodeFactory.createBlankNode();
        NodeWritable nw = new NodeWritable(n);
        testWriteRead(nw, nw);
    }

    /**
     * Basic node writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void node_writable_bnode_02() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Node n = NodeFactory.createBlankNode();
        NodeWritable nw = new NodeWritable(n);
        testWriteRead(nw, nw);
        NodeWritable nw2 = new NodeWritable(n);
        testWriteRead(nw2, nw2);

        Assert.assertEquals(0, nw.compareTo(nw2));
    }

    /**
     * Basic triple writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void triple_writable_01() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Triple t = new Triple(NodeFactory.createURI("http://example"), NodeFactory.createURI("http://predicate"), NodeFactory.createLiteral("value"));
        TripleWritable tw = new TripleWritable(t);
        testWriteRead(tw, tw);
    }
    
    /**
     * Basic triple writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void triple_writable_02() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Triple t = new Triple(NodeFactory.createBlankNode(), NodeFactory.createURI("http://predicate"), NodeFactory.createLiteral("value"));
        TripleWritable tw = new TripleWritable(t);
        testWriteRead(tw, tw);
    }

    /**
     * Basic quad writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void quad_writable_01() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Quad q = new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createURI("http://example"), NodeFactory.createURI("http://predicate"),
                NodeFactory.createLiteral("value"));
        QuadWritable qw = new QuadWritable(q);
        testWriteRead(qw, qw);
    }
    
    /**
     * Basic quad writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void quad_writable_02() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Quad q = new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createBlankNode(), NodeFactory.createURI("http://predicate"),
                NodeFactory.createLiteral("value"));
        QuadWritable qw = new QuadWritable(q);
        testWriteRead(qw, qw);
    }

    /**
     * Basic tuple writable round tripping test
     * 
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Test
    public void tuple_writable_01() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Tuple<Node> t = tuple(NodeFactory.createURI("http://one"), NodeFactory.createURI("http://two"),
                              NodeFactory.createLiteral("value"),
                              NodeFactory.createLiteral("foo"), NodeFactory.createURI("http://three"));
        NodeTupleWritable tw = new NodeTupleWritable(t);
        testWriteRead(tw, tw);
    }
}
