/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify.request;

import org.openjena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

public class UpdateWriter
{
    public static void output(UpdateRequest request, IndentedWriter out, SerializationContext sCxt)
    {
        prologue() ;
        out.println() ;
        for ( Update update : request.getOperations() )
        {
            outputUpdate(update, out, sCxt) ;
        }
    }
    
    public static void output(Update update, IndentedWriter out, SerializationContext sCxt)
    {
        prologue() ;
        outputUpdate(update, out, sCxt) ;
    }
    
    
    private static void outputUpdate(Update update, IndentedWriter out, SerializationContext sCxt)
    {
        Writer writer = new Writer(out, sCxt) ;
        update.visit(writer) ; 
    }

    private static void prologue()
    {}


    private static class Writer implements UpdateVisitor
    {
        private final IndentedWriter out ;
        private final SerializationContext sCxt ;

        public Writer(IndentedWriter out, SerializationContext sCxt)
        {
            this.out = out ;
            this.sCxt = sCxt ;
        }

        private void visitDropClear(String name, UpdateDropClear update)
        {
            out.print(name) ;
            out.print(" ") ;
            if ( update.isSilent() )
                out.print("SILENT ") ;
            
            if ( update.isAll() )               { out.print("ALL") ; }
            else if ( update.isAllNamed() )     { out.print("NAMED") ; }
            else if ( update.isDefault() )      { out.print("DEFAULT") ; }
            else if ( update.isOneGraph() )
            { 
                String s = FmtUtils.stringForNode(update.getGraph(), sCxt) ;
                out.print(s) ;
            }
            else
            {
                out.print("UpdateDropClearBROKEN") ;
                throw new ARQException("Malformed UpdateDrop") ;
            }
            out.println() ;
        }
    
        public void visit(UpdateDrop update)
        { visitDropClear("DROP", update) ; }

        public void visit(UpdateClear update)
        { visitDropClear("CLEAR", update) ; }

        public void visit(UpdateCreate update)
        {
            out.print("CREATE") ;
            out.print(" ") ;
            if ( update.isSilent() )
                out.print("SILENT ") ;
            
            String s = FmtUtils.stringForNode(update.getGraph(), sCxt) ;
            out.print(s) ;
            out.println() ;
        }

        public void visit(UpdateLoad update)
        {
        }

        public void visit(UpdateDataInsert update)
        {}

        public void visit(UpdateDataDelete update)
        {}

        public void visit(UpdateDeleteWhere update)
        {}

        public void visit(UpdateModify update)
        {}
        
    }
    
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