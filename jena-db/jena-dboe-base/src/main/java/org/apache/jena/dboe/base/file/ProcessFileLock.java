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

package org.apache.jena.dboe.base.file;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.DSYNC;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.DBOpEnvException;
import org.apache.jena.dboe.sys.ProcessUtils;

/** A simple packaging around a {@link java.nio.channels.FileLock}.
 * <p>
 * {@code ProcessFileLock}s are not reentrant locks.
 * <p>
 * See {@link #create} and {@link #release} to obtain and stop using a {@code ProcessFileLock}.
 */
public class ProcessFileLock {
    // Static (process-wide) sync.
    private static Object sync = new Object();
    
    // Map from path of the file to a ProcessFileLock
    private static ConcurrentHashMap<Path, ProcessFileLock> locks =  new ConcurrentHashMap<>();
    /*package-testing*/ static void clearLocksProcessState() { 
        synchronized(sync) {
            try { 
                locks.forEach((path,lock)->lock.free());
                locks.clear(); 
            }
            catch (Exception ex) {
                // Shouldn't happen - trace and then ignore. 
                ex.printStackTrace();
            }
        }
    }

    private final Path filepath;
    private final FileChannel fileChannel;
    private FileLock fileLock;

    // Different variations for what to do when a lock can not be taken.
    private enum NoLockAction { EXCEPTION, RETURN, WAIT }

    /** Create a {@code ProcessFileLock} using the named file.
     *  Locks are JVM-wide; each filename is associated with one lock object.  
     */
    public static ProcessFileLock create(String filename) {
        try {
            Path abspath = Paths.get(filename).toRealPath();
            return locks.computeIfAbsent(abspath, ProcessFileLock::new);
        }
        catch (IOException e) { IO.exception(e); return null; }
    }
    
    /** Return the lock, unlocking the file if this process has it locked. */
    public static void release(ProcessFileLock lockFile) {
        if ( lockFile == null )
            return ;
        locks.remove(lockFile.getPath());
        lockFile.free();
    }
    
    /** Create the structure for a ProcessFileLock on file {@code filename}.
     * This does not take the lock
     * 
     * @see #lockEx()
     * @see #tryLock()
     * @see #unlock()
     */
    private ProcessFileLock(Path filename) {
        try {
            this.filepath = filename ;
            // Much the same as ... 
//            randomAccessFile = new RandomAccessFile(filename, "rw");
//            fileChannel = randomAccessFile.getChannel();
            // Quite heavy weight but only used to lock long-term objects.
            this.fileChannel = FileChannel.open(filename, CREATE, WRITE, READ, DSYNC);
            this.fileLock = null;
        }
        catch (NoSuchFileException | FileNotFoundException ex) {
            // The path does not name a possible file in an exists directory.
            throw new RuntimeIOException("No such file '"+filename+"'", ex);
        }
        catch (IOException ex) {
            throw new RuntimeIOException("Failed to open '"+filename+"'", ex); 
        }
    }

    /** Lock the file or throw {@link DBOpEnvException}.
     * 
     * @throws AlreadyLocked if the lock is already held by this process.
     */
    public void lockEx() {
        lockOperation(NoLockAction.EXCEPTION);
    }
    
    /** Lock the file or wait.
     * 
     * @throws AlreadyLocked if the lock is already held by this process.
     */
    public void lockWait() {
        lockOperation(NoLockAction.WAIT);
    }
    
    /** Lock a file, return true on success else false.
     * 
     * @throws AlreadyLocked if the lock is already held by this process.
     */
    public boolean tryLock() {
        return lockOperation(NoLockAction.RETURN);
    }

    /** Release the lock - this must be paired with a "lock" operation.
     * @throws IllegalStateException if the lock is not held by this process.
     */
    public void unlock() {
        synchronized(sync) {
            if ( fileLock == null )
                throw new IllegalStateException("unlock not paired with a lock call");
            try {
                fileLock.release();
            } catch (IOException ex) { throw new RuntimeIOException("Failed to unlock '"+filepath+"'", ex); }
        }
    }

    /** Return true if this process holds the lock. */
    public boolean isLockedHere() {
        if ( fileLock == null )
            return false;
        return fileLock.isValid();
    }
    
    public Path getPath() {
        return filepath;
    }
    
    // Release ProcessFileLock. Applications use ProcessFileLock.release(lock)
    private void free() {
        try { 
            if ( fileLock != null )
                fileLock.release();
            fileChannel.close();
            fileLock = null;
        } catch (IOException ex) { IO.exception(ex); }
    }
    
    /** Take the lock.
     * <p>
     * Write our PID into the file and return true if it succeeds.
     * <p>
     * Try to get the existing PID if it fails.
     */
    private boolean lockOperation(NoLockAction action) {
        synchronized(sync) {
            if ( fileLock != null )
                throw new AlreadyLocked("Failed to get a lock: file='"+filepath+"': Lock already held");
            
            try {
                fileLock = (action != NoLockAction.WAIT) ? fileChannel.tryLock() : fileChannel.lock(); 
                if ( fileLock == null ) {
                    switch(action) {
                        case EXCEPTION: {
                            // Read without the lock.
                            // This isn't perfect (synchronization issues across multiple processes)
                            // but it is only providing helpful information.
                            int pid = readProcessId(-99);
                            if ( pid >= 0 )
                                throw new DBOpEnvException("Failed to get a lock: file='"+filepath+"': held by process "+pid);
                            throw new DBOpEnvException("Failed to get a lock: file='"+filepath+"': failed to get the holder's process id");
                        }   
                        case RETURN:
                            return false ;
                        case WAIT:
                            throw new InternalError("FileChannel.lock returned null");
                    }
                }
                // Got the lock. Record our process id.
                int pid = ProcessUtils.getPid(-1);
                writeProcessId(pid);
                return true;
            } catch (IOException ex) {
                if ( action == NoLockAction.RETURN ) 
                    return false;
                throw new DBOpEnvException("Failed to get a lock: file='"+filepath+"'", ex);
            }
        }
    }
    
    // I/O for a process id. 
    /** Read the file to get a process id */
    private int readProcessId(int dft) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(128);
        fileChannel.position(0);
        int len = fileChannel.read(bb);
        fileChannel.position(0);
        if ( len == 0 )
            return dft;
        if ( len == 128 )
            // Too much.
            return dft;
        // Bug in Jena 3.3.0
        //byte[] b = ByteBufferLib.bb2array(bb, 0, len);
        bb.flip();
        byte[] b = new byte[len];
        bb.get(b);
        
        String pidStr = StrUtils.fromUTF8bytes(b);
        // Remove all leading and trailing (vertical and horizontal) whitespace.
        pidStr = pidStr.replaceAll("[\\s\\t\\n\\r]+$", "");
        pidStr = pidStr.replaceAll("^[\\s\\t\\n\\r]+", "");
        try {
            // Process id.
            return Integer.parseInt(pidStr);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            Log.warn(this, "Bad process id: file='"+filepath+"': read='"+pidStr+"'");
            return dft; 
        }
    }

    /** Write the process id to the file. */
    private void writeProcessId(int pid) throws IOException {
        byte b[] = StrUtils.asUTF8bytes(Integer.toString(pid)+"\n");
        fileChannel.truncate(0);
        int len = fileChannel.write(ByteBuffer.wrap(b));
        fileChannel.position(0);
    }
}
