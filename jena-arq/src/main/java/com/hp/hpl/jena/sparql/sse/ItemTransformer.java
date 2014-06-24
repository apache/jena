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

package com.hp.hpl.jena.sparql.sse;

import java.util.ArrayDeque ;
import java.util.Deque ;

import com.hp.hpl.jena.graph.Node ;


public class ItemTransformer
{
    public static Item transform(ItemTransform transform, Item item)
    {
        TransformerApply v = new TransformerApply(transform) ;
        item.visit(v) ;
        return v.result() ;
    }
    
    // Is it worth being an ItemVisitor?
    // Why not directly dispatch - and make the "visit" operation return a result
    static class TransformerApply implements ItemVisitor
    {
        Deque<Item> stack = new ArrayDeque<>() ;
        private void push(Item item) { stack.push(item) ; }
        private Item pop() { return stack.pop() ; }
       
        private ItemTransform transform ;

        public TransformerApply(ItemTransform transform)
        { this.transform = transform ; }

        public Item result()
        { return stack.peek() ; }
        
        @Override
        public void visit(Item item, ItemList list)
        {
            ItemList newList = new ItemList(item.getLine(), item.getColumn()) ;
            
            for ( Item subItem : list )
            {
                subItem.visit(this) ;
                Item newItem = pop();
                newList.add(newItem) ;
            }
            Item newItemList = Item.createList(newList, item.getLine(), item.getColumn()) ;
            push(newItemList) ;
        }

        @Override
        public void visit(Item item, Node node)
        {
            Item newItem = transform.transform(item, node) ;
            push(newItem) ;
        }

        @Override
        public void visit(Item item, String symbol)
        {
            Item newItem = transform.transform(item, symbol) ;
            push(newItem) ;
        }
        @Override
        public void visitNil()
        { push(Item.nil) ; }
    }
}
