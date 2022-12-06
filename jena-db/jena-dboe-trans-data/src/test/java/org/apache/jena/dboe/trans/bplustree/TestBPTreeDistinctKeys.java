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

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.test.RecordLib;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.*;

import static org.apache.jena.dboe.index.testlib.IndexTestLib.testInsert;
import static org.apache.jena.dboe.test.RecordLib.TestRecordLength;
import static org.apache.jena.dboe.test.RecordLib.toIntList;

@RunWith(Parameterized.class)
public class TestBPTreeDistinctKeys extends TestBPTreeModes {
    public static final byte[] DISTINCT_KEYS = new byte[] {
            0x00,
            0x04,
            0x08,
            0x0F,
            0x40,
            (byte) 0x80,
            (byte) 0xF0,
            (byte) 0xFF
    };

    public static final int[] RANDOM_SIZES = new int[] {
            10, 100, 1_000, 5_000, 10_000
    };

    public static final int[] TREE_ORDERS = new int[] { 2, 4 };

    @Parameterized.Parameters(name = "Random Size={0}, Tree Order={1}, Node dup={2}, Record dup={3}")
    public static Collection<Object[]> data() {
        List<Object[]> parameters = new ArrayList<>();
        boolean[] modes = new boolean[] { true, false };
        for (int size : RANDOM_SIZES) {
            for (int order : TREE_ORDERS) {
                for (boolean nodeDup : modes) {
                    for (boolean recordDup : modes) {
                        parameters.add(new Object[] { size, order, nodeDup, recordDup });
                    }
                }
            }
        }
        return parameters;
    }

    int randomSize, treeOrder;
    List<Integer> randomData;
    List<Integer> expectedData;

    public TestBPTreeDistinctKeys(int randomSize, int treeOrder, boolean nodeMode, boolean recordsMode) {
        super(nodeMode, recordsMode);
        this.randomSize = randomSize;
        this.treeOrder = treeOrder;

        // Generate random data
        Random random = new Random();
        randomData = new ArrayList<>();
        int maxKey = (int) Math.pow(2, 24);
        while (randomData.size() < this.randomSize) {
            int x = random.nextInt(maxKey);
            if (!randomData.contains(x)) {
                randomData.add(x);
            }
        }

        // Sort out random data into the order we expect it back out
        expectedData = new ArrayList<>(randomData);
        Collections.sort(expectedData);
    }

    private static List<Integer> populateKeys(BPlusTree bpt, byte[] keys, int keyPosition) {
        RecordFactory factory = bpt.getParams().recordFactory;
        List<Integer> expected = new ArrayList<>();
        for (byte key : keys) {
            byte[] actualKey = new byte[TestRecordLength];
            for (int i = 0; i < TestRecordLength; i++) {
                actualKey[i] = keyPosition == i ? key : 0;
            }
            Record r = factory.create(actualKey);
            bpt.insert(r);
            expected.add(Bytes.getInt(r.getKey()));
        }
        return expected;
    }

    @Test(expected = IllegalArgumentException.class)
    public void bptree_distinct_by_key_bad_01() {
        BPlusTree bpt = makeRangeIndex(this.treeOrder, 0);
        Iterator<Record> iter = bpt.distinctByKeyPrefix(0);
        Assert.assertFalse(iter.hasNext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void bptree_distinct_by_key_bad_02() {
        BPlusTree bpt = makeRangeIndex(this.treeOrder, 0);
        Iterator<Record> iter = bpt.distinctByKeyPrefix(6);
        Assert.assertFalse(iter.hasNext());
    }

    @Test
    public void bptree_distinct_by_key_01() {
        BPlusTree bpt = makeRangeIndex(this.treeOrder, 0);
        Iterator<Record> iter = bpt.distinctByKeyPrefix(1);
        Assert.assertFalse(iter.hasNext());
    }

    @Test
    public void bptree_distinct_by_key_02() {
        BPlusTree bpt = makeRangeIndex(this.treeOrder, 0);
        int[] keys = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        testInsert(bpt, keys);
        // Given a single byte prefix all the above keys have a prefix of 0 so only the first key
        // should be returned
        Iterator<Record> iter = bpt.distinctByKeyPrefix(1);
        List<Integer> actual = toIntList(iter);
        List<Integer> expected = toIntList(1);
        assertEquals(expected, actual);
    }

    @Test
    public void bptree_distinct_by_key_03() {
        BPlusTree bpt = makeRangeIndex(this.treeOrder, 0);
        int[] keys = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        testInsert(bpt, keys);
        // Given a prefix of the whole record length all keys are distinct
        Iterator<Record> iter = bpt.distinctByKeyPrefix(RecordLib.TestRecordLength);
        List<Integer> actual = toIntList(iter);
        List<Integer> expected = toIntList(keys);
        assertEquals(expected, actual);
    }

    @Test
    public void bptree_distinct_by_key_04() {
        BPlusTree bpt = makeRangeIndex(this.treeOrder, 0);
        // Keys chosen so that the first byte of the key will be different for each record
        int[] keys = new int[] { 0, 536870912, 1073741284, 1610612736 };
        List<Integer> expected = toIntList(keys);
        testInsert(bpt, keys);
        Iterator<Record> all = bpt.iterator();
        assertEquals(expected, toIntList(all));

        // Given a 1 byte prefix each key is distinct
        Iterator<Record> iter = bpt.distinctByKeyPrefix(1);
        List<Integer> actual = toIntList(iter);
        assertEquals(expected, actual);
    }

    @Test
    public void bptree_distinct_by_key_05() {
        BPlusTree bpt = makeRangeIndex(this.treeOrder, 0);
        // Same keys as 04 BUT add a few additional keys that are close to them such that their prefixes
        // are the same
        int[] keys = new int[] { 0, 1, 2, 3, 4, 536870912, 536870913, 1073741284, 1073741285, 1610612736, 1610612737 };
        testInsert(bpt, keys);
        // Given a 1 byte prefix there are 4 distinct keys based on that prefix
        Iterator<Record> iter = bpt.distinctByKeyPrefix(1);
        List<Integer> actual = toIntList(iter);
        List<Integer> expected = toIntList(0, 536870912, 1073741284, 1610612736);
        assertEquals(expected, actual);
    }

    @Test
    public void bptree_distinct_by_key_06() {
        BPlusTree bpt = makeRangeIndex(this.treeOrder, 0);
        // Create a selection of keys with distinct prefixes
        List<Integer> expected = populateKeys(bpt, DISTINCT_KEYS, 0);

        // Given a 1 byte prefix all keys are distinct
        Iterator<Record> iter = bpt.distinctByKeyPrefix(1);
        List<Integer> actual = toIntList(iter);
        assertEquals(expected, actual);
    }

    @Test
    public void bptree_distinct_by_key_07() {
        BPlusTree bpt = makeRangeIndex(this.treeOrder, 0);
        // Create a selection of keys with distinct prefixes
        List<Integer> expected = populateKeys(bpt, DISTINCT_KEYS, 1);

        // Given a 1 byte prefix all keys are non-distinct so only get first key back
        Iterator<Record> iter = bpt.distinctByKeyPrefix(1);
        List<Integer> actual = toIntList(iter);
        assertEquals(toIntList(Bytes.getInt(new byte[] { 0, DISTINCT_KEYS[0], 0, 0 })), actual);

        // Given a 2 byte prefix all keys are distinct so get all keys back
        iter = bpt.distinctByKeyPrefix(2);
        actual = toIntList(iter);
        assertEquals(expected, actual);
    }

    @Test
    public void bptree_distinct_by_key_08() {
        BPlusTree bpt = makeRangeIndex(this.treeOrder, 0);

        // Fill with a range of keys that will be non-distinct when using a 1 byte prefix
        RecordFactory factory = bpt.getParams().recordFactory;
        List<Integer> expected = new ArrayList<>();
        int maxKey = (int) Math.pow(2, 24);
        for (int i = 1; i < maxKey; i *= 2) {
            bpt.insert(factory.create(Bytes.packInt(i)));
            expected.add(i);
        }
        // Given a prefix of 1 byte only the first key is distinct
        Iterator<Record> iter = bpt.distinctByKeyPrefix(1);
        List<Integer> actual = toIntList(iter);
        assertEquals(toIntList(1), actual);

        // Given a prefix of the whole record length every key should be returned
        actual = toIntList(bpt.distinctByKeyPrefix(TestRecordLength));
        assertEquals(expected, actual);
    }

    @Test
    public void bptree_distinct_by_key_09() {
        BPlusTree bpt = makeRangeIndex(this.treeOrder, 0);
        RecordFactory factory = bpt.getRecordFactory();
        for (int key : randomData) {
            bpt.insert(factory.create(Bytes.packInt(key)));
        }

        // Given a prefix of the whole record length every key should be returned
        Iterator<Record> iter = bpt.distinctByKeyPrefix(TestRecordLength);
        verifyAllRecordsReturned(bpt, iter, expectedData);
    }

    @Test
    public void bptree_distinct_by_key_10() {
        BPlusTree bpt = makeRangeIndex(this.treeOrder, 0);
        RecordFactory factory = bpt.getRecordFactory();
        for (int key : randomData) {
            bpt.insert(factory.create(Bytes.packInt(key)));
        }

        // Find only the keys with a unique 2 byte prefix, since we already ensured with our data generation that the
        // first byte is always identical only need to use the 2nd byte as the key here
        Map<Byte, Integer> relevantKeys = new HashMap<>();
        for (int key : expectedData) {
            byte[] keyBytes = Bytes.packInt(key);
            relevantKeys.computeIfAbsent(keyBytes[1], p -> key);
        }
        List<Integer> expected = new ArrayList<>(relevantKeys.values());
        Collections.sort(expected);

        // Given a prefix of 2 bytes only the first keys with that prefix should be returned
        Iterator<Record> iter = bpt.distinctByKeyPrefix(2);
        verifyAllRecordsReturned(bpt, iter, expected);
    }

    @Test
    public void bptree_distinct_by_key_11() {
        BPlusTree bpt = makeRangeIndex(this.treeOrder, 0);
        RecordFactory factory = bpt.getRecordFactory();
        for (int key : randomData) {
            bpt.insert(factory.create(Bytes.packInt(key)));
        }

        // Find only the keys with a unique 3 byte prefix
        Map<Record, Integer> relevantKeys = new HashMap<>();
        for (int key : expectedData) {
            byte[] keyBytes = Bytes.packInt(key);
            Record record = new Record(new byte[] { keyBytes[0], keyBytes[1], keyBytes[2]}, new byte[0]);
            relevantKeys.computeIfAbsent(record, p -> key);
        }
        List<Integer> expected = new ArrayList<>(relevantKeys.values());
        Collections.sort(expected);

        // Given a prefix of 3 bytes only the first keys with that prefix should be returned
        Iterator<Record> iter = bpt.distinctByKeyPrefix(3);
        verifyAllRecordsReturned(bpt, iter, expected);
    }

    private void verifyAllRecordsReturned(BPlusTree bpt, Iterator<Record> iter, List<Integer> expected) {
        List<Integer> actual = toIntList(iter);
        for (int i = 0; i < expected.size(); i++) {
            if (i >= actual.size()) {
                dumpOnFailure(bpt, null);
                Assert.fail("Missing key at Index " + i + ", expected " + expected.get(i));
            }
            if ((int) actual.get(i) != expected.get(i)) {
                dumpOnFailure(bpt, bpt.getRecordFactory().create(Bytes.packInt(expected.get(i))));
                Assert.fail("Actual key at Index " + i + " is incorrect (" + actual.get(
                        i) + "), expected " + expected.get(i));
            }
        }
        assertEquals(expected, actual);
    }

    private void dumpOnFailure(BPlusTree bpt, Record record) {
        File treeDump = new File("target/bptree-dump-order-" + this.treeOrder + "-random-" + this.randomSize + ".txt");
        File insertDump = new File(
                "target/bptree-data-insertion-order-" + this.treeOrder + "-random-" + this.randomSize + ".txt");
        try (OutputStream output = new FileOutputStream(treeDump)) {
            bpt.dump(new IndentedWriter(output));
            System.out.println("Tree dump written to " + treeDump.getAbsolutePath());
            if (record != null) {
                System.out.println("Missing Record is: " + record);
            }
            try (BufferedWriter insertOutput = new BufferedWriter(new FileWriter(insertDump))) {
                for (int key : this.randomData) {
                    insertOutput.write(Integer.toString(key));
                    insertOutput.write('\n');
                }
            }
            System.out.println("Data Insertion dump is written to " + insertDump.getAbsolutePath());
        } catch (Throwable t) {
            // Ignore errors trying to dump debug output
        }
    }

}
