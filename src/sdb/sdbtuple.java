/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb;

import java.util.ArrayList;
import java.util.List;

import sdb.cmd.CmdArgsDB;
import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sparql.util.Utils;

import dev.inf.TupleTable;

public class sdbtuple extends CmdArgsDB
{
    private static ArgDecl argDeclCmdPrint = new ArgDecl(false, "print") ;
    private static ArgDecl argDeclCmdLoad = new ArgDecl(true, "load") ;
    
    private static ArgDecl argDeclCmdTable= new ArgDecl(true, "table") ;
    
    boolean cmdPrint = false ;
    boolean cmdLoad = false ;
    String loadFile = null ;
    
    public static void main(String ... args) { new sdbtuple(args).main() ; }
    
    public List<String> tables = new ArrayList<String>() ;
    public sdbtuple(String... argv)
    {
        super(argv) ;
        add(argDeclCmdTable, "--table=TableName", "Tuple table to operate on (incldues positional arguments as well)") ;
        add(argDeclCmdPrint, "--print", "Print a tuple table") ;
    }
    
    @Override
    protected void execCmd(List<String> positionalArgs)
    {
        for ( String tableName : tables )
            execOne(tableName) ;
    }
    
    @Override
    protected void processModulesAndArgs()
    {
        if ( !contains(argDeclCmdTable) && getNumPositional() == 0 )
            cmdError("No tables specified", true) ;
        
        @SuppressWarnings("unchecked")
        List<String>x = (List<String>)getPositional() ;
        tables.addAll(x) ;
        
        @SuppressWarnings("unchecked")
        List<String>y = (List<String>)getValues(argDeclCmdTable) ;
        tables.addAll(y) ;
        
        cmdLoad = contains(argDeclCmdLoad) ;
        if ( cmdLoad )
            loadFile = getValue(argDeclCmdLoad) ;
        
        cmdPrint = contains(argDeclCmdPrint) ;
        
    }

    @Override
    protected String getSummary()
    { return getCommandName()+" --sdb <SPEC> [--print|--??] [--table TableName] TableName..." ; }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }

    static final String divider = "- - - - - - - - - - - - - -" ;
    boolean needDivider = false ;
    private void divider()
    {
        if ( needDivider ) System.out.println(divider) ;
        needDivider = true ;
    }
    
    // ---- Execution
    
    private void execOne(String tableName)
    {
        if ( ! cmdPrint & ! cmdLoad )
            cmdError("Nothing to do!", true) ;
        
        if ( cmdPrint ) execPrint(tableName) ;
        if ( cmdLoad ) execLoad(tableName) ;
    }

    private void execPrint(String tableName)
    {
        Store store = getStore() ;
        TupleTable table = new TupleTable(store, tableName) ;
        divider() ;
        table.dump() ;
    }

    private void execLoad(String tableName)
    {
        cmdError("Tuple load - not implemented (yet)", true) ;
        Store store = getStore() ;
        TupleTable table = new TupleTable(store, tableName) ;
        
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