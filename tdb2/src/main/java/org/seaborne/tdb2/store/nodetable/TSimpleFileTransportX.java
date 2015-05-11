/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.seaborne.tdb2.store.nodetable;

import java.io.File ;
import java.io.IOException ;
import java.io.RandomAccessFile ;

import org.apache.thrift.transport.TTransport ;
import org.apache.thrift.transport.TTransportException ;


/**
 * Basic file support for the TTransport interface
 */
public final class TSimpleFileTransportX extends TTransport {

  private RandomAccessFile file = null;   
  private boolean readable;               
  private boolean writable;               
  private String path_;               


  /**
   * Create a transport backed by a simple file 
   * 
   * @param path the path to the file to open/create
   * @param read true to support read operations
   * @param write true to support write operations
   * @param openFile true to open the file on construction
   * @throws TTransportException if file open fails
   */
  public TSimpleFileTransportX(String path, boolean read, 
                              boolean write, boolean openFile)
          throws TTransportException {
    if (path.length() <= 0) {
      throw new TTransportException("No path specified");
    }
    if (!read && !write) {
      throw new TTransportException("Neither READ nor WRITE specified");
    }
    readable = read;
    writable = write;
    path_ = path;
    if (openFile) {
      open();
    }
  }
  
  /**
   * Create a transport backed by a simple file 
   * Implicitly opens file to conform to C++ behavior.
   * 
   * @param path the path to the file to open/create
   * @param read true to support read operations
   * @param write true to support write operations
   * @throws TTransportException if file open fails
   */
  public TSimpleFileTransportX(String path, boolean read, boolean write)
          throws TTransportException {
    this(path, read, write, true);
  }
  
  /**
   * Create a transport backed by a simple read only disk file (implicitly opens
   * file)
   *
   * @param path the path to the file to open/create
   * @throws TTransportException if file open fails
   */
  public TSimpleFileTransportX(String path) throws TTransportException {
    this(path, true, false, true);
  }

  /**
   * Test file status
   *
   * @return true if open, otherwise false
   */
  @Override
  public boolean isOpen() {
    return (file != null);
  }

  /**
   * Open file if not previously opened. 
   *
   * @throws TTransportException if open fails
   */
  @Override
  public void open() throws TTransportException {
    if (file == null){
      try {
        String access = "r";       //RandomAccessFile objects must be readable
        if (writable) {
          access += "w";
        }
        file = new BufferedRandomAccessFile(path_, access, 16*1024);
      } catch (IOException ioe) {
        file = null;
        throw new TTransportException(ioe.getMessage());
      }      
    }
  }

  /**
   * Close file, subsequent read/write activity will throw exceptions
   */
  @Override
  public void close() {
    if (file != null) {
      try {
        file.close();
      } catch (Exception e) {
        //Nothing to do
      }
      file = null;
    }
  }

  /**
   * Read up to len many bytes into buf at offset 
   *
   * @param buf houses bytes read
   * @param off offset into buff to begin writing to
   * @param len maximum number of bytes to read
   * @return number of bytes actually read
   * @throws TTransportException on read failure
   */
  @Override
  public int read(byte[] buf, int off, int len) throws TTransportException {
    if (!readable) {
      throw new TTransportException("Read operation on write only file");
    }
    int iBytesRead = 0;
    try {
      iBytesRead = file.read(buf, off, len);
    } catch (IOException ioe) {
      file = null;
      throw new TTransportException(ioe.getMessage());
    }
    return iBytesRead;
  }

  /**
   * Write len many bytes from buff starting at offset 
   *
   * @param buf buffer containing bytes to write
   * @param off offset into buffer to begin writing from
   * @param len number of bytes to write
   * @throws TTransportException on write failure
   */
  @Override
  public void write(byte[] buf, int off, int len) throws TTransportException {
    try {
      file.write(buf, off, len);
    } catch (IOException ioe) {
      file = null;
      throw new TTransportException(ioe.getMessage());
    }
  }

  /**
   * Move file pointer to specified offset, new read/write calls will act here
   *
   * @param offset bytes from beginning of file to move pointer to
   * @throws TTransportException is seek fails
   */
  public void seek(long offset) throws TTransportException {
    try {
      file.seek(offset);
    } catch (IOException ex) {
      throw new TTransportException(ex.getMessage());
    }
  }

  /**
   * Return the length of the file in bytes
   *
   * @return length of the file in bytes
   * @throws TTransportException if file access fails
   */
  public long length() throws TTransportException {
    try {
      return file.length();
    } catch (IOException ex) {
      throw new TTransportException(ex.getMessage());
    }
  }

  /**
   * Return current file pointer position in bytes from beginning of file
   *
   * @return file pointer position
   * @throws TTransportException if file access fails
   */
  public long getFilePointer() throws TTransportException {
    try {
      return file.getFilePointer();
    } catch (IOException ex) {
      throw new TTransportException(ex.getMessage());
    }
  }
  
  public static class BufferedRandomAccessFile extends RandomAccessFile
  {
      boolean reading=true;
      private byte buffer[];
      private int bufferSize = 0;

      private long filePos=0;
      private long fileLength=0;
      private long bufferStart=0;

      public BufferedRandomAccessFile(String filename, String mode, int bufsize) throws IOException
      {
          this(new File(filename),mode,bufsize);
      }

      public BufferedRandomAccessFile(File file, String mode, int bufsize) throws IOException
      {
          super(file, mode);
          fileLength=file.length();
          buffer = new byte[bufsize];
      }

      public final int read() throws IOException
      {
          if (!reading) switchToReadBuffer();
          while(true)
          {
              if (filePos==fileLength) return -1;
              // read the data
              int readAtIdx=(int) (filePos-bufferStart);
              if (readAtIdx<0 || readAtIdx>=bufferSize)
                  updateReadBuffer();
              else
              {
                  ++filePos;
                  return ((int)buffer[readAtIdx]) & 0xff;
              }
          }
      }

      @Override
      public int read(byte[] b, int off, int len) throws IOException
      {
          int fileAvailable=(int) (fileLength-filePos);
          if (fileAvailable==0) return -1;
          if (!reading) switchToReadBuffer();
          if (len>fileAvailable) len=fileAvailable;
          int readAtIdx=(int) (filePos-bufferStart);
          if (readAtIdx<0 || readAtIdx>=bufferSize)
          {
              updateReadBuffer();
              readAtIdx=(int) (filePos-bufferStart);
          }
          int availableInBuffer=bufferSize-readAtIdx;
          if (len>availableInBuffer) len=availableInBuffer;
          System.arraycopy(buffer, readAtIdx, b, off, len);
          filePos+=len;
          return len;
      }

      @Override
      public void write(int b) throws IOException
      {
          if (reading)
              switchToWriteBuffer();
          while(true)
          {
              if (bufferSize==0)
                  bufferStart=filePos;
              int writeAtIdx=(int) (filePos-bufferStart);
              if (writeAtIdx<0 || writeAtIdx>=buffer.length)
                  flush();
              else
              {
                  buffer[writeAtIdx]=(byte) b;
                  if (writeAtIdx==bufferSize) bufferSize++;
                  if (++filePos>fileLength) fileLength=filePos;
                  return;
              }
          }
      };

      @Override
      public void write(byte[] b, int off, int len) throws IOException
      {
          if (reading)
              switchToWriteBuffer();
          int from=off;
          int remaining=len;
          while(remaining>0)
          {
              if (bufferSize==0)
                  bufferStart=filePos;
              int writeAtIdx=(int) (filePos-bufferStart);
              if (writeAtIdx<0 || writeAtIdx>=buffer.length)
                  flush();
              else
              {
                  int todo=buffer.length-writeAtIdx;
                  if (todo>remaining) todo=remaining;
                  System.arraycopy(b, from, buffer, writeAtIdx, todo);
                  writeAtIdx+=todo;
                  if (writeAtIdx>bufferSize) bufferSize=writeAtIdx;
                  filePos+=todo;
                  if (filePos>fileLength) fileLength=filePos;
                  remaining-=todo;
                  from+=todo;
              }
          }
      }

      private void switchToWriteBuffer()
      {
          bufferSize=0;
          bufferStart=filePos;
          reading=false;
      }

      public void switchToReadBuffer() throws IOException
      {
          flush();
          reading=true;
      }

      private void updateReadBuffer() throws IOException
      {
          super.seek(filePos);
          bufferStart=filePos;
          int n = super.read(buffer, 0, buffer.length);
          if (n < 0) n=0;
          bufferSize = n;
      }

      @Override
      public long getFilePointer() throws IOException
      {
          return filePos;
      }

      @Override
      public long length() throws IOException
      {
          return fileLength;
      }

      @Override
      public void seek(long pos) throws IOException
      {
          filePos=pos;
          if (filePos>fileLength) filePos=fileLength;
          if (filePos<0) filePos=0;
      }

      public void flush() throws IOException
      {
          if (reading) return;
          super.seek(bufferStart);
          super.write(buffer,0,bufferSize);
          bufferSize=0;
      }

      @Override
      public void setLength(long newLength) throws IOException
      {
          flush();
          super.setLength(newLength);
          fileLength=newLength;
          seek(filePos);
      }

      @Override
      public void close() throws IOException
      {
          flush();
          super.close();
      }
  } 
}