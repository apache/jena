/*
 * (c) Copyright 2006, 2007 Hewlett--Packard Development Company, LP
 * [See end of file]
 */

package sdb;


import java.util.List;

import sdb.cmd.CmdArgsDB;
import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.store.StoreBaseHSQL;
import com.hp.hpl.jena.sparql.util.Utils;
 
 /** Load data files into an SDB model in a database.
  * 
  *  <p>
  *  Usage:<pre>
  *    sdbdelete [db spec] default | graph iri [...]
  *  </pre>
  *  </p>
  * 
  * @author Andy Seaborne
  */ 
 
public class sdbdelete extends CmdArgsDB
{
    private static final String usage = "sdbdelete --sdb <SPEC> default | <IRI> ..." ;
    
    private static ArgDecl argDeclConfirm  = new ArgDecl(false,  "confirm", "force") ;
    
    public static void main(String... argv)
    {
        new sdbdelete(argv).mainRun() ;
    }
    
    String filename = null ;

    public sdbdelete(String... args)
    {
        super(args);
        add(argDeclConfirm) ;
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary()  { return getCommandName()+" <SPEC> default | <IRI> ..."; }
    
    @Override
    protected void processModulesAndArgs()
    {
        if ( getNumPositional() == 0 )
            cmdError("Need IRIs of graphs to delete", true) ;
    }
    
    @Override
    protected void execCmd(List<String> args)
    {
        //if ( contains(argDeclTruncate) )
        //    getStore().getTableFormatter().truncate() ;
        for ( String x : args )
            removeOne(x) ;
        StoreBaseHSQL.close(getStore()) ;
    }
    
    private void removeOne(String IRI)
    {
        boolean removeDefault = "default".equals(IRI);

        if (isVerbose()) {
            if (removeDefault) System.out.println("Removing default graph");
            else System.out.println("Removing graph named <" + IRI + ">");
        }

        Model model = (removeDefault) ?
            SDBFactory.connectDefaultModel(getStore()) :
            SDBFactory.connectNamedModel(getStore(), IRI) ;

        model.removeAll();
    }
}
 
/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
