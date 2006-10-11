/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb;

import java.sql.Connection;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.notification.RunListener;
import sdb.cmd.CmdArgsDB;
import sdb.junit.TextListenerCustom;
import sdb.test.Params;
import sdb.test.ParamsVocab;
import sdb.test.TestI18N;
import sdb.test.TestStringBasic;
import arq.cmd.CmdException;

import com.hp.hpl.jena.query.util.Utils;

/** Run some DB tests to check setup */ 

public class DBTest extends CmdArgsDB 
{
    
    public static void main(String [] argv)
    {
        new DBTest(argv).mainAndExit() ;
    }
    
    String filename = null ;

    public DBTest(String[] args)
    {
        super(args);
    }
    
    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary()  { return getCommandName()+" <SPEC> "; }
    
    @Override
    protected void processModulesAndArgs()
    {
        @SuppressWarnings("unchecked")
        List<String> args = getPositional() ;
        setParams(args) ;
    }
    
    Params params = new Params() ;
    {
        params.put( ParamsVocab.TempTableName,    "FOO") ;

        params.put( ParamsVocab.BinaryType,       "BLOB") ;
        params.put( ParamsVocab.BinaryCol,        "colBinary") ;
        
        params.put( ParamsVocab.VarcharType,      "VARCHAR(200)") ;
        params.put( ParamsVocab.VarcharCol,       "colVarchar") ;
    }
    
    @Override
    protected void execCmd(List<String> args)
    {
        if ( verbose )
        {
            for ( String k : params )
                System.out.printf("%-20s = %-20s\n", k, params.get(k) );
            System.out.println() ;
        }
        
        Connection jdbc = getModStore().getConnection().getSqlConnection() ;
        // Hack to pass to calculated parameters to the test subsystem.

        sdb.test.Env.set(jdbc, params, false) ;
        
        JUnitCore x = new org.junit.runner.JUnitCore() ;
        RunListener listener = new TextListenerCustom() ;
        x.addListener(listener) ;
        
        //x.run(sdb.test.AllTests.class) ;
        System.out.println("String basic") ;
        x.run(TestStringBasic.class) ;
        
        System.out.println("String I18N") ;
        x.run(TestI18N.class) ;

        // Better way of having parameters for a class than a @Parameterised test of one thing?
//        Request request = Request.aClass(sdb.test.T.class) ;
//        x.run(request) ;
    }
    
    private void setParams(List<String> args)
    {
        for ( String s : args )
        {
            String[] frags = s.split("=", 2) ;
            if ( frags.length != 2)
                throw new CmdException("Can't split '"+s+"'") ;
            params.put(frags[0], frags[1] ) ;
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