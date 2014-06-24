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

import com.hp.hpl.jena.graph.Node ;

public class ItemWalker
{
    static void walk(ItemVisitor visitor, Item item)
    {
        item.visit(new Worker(visitor)) ;
    }
    
    
    static class Worker implements ItemVisitor
    {
        private ItemVisitor visitor ;
        Worker(ItemVisitor visitor) { this.visitor = visitor ; }
        
        @Override
        public void visit(Item item, ItemList list)
        {
            for ( Item subItem : list )
            {
                subItem.visit( this );
            }
            visitor.visit(item, list) ;
        }
        
        @Override
        public void visit(Item item, Node node)
        {
            visitor.visit(item, node) ;
        }
        
        @Override
        public void visit(Item item, String symbol)
        {
            visitor.visit(item, symbol) ;
        }

        @Override
        public void visitNil()
        { visitor.visitNil() ; }
    }
}
