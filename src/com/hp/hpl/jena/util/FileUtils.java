/*
 * (c) Copyright 2002, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.util;

import java.io.*;

import com.hp.hpl.jena.shared.JenaException;

public class FileUtils {
    
	public static String readWholeFile(String filename) throws IOException {
		Reader r = new BufferedReader(new FileReader(filename), 1024);
		StringWriter sw = new StringWriter(1024);
		char buff[] = new char[1024];
		while (r.ready()) {
			int l = r.read(buff);
			if (l <= 0)
				break;
			sw.write(buff, 0, l);
		}
		r.close();
		sw.close();
		return sw.toString();
	}

	static public Reader asUTF8(InputStream in) {
		try {
			return new InputStreamReader(in, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// Give up and die.
			throw new Error("utf-8 *must* be a supported encoding.");
		}
	}
	

	static public Writer asUTF8(OutputStream out) {
		try {
			return new OutputStreamWriter(out, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// Give up and die.
			throw new Error("utf-8 *must* be a supported encoding.");
		}
	}

	/**
	    create a temporary file that will be deleted on exit, and do something
	    sensible with any IO exceptions - namely, throw them up wrapped in
	    a JenaException.
	
	    @param prefix the prefix for File.createTempFile
	    @param suffix the suffix for File.createTempFile
	    @return the temporary File
	*/
	public static  File tempFileName( String prefix, String suffix )
	    {
	    File result = new File( getTempDirectory(), prefix + randomNumber() + suffix );
	    if (result.exists()) return tempFileName( prefix, suffix );
	    result.deleteOnExit();
	    return result;
	    }  

    private static int counter = 0;

    private static int randomNumber()
        {
        return ++counter;
        }
 
    /**
        Answer a File naming a freshly-created directory in the temporary directory. This
        directory should be deleted on exit.
        TODO handle threading issues, mkdir failure, and better cleanup
        
        @param prefix the prefix for the directory name
        @return a File naming the new directory
     */
    public static File getScratchDirectory( String prefix )
        {
        File result = new File( getTempDirectory(), prefix + randomNumber() );
        if (result.exists()) return getScratchDirectory( prefix );
        if (result.mkdir() == false) throw new JenaException( "mkdir failed on " + result );
        result.deleteOnExit();
        return result;   
        } 
        
    public static String getTempDirectory()
        { return temp; }
    
    private static String temp = constructTempDirectory();

    private static String constructTempDirectory()
        {
        try 
            { 
            File x = File.createTempFile( "xxx", ".none" );
            x.delete();
            return x.getParent(); 
            }
        catch (IOException e) 
            { throw new JenaException( e ); }
        }
                 
}

/*
 *  (c) Copyright 2002 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
