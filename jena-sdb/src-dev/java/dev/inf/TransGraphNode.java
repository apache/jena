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

package dev.inf;

import static com.hp.hpl.jena.sparql.sse.builders.BuilderLib.checkLength ;
import static com.hp.hpl.jena.sparql.sse.builders.BuilderLib.checkList ;
import static com.hp.hpl.jena.sparql.sse.builders.BuilderLib.checkNode ;
import org.apache.jena.atlas.iterator.Iter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;

class TransGraphNode extends TransGraph<Node>
{
    public static TransGraphNode build(Item item)
    {
        TransGraphNode tg = new TransGraphNode() ;
        checkList(item) ;
        ItemList list = item.getList() ;
        list = list.cdr() ;
        
        Iter<Item> xIter = Iter.iter(list.iterator()) ;
        
        for ( Item x : xIter)
        {
            checkList(item) ;
            ItemList pair = x.getList() ;
            checkLength(2, pair, "Not a pair: "+pair.shortString()) ;
            Item a = pair.get(0) ;
            Item b = pair.get(1) ;
            checkNode(a) ;
            checkNode(b) ;
            tg.add(a.getNode(), b.getNode()) ;
        }
        return tg ;
    }
    
    public Item asItem(String tag)
    {
        final Item top = Item.createList() ;
        top.getList().add(Item.createSymbol(tag)) ;

        LinkApply<Node> x = new LinkApply<Node>() {
            @Override
            public void apply(Node i, Node j)
            {
                Item pair = pair(i,j) ;
                top.getList().add(pair) ;

                    // Make if BuilderTable compatible.
//                    item.getList().add(Item.createWord("row")) ;
//                    
//                    Item x = pair("sub", i) ;
//                    Item y = pair("super", j) ;
//                    
//                    item.getList().add(x) ;
//                    item.getList().add(y) ;
//                    top.getList().add(item) ;
            }

            private Item pair(Node x, Node y)
            {
                Item item = Item.createList() ;
                item.getList().add(x) ;
                item.getList().add(y) ;
                return item ;
            } } ;
            super.linkApply(x) ;
            return top ;
    }
}
