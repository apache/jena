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

import org.apache.jena.atlas.io.IO;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;
import org.apache.jena.sparql.sse.Item ;
import org.apache.jena.sparql.sse.SSE ;

public class ModItem extends ModBase
{
    protected final ArgDecl queryFileDecl = new ArgDecl(ArgDecl.HasValue, "file") ;

    private String filename = null ;
    private String parseString = null ;
    private Item item = null ;

    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Item") ;
        cmdLine.add(queryFileDecl, "--file=", "File") ;
    }

    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
        if ( cmdLine.contains(queryFileDecl) )
        {
            filename = cmdLine.getValue(queryFileDecl) ;
            parseString = IO.readWholeFileAsUTF8(filename) ;
            return ;
        }

        if ( cmdLine.getNumPositional() == 0 && filename == null )
            cmdLine.cmdError("No query string or query file") ;

        if ( cmdLine.getNumPositional() > 1 )
            cmdLine.cmdError("Only one query string allowed") ;

        if ( cmdLine.getNumPositional() == 1 && filename != null )
            cmdLine.cmdError("Either query string or query file - not both") ;

        if ( filename == null )
        {
            String qs = cmdLine.getPositionalArg(0) ;
            parseString = cmdLine.indirect(qs) ;
        }
    }

    public Item getItem()
    {
        if ( item != null )
            return item ;
        // Need to get the (outer) prologue.
        item = SSE.parseItem(parseString) ;
        return item ;
    }

}
