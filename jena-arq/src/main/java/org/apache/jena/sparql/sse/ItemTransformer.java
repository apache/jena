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

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.jena.graph.Node;

public class ItemTransformer
{
    public static Item transform(ItemTransform transform, Item item) {
        TransformerApply v = new TransformerApply(transform);
        item.visit(v);
        return v.result();
    }

    static class TransformerApply implements ItemVisitor {
        Deque<Item> stack = new ArrayDeque<>();
        private void push(Item item, Item newItem) {
            if ( item == newItem )
                stack.push(item);
            else
                stack.push(newItem);
        }

        private Item pop() {
            return stack.pop();
        }

        private ItemTransform transform;

        TransformerApply(ItemTransform transform) {
            this.transform = transform;
        }

        Item result() {
            return stack.peek();
        }

        @Override
        public void visit(Item item, ItemList list) {
            ItemList newList = new ItemList(item.getLine(), item.getColumn());
            boolean hasNewItems = false;
            for ( Item subItem : list ) {
                subItem.visit(this);
                Item newItem = pop();
                newList.add(newItem);
                // Object identity test.
                if ( newItem != subItem )
                    hasNewItems = true;
            }

            Item newItemList;

            if ( !hasNewItems ) {
                // Use old objects, avoid copy of identical items.
                newList = list;
                newItemList = item;
            } else {
                newItemList = Item.createList(newList, item.getLine(), item.getColumn());
            }
            Item newItem = transform.transform(newItemList, newList);
            push(item, newItem);
        }

        @Override
        public void visit(Item item, Node node) {
            Item newItem = transform.transform(item, node);
            push(item, newItem);
        }

        @Override
        public void visit(Item item, String symbol) {
            Item newItem = transform.transform(item, symbol);
            push(item, newItem);
        }

        @Override
        public void visitNil(Item item) {
            Item newItem = transform.transformNil(item);
            push(item, newItem);
        }
    }
}
