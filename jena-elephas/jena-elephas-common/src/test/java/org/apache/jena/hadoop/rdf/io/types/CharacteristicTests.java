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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.hadoop.rdf.types.CharacteristicSetWritable;
import org.apache.jena.hadoop.rdf.types.CharacteristicWritable;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link CharacteristicWritable} and
 * {@link CharacteristicSetWritable}
 * 
 * 
 * 
 */
public class CharacteristicTests {

    /**
     * Checks whether a writable round trips successfully
     * 
     * @param cw
     *            Characteristic writable
     * @throws IOException
     */
    private void checkRoundTrip(CharacteristicWritable cw) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(outputStream);
        cw.write(output);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        DataInputStream input = new DataInputStream(inputStream);
        CharacteristicWritable actual = CharacteristicWritable.read(input);
        Assert.assertEquals(cw, actual);
    }

    /**
     * Tests characteristic round tripping
     * 
     * @throws IOException
     */
    @Test
    public void characteristic_writable_01() throws IOException {
        Node n = NodeFactory.createURI("http://example.org");
        CharacteristicWritable expected = new CharacteristicWritable(n);
        Assert.assertEquals(1, expected.getCount().get());

        this.checkRoundTrip(expected);
    }

    /**
     * Tests characteristic properties
     * 
     * @throws IOException
     */
    @Test
    public void characteristic_writable_02() throws IOException {
        Node n = NodeFactory.createURI("http://example.org");
        CharacteristicWritable cw1 = new CharacteristicWritable(n);
        CharacteristicWritable cw2 = new CharacteristicWritable(n, 100);
        this.checkRoundTrip(cw1);
        this.checkRoundTrip(cw2);

        // Should still be equal since equality is only on the node not the
        // count
        Assert.assertEquals(cw1, cw2);
    }

    /**
     * Tests characteristic properties
     * 
     * @throws IOException
     */
    @Test
    public void characteristic_writable_03() throws IOException {
        CharacteristicWritable cw1 = new CharacteristicWritable(NodeFactory.createURI("http://example.org"));
        CharacteristicWritable cw2 = new CharacteristicWritable(NodeFactory.createURI("http://example.org/other"));
        this.checkRoundTrip(cw1);
        this.checkRoundTrip(cw2);

        // Should not be equal as different nodes
        Assert.assertNotEquals(cw1, cw2);
    }

    /**
     * Checks that a writable round trips
     * 
     * @param set
     *            Characteristic set
     * @throws IOException
     */
    private void checkRoundTrip(CharacteristicSetWritable set) throws IOException {
        // Test round trip
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(outputStream);
        set.write(output);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        DataInputStream input = new DataInputStream(inputStream);
        CharacteristicSetWritable actual = CharacteristicSetWritable.read(input);
        Assert.assertEquals(set, actual);
    }

    /**
     * Checks a characteristic set
     * 
     * @param set
     *            Set
     * @param expectedItems
     *            Expected number of characteristics
     * @param expectedCounts
     *            Expected counts for characteristics
     */
    protected final void checkCharacteristicSet(CharacteristicSetWritable set, int expectedItems, long[] expectedCounts) {
        Assert.assertEquals(expectedItems, set.size());
        Assert.assertEquals(expectedItems, expectedCounts.length);
        Iterator<CharacteristicWritable> iter = set.getCharacteristics();
        int i = 0;
        while (iter.hasNext()) {
            CharacteristicWritable cw = iter.next();
            Assert.assertEquals(expectedCounts[i], cw.getCount().get());
            i++;
        }
    }

    /**
     * Tests characteristic sets
     * 
     * @throws IOException
     */
    @Test
    public void characteristic_set_writable_01() throws IOException {
        CharacteristicSetWritable set = new CharacteristicSetWritable();

        // Add some characteristics
        CharacteristicWritable cw1 = new CharacteristicWritable(NodeFactory.createURI("http://example.org"));
        CharacteristicWritable cw2 = new CharacteristicWritable(NodeFactory.createURI("http://example.org/other"));
        set.add(cw1);
        set.add(cw2);
        this.checkCharacteristicSet(set, 2, new long[] { 1, 1 });
        this.checkRoundTrip(set);
    }

    /**
     * Tests characteristic sets
     * 
     * @throws IOException
     */
    @Test
    public void characteristic_set_writable_02() throws IOException {
        CharacteristicSetWritable set = new CharacteristicSetWritable();

        // Add some characteristics
        CharacteristicWritable cw1 = new CharacteristicWritable(NodeFactory.createURI("http://example.org"));
        CharacteristicWritable cw2 = new CharacteristicWritable(NodeFactory.createURI("http://example.org"), 2);
        set.add(cw1);
        set.add(cw2);
        this.checkCharacteristicSet(set, 1, new long[] { 3 });
        this.checkRoundTrip(set);
    }
    
    /**
     * Tests characteristic sets
     * 
     * @throws IOException
     */
    @Test
    public void characteristic_set_writable_03() throws IOException {
        CharacteristicSetWritable set1 = new CharacteristicSetWritable();
        CharacteristicSetWritable set2 = new CharacteristicSetWritable();

        // Add some characteristics
        CharacteristicWritable cw1 = new CharacteristicWritable(NodeFactory.createURI("http://example.org"));
        CharacteristicWritable cw2 = new CharacteristicWritable(NodeFactory.createURI("http://example.org/other"));
        set1.add(cw1);
        set2.add(cw2);
        this.checkCharacteristicSet(set1, 1, new long[] { 1 });
        this.checkCharacteristicSet(set2, 1, new long[] { 1 });
        this.checkRoundTrip(set1);
        this.checkRoundTrip(set2);
        
        Assert.assertNotEquals(set1, set2);
    }
}
