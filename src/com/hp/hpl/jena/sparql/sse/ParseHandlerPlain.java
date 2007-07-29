/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

import java.util.Stack;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarAlloc;
import com.hp.hpl.jena.sparql.util.LabelToNodeMap;

import org.apache.commons.logging.LogFactory;

public class ParseHandlerPlain implements ParseHandler 
{
    private ListStack      listStack   = new ListStack() ;
    private Item           currentItem = null ;
    private int            depth       = 0 ;
    private LabelToNodeMap bNodeLabels = LabelToNodeMap.createBNodeMap() ;
    private VarAlloc       varAlloc    = new VarAlloc("") ;
    
    public Item getItem()
    {
        return currentItem ; 
    }
    
    public void parseStart()
    { depth = 0 ; }
    
    public void parseFinish()
    {
        if ( depth != 0 )
            LogFactory.getLog(ParseHandlerPlain.class).warn("Stack error: depth ="+depth+" at end of parse run") ;
        depth = -1 ;

    }
    
    public void listStart(int line, int column)
    {
        ItemList list = new ItemList(line, column) ;
        pushList(list) ;
        setCurrentItem(Item.createList(list)) ;
    }

    public void listFinish(int line, int column)
    {
        ItemList list = popList() ;
        Item item = Item.createList(list) ;
        listAdd(item) ;
    }

    
    // Share??
    
    protected void setCurrentItem(Item item)
    {
        currentItem = item ;
    }
    
    protected void listAdd(Item item)
    {
        if ( listStack.isEmpty() )
        {
            // Top level is outside a list.
            setCurrentItem(item) ;
            return ;
        }
        
        ItemList list = currentList() ;
        list.add(item) ;
        setCurrentItem(item) ;
    }
    
    public void emitSymbol(int line, int column, String symbol)
    {
        listAdd(Item.createSymbol(symbol, line, column)) ;
    }

    public void emitVar(int line, int column, String varName)
    {
        Var var = null ;
        if ( varName.equals("") )
            var = varAlloc.allocVar()  ;
        else
            var = Var.alloc(varName) ;
        Item item = Item.createNode(var, line, column) ;
        listAdd(item) ;
    }

    public void emitLiteral(int line, int column, String lexicalForm, String langTag, String datatypeIRI, String datatypePN)
    {
        Node n = null ;
        
        if ( datatypeIRI != null || datatypePN != null )
        {
            if ( datatypePN != null )
                datatypeIRI = resolvePrefixedName(datatypePN, line, column) ;
            
            RDFDatatype dType = TypeMapper.getInstance().getSafeTypeByName(datatypeIRI) ;
            n = Node.createLiteral(lexicalForm, null, dType) ;
        }
        else
            n = Node.createLiteral(lexicalForm, langTag, null) ;
        Item item = Item.createNode(n, line, column) ;
        listAdd(item) ;
    }

    public void emitBNode(int line, int column, String label)
    {
        Node n = bNodeLabels.asNode(label) ;
        Item item = Item.createNode(n, line, column) ;
        listAdd(item) ;
    }

    public void emitIRI(int line, int column, String iriStr)
    {
        Node n = Node.createURI(iriStr) ;
        Item item = Item.createNode(n, line, column) ;
        listAdd(item) ;
    }

    public void emitPName(int line, int column, String pname)
    {
        String iriStr = resolvePrefixedName(pname, line, column) ;
        emitIRI(line, column, iriStr) ;
    }

    protected ItemList currentList()        { return (ItemList)listStack.getCurrent() ; }
    protected ItemList popList()            { depth-- ; return listStack.pop() ; }
    protected void pushList(ItemList list)  { listStack.push(list) ; depth++ ; }
    
    protected String resolvePrefixedName(String pname, int line, int column)
    {
        // Not resolving.  Make a strange URI.
        return "pname:"+pname ;
    }
    
    protected static class ListStack
    {
        private Stack stack    = new Stack() ;
        
        boolean isEmpty() { return stack.size() == 0 ; }
        
        ItemList getCurrent()
        {
            if ( stack.size() == 0 )
                return null ;
            return (ItemList)stack.peek() ;
        }
    
        void push(ItemList list) { stack.push(list) ; }
        ItemList pop() { return (ItemList)stack.pop() ; }
    }

    
    protected static void throwException(String msg, int line, int column)
    {
        throw new SSEParseException("[" + line + ", " + column + "] " + msg, line, column) ;
    }
    
    protected static void throwException(String msg, ItemLocation loc)
    {
        throwException(msg, loc.getLine(), loc.getColumn()) ;
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