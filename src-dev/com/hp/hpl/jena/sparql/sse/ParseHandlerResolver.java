/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

import java.util.Stack;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.n3.IRIResolver;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

import com.hp.hpl.jena.sparql.ARQNotImplemented;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarAlloc;
import com.hp.hpl.jena.sparql.sse.builders.BuilderPrefixMapping;
import com.hp.hpl.jena.sparql.util.LabelToNodeMap;
import com.hp.hpl.jena.sparql.util.PrefixMapping2;

/** Resolve prefixed names in a prefix map and IRIs relative to a base.
 *  Forms: 
 *    (prefix (DECL) TERM) => TERM with prefix names expanded
 *    (base IRI TERM) => TERM with IRIs resolved to absolute IRIs
 * 
 *    
 * @author Andy Seaborne
 * @version $Id$
 */

public class ParseHandlerResolver implements ParseHandler 
{
    /*  Both forms have a common structure.
     *    Exactly 3 items long.
     *    There is a stack of previous settings (i.e. things always nest) 
     *  During DECL, for prefix, (the 2nd term) processing, we flag that no prefix processing should be done.
     *  Base can include prefixes, and relative URIs.
     *  
     *  The first base and first prefix mapping are remembered.
     *  Better: look at the first thing ever seen. 
     */
    
    // Maybe more restrictive.  Spot the special form (base BASE (prefix ....))
    
    
    private static final String prefixTag       = "prefix" ;
    private static final String baseTag         = "base" ;

    private PrefixMapping       topMap          = null ;
    private String              topBase         = null ;

    private boolean             inPrefixDecl    = false ;
    private boolean             inBaseDecl      = false ;
    private PrefixMapping       prefixMap ;
    private IRIResolver         resolver ;
    private FrameStack          frameStack      = new FrameStack() ;
    private ListStack           listStack       = new ListStack() ;
    private VarAlloc            varAlloc        = new VarAlloc("") ;
    private LabelToNodeMap      bNodeLabels     = LabelToNodeMap.createBNodeMap() ;

    private Item                currentItem     = null ;
    private int                 depth           = 0 ;
    public ParseHandlerResolver() { this(null, null) ; }

    public ParseHandlerResolver(PrefixMapping pmap) { this(pmap, null) ; }
    
    public ParseHandlerResolver(PrefixMapping pmap, String base)
    { 
        if ( pmap == null )
            pmap = new PrefixMappingImpl() ;
        prefixMap = pmap ;
        resolver = new IRIResolver(base) ;
    }
    
    public ParseHandlerResolver(Prologue prologue)
    {
        prefixMap = prologue.getPrefixMapping() ;
        resolver = prologue.getResolver() ;
    }

    public Item getItem()
    {
        return currentItem ; 
    }
    
    public Prologue getPrologue()
    {
        throw new ARQNotImplemented("getPrologue") ;
        //return null ;
    }
     
    
    public void parseStart()
    {}

    public void parseFinish()
    {
        if ( depth != 0 )
            LogFactory.getLog(ParseHandlerResolver.class).warn("Stack error: depth ="+depth+" at end of parse run") ;
    }

    public void listStart(int line, int column)
    {
        depth++ ;
        ItemList list = new ItemList(line, column) ;
        listStack.push(list) ;
        setCurrentItem(Item.createList(list)) ;
    }

    public void listFinish(int line, int column)
    {
        --depth ;
        // At end of a list
        // If it's the current frame stack front, i.e. (prefix ...) or (base ...)
        //   pop the stack and return the inner form instead. 
        ItemList list = listStack.pop() ;
        Item item = null ; 
        
        if ( frameStack.isCurrent(list) )
        {
            // End of prefix item or base. 
            // Restore previous state.
            Frame f = frameStack.pop() ;
            prefixMap = f.prefixMap ;
            resolver = f.resolver ;
            item = f.result ;
            if ( item == null )
                item = Item.createNil(list.line, list.column) ;
            
        }
        else
            item = Item.createList(list) ;
        
        // Add to list, or pop'ed 
        listAdd(item) ;
    }

    private void setCurrentItem(Item item)
    {
        currentItem = item ;
    }
    
    private void listAdd(Item item)
    {
        if ( listStack.isEmpty() )
        {
            // Top level is outside a list.
            setCurrentItem(item) ;
            return ;
        }
            
        
        ItemList list = listStack.getCurrent() ;
        // Always add to the current list, even for (base...) and (prefix...)
        // They change the result list later.
        list.add(item) ;
        setCurrentItem(item) ;

        if ( list.size() > 2 )
        {
            // Was it a (prefix...) or (base..)
            // Is it too long?
            // The list and frame stacks have not been pop'ed yet
            if ( list.get(0).isWord(baseTag) || list.get(0).isWord(prefixTag) )
            {
                if ( list.size() > 3 )
                    throwException("List too long for (base...) or (prefix...) body", item.getLine(), item.getColumn()) ;
            }
        }
        
        
        // Build the prefix mapping, we continue parsing, to accumulate
        // a structure with prefixes as "special" words.
        // We parse that anbd continue.
        
        
        if ( inPrefixDecl )
        {
            // We're at the start of the decl block.
            PrefixMapping newMappings = BuilderPrefixMapping.build(item) ; 
            PrefixMapping2 ext = new PrefixMapping2(prefixMap, newMappings) ;
            // Remember first prefix mapping seen. 
            if( topMap == null )
                topMap = newMappings ;
            Frame f = new Frame(listStack.getCurrent(), ext, resolver) ;
            frameStack.push(f) ;
            // End of prefix declaration handled in listFinish. 
            // List length is checked on next add's
            return ;
        }
        
        if ( inBaseDecl )
        {
            // At end of base IRI
            if ( ! item.isNode() )
                throwException("(base ...): not an RDF node for the base.", item.getLine(), item.getColumn()) ;
            if ( ! item.getNode().isURI() )
                throwException("(base ...): not an IRI for the base.", item.getLine(), item.getColumn()) ;
            String baseIRI = item.getNode().getURI() ;
            resolver = new IRIResolver(baseIRI) ;
            if ( topBase == null )
                topBase = baseIRI ; 
            inBaseDecl = false ;
            // List length is checked on next add's
        }

        // Start of declaration?
        if ( list.size() == 0 )
        {
            if ( item.isWord(baseTag) )
            {
                inBaseDecl = true ;
                return ;
            }
            if ( item.isWord(prefixTag) )
            {
                inPrefixDecl = true ;
                return ;
            }
            // Note must be (... body)
        }
    }
    
    public void emitSymbol(int line, int column, String symbol)
    {
        if ( inPrefixDecl )
            throwException("Symbols not allows in prefix declarations: "+symbol, line, column) ;
        listAdd(Item.createWord(symbol, line, column)) ;
    }

    public void emitVar(int line, int column, String varName)
    {
        if ( inPrefixDecl )
            throwException("Variables not allowed in prefix declarations: ?"+varName, line, column) ;
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
        if ( inPrefixDecl )
            throwException("Literals not allowed in prefix declarations", line, column) ;
        
        Node n = null ;
        if ( datatypeIRI != null || datatypePN != null )
        {
            if ( datatypePN != null )
                datatypeIRI = resolvePName(datatypePN, line, column) ;
            else
                datatypeIRI = resolveIRI(datatypeIRI, line, column) ;
            
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
        if ( inPrefixDecl )
            throwException("Blank nodes not allowed in prefix declarations", line, column) ;
        Node n = bNodeLabels.asNode(label) ;
        Item item = Item.createNode(n, line, column) ;
        listAdd(item) ;
    }

    // Only lists, IRIs and PName in a prefix decl.
    
    public void emitIRI(int line, int column, String iriStr)
    { 
        // resolve as normal. No special action.
        //if ( inPrefixDecl ) {}
        iriStr = resolveIRI(iriStr, line, column) ;
        Node n = Node.createURI(iriStr) ;
        Item item = Item.createNode(n, line, column) ;
        listAdd(item) ;
    }

    public void emitPName(int line, int column, String pname)
    {
        if ( inPrefixDecl )
        {
            // Record a faked PName.
            Item item = Item.createWord(pname, line, column) ;
            listAdd(item) ;
            return ;
        }
        String iriStr = resolvePName(pname, line, column) ;
        Node n = Node.createURI(iriStr) ;
        Item item = Item.createNode(n, line, column) ;
        listAdd(item) ;
    }
    
    
    private String resolvePName(String pname, int line, int column)
    { 
        if ( prefixMap == null )
            throwException("No prefix mapping for prefixed name: "+pname, line, column) ;
        
        if ( ! pname.contains(":") )
            throwException("Prefixed name does not have a ':': "+pname, line, column) ;
        
        String uri = prefixMap.expandPrefix(pname) ;
        if ( uri == null || uri.equals(pname) )
            throwException("Can't resolve prefixed name: "+pname, line, column) ;
        return uri ;
    }
    
    private String resolveIRI(String iriStr, int line, int column) 
    {
        if ( resolver != null )
            return resolver.resolve(iriStr) ;
        return iriStr ;
    }
    
    private static class Frame
    {
        ItemList listItem ;
        Item result ;
        PrefixMapping prefixMap;
        IRIResolver resolver ;
        
        Frame(ItemList listItem, PrefixMapping pmap, IRIResolver resolver)
        {
            this.listItem = listItem ;
            this.prefixMap = pmap ;
            this.resolver = resolver ;
        }
    }

    // ----------------
    
    private static class FrameStack
    {
        private Stack frames    = new Stack() ;
    
        boolean isCurrent(ItemList list)
        {
            if ( frames.size() == 0 )
                return false ;
    
            Frame f = (Frame)frames.peek();
    
            return f.listItem == list ;
        }
    
        Frame getCurrent()
        {
            if ( frames.size() == 0 )
                return null ;
            return (Frame)frames.peek() ;
        }
    
        void push(Frame f) { frames.push(f) ; }
        Frame pop() { return (Frame)frames.pop() ; }
    }

    // ----------------
    
    private static class ListStack
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

    
    private static void throwException(String msg, int line, int column)
    {
        throw new SSEParseException("[" + line + ", " + column + "] " + msg, line, column) ;
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