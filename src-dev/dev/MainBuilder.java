/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.io.FileInputStream;
import java.io.InputStream;


import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.sse.Writer;
import com.hp.hpl.jena.sparql.sse.builders.BuilderExpr;
import com.hp.hpl.jena.sparql.sse.builders.BuilderOp;
import com.hp.hpl.jena.sparql.sse.builders.ResolvePrefixedNames;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.util.FileUtils;

public class MainBuilder
{
    public static void main(String[] argv)
    {
        mainExpr(argv) ;
        //System.out.println() ;
        mainOp(argv) ;
        //mainTable(argv) ;
    }
    
    public static void mainExpr(String[] argv)
    {
        try
        {
            InputStream in = new FileInputStream("SSE/expr.sse") ;
            Item item = SSE.parse(in) ;
            if ( true )
            {
                System.out.println("**** SSE expression") ;
                Writer.write(System.out, item) ;
                System.out.println() ;
            }
            
            Expr expr = BuilderExpr.build(item) ;
            System.out.println(expr) ;
            System.out.println() ;
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public static void mainOp(String[] argv)
    {
        try
        {

            if ( false )
            {
                String s = FileUtils.readWholeFileAsUTF8("SSE/op.sse") ;
                IndentedWriter iw = new IndentedWriter(System.out, true) ;
                iw.print(s) ;
                iw.flush();
                if ( ! iw.atLineStart() )
                    System.out.println() ;
                System.out.println() ;
            }

            InputStream in = new FileInputStream("SSE/op.sse") ;
            Item item = SSE.parse(in) ;

            if ( true )
            {
                System.out.println("**** SSE expression") ;
                Writer.write(System.out, item) ;
            }

//          item = ItemTransformer.transform(new ItemTransformBase(), item) ;
//          Writer.write(System.out, item) ;


            System.out.println() ;
            PrefixMapping pmap = new PrefixMappingImpl() ;
            pmap.setNsPrefix("", "http://example/") ;
            item = ResolvePrefixedNames.resolve(item, pmap) ;

            Op op = BuilderOp.build(item) ;
            System.out.println("**** Algebra") ;
            System.out.print(op.toString()) ;
            System.out.println() ;

            System.out.println("**** Reparse") ;
            // Parser broken?
            String str = op.toString() ; //.replace("[", "(").replace("]", ")") ;

            item = SSE.parseString(str) ;
            System.out.print(item.toString()) ;

        } catch (Exception ex)
        {
            ex.printStackTrace();
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