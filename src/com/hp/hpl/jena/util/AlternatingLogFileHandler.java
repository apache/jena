/*
 *  (c) Copyright Hewlett-Packard Company 2001
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
 *
 * $Id: AlternatingLogFileHandler.java,v 1.1.1.1 2002-12-19 19:20:55 bwm Exp $
 *
 * Created on 05 January 2002, 13:51
 */

package com.hp.hpl.jena.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/** A log file handler which write the log to a number of
 * files in a round robbin fashion.
 *
 * <p>This is a subclass of LogFileHandler, which instead of
 * writing all the log data to one file, will write <CODE>
 * numEntries</CODE> entries to one file, then the next <CODE>
 * numEntries</CODE> to another, continuing until <CODE>
 * numFiles</CODE> files have been written when it will start
 * again with the first file.</p>
 *
 * <p>The files are written according to a pattern:<p>
 * <CODE><PRE>
 *         fileNameRoot[N].fileNameExt
 * </PRE></CODE>
 *
 * <p>This log file handler is useful for long term logging
 * when the amount of log data generated is large, but only
 * the latest entries are of interest.</p>
 *
 * @author bwm
 * @version $Revision: 1.1.1.1 $
 */
public class AlternatingLogFileHandler extends LogFileHandler {
    
    /** The default root part of the log file name.  
     */    
    public static String DEFAULT_FILE_NAME_ROOT = "jena";
    
    /** The default extension part of the log file name.  
     */    
    public static String DEFAULT_FILE_NAME_EXT = ".log";
    
    /** The default number of files.  The value is 3.
     */    
    public static int DEFAULT_NUM_FILES = 3;
    
    /** The default number of entries per file.  The value is 5000.
     */    
    public static int DEFAULT_ENTRIES_PER_FILE = 5000;
    
    protected int numFiles;
    protected int fileCount = 1;
    protected int entriesPerFile;
    protected int entryCount = 0;
    protected String fileNameRoot;
    protected String fileNameExt;                           ;
    
    /** Create an <CODE>AlternatingLogFileHandler</CODE> with a
     * default configuration.
     * @throws IOException can occur creating the log files
     */    
    public AlternatingLogFileHandler() throws IOException {
        this(DEFAULT_FILE_NAME_ROOT, DEFAULT_FILE_NAME_EXT);
    }
    
    /** Create an <CODE>AlternatingLogFileHandler</CODE> with
     * a given root file name and extension.
     * @param fileNameRoot the root part of the log file names
     * @param fileNameExt the extension part of the filenames
     * @throws IOException can occur creating the files
     */    
    public AlternatingLogFileHandler(String fileNameRoot, String fileNameExt)
      throws IOException {
        this(fileNameRoot, fileNameExt, DEFAULT_NUM_FILES, 
                                                      DEFAULT_ENTRIES_PER_FILE);
    }

    /** Creates new AlternatingLogFileHandler with the specifed
     * configuration.
     * @param fileNameRoot the root part of the log file names
     * @param fileNameExt the extension part of the log file names
     * @param numFiles the number of log files to use
     * @param entriesPerFile the number of log entries in each file
     * @throws IOException can occur creating the log file
     */
    public AlternatingLogFileHandler(String fileNameRoot, String fileNameExt,
                                               int numFiles, int entriesPerFile) 
      throws IOException {
        super(fileNameRoot + "1" + fileNameExt);
        this.fileNameRoot = fileNameRoot;
        this.fileNameExt = fileNameExt;
        this.numFiles = numFiles;
        this.entriesPerFile = entriesPerFile;
    }
    
    /** The same as {@link LogFileHandler#publish}
     */    
    public void publish( int level, String msg, String cls, 
                                                 String method, Throwable ex ) {
        super.publish(level, msg, cls, method, ex);
        if (++entryCount > entriesPerFile) {
            nextFile();
        }
    }
    
    protected void nextFile() {
        close();
        if (++fileCount > numFiles) {
            fileCount = 1;
        }
        try {
            m_file = new FileWriter(fileNameRoot + fileCount + fileNameExt);
            m_print = new PrintWriter( m_file );
        } catch (IOException ignore) {}
        entryCount = 0;
    }
}
