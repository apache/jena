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

package org.apache.jena.sparql.serializer;

import java.util.List;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.syntax.Template;

public class FmtTemplate extends FormatterBase
    implements FormatterTemplate 
{
    static final int INDENT = 2 ;
    
    public FmtTemplate(IndentedWriter out, SerializationContext context)
    {
        super(out, context) ;
    }
    
    public static void format(IndentedWriter out, SerializationContext cxt, Template template)
    {
        FmtTemplate fmt = new FmtTemplate(out, cxt) ;
        fmt.startVisit() ;
        fmt.format(template) ;
        fmt.finishVisit() ;
    }
    
    public static String asString(Template template)
    {
        SerializationContext cxt = new SerializationContext() ;
        IndentedLineBuffer b = new IndentedLineBuffer() ;
        FmtTemplate.format(b, cxt, template) ;
        return b.toString() ;
    }

    @Override
    public void format(Template template)
    {
        out.print("{") ;
        out.incIndent(INDENT) ;
        out.pad() ;
        
        List<Quad> quads = template.getQuads();
        for(Quad quad: quads){
          BasicPattern bgp = new BasicPattern();
          bgp.add(quad.asTriple());
          out.newline() ;
          if(! Quad.defaultGraphNodeGenerated.equals(quad.getGraph()) ){

        	out.print("GRAPH");
      		out.print(" ");
      		out.print(slotToString(quad.getGraph()));
      		out.print(" ");
      		
      		out.newline() ;
            out.incIndent(INDENT) ;
            out.pad() ;
            out.print("{") ;
            out.incIndent(INDENT) ;
            out.pad() ;
          }
          
          formatTriples(bgp) ;
          
          if(! Quad.defaultGraphNodeGenerated.equals(quad.getGraph()) ){
              out.decIndent(INDENT) ;
              out.print("}") ;
              out.decIndent(INDENT) ;
          }
       }
       out.newline() ;
       out.decIndent(INDENT) ;
       out.print("}") ;
       out.newline() ;
    }

}
