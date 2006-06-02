/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.assembler;

import sdb.cmd.CmdDesc;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sdb.util.AssemblerUtils;
import com.hp.hpl.jena.vocabulary.RDF;

public class CmdDescAssembler extends AssemblerBase implements Assembler
{
    
    

    
    /* This SPARQL query will process arguments 
PREFIX acmd:     <http://jena.hpl.hp.com/2006/01/acmd#>
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
        
        String main = AssemblerUtils.getStringValue(root, AssemblerVocab.pMain) ;
        if ( main == null )
            main = AssemblerUtils.getStringValue(root, AssemblerVocab.pClassname) ;
        cd.setCmd(main) ;
        
        Resource x = AssemblerUtils.getResourceValue(root, AssemblerVocab.pArgs) ;
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
                String name = AssemblerUtils.getStringValue(entry, AssemblerVocab.pArgName) ;
                String value = AssemblerUtils.getStringValue(entry, AssemblerVocab.pArgValue) ;
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