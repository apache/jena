/*
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package jena;

import com.hp.hpl.jena.rdf.model.* ;
//import com.hp.hpl.jena.db.* ;
//import com.hp.hpl.jena.util.iterator.* ;
//import java.util.* ;
 
 /** Create one Jena RDF model in a database.
  *  <p>
  *  Usage:<pre>
  *  jena.dbcreate [db spec]
  *  where [db spec] is:
  *    --spec file        Contains an RDF description of the model 
  *    --db JDBC_url --dbUser userId --dbPassword password --dbType [--model modelName]  
  *  </pre>
  *  </p>
  * 
  * @author Andy Seaborne
  * @version $Id: dbcreate.java,v 1.5 2005-02-21 11:49:11 andy_seaborne Exp $
  */ 
 
public class dbcreate extends DBcmd
{
    public static final String[] usage = new String[]
    { 
        "dbcreate [--spec spec] | [db_description] [--model name]" ,
        "  where db_description is" ,
        "    --db JDBC URL --dbType type" ,
        "    --dbUser user --dbPassword password" 
     } ;

    public static void main(String[] args)
    {
        dbcreate db = new dbcreate();
        db.setUsage(usage) ;
        db.init(args);
        db.exec();
    }

    public dbcreate()
    {
        super("dbcreate", false);
    }

    protected void exec0()
    {
        Model m = null;

        if (super.argModelName == null)
        {
            System.out.println("Create default model");
            m = ModelFactory.createModelRDBMaker(getConnection()).createModel();  
        }  
        else
        {
            System.out.println("Create named model: " + argModelName);
            m = ModelFactory.createModelRDBMaker(getConnection()).createModel(argModelName);
        }
        
    }
    
    protected boolean exec1(String arg) { return false ; } 
}
 


/*
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
