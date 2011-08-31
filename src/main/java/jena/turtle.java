/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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