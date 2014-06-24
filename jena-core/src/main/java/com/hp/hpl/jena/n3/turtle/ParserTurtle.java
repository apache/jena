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

package com.hp.hpl.jena.n3.turtle;

import java.io.InputStream ;
import java.io.Reader ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.n3.turtle.parser.ParseException ;
import com.hp.hpl.jena.n3.turtle.parser.TokenMgrError ;
import com.hp.hpl.jena.n3.turtle.parser.TurtleParser ;
import com.hp.hpl.jena.util.FileUtils ;


public class ParserTurtle
{
    public ParserTurtle() {}
    
    public void parse(Graph graph, String baseURI, InputStream in)
    {
        Reader reader = FileUtils.asUTF8(in) ;
        parse(graph, baseURI, reader) ;
    }
    
    public void parse(Graph graph, String baseURI, Reader reader)
    {
        // Nasty things happen if the reader is not UTF-8.
        try {
            TurtleParser parser = new TurtleParser(reader) ;
            parser.setEventHandler(new TurtleRDFGraphInserter(graph)) ;
            parser.setBaseURI(baseURI) ;
            parser.parse() ;
        }

        catch (ParseException | TokenMgrError ex)
        { throw new TurtleParseException(ex.getMessage()) ; }

        catch (TurtleParseException ex) { throw ex ; }

        catch (Throwable th)
        {
            throw new TurtleParseException(th.getMessage(), th) ;
        }
    }
    
    
}
