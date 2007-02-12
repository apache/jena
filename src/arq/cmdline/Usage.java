/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.util.IndentedWriter;

public class Usage
{
    class Category
    {
        String desc ;
        List entries = new ArrayList() ;
        Category(String desc) { this.desc = desc ; }
    }
    
   class Entry
   { String arg; String msg ;
     Entry(String arg, String msg) { this.arg = arg ; this.msg = msg ; }
   }
   
   List categories = new ArrayList() ;
   public Usage()
   {
       // Start with an unnamed category
       startCategory(null) ; 
   }
   
   public void startCategory(String desc) 
   {
       categories.add(new Category(desc)) ;
   }
   
   public void addUsage(String argName, String msg)
   {
       current().entries.add(new Entry(argName, msg)) ;
   }
   
   
   public void output(PrintStream out)
   {
       output(new IndentedWriter(out)) ;
   }
   
   public void output(IndentedWriter out)
   {
       int INDENT1 = 2 ;
       int INDENT2 = 4 ;
       out.incIndent(INDENT1) ;
       for ( Iterator iter = categories.iterator() ; iter.hasNext() ; )
       {
           Category c = (Category)iter.next() ;
           if ( c.desc != null )
               out.println(c.desc) ;
           out.incIndent(INDENT2) ;
           for ( Iterator iter2 = c.entries.iterator() ; iter2.hasNext() ; )
           {
               Entry e = (Entry)iter2.next() ;
               out.print(e.arg) ;
               if ( e.msg != null )
               {
                   out.pad(20) ;
                   out.print("   ") ;
                   out.print(e.msg) ;
               }
               out.println() ;
           }
           out.decIndent(INDENT2) ;
       }
       out.decIndent(INDENT1) ;
       out.flush() ;
   }
   
   private Category current()
   {
       return (Category)categories.get(categories.size()-1) ;
   }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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