/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.lang;

import java.util.Stack;

import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;

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
 */

public abstract class ParseHandlerForm extends ParseHandlerPlain 
{
    // generally: (FORM DECL* TERM?)
    // TERM may be absent, in which case the FORM makes
    // no contribution to the output (it just disappears).
    
    // Stackable to enable multiple forms??
    // Or "FormHandler" with a dispatch on registered tags?
    
    private boolean             inDecl      = false ;
    private FrameStack          frameStack  = new FrameStack() ;

    public ParseHandlerForm() {}
    
    @Override
    public void listStart(int line, int column)
    { super.listStart(line, column) ; }

    @Override
    public void listFinish(int line, int column)
    {
        ItemList list = currentList() ;

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

        finishForm(list) ;
        
        // Frame
        Frame f = frameStack.pop() ;
        
        // Drop the form list.
        popList() ;
        //setCurrentItem(null) ;  // Clear, in case top level item is a form of nothing.
        
        // Form output skipped if no result registered.
        Item item = f.result ;

        // If all forms at least evaluate to nil.
//        if ( item == null )
//            item = Item.createNil(list.getLine(), list.getColumn()) ;
        
        // And emit a result as a listAdd.
        // Must go through our listAdd() here to handle nested forms.
        
        // item==null : remove nil code above to allow forms that have no output.
        
//        if ( item != null )
            listAdd(item) ;
    }

    @Override
    protected void listAdd(Item item)
    {
        // Always add to the current list, even for (base...) and (prefix...)
        // Then change the result list later.
        super.listAdd(item) ;
        
        ItemList list = super.currentList() ;
        
        if ( list == null )
            // Top level is outside a list.  Can't be a form.
            return ;

        Frame lastFrame = frameStack.getCurrent()  ;
        
        if ( ! inDecl && /*! sameAsLast &&*/ list.size() == 1 && isForm(list.getFirst() ) ) 
        {
            startForm(list) ;
            Frame f = new Frame(list) ;
            frameStack.push(f) ;
            inDecl = true ;
            return ;
        }

        if ( inDecl )
        {
            // Only trigger form operations when items at the top of the form are seen.
            boolean atTopOfDecl = ( lastFrame != null && lastFrame.listItem == list ) ;
            if ( ! atTopOfDecl )
                return ;

            declItem(list, item) ;
            if ( endOfDecl(list, item) )
            {
                inDecl = false ;
                // Already added.
                return ;
            }
        }
    }
    
    protected boolean inFormDecl()  { return inDecl; }
    
    abstract protected void declItem(ItemList list, Item item) ;
    
    abstract protected boolean isForm(Item tag) ;

    abstract protected boolean endOfDecl(ItemList list, Item item) ;

    abstract protected void startForm(ItemList list) ;
    abstract protected void finishForm(ItemList list) ;

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
        private Stack<Frame> frames    = new Stack<Frame>() ;
    
        boolean isCurrent(ItemList list)
        {
            if ( frames.size() == 0 )
                return false ;
    
            Frame f = frames.peek();
    
            return f.listItem == list ;
        }
    
        Frame getCurrent()
        {
            if ( frames.size() == 0 )
                return null ;
            return frames.peek() ;
        }
    
        void push(Frame f) { frames.push(f) ; }
        Frame pop() { return frames.pop() ; }
    }

}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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