/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: InsertFile.java,v 1.1 2005-04-06 15:28:14 chris-dollin Exp $
*/

package com.hp.hpl.jena.internal.tools;

import java.io.*;

import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.util.FileManager;

/**
    Horrible untested code which runs as a main() with two arguments: a
    template file to copy to stdout, and an argument file to be inserted
    inplace of the (any) line with content &ltdiv id='insert-file-here'/&gt.
    
    @author kers
*/
public class InsertFile
    {
    public static void main( String [] args ) throws IOException
        { // args: templateFile insertFile
        InputStream x = FileManager.get().open( args[0] );
        if (x == null) throw new NotFoundException( args[0] );
        BufferedReader r = new BufferedReader(new InputStreamReader( x, "UTF-8" ) );
        String line;
        while ((line = r.readLine()) != null) 
            if (line.equals( "<div id='insert-file-here'/>" ))
                writeFile( args[1] );
            else
                System.out.println( line );
        r.close();
        }
    
    protected static void writeFile( String name ) throws IOException
        {
        InputStream x = FileManager.get().open( name );
        if (x == null) throw new NotFoundException( name );
        BufferedReader r = new BufferedReader( new InputStreamReader( x, "UTF-8" ) );
        String line;
        while ((line = r.readLine()) != null) System.out.println( line );
        r.close();
        }

    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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