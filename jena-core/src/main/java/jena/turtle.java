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

package jena;

import static jena.cmdline.CmdLineUtils.setLog4jConfiguration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;

import com.hp.hpl.jena.n3.turtle.Turtle2NTriples;
import com.hp.hpl.jena.n3.turtle.TurtleParseException;
import com.hp.hpl.jena.n3.turtle.parser.ParseException;
import com.hp.hpl.jena.n3.turtle.parser.TokenMgrError;
import com.hp.hpl.jena.n3.turtle.parser.TurtleParser;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileUtils;

public class turtle
{
	static {
    	setLog4jConfiguration() ;
    }
	
    /** Run the Turtle parser - produce N-triples */
    public static void main(String[] args)
    {
        if ( args.length == 0 )
        {
            parse("http://example/BASE", System.in) ;
            return ;
        }
        
        
        for ( int i = 0 ; i < args.length ; i++ )
        {
            String fn = args[i] ;
            parse("http://base/", fn) ;
        }
    }        


    public static void parse(String baseURI, String filename)
    {
        InputStream in = null ;
        try {
            in = new FileInputStream(filename) ;
        } catch (FileNotFoundException ex)
        {
            System.err.println("File not found: "+filename) ;
            return ;
        }
        parse(baseURI, in) ;
    }


    public static void parse(String baseURI, InputStream in)
    {
        Reader reader = FileUtils.asUTF8(in) ;
        try {
            TurtleParser parser = new TurtleParser(reader) ;
            //parser.setEventHandler(new TurtleEventDump()) ;
            parser.setEventHandler(new Turtle2NTriples(System.out)) ;
            parser.setBaseURI(baseURI) ;
            parser.parse() ;
        }
        catch (ParseException ex)
        { throw new TurtleParseException(ex.getMessage()) ; }
        catch (TokenMgrError tErr)
        { throw new TurtleParseException(tErr.getMessage()) ; }

        catch (TurtleParseException ex) { throw ex ; }

        catch (JenaException ex)  { throw new TurtleParseException(ex.getMessage(), ex) ; }
        catch (Error err)
        {
            throw new TurtleParseException(err.getMessage() , err) ;
        }
        catch (Throwable th)
        {
            throw new TurtleParseException(th.getMessage(), th) ;
        }
    }
}
