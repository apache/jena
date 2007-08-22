/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;


import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.PrintSerializable;
import com.hp.hpl.jena.sparql.util.PrintUtils;

public class Item extends ItemLocation implements PrintSerializable
{
    protected ItemList list = null ;
    protected Node node = null ;
    protected String symbol = null ;
    protected boolean isNil = false ;
    
    // Constants
    public static final Item nil = createNil() ;
    public static final Item defaultItem = createSymbol("_") ;
    
    public static Item createList() { return createList(noLine, noColumn) ; }
    public static Item createList(int line, int column)
    {
        Item item = new Item(line, column) ;
        item.list = new ItemList(line, column) ;
        return item ;
    }
    
    public static Item createList(ItemList list)
    { return createList(list, noLine, noColumn) ; }
    
    public static Item createList(ItemList list, int line, int column)
    {
        Item item = new Item(line, column) ;
        item.list = list ;
        return item ;
    }
    
    public static Item createNode(Node node) { return createNode(node, noLine, noColumn) ; } 
    public static Item createNode(Node node, int line, int column)
    {
        Item item = new Item(line, column) ;
        item.node = node ;
        return item ;
    }
    
    public static Item createSymbol(String symbol) { return createSymbol(symbol, noLine, noColumn) ; }
    public static Item createSymbol(String symbol, int line, int column)
    {
        if ( symbol.equals("nil") )
            return createNil(line, column) ;
        Item item = new Item(line, column) ;
        item.symbol = symbol;
        return item ;
    }
    
    private static Item createNil() { return createNil(noLine, noColumn) ; }
    private static Item createNil(int line, int column)
    { 
        // Not a symbol
        Item item = new Item(noLine, noColumn) ;
        item.isNil = true ;
        return item ;
    }
    
    
    private Item(int line, int column)
    {
        super(line, column) ;
    }
    
    private Item() { super(noLine, noColumn) ; }

    // ---- Equality and hashcode
    
    static class ItemHashCode implements ItemVisitor
    {
        int hashCode = 0 ;
        public void visit(Item item, ItemList list)
        { hashCode = list.hashCode() ; }

        public void visit(Item item, Node node)
        { hashCode = node.hashCode() ; }

        public void visit(Item item, String symbol)
        { hashCode = symbol.hashCode() ; }
        
        public void visitNil()
        { hashCode = -99 ; }
    }
    
    public int hashCode()
    {
        ItemHashCode itemHashCode = new ItemHashCode() ;
        this.visit(itemHashCode) ;
        return itemHashCode.hashCode ;
    }
    
    static class ItemEquals implements ItemVisitor
    {
        private Item other ;
        ItemEquals(Item other) { this.other = other ; }
        boolean result = false ;

        public void visit(Item item, ItemList list)
        { result = ( other.isList() && other.getList().equals(list) ) ; } 

        public void visit(Item item, Node node)
        { result = ( other.isNode() && other.getNode().equals(node) ) ; }

        public void visit(Item item, String symbol)
        { result = ( other.isSymbol() && other.getSymbol().equals(symbol) ) ; }

        public void visitNil()
        { result = other.isNil() ; }

    }
    
    public boolean equals(Object other)
    { 
        if ( ! ( other instanceof Item ) ) return false ;
        
        ItemEquals x = new ItemEquals((Item)other) ;
        this.visit(x) ;
        return x.result ;
    }
    
    // ----

    public ItemList getList()           { return list ; }
    public Node getNode()               { return node ; }
    //public String getPrefixedName()     { return prefixedName ; }
    public String getSymbol()             { return symbol ; }
    
    public String sniff()
    {
        if ( ! isTaggable() ) return null ;
        return getList().get(0).getSymbol() ;
    }
    
    public boolean isTaggedIgnoreCase(String tag)
    {
        if ( ! isTaggable() ) return false ;
        return getList().get(0).isSymbolIgnoreCase(tag) ;
    }
    public boolean isTagged(String tag)
    {
        if ( ! isTaggable() ) return false ;
        return getList().get(0).isSymbol(tag) ;
    }
    public boolean isTagged()
    {
        if ( ! isTaggable() ) return false ;
        return list.get(0).isSymbol() ; 
    }
    private boolean isTaggable()
    {
        if ( ! isList() ) return false ;
        if ( list.size() == 0 ) return false ;
        return true ; 
    }
    
    public boolean isNil()              { return isNil ; } 
    public boolean isList()             { return list != null ; }
    public boolean isNode()             { return node != null ; }
    public boolean isSymbol()             { return symbol != null ; }
    public boolean isSymbol(String testSymbol)
    { 
        if ( symbol == null )
            return false ;
        return symbol.equals(testSymbol) ;
    }
    public boolean isSymbolIgnoreCase(String testSymbol)
    { 
        if ( symbol == null )
            return false ;
        return symbol.equalsIgnoreCase(testSymbol) ;
    }
    public void visit(ItemVisitor visitor)
    {
        if ( isList() )
            visitor.visit(this, getList()) ;
        else if ( isNode() ) 
            visitor.visit(this, getNode()) ;
        else if ( isSymbol() )
            visitor.visit(this, getSymbol()) ;
        else if ( isNil() )
            visitor.visitNil() ;
        else
            System.err.println("broken item") ;
    }
    
    public Item transform(ItemTransform transformer)
    {
        // transformations keep the list structure
//        if ( isList() )
//            return transformer.transform(this, getList()) ;
//        else
        if ( isNode() ) 
            return transformer.transform(this, getNode()) ;
        else if ( isSymbol() )
            return transformer.transform(this, getSymbol()) ;
        else
            System.err.println("broken item") ;
        return null ;
    }
    
    public String toString(PrefixMapping pmap)
    { return PrintUtils.toString(this, pmap) ; }

    public String shortString()
    {
        if ( isSymbol() ) return getSymbol();
        if ( isNode() ) return FmtUtils.stringForNode(getNode());
        return getList().shortString() ;
    }
    
    //@Override
    public String toString()
    { return PrintUtils.toString(this) ; }
    
    public void output(IndentedWriter out)
    { output(out, null) ; }
    
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        ItemWriter.write(out, this, sCxt) ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */