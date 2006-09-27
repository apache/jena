/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package sdb;

import java.util.List;

import sdb.cmd.CmdArgsDB;
import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.query.util.Utils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.store.StoreConfig;

 /** Write out the data from an SDB model.  Only works for small models
  * because of JDBC limitations in default configurations. 
  * 
  *  <p>
  *  Usage:<pre>
  *  sdbdump [db spec]
  *  where [db spec] is:
  *    --spec file        Contains an RDF description of the model 
  *    --db JDBC_url --dbUser userId --dbPassword password --dbType [--model modelName]  
  *  </pre>
  *  </p>
  * 
  * @author Andy Seaborne
  * @version $Id: sdbinfo.java,v 1.2 2006/04/23 21:24:21 andy_seaborne Exp $
  */ 
 
public class sdbinfo extends CmdArgsDB
{
    static ArgDecl argDeclFormat = new ArgDecl(true, "format","fmt") ;
    String format = "N3" ; 


    public static void main (String [] argv)
    {
        new sdbinfo(argv).mainAndExit() ;
    }
    
    protected sdbinfo(String[] args)
    {
        super(args);
        add(argDeclFormat) ;
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary()  { return Utils.className(this)+" --sdb <SPEC>" ; }

    @Override
    protected void processModulesAndArgs()
    {
        if ( contains(argDeclFormat) )
            format = getValue(argDeclFormat) ;
        if ( getNumPositional() > 0 )
            cmdError("No positional arguments allowed", true) ;
    }
    
    @Override
    protected void execCmd(List args)
    {
        StoreConfig sConf = getModStore().getStore().getConfiguration() ;
        if ( sConf == null )
        {
            System.out.println("Configuration is null") ;
            return ;
        }
        Model m = sConf.getModel() ;
        if ( m == null )
            System.out.println("No configuration model") ;
        else            
            m.write(System.out, format) ;
        
        List<String> tableNames = getModStore().getConnection().getTableNames() ;
        for ( String tableName : tableNames )
        {
            System.out.println("Table: "+tableName) ;
        }
        
    }
}
 


/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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
