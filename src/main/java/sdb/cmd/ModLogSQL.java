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

package sdb.cmd;

import java.util.List;

import com.hp.hpl.jena.sdb.sql.SDBConnection;

import arq.cmd.CmdException;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdArgModule;
import arq.cmdline.CmdGeneral;
import arq.cmdline.ModBase;

public class ModLogSQL extends ModBase
{
    // Logging.
    // query, sql
    protected final ArgDecl argDeclLogSQL          = new ArgDecl(true, "log") ;

    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("DB logging") ;
        cmdLine.add(argDeclLogSQL,         "--log=", "SQL logging [none, all, query, exceptions, statement]") ;
        
    }

    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
        List<String> vals = cmdLine.getValues(argDeclLogSQL) ;
        for ( String v : vals )
        {
            if ( v.equalsIgnoreCase("none") )
            {
                SDBConnection.logSQLExceptions = false ;
                SDBConnection.logSQLQueries = false ;
                SDBConnection.logSQLStatements = false ;
                continue ;
            }
            if ( v.equalsIgnoreCase("query") || v.equalsIgnoreCase("queries") )
            {
                SDBConnection.logSQLQueries = true ;
                continue ;
            }
            if ( v.equalsIgnoreCase("exception") || v.equalsIgnoreCase("exceptions") )
            {
                SDBConnection.logSQLExceptions = true ;
                continue ;
            }
            if ( v.equalsIgnoreCase("statement") || v.equalsIgnoreCase("statements") )
            {
                SDBConnection.logSQLStatements = true ;
                continue ;
            }
            if ( v.equalsIgnoreCase("all") || v.equalsIgnoreCase("sql") )
            {
                SDBConnection.logSQLExceptions = true ;
                SDBConnection.logSQLQueries = true ;
                SDBConnection.logSQLStatements = true ;
                continue ;
            }
            
            throw new CmdException("Not recognized as a log form: "+v) ;
        }
    }
    
}
