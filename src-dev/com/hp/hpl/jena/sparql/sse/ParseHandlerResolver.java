/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

import java.util.Iterator;
import java.util.Stack;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.sse.builders.BuilderBase;
import com.hp.hpl.jena.sparql.util.PrefixMapping2;

public class  ParseHandlerResolver implements ParseHandler 
{
    ParseHandler other = null ; //new ParseHandlerDebug() ;
    static final String prefixTag = "prefix" ;
    Stack prefixTags = new Stack() ;
    Stack pmapStack = new Stack() ;
    PrefixMapping currentMap = new PrefixMappingImpl() ;
    
    // States 
    // 0 : Nothing
    // 1 : Waiting for list to start
    // 2 : Seeing prefix pairs
    // 3 : In
    
    private static final int  STATE_NORMAL          = 10 ;
    private static final int  STATE_DECL            = 20 ;
    private int state = STATE_NORMAL ;
    
    public ParseHandlerResolver()
    {
        pmapStack.push(currentMap) ;
    }
    
    public void listStart(Item listItem)
    { if ( other != null ) other.listStart(listItem) ; }
    
    public void listFinish(Item listItem)
    {
        if ( other != null ) other.listFinish(listItem) ;
        
        if ( isCurrent(prefixTags, listItem) )
        {
            pmapStack.pop() ;
            currentMap = (PrefixMapping)pmapStack.peek() ;
            prefixTags.pop();
        }
    }
    
    /* Processing (prefix ...)
     * Step 1 : spot (prefix ....) [listAdd]
     *   Do not add this tag to the list - this list will be the body.
     *     Turn off resolution, and get the next item (the prefix decls).
     *       Presence of (prefix...) is illegal
     *     At end of this list [listAdd or listFinish]
              End of list: count by dept returning to zero: no nested decls 
     * Step 2:
     *   Decls to prefix.
     *   Push new prefix mapping, set the current prefix mapping.
     *   Push the list item onto the prefix stack.
     *   
     * Step 3: Process body
     * 
     * Step 4:
     *   [listFinish]
     *   When we see the body end, pop the prefix mapping, and pop the list item 
     */
    
    public void listAdd(Item listItem, Item elt)
    {
        if ( other != null ) other.listAdd(listItem, elt) ;
        
        // Spot the tag
        if ( state == STATE_NORMAL &&
             listItem.getList().size() == 0 &&
             elt.isWord(prefixTag) &&
             // Special case - could be (prefix (...) prefix ...) 
             // because we are not pushing elements until the body.
             ! isCurrent(prefixTags, listItem) )
        {
            // It's  (prefix ...)
            state = STATE_DECL ;
            // Remember this list 
            prefixTags.push(listItem) ;
            return ;
        }

        // NB There may not be a body
        
        // Spot the end of decls.
        if ( state == STATE_DECL &&
             isCurrent(prefixTags, listItem) )
        {
            PrefixMapping2 ext = new PrefixMapping2(currentMap) ;
            PrefixMapping newMappings = ext.getLocalPrefixMapping() ;
            parsePrefixes(newMappings, elt) ;
            pmapStack.push(ext) ;
            currentMap = ext ;
            state = STATE_NORMAL ;
            // Leave listItem on the stack
            return ;
        }

        // Just add it to the list
        listItem.getList().add(elt) ;
    }
    
    public Item itemWord(Item item)     //{ return item ; }
    { 
        if ( other != null ) other.itemWord(item) ;
        return item ;
    }
    
    public Item itemNode(Item item)     //{ return item ; }
    { 
        if ( other != null ) other.itemNode(item) ;
        return item ;
    }

    public Item itemPName(Item item)
    { 
        if ( other != null ) other.itemPName(item) ;
        if ( state == STATE_NORMAL )
        {
            String lex = item.getWord() ;
            Node node = resolve(lex, currentMap, item) ;
            if ( node == null )
            {}
            item = Item.createNode(node, item.getLine(), item.getColumn()) ;
        }
        
        return item ;
        
    }
    
    private boolean isCurrent(Stack stack, Item item)
    {
        return stack.size() != 0 && (Item)stack.peek() == item ;
    }
    
//    // Resolve from a node (prefix name encoded) 
//    private static Node resolve(Node node, PrefixMapping pmap, ItemLocation location)
//    {
//        if ( ! node.isURI() )
//            return node ;
//        
//        String uri = node.getURI() ;
//        if ( uri.startsWith(":") )
//        {
//            String qname = uri.substring(1) ;
//            if ( pmap != null )
//                uri = pmap.expandPrefix(qname) ;
//            if ( uri == null || uri.equals(qname) )
//                BuilderBase.broken(location, "Can't resolve prefixed name: "+uri) ;
//            return Node.createURI(uri) ;
//        }
//        else
//        {
//            uri = RelURI.resolve(uri) ;
//            return Node.createURI(uri) ;
//        }
//    }

    // Returns a word if resolved - else null.
    private static Node resolve(String word, PrefixMapping pmap, ItemLocation location)
    {
        if ( pmap == null )
            return null ;
        
        if ( ! word.contains(":") )
            return null ;
        
        String uri = pmap.expandPrefix(word) ;
        if ( uri == null || uri.equals(word) )
        {
            // Unresolved in some way.  
            BuilderBase.broken(location, "Can't resolve prefixed name: "+uri) ;
            // OR Make into a funny node
            return Node.createURI(":"+word) ;
        }
        return Node.createURI(uri) ;
    }
    
    private static void parsePrefixes(PrefixMapping newMappings, Item elt)
    {
        if ( ! elt.isList() )
            BuilderBase.broken(elt, "Prefixes must be a list: "+elt) ;
        
        ItemList prefixes = elt.getList() ; 
        for ( Iterator iter = prefixes.iterator() ; iter.hasNext() ; )
        {
            Item pair = (Item)iter.next() ;
            if ( !pair.isList() || pair.getList().size() != 2 )
                BuilderBase.broken(pair, "Not a prefix/IRI pair") ;
            Item prefixItem = pair.getList().get(0) ;
            Item iriItem = pair.getList().get(1) ;

            // Maybe a Node (fake prefixed name) or a Word, depending on parser set up.
            
            String prefix = null ;

            // -- Prefix as word
            if ( prefixItem.isWord() )
                prefix = prefixItem.getWord() ;

            // -- Prefix as Node
//            if ( prefixItem.isNode())
//            {
//                Node n = prefixItem.getNode() ;
//                if ( ! n.isURI() )
//                    BuilderBase.broken(pair, "Prefix part is not a prefixed name: "+pair) ;
//
//                prefix = n.getURI();
//                // It will look like :x:
//                
//                if ( ! prefix.startsWith(":") )
//                    BuilderBase.broken(pair, "Prefix part is not a prefix name: "+pair) ;
//                prefix = prefix.substring(1) ;
//            }            

            if ( prefix == null )
                BuilderBase.broken(pair, "Prefix part nor recognized: "+prefixItem) ;
            
            if ( ! prefix.endsWith(":") )
                BuilderBase.broken(pair, "Prefix part does not end with a ':': "+pair) ;
            prefix = prefix.substring(0, prefix.length()-1) ;
            if ( prefix.contains(":") )
                BuilderBase.broken(pair, "Prefix itseld contains a ':' : "+pair) ;
            // -- /Prefix
            
            Node iriNode = iriItem.getNode() ;

            if ( iriNode == null || ! iriNode.isURI() )
                BuilderBase.broken(pair, "Not an IRI: "+iriItem) ;

            String iri = iriNode.getURI();

            newMappings.setNsPrefix(prefix, iri) ;
        }
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