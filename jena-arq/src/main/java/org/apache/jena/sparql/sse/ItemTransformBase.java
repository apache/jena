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

package org.apache.jena.sparql.sse;

import org.apache.jena.graph.Node;

public class ItemTransformBase implements ItemTransform
{
    public static final boolean COPY_ALWAYS = true;
    public static final boolean COPY_ONLY_ON_CHANGE = false;
    private boolean alwaysCopy = false;

    public ItemTransformBase() {
        this(COPY_ONLY_ON_CHANGE);
    }

    public ItemTransformBase(boolean alwaysDuplicate) {
        this.alwaysCopy = alwaysDuplicate;
    }

    @Override
    public Item transform(Item item, ItemList itemList) {
        return xform(item, itemList);
    }

    @Override
    public Item transform(Item item, Node node) {
        return xform(item, node);
    }

    @Override
    public Item transform(Item item, String symbol) {
        return xform(item, symbol);
    }

    @Override
    public Item transformNil(Item item) {
        return xformNil(item);
    }

    private Item xform(Item item, ItemList itemList) {
        if ( !alwaysCopy && item.getList() == itemList )
            return item;
        return Item.createList(itemList, item.getLine(), item.getColumn());
    }

    private Item xform(Item item, Node node) {
        if ( !alwaysCopy && item.getNode().equals(node) )
            return item;
        return Item.createNode(node, item.getLine(), item.getColumn());
    }

    private Item xform(Item item, String symbol) {
        if ( !alwaysCopy && item.getSymbol().equals(symbol) )
            return item;
        return Item.createSymbol(symbol, item.getLine(), item.getColumn());
    }

    private Item xformNil(Item item) {
        return item;
    }

}
