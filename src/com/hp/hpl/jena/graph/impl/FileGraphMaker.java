/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: FileGraphMaker.java,v 1.21 2005-02-21 11:52:10 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;

import java.io.*;
import java.util.*;

import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;

/**
    A FileGraph factory, making FileGraphs based round some supplied
    directory. We have to keep track of created files ourselves, because a
    FileGraph that has been created but not closed is not visible in the
    filing system. (This might count as a bug at some point.)
    
 	@author hedgehog
*/
public class FileGraphMaker extends BaseGraphMaker
    {
    private String fileBase;
    private boolean deleteOnClose;
    private Map created = CollectionFactory.createHashedMap();
    
    /**
        Construct a file graph factory whose files will appear in root. The reifier
        style is Minimal and the files will be retained when the maker is closed.
        
     	@param root the directory to keep the files in.
     */
    public FileGraphMaker( String root )
        { this( root, ReificationStyle.Minimal ); }
        
    /**
        Construct a file graph factory whose files will appear in root. The files 
        will be retained when the maker is closed.
    
        @param root the directory to keep the files in.
        @param style the reification style of the resulting graph
     */
    public FileGraphMaker( String root, ReificationStyle style )
        { this( root, style, false ); }
 
    /**
        Construct a file graph factory whose files will appear in root.
        If deleteOnClose is true, the files created by this factory will be deleted
        when the factory is closed.
        
     	@param root the directory to keep the files in
        @param style the reification style of the graph
     	@param deleteOnClose iff true, delete created files on close
     */
    public FileGraphMaker( String root, ReificationStyle style, boolean deleteOnClose )
        {
        super( style );
        this.fileBase = root;
        this.deleteOnClose = deleteOnClose;       
        }

    /**
        Answer the RDFS class of a FileGraphMaker
        @return JMS.FileMakerClass [node version]
    */
    public Node getMakerClass()
        { return JMS.FileMakerSpec.asNode(); }

    /**
        Answer the fileBase of all the graphs created by this FileGraphMaker.
        @return the fileBase of this Maker
    */
    public String getFileBase()
        { return fileBase; }
        
    protected void augmentDescription( Graph g, Node self )
        { g.add( Triple.create( self, JMS.fileBase.asNode(), Node.createLiteral( fileBase, "", false ) ) ); }
                
    /**
        Answer a new, anonynous FileGraph. See FileGraph.create().
        @return a new anonymous FileGraph
    */
    public Graph createGraph()
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
        { return new File( fileBase, toFilename( name ) ); }
        
    /**
        Make <code>name</name> into a "safe" filename. "safe" is a bit weak
        here; we want to allow URIs as graph names and assume that our filing
        systems will be reasonably liberal. We'll see ...
        
    	@param name
    	@return name with underbar, slash, and colon escaped as _U, _S, _C
     */
    public static String toFilename( String name )
        {
        return replaceBy( name, "_/:", "USC" );
// Jave 1.4 please!
//        return name
//            .replaceAll( "_", "_U" )
//            .replaceAll( "/", "_S" )
//            .replaceAll( ":", "_C" )
//            ;    
        }
        
    private static String replaceBy( String x, String from, String to )
        {
        int len = x.length();
        StringBuffer result = new StringBuffer( len + 10 );
        for (int i = 0; i < len; i += 1)
            {
            char ch = x.charAt( i );
            int where = from.indexOf( ch );
            if (where < 0) result.append( ch );
            else result.append( '_' ).append( to.charAt( where ) );
            }
        return result.toString();
        }

    /**
        Answer the graphname corresponding to the given filename, undoing the 
        conversion done by toFilename.
        
     	@param fileName a filename, possible containing _U, _C, and _S escapes
     	@return the unescaped name
     */
    public static String toGraphname( String fileName )
        { 
        StringBuffer result = new StringBuffer( fileName.length() );
        int here = 0;
        while (true)
            {
            int ubar = fileName.indexOf( '_', here );    
            if (ubar < 0) break;
            result.append( fileName.substring( here, ubar ) );
            char ch = fileName.charAt( ubar + 1 );
            if (ch == 'S') result.append( '/' );
            else if (ch == 'U') result.append( '_' );
            else if (ch == 'C') result.append( ':' );
            here = ubar + 2;
            }
        result.append( fileName.substring( here ) );
        return result.toString();
// Java 1.4 please!
//            return fileName
//            .replaceAll( "_C", ":" )
//            .replaceAll( "_S", "/" )
//            .replaceAll( "_U", "_" ); 
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
        
    /**
        A Map1 that will convert filename strings to the corresponding graphname strings.
    */
    private static Map1 unconvert = new Map1()
        { public Object map1( Object x )
            { return toGraphname( (String) x ); }
        };
        
    /**
        Answer a FilenameFilter which recognises plausibly RDF filenames; they're not
        directories, and FileGraph likes them. Pass the buck, pass the buck ...
        
     	@return a FilenameFilter that accepts plausible graph names
     */
    public static FilenameFilter graphName()
        { return new FilenameFilter()
            {
            public boolean accept( File file, String name )
                { return !new File( file, name ).isDirectory()
                    && FileGraph.isPlausibleGraphName( name ); }    
            }; }
            
    /**
        Answer an iterator over the names of graphs in the FileGraphMaker. This is all the
        names of freshly-created graphs, plus the names of any files in the fileBase that
        might be RDF files. "Might" is weaker than we'd like for now.
         
     	@see com.hp.hpl.jena.graph.GraphMaker#listGraphs()
     */
    public ExtendedIterator listGraphs()
        { String [] fileNames = new File( fileBase ).list( graphName() );
        Set allNames = CollectionFactory.createHashedSet( Arrays.asList( fileNames ) );
        Iterator it = created.keySet().iterator();
        while (it.hasNext()) allNames.add( ((File) it.next()).getName() ); 
		return WrappedIterator.create( allNames.iterator() ) .mapWith( unconvert ); }
    }

/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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