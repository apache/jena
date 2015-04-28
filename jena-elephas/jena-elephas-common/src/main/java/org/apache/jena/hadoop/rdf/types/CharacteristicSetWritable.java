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
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableUtils;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;

/**
 * Represents a characteristic set which is comprised of a count of nodes for
 * which the characteristic is applicable and a set of characteristics which
 * represents the number of usages of predicates with those nodes
 * 
 * 
 * 
 */
public class CharacteristicSetWritable implements WritableComparable<CharacteristicSetWritable> {

    private Map<NodeWritable, CharacteristicWritable> characteristics = new TreeMap<NodeWritable, CharacteristicWritable>();
    private LongWritable count = new LongWritable();

    /**
     * Creates a new empty characteristic set with the default count of 1
     */
    public CharacteristicSetWritable() {
        this(1);
    }

    /**
     * Creates a new characteristic set with the default count of 1 and the
     * given characteristics
     * 
     * @param characteristics
     *            Characteristics
     */
    public CharacteristicSetWritable(CharacteristicWritable... characteristics) {
        this(1, characteristics);
    }

    /**
     * Creates an empty characteristic set with the given count
     * 
     * @param count
     *            Count
     */
    public CharacteristicSetWritable(long count) {
        this(count, new CharacteristicWritable[0]);
    }

    /**
     * Creates a new characteristic set
     * 
     * @param count
     *            Count
     * @param characteristics
     *            Characteristics
     */
    public CharacteristicSetWritable(long count, CharacteristicWritable... characteristics) {
        this.count.set(count);
        for (CharacteristicWritable characteristic : characteristics) {
            this.characteristics.put(characteristic.getNode(), characteristic);
        }
    }

    /**
     * Creates a new instance and reads its data from the given input
     * 
     * @param input
     *            Input
     * @return New instance
     * @throws IOException
     */
    public static CharacteristicSetWritable read(DataInput input) throws IOException {
        CharacteristicSetWritable set = new CharacteristicSetWritable();
        set.readFields(input);
        return set;
    }

    /**
     * Gets the count
     * 
     * @return Count
     */
    public LongWritable getCount() {
        return this.count;
    }

    /**
     * Gets the characteristics
     * 
     * @return Characteristics
     */
    public Iterator<CharacteristicWritable> getCharacteristics() {
        return this.characteristics.values().iterator();
    }

    /**
     * Gets the size of the characteristic set
     * 
     * @return Size
     */
    public int size() {
        return this.characteristics.size();
    }

    /**
     * Adds a characteristic to the set merging it into the appropriate existing
     * characteristic if applicable
     * 
     * @param characteristic
     *            Characteristics
     */
    public void add(CharacteristicWritable characteristic) {
        if (this.characteristics.containsKey(characteristic.getNode())) {
            this.characteristics.get(characteristic.getNode()).increment(characteristic.getCount().get());
        } else {
            this.characteristics.put(characteristic.getNode(), characteristic);
        }
    }

    /**
     * Adds some characteristics to the set merging them with the appropriate
     * existing characteristics if applicable
     * 
     * @param characteristics
     */
    public void add(CharacteristicWritable... characteristics) {
        for (CharacteristicWritable characteristic : characteristics) {
            this.add(characteristic);
        }
    }

    /**
     * Adds the contents of the other characteristic set to this characteristic
     * set
     * 
     * @param set
     *            Characteristic set
     */
    public void add(CharacteristicSetWritable set) {
        this.increment(set.getCount().get());
        Iterator<CharacteristicWritable> iter = set.getCharacteristics();
        while (iter.hasNext()) {
            this.add(iter.next());
        }
    }

    /**
     * Gets whether the set contains a characteristic for the given predicate
     * 
     * @param uri
     *            Predicate URI
     * @return True if contained in the set, false otherwise
     */
    public boolean hasCharacteristic(String uri) {
        return this.hasCharacteristic(NodeFactory.createURI(uri));
    }

    /**
     * Gets whether the set contains a characteristic for the given predicate
     * 
     * @param n
     *            Predicate
     * @return True if contained in the set, false otherwise
     */
    public boolean hasCharacteristic(Node n) {
        return this.hasCharacteristic(new NodeWritable(n));
    }

    /**
     * Gets whether the set contains a characteristic for the given predicate
     * 
     * @param n
     *            Predicate
     * @return True if contained in the set, false otherwise
     */
    public boolean hasCharacteristic(NodeWritable n) {
        return this.characteristics.containsKey(n);
    }

    /**
     * Increments the count by the given increment
     * 
     * @param l
     *            Increment
     */
    public void increment(long l) {
        this.count.set(this.count.get() + l);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        // Write size, then count, then characteristics
        WritableUtils.writeVInt(output, this.characteristics.size());
        this.count.write(output);
        for (CharacteristicWritable characteristic : this.characteristics.values()) {
            characteristic.write(output);
        }
    }

    @Override
    public void readFields(DataInput input) throws IOException {
        // Read size, then count, then characteristics
        int size = WritableUtils.readVInt(input);
        this.count.readFields(input);
        this.characteristics.clear();
        for (int i = 0; i < size; i++) {
            CharacteristicWritable cw = CharacteristicWritable.read(input);
            this.characteristics.put(cw.getNode(), cw);
        }
    }

    @Override
    public int compareTo(CharacteristicSetWritable cs) {
        int size = this.characteristics.size();
        int otherSize = cs.characteristics.size();
        if (size < otherSize) {
            return -1;
        } else if (size > otherSize) {
            return 1;
        } else {
            // Compare characteristics in turn
            Iterator<CharacteristicWritable> iter = this.getCharacteristics();
            Iterator<CharacteristicWritable> otherIter = cs.getCharacteristics();

            int compare = 0;
            while (iter.hasNext()) {
                CharacteristicWritable c = iter.next();
                CharacteristicWritable otherC = otherIter.next();
                compare = c.compareTo(otherC);
                if (compare != 0)
                    return compare;
            }
            return compare;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CharacteristicSetWritable))
            return false;
        return this.compareTo((CharacteristicSetWritable) other) == 0;
    }

    @Override
    public int hashCode() {
        // Build a hash code from characteristics
        if (this.characteristics.size() == 0)
            return 0;
        Iterator<CharacteristicWritable> iter = this.getCharacteristics();
        int hash = 17;
        while (iter.hasNext()) {
            hash = hash * 31 + iter.next().hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        builder.append(this.count.get());
        Iterator<CharacteristicWritable> iter = this.getCharacteristics();
        while (iter.hasNext()) {
            builder.append(" , ");
            builder.append(iter.next().toString());
        }
        builder.append(" }");
        return builder.toString();
    }

}
