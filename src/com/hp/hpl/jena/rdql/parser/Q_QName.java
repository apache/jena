/*
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 */


package com.hp.hpl.jena.rdql.parser;
import com.hp.hpl.jena.rdql.* ;

public class Q_QName extends Q_URI
{
    // The form actually coming from the parser.
    String seen = "";
    String prefix = null ;
    String lcname = null  ;
    
    public Q_QName(int id)
    {
        super(id);
    }

    public Q_QName(RDQLParser p, int id)
    {
        super(p, id);
    }

    void set(String s)
    {
        seen = s ;
    }
    
    public void jjtClose()
    {
        if ( jjtGetNumChildren() != 2 )
            throw new RDQL_InternalErrorException("Q_QName: expected 2 children: got "+jjtGetNumChildren()) ;
        prefix = ((Q_Identifier)jjtGetChild(0)).id ;
        lcname = ((Q_Identifier)jjtGetChild(1)).id ;
        seen = prefix+":"+lcname ;
        //super.setURI(seen) ;
    }

    public void fixup(Q_Query qnode)
    {
        
        if ( isRDFResource() )
            // Already done.
            return ;
        String full = qnode.getPrefix(prefix) ;

        if ( full == null )
            throw new QueryException("Query error: QName '"+seen+"' can not be expanded.") ;

        super.setRDFResource(model.createResource(full+lcname)) ;
    }
    


    
    public String asQuotedString()    { return seen ; }
    
    public String asUnquotedString()  { return seen ; }
    // Must return the expanded form
    public String valueString()       { return super.getURI() ; }

    public String toString() { return seen ; }
}

/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
