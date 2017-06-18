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

import jena.cmd.CmdArgModule;
import jena.cmd.CmdException;
import jena.cmd.CmdGeneral;

import org.apache.jena.query.Dataset ;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.shared.NotFoundException ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab ;

/** Add assembler to a general dataset description */
public class ModDatasetAssembler extends ModDataset
{
    private ModAssembler modAssembler = new ModAssembler() ;

    @Override
    public Dataset createDataset() {
        if ( modAssembler.getAssemblerFile() == null )
            return null ;
        
        try {
            dataset = (Dataset)modAssembler.create(DatasetAssemblerVocab.tDataset) ;
            if ( dataset == null )
                throw new CmdException("No dataset description found in: "+modAssembler.getAssemblerFile()) ;
        }
        catch (CmdException | ARQException ex) { throw ex ; }
        catch (NotFoundException ex)
        { throw new CmdException("Not found: "+ex.getMessage()) ; }
        catch (JenaException ex)
        { throw ex ; }
        catch (Exception ex)
        { throw new CmdException("Error creating dataset", ex) ; }
        return dataset ;
    }

    @Override
    public void registerWith(CmdGeneral cmdLine) {
        modAssembler.registerWith(cmdLine);
    }

    @Override
    public void processArgs(CmdArgModule cmdLine) {
        modAssembler.processArgs(cmdLine);
    }
    
    public String getAssemblerFile() {
        return modAssembler.getAssemblerFile() ;
    }
}
