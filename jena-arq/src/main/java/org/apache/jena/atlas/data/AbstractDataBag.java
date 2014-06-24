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

package org.apache.jena.atlas.data;

import java.io.BufferedInputStream ;
import java.io.BufferedOutputStream ;
import java.io.File ;
import java.io.FileInputStream ;
import java.io.FileNotFoundException ;
import java.io.FileOutputStream ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.OutputStream ;
import java.lang.ref.WeakReference ;
import java.util.ArrayList ;
import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;
import java.util.UUID ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.FileOps ;

/**
 * Abstract implementation of DataBag.  Used as a parent for all three of the types of data bags.
 */
public abstract class AbstractDataBag<E> implements DataBag<E>
{
    private final List<File> spillFiles = new ArrayList<>();
    protected Collection<E> memory = new ArrayList<>();
    
    private final List<WeakReference<Closeable>> closeableIterators = new ArrayList<>();
    
    // Total size, including tuples on disk.
    protected long size = 0;
    
    
    public boolean isEmpty()
    {
        return (size == 0);
    }

    @Override
    public long size()
    {
        return size;
    }

    @Override
    public void addAll(Iterable<? extends E> c)
    {
        addAll(c.iterator());
    }

    @Override
    public void addAll(Iterator<? extends E> it)
    {
        while (it.hasNext())
        {
            E item = it.next();
            add(item);
        }
    }

    @Override
    public void send(E item)
    {
        add(item);
    }
    
    /**
     * Returns a handle to a temporary file.  Does not actually create the file on disk.
     * 
     * TODO Improve this by getting the directory from a config file
     */
    protected File getNewTemporaryFile()
    {
        File sysTempDir = new File(System.getProperty("java.io.tmpdir")) ;
        File tmpFile = new File(sysTempDir, "DataBag-" + UUID.randomUUID().toString() + ".tmp") ;
        return tmpFile ;
    }
    
    /**
     * Register the spill file handle for use later in the iterator.
     */
    protected void registerSpillFile(File spillFile)
    {
        spillFiles.add(spillFile);
    }
    
    protected static OutputStream getOutputStream(File file) throws FileNotFoundException
    {
        return new BufferedOutputStream(new FileOutputStream(file));
    }
    
    protected static InputStream getInputStream(File file) throws FileNotFoundException
    {
        return new BufferedInputStream(new FileInputStream(file));
    }
    
    /** 
     * Get a stream to spill contents to.  The file that backs this stream will be registered in the spillFiles array.
     * @return stream to write tuples to
     */
    protected OutputStream getSpillStream() throws IOException
    {
        File outputFile = getNewTemporaryFile();
        OutputStream toReturn = getOutputStream(outputFile);
        registerSpillFile(outputFile);
        
        return toReturn;
    }
    
    /**
     * Register an iterator to be closed when this data bag is closed.  The iterator
     * is held via a weak reference, and is meant as a backup if the user does not
     * close it themselves.
     * @param c the Closeable iterator to register
     */
    protected void registerCloseableIterator(Closeable c)
    {
        closeableIterators.add(new WeakReference<>(c)) ;
    }
    
    /**
     * Users should either exhaust or close any iterators they get, but if they don't we
     * should forcibly close them so that we can delete any temporary files.  Any further
     * operations on the iterator will throw an exception.
     */
    protected void closeIterators()
    {
        for (WeakReference<Closeable> wr : closeableIterators)
        {
            Closeable c = wr.get();
            if (null != c)
            {
                c.close();
            }
        }
    }
    
    protected List<File> getSpillFiles()
    {
        return spillFiles;
    }
    
    protected void deleteSpillFiles()
    {
        for (File file : spillFiles)
        {
            FileOps.delete(file, false);
        }
        spillFiles.clear();
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        // Last chance to remove any files if the user forgot to call close()
        try
        {
            close();
        }
        finally
        {
            super.finalize();
        }
    }
}
