/*
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package jena;

import com.hp.hpl.jena.util.* ;
 
 /** Load data files into a Jena model in a database.
  * 
  *  <p>
  *  Usage:<pre>
  *  jena.dbload [db spec] file [file ...]
  *  where [db spec] is:
  *    --spec file        Contains an RDF description of the model 
  *    --db JDBC_url --dbUser userId --dbPassword password --dbType [--model modelName]  
  *  </pre>
  *  The syntax of a file is determimed by its extension (.n3, .nt) and defaults to RDF/XML. 
  *  </p>
  * 
  * @author Andy Seaborne
  * @version $Id: dbload.java,v 1.3 2004-01-23 16:52:55 andy_seaborne Exp $
  */ 
 
public class dbload extends DBcmd
{
    public static final String[] usage = new String[]
    { 
        "dbload [--spec spec] | [db_description] [--model name] file" ,
        "  where db_description is" ,
        "    --db JDBC URL --dbType type" ,
        "    --dbUser user --dbPassword password" 
     } ;
    
    public static void main(String[] args)
    {
        dbload db = new dbload();
        db.setUsage(usage) ;
        
        // add any new args
        db.init(args);
        // do any additional test here

        // Action!
        db.exec();
    }
    
    String filename = null ;

    public dbload()
    {
        super("dbload", true);
    }

    void exec0() { return ; }

    boolean exec1(String arg)
    {
        if ( verbose )
            System.out.println("Start load: "+arg) ;
        // Crude but convenient
        if ( arg.indexOf(':') == -1 )
            arg = "file:"+arg ;

        String lang = ModelLoader.guessLang(arg) ;
        getRDBModel().read(arg, lang) ;
        return true ;
    }
}
 


/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP
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
