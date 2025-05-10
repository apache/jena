/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.locationtech.jts.index.strtree;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Envelope;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/*
 * This file is an adapted copy of org.locationtech.jts.index.strtree.IndexSerde from
 * the Apache Sedona project:
 *
 * https://github.com/apache/sedona/blob/sedona-1.7.0/common/src/main/java/org/locationtech/jts/index/strtree/IndexSerde.java
 *
 * This file contains the class STRtreeSerializer for kryo-based serialization of
 * STRtree instances from the Java Topology Suite (JTS).
 *
 * The STRtreeSerializer must remain in the org.locationtech.jts.index.strtree package because
 * it accesses non-public members of STRtree.
 *
 * The change over the Sedona version is, that the tree content is written out by handing control
 * idiomatically back to kryo instead of making assumptions about the tree content.
 */

/**
 * Provides methods to efficiently serialize and deserialize the index. trees
 * are serialized recursively.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class STRtreeSerializer
    extends Serializer<STRtree>
{
    @Override
    public STRtree read(Kryo kryo, Input input, Class<STRtree> type) {
        int nodeCapacity = input.readInt();
        boolean notEmpty = (input.readByte() & 0x01) == 1;
        if (notEmpty) {
            boolean built = (input.readByte() & 0x01) == 1;
            if (built) {
                // if built, root is not null, set itemBoundables to null
                STRtree index = new STRtree(nodeCapacity, readSTRtreeNode(kryo, input));
                return index;
            } else {
                // if not built, just read itemBoundables
                ArrayList itemBoundables = new ArrayList();
                int itemSize = input.readInt();
                for (int i = 0; i < itemSize; ++i) {
                    itemBoundables.add(readItemBoundable(kryo, input));
                }
                STRtree index = new STRtree(nodeCapacity, itemBoundables);
                return index;
            }
        } else {
            return new STRtree(nodeCapacity);
        }
    }

    @Override
    public void write(Kryo kryo, Output output, STRtree tree) {
        output.writeInt(tree.getNodeCapacity());
        if (tree.isEmpty()) {
            output.writeByte(0);
        } else {
            output.writeByte(1);
            // write head
            boolean isBuilt = tree.getItemBoundables() == null;
            output.writeByte(isBuilt ? 1 : 0);
            if (!isBuilt) {
                // if not built, itemBoundables will not be null, record it
                ArrayList itemBoundables = tree.getItemBoundables();
                output.writeInt(itemBoundables.size());
                for (Object obj : itemBoundables) {
                    if (!(obj instanceof ItemBoundable)) {
                        throw new UnsupportedOperationException(
                                " itemBoundables should only contain ItemBoundable objects ");
                    }
                    ItemBoundable itemBoundable = (ItemBoundable) obj;
                    // write envelope
                    writeItemBoundable(kryo, output, itemBoundable);
                }
            } else {
                // if built, write from root
                writeSTRTreeNode(kryo, output, tree.getRoot());
            }
        }
    }

    private void writeSTRTreeNode(Kryo kryo, Output output, AbstractNode node) {
        // write head
        output.writeInt(node.getLevel());
        // write children
        List children = node.getChildBoundables();
        int childrenSize = children.size();
        output.writeInt(childrenSize);
        // if children not empty, write children
        if (childrenSize > 0) {
            if (children.get(0) instanceof AbstractNode) {
                // write type as 0, non-leaf node
                output.writeByte(0);
                for (Object obj : children) {
                    AbstractNode child = (AbstractNode) obj;
                    writeSTRTreeNode(kryo, output, child);
                }
            } else if (children.get(0) instanceof ItemBoundable) {
                // write type as 1, leaf node
                output.writeByte(1);
                // for leaf node, write items
                for (Object obj : children) {
                    writeItemBoundable(kryo, output, (ItemBoundable) obj);
                }
            } else {
                throw new UnsupportedOperationException("wrong node type of STRtree");
            }
        }
    }

    private STRtree.STRtreeNode readSTRtreeNode(Kryo kryo, Input input) {
        int level = input.readInt();
        STRtree.STRtreeNode node = new STRtree.STRtreeNode(level);
        int childrenSize = input.readInt();
        boolean isLeaf = (input.readByte() & 0x01) == 1;
        ArrayList children = new ArrayList();
        if (isLeaf) {
            for (int i = 0; i < childrenSize; ++i) {
                children.add(readItemBoundable(kryo, input));
            }
        } else {
            for (int i = 0; i < childrenSize; ++i) {
                children.add(readSTRtreeNode(kryo, input));
            }
        }
        node.setChildBoundables(children);
        return node;
    }

    private void writeItemBoundable(Kryo kryo, Output output, ItemBoundable itemBoundable) {
        kryo.writeObject(output, itemBoundable.getBounds());
        kryo.writeClassAndObject(output, itemBoundable.getItem());
    }

    private ItemBoundable readItemBoundable(Kryo kryo, Input input) {
        Envelope envelope = kryo.readObject(input, Envelope.class);
        Object item = kryo.readClassAndObject(input);
        return new ItemBoundable(envelope, item);
    }
}
