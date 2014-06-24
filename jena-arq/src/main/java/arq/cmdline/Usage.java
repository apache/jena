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

package arq.cmdline;

import java.io.PrintStream ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.io.IndentedWriter ;

public class Usage
{
    class Category
    {
        String desc ;
        List<Entry> entries = new ArrayList<>() ;
        Category(String desc) { this.desc = desc ; }
    }
    
   class Entry
   { String arg; String msg ;
     Entry(String arg, String msg) { this.arg = arg ; this.msg = msg ; }
   }
   
   List<Category> categories = new ArrayList<>() ;
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
       
       List<Category> categories2 = new ArrayList<>(categories) ;
       Collections.reverse(categories2) ;

       for ( Category c : categories2 )
       {
           if ( c.desc != null )
           {
               out.println( c.desc );
           }
           out.incIndent( INDENT2 );
           for ( Iterator<Entry> iter2 = c.entries.iterator(); iter2.hasNext(); )
           {
               Entry e = iter2.next();
               out.print( e.arg );
               if ( e.msg != null )
               {
                   out.pad( 20 );
                   out.print( "   " );
                   out.print( e.msg );
               }
               out.println();
           }
           out.decIndent( INDENT2 );
       }
       out.decIndent(INDENT1) ;
       out.flush() ;
   }
   
   private Category current()
   {
       return categories.get(categories.size()-1) ;
   }
}
