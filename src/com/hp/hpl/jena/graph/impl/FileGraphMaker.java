/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: FileGraphMaker.java,v 1.7 2003-07-24 15:29:31 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;
import java.io.*;
import java.util.*;
import com.hp.hpl.jena.shared.*;

/**
    A FileGraph factory, makeing FileGraphs based round some supplied
    directory. We have to keep track of created files ourselves, because a
    FileGraph that has been created but not closed is not visible in the
    filing system. (This might count as a bug at some point.)
    
 	@author hedgehog
*/
public class FileGraphMaker extends BaseGraphMaker
    {
    private String root;
    private boolean deleteOnClose;
    private Map created = new HashMap();
    
    /**
        Construct a file graph factory whose files will appear in root. The reifier
        style is Minimal and the files will be retained when the maker is closed.
        
     	@param root the directory to keep the files in.
     */
    public FileGraphMaker( String root )
        { this( root, Reifier.Minimal ); }
        
    /**
        Construct a file graph factory whose files will appear in root. The files 
        will be retained when the maker is closed.
    
        @param root the directory to keep the files in.
        @param style the reification style of the resulting graph
     */
    public FileGraphMaker( String root, Reifier.Style style )
        { this( root, style, false ); }
 
    /**
        Construct a file graph factory whose files will appear in root.
        If deleteOnClose is true, the files created by this factory will be deleted
        when the factory is closed.
        
     	@param root the directory to keep the files in
        @param style the reification style of the graph
     	@param deleteOnClose iff true, delete created files on close
     */
    public FileGraphMaker( String root, Reifier.Style style, boolean deleteOnClose )
        {
        super( style );
        this.root = root;
        this.deleteOnClose = deleteOnClose;       
        }
        
    public Graph getGraph()
        { return FileGraph.create(); }

    public Graph createGraph( String name, boolean strict )
        {
        File f = withRoot( name );
        FileGraph already = (FileGraph) created.get( f );
        if (already == null)
            return remember( f, new FileGraph( f, true, strict, style ) ); 
        else
            {
            if (strict) throw new AlreadyExistsException( name );
            else return already.openAgain();
            }
        }

    public Graph openGraph( String name, boolean strict )
        { 
        File f = withRoot( name );
        return created.containsKey( f )  
            ? ((FileGraph) created.get( f )).openAgain()
            : remember( f, new FileGraph( f, false, strict, style ) )
            ;
        }

    private File withRoot( String name )
        { return new File( root, makeSafe( name ) ); }
        
    /**
        Make <code>name</name> safe for use as a filename. "safe" is a bit weak
        here; we want to allow URIs as graph names and assume that our filing
        systems will be reasonably liberal. We'll see ...
        
    	@param name
    	@return
     */
    private String makeSafe( String name )
        {
        return replaceBy( name, "_/:", "USC" );
//        return name
//            .replaceAll( "_", "_U" )
//            .replaceAll( "/", "_S" )
//            .replaceAll( ":", "_C" )
//            ;    
        }
        
    private String replaceBy( String x, String from, String to )
        {
        int len = x.length();
        StringBuffer result = new StringBuffer( len + 10 );
        for (int i = 0; i < len; i += 1)
            {
            char ch = x.charAt( i );
            int where = from.indexOf( ch );
            result.append( where < 0 ? ch : to.charAt( where ) );
            }
        return result.toString();
        }
        
    public void removeGraph( String name )
        { forget( withRoot( name ) ).delete(); }

    private FileGraph remember( File f, FileGraph g )
        {
        created.put( f, g );
        return g;
        }
        
    private File forget( File f )
        {
        created.remove( f );
        return f;
        }
        
    public boolean hasGraph( String name )
        {
        File f = withRoot( name );
        return created.containsKey( f ) || f.exists(); 
        }
        
    public void close()
        {
        if (deleteOnClose)
            {
            Iterator it = created.keySet().iterator();
            while (it.hasNext()) ((File) it.next()).delete();
            }
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/