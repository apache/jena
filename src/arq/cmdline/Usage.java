/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import java.io.PrintStream ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.io.IndentedWriter ;

public class Usage
{
    class Category
    {
        String desc ;
        List<Entry> entries = new ArrayList<Entry>() ;
        Category(String desc) { this.desc = desc ; }
    }
    
   class Entry
   { String arg; String msg ;
     Entry(String arg, String msg) { this.arg = arg ; this.msg = msg ; }
   }
   
   List<Category> categories = new ArrayList<Category>() ;
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
       //for ( Iterator<Category> iter = categories.iterator() ; iter.hasNext() ; )
       
       List<Category> categories2 = new ArrayList<Category>(categories) ;
       Collections.reverse(categories2) ;
       
       for ( Iterator<Category> iter = categories2.iterator() ; iter.hasNext() ; )
       
       {
           Category c = iter.next() ;
           if ( c.desc != null )
               out.println(c.desc) ;
           out.incIndent(INDENT2) ;
           for ( Iterator<Entry> iter2 = c.entries.iterator() ; iter2.hasNext() ; )
           {
               Entry e = iter2.next() ;
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
       return categories.get(categories.size()-1) ;
   }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
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