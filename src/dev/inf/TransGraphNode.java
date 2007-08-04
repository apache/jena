/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.inf;

import static com.hp.hpl.jena.sparql.sse.builders.BuilderBase.checkLength;
import static com.hp.hpl.jena.sparql.sse.builders.BuilderBase.checkList;
import static com.hp.hpl.jena.sparql.sse.builders.BuilderBase.checkNode;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.util.Iter;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.sse.builders.BuilderBase;

class TransGraphNode extends TransGraph<Node>
{
    public static TransGraphNode build(Item item)
    {
        TransGraphNode tg = new TransGraphNode() ;
        BuilderBase.checkList(item) ;
        ItemList list = item.getList() ;
        list = list.cdr() ;
        
        Iter<Item> xIter = Iter.convert(list.iterator()) ;
        
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
/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */