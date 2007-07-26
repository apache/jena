/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

import java.util.Stack;

/** Resolve syntacic forms like (base ...) and (prefix...)
 *  where the syntax modifies the enclosed sub term.
 *  
 *  
 *  Forms:
 *    (FORM DECL... TERM) => where TERM is the result.
 *  Examples 
 *    (prefix (PREFIXES) TERM) => TERM with prefix names expanded
 *    (base IRI TERM) => TERM with IRIs resolved to absolute IRIs
 *  
 *  The DECL part can not itself have nested, independent forms
 *  unless a subclass (carefully) manages that. 
 *    
 * @author Andy Seaborne
 * @version $Id$
 */

public abstract class ParseHandlerForm extends ParseHandlerPlain 
{
    // generally: (FORM DECL* TERM)
    // TERM may be absent .
    
    // Stackable to enable multiple forms??
    // Or "FormHandler"
    
    private boolean             inDecl      = false ;
    private FrameStack          frameStack      = new FrameStack() ;

    public ParseHandlerForm() {}
    
    public void listStart(int line, int column)
    { super.listStart(line, column) ; }

    public void listFinish(int line, int column)
    {
        ItemList list = listStack.getCurrent() ;

        if ( ! frameStack.isCurrent(list) )
        {
            // Nothing special - proceed as normal.
            super.listFinish(line, column) ;
            return ;
        }
        
        if ( inDecl )
            throwException("Inconsistent form: Still in DECL at end of the form", line, column) ;
        
        // For later: no exception: ensure this is cleared.
        inDecl = false ;

        // Frame
        Frame f = frameStack.pop() ;
        
        // Drop the form list.
        listStack.pop();
        --depth ;
        
        // The result is the last element of the list if not already set.
        Item item = f.result ;
        if ( item == null )
        {
            // Checking for explicit setting of the result.
            throwException("Inconsistent form: No result", line, column) ;
            // Defaulting result.
            item = list.getLast() ;
        }
        if ( item == null )
            item = Item.createNil(list.line, list.column) ;
        
        // And emit a result as a listAdd.
        // Must go through our listAdd() here. ?? super.listAdd should work?
        listAdd(item) ;
    }

    protected void listAdd(Item item)
    {
        // Always add to the current list, even for (base...) and (prefix...)
        // Then change the result list later.
        super.listAdd(item) ;
        
        if ( listStack.isEmpty() )
            // Top level is outside a list.  Can't be a form.
            return ;

        ItemList list = listStack.getCurrent() ;
        Frame lastFrame = frameStack.getCurrent()  ;
        
        if ( ! inDecl && lastFrame.listItem != list 
             && list.size() == 1 && isForm(list.getFirst() ) ) 
        {
            Frame f = new Frame(listStack.getCurrent()) ;
            frameStack.push(f) ;
            inDecl = false ;
            return ;
        }
        
        if ( endOfDecl(list, item) )
        {
            inDecl = false ;
            // Already added.
            return ;
        }

        if ( inDecl )
            declItem(list, item) ;
    }
    
    protected boolean inFormDecl()  { return inDecl; }
    
    abstract protected void declItem(ItemList list, Item item) ;
    
    abstract protected boolean endOfDecl(ItemList list, Item item) ;

    abstract protected boolean isForm(Item tag) ;
    
    protected void setFormResult(Item item)
    {
        if ( frameStack.getCurrent() == null )
            throwException("Internal error : no current form", item.getLine(), item.getColumn()) ;
        frameStack.getCurrent().result = item ;
    }

    private static class Frame
    {
        ItemList listItem ;
        Item result ;
        
        Frame(ItemList listItem)
        {
            this.listItem = listItem ;
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