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

package com.hp.hpl.jena.sdb.script;


import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import com.hp.hpl.jena.sdb.assembler.AssemblerVocab;
import com.hp.hpl.jena.sdb.assembler.CommandAssemblerException;

public class CmdDescAssembler extends AssemblerBase implements Assembler
{
    
    /* This SPARQL query will process arguments 
PREFIX acmd:     <http://jena.hpl.hp.com/2007/sdb#>
PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX list:    <http://jena.hpl.hp.com/ARQ/list#>

SELECT ?name ?value
{ ?x rdf:type acmd:Cmd ;
     acmd:args ?args .

  { ?args list:member [ acmd:name ?name  ; acmd:value  ?value ] }
UNION
  { ?args list:member ?e .
    OPTIONAL { ?e acmd:name ?name }
    FILTER (!bound(?name)) .
    ?e acmd:value ?value .
  }
UNION
  { ?args list:member ?value . FILTER isLiteral(?value) }
}     
     */
    
    @Override
    public Object open(Assembler a, Resource root, Mode mode)
    {
        CmdDesc cd = new CmdDesc() ; 
        
        String main = GraphUtils.getStringValue(root, AssemblerVocab.pMain) ;
        if ( main == null )
            main = GraphUtils.getStringValue(root, AssemblerVocab.pClassname) ;
        cd.setCmd(main) ;
        
        Resource x = GraphUtils.getResourceValue(root, AssemblerVocab.pArgs) ;
        if ( x != null )
        {
            for (; !x.equals(RDF.nil); )
            {
                RDFNode e = x.getRequiredProperty(RDF.first).getObject();
                // Move to next list item
                x = x.getRequiredProperty(RDF.rest).getResource();

                // Either : a literal or a named pair.
                if ( e.isLiteral() )
                {
                    cd.addPosn( ((Literal)e).getString() ) ;
                    continue ;
                }
                
                Resource entry = (Resource)e ; 
                String name = GraphUtils.getStringValue(entry, AssemblerVocab.pArgName) ;
                String value = GraphUtils.getStringValue(entry, AssemblerVocab.pArgValue) ;
                if ( value == null )
                    throw new CommandAssemblerException(entry, "Strange entry: "+entry) ;
                
                if ( name != null )
                    cd.addNamedArg(name, value) ;
                else
                    cd.addPosn(value) ;
            }
        }
        return cd ;
    }

}
