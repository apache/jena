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

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;

import java.io.*;
import java.util.*;

import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.*;

/**
    A FileGraph factory, making FileGraphs based round some supplied
    directory. We have to keep track of created files ourselves, because a
    FileGraph that has been created but not closed is not visible in the
    filing system. (This might count as a bug at some point.)
*/
public class FileGraphMaker 
    extends BaseGraphMaker 
    implements FileGraph.NotifyOnClose
    {
    protected String fileBase;
    protected boolean deleteOnClose;
    protected Map<File, FileGraph> created = CollectionFactory.createHashedMap();
    protected Set<File> toDelete = CollectionFactory.createHashedSet();
    
    /**
        Construct a file graph factory whose files will appear in root. The reifier
        style is Minimal and the files will be retained when the maker is closed.
        
     	@param root the directory to keep the files in.
     */
    public FileGraphMaker( String root )
    { this ( root, false ) ; }
    /**
        Construct a file graph factory whose files will appear in root.
        If deleteOnClose is true, the files created by this factory will be deleted
        when the factory is closed.
        
     	@param root the directory to keep the files in
     	@param deleteOnClose iff true, delete created files on close
     */
    public FileGraphMaker( String root, boolean deleteOnClose )
        {
        super( );
        this.fileBase = root;
        this.deleteOnClose = deleteOnClose;       
        }

    /**
        Answer the fileBase of all the graphs created by this FileGraphMaker.
        @return the fileBase of this Maker
    */
    public String getFileBase()
        { return fileBase; }
                
    /**
        Answer a new, anonynous FileGraph. See FileGraph.create().
        @return a new anonymous FileGraph
    */
    @Override
    public Graph createGraph()
        { return FileGraph.create(); }
        
    @Override
    public Graph createGraph( String name, boolean strict )
        {
        File f = withRoot( name );
        FileGraph already = created.get( f );
        if (already == null)
            return remember( f, new FileGraph( this, f, true, strict ) ); 
        else
            {
            if (strict) throw new AlreadyExistsException( name );
            else return already.openAgain();
            }
        }

    @Override
    public Graph openGraph( String name, boolean strict )
        { 
        File f = withRoot( name );
        return created.containsKey( f )  
            ? created.get( f ).openAgain()
            : remember( f, new FileGraph( this, f, false, strict ) )
            ;
        }

    @Override
    public void notifyClosed( File f )
        {
        toDelete.add( f );
        created.remove( f ); 
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
        return name
            .replaceAll( "_", "_U" )
            .replaceAll( "/", "_S" )
            .replaceAll( ":", "_C" )
            ;    
        }

    /**
        Answer the graphname corresponding to the given filename, undoing the 
        conversion done by toFilename.
        
     	@param fileName a filename, possible containing _U, _C, and _S escapes
     	@return the unescaped name
     */
    public static String toGraphname( String fileName )
        { 
        return fileName
            .replaceAll( "_C", ":" )
            .replaceAll( "_S", "/" )
            .replaceAll( "_U", "_" ); 
        }
                
    @Override
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
        
    @Override
    public boolean hasGraph( String name )
        {
        File f = withRoot( name );
        return created.containsKey( f ) || f.exists(); 
        }
        
    @Override
    public void close()
        {
        if (deleteOnClose)
            {
            deleteFiles( created.keySet().iterator() );
            deleteFiles( toDelete.iterator() );
            }
        }

    protected void deleteFiles( Iterator<File> it )
        { while (it.hasNext()) it.next().delete(); }
        
    /**
        A Map1 that will convert filename strings to the corresponding graphname strings.
    */
    private static Map1<String, String> unconvert = new Map1<String, String>()
        { @Override
        public String map1( String x )
            { return toGraphname( x ); }
        };
        
    /**
        Answer a FilenameFilter which recognises plausibly RDF filenames; they're not
        directories, and FileGraph likes them. Pass the buck, pass the buck ...
        
     	@return a FilenameFilter that accepts plausible graph names
     */
    public static FilenameFilter graphName()
        { return new FilenameFilter()
            {
            @Override
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
    @Override
    public ExtendedIterator<String> listGraphs()
        { String [] fileNames = new File( fileBase ).list( graphName() );
        Set<String> allNames = CollectionFactory.createHashedSet( Arrays.asList( fileNames ) );
            for ( File file : created.keySet() )
            {
                allNames.add( file.getName() );
            }
		return WrappedIterator.create( allNames.iterator() ) .mapWith( unconvert ); }
    }
