/**
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

package arq;

import java.util.List ;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.update.*;
import arq.cmdline.CmdARQ ;
import arq.cmdline.ModRemote ;

public class rupdate extends CmdARQ
{
    static final ArgDecl updateArg = new ArgDecl(ArgDecl.HasValue, "update", "file") ;

    protected ModRemote     modRemote =     new ModRemote() ;

    List<String> requestFiles = null ;

    public static void main(String... argv)
    {
        new rupdate(argv).mainRun() ;
    }

    protected rupdate(String[] argv)
    {
        super(argv) ;
        super.add(updateArg, "--update=FILE", "Update commands to execute") ;
        super.addModule(modRemote) ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        requestFiles = getValues(updateArg) ;   // ????
        super.processModulesAndArgs() ;
    }


    @Override
    protected String getSummary()
    {
        return getCommandName()+" --service=URL --update=<request file>" ;
    }

    @Override
    protected void exec()
    {
        if ( modRemote.getServiceURL() == null )
        {
            throw new CmdException("No endpoint given") ;
        }
        String endpoint = modRemote.getServiceURL() ;

        for ( String filename : requestFiles )
        {
            UpdateRequest req = UpdateFactory.read( filename );
            exec( endpoint, req );
        }

        for ( String requestString : super.getPositional() )
        {
            requestString = indirect( requestString );
            UpdateRequest req = UpdateFactory.create( requestString );
            exec( endpoint, req );
        }
    }

    private void exec(String endpoint, UpdateRequest req)
    {
        UpdateExecution.service(endpoint).update(req).execute();
    }

}

