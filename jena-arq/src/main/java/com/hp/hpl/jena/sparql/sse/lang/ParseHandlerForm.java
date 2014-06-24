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

package com.hp.hpl.jena.sparql.sse.lang;

import java.util.ArrayDeque ;
import java.util.Deque ;

import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;

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
 *  unless a subclass (carefully) manages that. */

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
        private Deque<Frame> frames    = new ArrayDeque<>() ;
    
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
