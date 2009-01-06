/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lang;

import java.util.Collection;

import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.syntax.TemplateGroup;
import com.hp.hpl.jena.sparql.syntax.TemplateTriple;
import com.hp.hpl.jena.sparql.syntax.TemplateVisitor;

public class TriplesDataCollector implements TemplateVisitor
{
    private Collection<Triple> acc ;
    private int line ;
    private int col ;

    public TriplesDataCollector(Collection<Triple> acc, int line, int col)
    { 
        this.acc = acc ;
        this.line = line ;
        this.col = col ;
    }
        
    public void visit(TemplateTriple template)
    {
        Triple t = template.getTriple() ;
        if ( t.getSubject().isVariable() ||
            t.getPredicate().isVariable() ||
            t.getObject().isVariable() )
        {
            ParserBase.throwParseException("Triples may not contain variables in ADD or REMOVE", line, col) ;
        }
        acc.add(t) ;
    }

    public void visit(TemplateGroup template)
    {
        for (Template t : template.getTemplates())
            t.visit(this) ;
    }
    
}
/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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