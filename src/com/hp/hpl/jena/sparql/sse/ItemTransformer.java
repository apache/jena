/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

import java.util.Stack;

import com.hp.hpl.jena.graph.Node;


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
        Stack<Item> stack = new Stack<Item>() ;
        private void push(Item item) { stack.push(item) ; }
        private Item pop() { return stack.pop() ; }
       
        private ItemTransform transform ;

        public TransformerApply(ItemTransform transform)
        { this.transform = transform ; }

        public Item result()
        { return stack.peek() ; }
        
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

        public void visit(Item item, Node node)
        {
            Item newItem = transform.transform(item, node) ;
            push(newItem) ;
        }

        public void visit(Item item, String symbol)
        {
            Item newItem = transform.transform(item, symbol) ;
            push(newItem) ;
        }
        public void visitNil()
        { push(Item.nil) ; }
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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