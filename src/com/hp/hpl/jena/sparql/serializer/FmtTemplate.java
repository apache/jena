/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.serializer;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.syntax.TemplateGroup;
import com.hp.hpl.jena.sparql.syntax.TemplateTriple;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

/** com.hp.hpl.jena.query.core.FormatterARQ
 * 
 * @author Andy Seaborne
 */

public class FmtTemplate extends FormatterBase
    implements FormatterTemplate 
{
    public FmtTemplate(IndentedWriter out, SerializationContext context)
    {
        super(out, context) ;
    }
    
    public static void format(IndentedWriter out, SerializationContext cxt, Template template)
    {
        FmtTemplate fmt = new FmtTemplate(out, cxt) ;
        fmt.startVisit() ;
        template.visit(fmt) ;
        fmt.finishVisit() ;
    }
    
    public static String asString(Template template)
    {
        SerializationContext cxt = new SerializationContext() ;
        IndentedLineBuffer b = new IndentedLineBuffer() ;
        FmtTemplate.format(b, cxt, template) ;
        return b.toString() ;
    }

    public void visit(TemplateGroup template)
    {
        out.print("{") ;
        out.incIndent(INDENT) ;
        out.pad() ;
    
        //boolean first = true ;
        BasicPattern acc = new BasicPattern() ;    // Accumulator of successive triples
        
        for ( Iterator<Template> iter = template.templates() ; iter.hasNext() ; )
        {
            Template temp = iter.next() ;
            if ( temp instanceof TemplateTriple )
            {
                Triple triple = ((TemplateTriple)temp).getTriple() ;
                acc.add(triple) ;
                continue ;
            }
            // Flush accumulator
            if ( ! acc.isEmpty() )
                formatTriples(acc) ;
            acc = new BasicPattern() ;
            temp.visit(this) ;
            out.print(" .") ;
            out.newline() ;
            //first = false ;
        }
    
        // Flush accumulator
        if ( ! acc.isEmpty() )
            formatTriples(acc) ;
        
        out.decIndent(INDENT) ;
        out.print("}") ;
        out.newline() ;
    }

    public void visit(TemplateTriple template)
    {
        formatTriple(template.getTriple()) ;
    }

}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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