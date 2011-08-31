/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package jena;

import static jena.cmdline.CmdLineUtils.setLog4jConfiguration;
import jena.util.DBcmd;

import com.hp.hpl.jena.util.iterator.ClosableIterator;

 
/** List the models available in a database
 *   <p>
 *  Usage:<pre>
 *  jena.dblist [db spec]
 *  where [db spec] is:
 *    --spec file        Contains an RDF description of the model 
 *    --db JDBC_url --dbUser userId --dbPassword password --dbType
 *  </pre>
 *  Ignores any <code>--model modelName</code>.
 *  </p>
 * 
 * @author Andy Seaborne
 * @version $Id: dblist.java,v 1.1 2009-06-29 08:55:51 castagna Exp $
 */ 
 
public class dblist extends DBcmd
{
    public static final String[] usage = new String[]
                                                    { 
            "dblist [db_description] [--model name]" ,
            "  where db_description is" ,
            "    --db JDBC URL --dbType type" ,
            "    --dbUser user --dbPassword password" 
    } ;
    
    static {
    	setLog4jConfiguration() ;
    }
    
    public static void main(String[] args)
    {
        dblist db = new dblist();
        db.setUsage(usage) ;
        db.init(args);
        db.exec();
    }

    public dblist()
    {
        super("dblist", false) ;
    }

    static String defaultModelName = "DEFAULT" ;
    
    @Override
    protected void exec0()
    {
//        if ( getConnection().containsDefaultModel() )
//        {
//            System.out.println("Model: <<default model>>") ;
//            properties(null) ;
//        }
        
        ClosableIterator<String> iter = getConnection().getAllModelNames() ;
        try {
            for ( ; iter.hasNext() ; )
            {
                String name = iter.next() ; 
                System.out.println("Model: "+name) ;
                properties(name) ;
            }
        }
        finally
        {
            iter.close() ;
        }
      
    }
    
    @Override
    protected boolean exec1(String arg) { return true ; } 

    private void properties(String name)
    {
        if ( true )
            return ;        
//        ModelRDB m = ModelRDB.open(getConnection(), name) ;
//        Model props = m.getModelProperties() ;
//        props.setNsPrefix("db", "http://jena.hpl.hp.com/2003/04/DB#") ;
//        props.write(System.out, "N3") ;
//        props.close() ;
    }

}


/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
