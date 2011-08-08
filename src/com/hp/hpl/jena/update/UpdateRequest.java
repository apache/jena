/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.update;

import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.io.PrintUtils ;
import org.openjena.atlas.io.Printable ;

import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.modify.request.UpdateWriter ;


public class UpdateRequest extends Prologue implements Printable, Iterable<Update>
{
    private List<Update> operations = new ArrayList<Update>() ;
    private List<Update> operationsView = Collections.unmodifiableList(operations) ;

    public UpdateRequest() { super() ; }
    public UpdateRequest(Update update)
    {
        this() ;
        add(update) ;
    }

    /** @deprecated Use @link{#add(Update)} */
    @Deprecated
    public void addUpdate(Update update) { add(update) ; } 

    public UpdateRequest add(Update update) { operations.add(update) ; return this ; } 
    public UpdateRequest add(String string)
    { 
        UpdateFactory.parse(this, string) ;
        return this ;
    }

    public List<Update> getOperations() { return operationsView ; }
    
    @Deprecated
    /** @deprecated Use @link{#getOperations()} instead. */
    public List<Update> getUpdates() { return operationsView ; }
    
    public Iterator<Update> iterator()
    {
        return operationsView.iterator() ;
    }
    
    @Override
    public String toString()
    { return PrintUtils.toString(this) ; } 
    
    public void output(IndentedWriter out)
    { UpdateWriter.output(this, out) ; }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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