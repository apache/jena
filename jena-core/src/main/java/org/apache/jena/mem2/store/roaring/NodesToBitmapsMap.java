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

package org.apache.jena.mem2.store.roaring;

import org.apache.jena.atlas.lib.Copyable;
import org.apache.jena.graph.Node;
import org.apache.jena.mem2.collection.FastHashMap;
import org.roaringbitmap.RoaringBitmap;

/**
 * Map from {@link Node} to {@link RoaringBitmap}.
 */
public class NodesToBitmapsMap
        extends FastHashMap<Node, RoaringBitmap>
        implements Copyable<NodesToBitmapsMap> {

    public NodesToBitmapsMap() {
        super();
    }

    public NodesToBitmapsMap(final NodesToBitmapsMap mapToCopy) {
        super(mapToCopy, RoaringBitmap::clone);
    }

    @Override
    protected Node[] newKeysArray(int size) {
        return new Node[size];
    }

    @Override
    protected RoaringBitmap[] newValuesArray(int size) {
        return new RoaringBitmap[size];
    }

    /**
     * Create a copy of this map.
     * The new map will contain all the same nodes as keys of this map, but clones of the bitmaps as values.
     *
     * @return a copy of this map
     */
    @Override
    public NodesToBitmapsMap copy() {
        return new NodesToBitmapsMap(this);
    }
}
