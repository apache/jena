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

import org.apache.jena.cmd.*;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.shared.NotFoundException ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.core.assembler.AssemblerUtils ;


public class ModAssembler extends ModBase
{
    public static final ArgDecl assemblerDescDecl = new ArgDecl(ArgDecl.HasValue, "desc", "dataset") ;
    
    private String assemblerFile = null ;
    Object thingDescribed = null ;
    
    public ModAssembler()
    { 
        // Wire in assembler implementations
        AssemblerUtils.init() ;
    }
    
    // Dataset : default graph and named graphs

    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
        if ( cmdLine.contains(assemblerDescDecl) )
            assemblerFile = cmdLine.getValue(assemblerDescDecl) ;
    }
    
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        //cmdLine.getUsage().startCategory("Dataset") ;
        cmdLine.add(assemblerDescDecl,
                    "--desc=",
                    "Assembler description file") ;
    }
    
    public String getAssemblerFile() { return assemblerFile ; }
    
    // Should subclass and apply typing.
    
    public Object create(Resource type)
    {
        Object thing = null ;
        try {
            thing = AssemblerUtils.build(assemblerFile, type) ;
        }
        catch (ARQException ex) { throw ex; }
        catch (NotFoundException ex)
        { throw new CmdException("Not found: "+ex.getMessage()) ; }
        catch (JenaException ex)
        { throw ex ; }
        catch (Exception ex)
        { throw new CmdException("Error creating", ex) ; }
        
        return thing ;
    }
}
