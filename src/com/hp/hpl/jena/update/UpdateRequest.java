/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.update;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.modify.op.Update;
import com.hp.hpl.jena.sparql.serializer.PrologueSerializer;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.PrintUtils;
import com.hp.hpl.jena.sparql.util.Printable;

public class UpdateRequest extends Prologue
    implements Printable//, Iterable<Update>
{
    private List requests = new ArrayList() ;
    public UpdateRequest() { super() ; }
    public UpdateRequest(Update graphUpdate) { super() ; requests.add(graphUpdate) ; }
    
    public void addUpdate(Update update) { requests.add(update) ; }
//    public List<Update> getUpdates(Update update) { return requests ; }
    
    public void exec(GraphStore graphStore)
    { 
        for ( Iterator iter = requests.iterator() ; iter.hasNext(); )
        {
            Update update = (Update)iter.next() ;
            update.exec(graphStore) ;
        }
    }
    
    //@Override
    public String toString()
    { return PrintUtils.toString(this) ; } 

    //@Override
    public void output(IndentedWriter out)
    {  
        PrologueSerializer.output(out, this) ;
        SerializationContext sCxt = new SerializationContext(this) ;
        boolean first = true ;
        out.println() ;
        
        for ( Iterator iter = requests.iterator() ; iter.hasNext(); )
        {
            Update update = (Update)iter.next() ;

            if ( ! first )
                out.println("    # ----------------") ;
            else
                first = false ;
            update.output(out, sCxt) ;
            out.ensureStartOfLine() ;
            //out.println();
        }
    }
    
    public Iterator iterator()
    {
        return requests.iterator() ;
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