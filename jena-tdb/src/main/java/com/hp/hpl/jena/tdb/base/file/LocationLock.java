package com.hp.hpl.jena.tdb.base.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.io.IO;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/**
 * Represents a lock on a TDB location
 * 
 * @author rvesse
 * 
 */
public class LocationLock {
    private static final int NO_OWNER = 0;
    private static final String LOCK_FILENAME = "tdb.lock";

    private static int myPid = -1;

    /**
     * Tries to get the PID of the current process
     * 
     * @return PID of current process or zero if unable to determine PID
     */
    private static int getPid() {
        if (myPid != -1)
            return myPid;

        String runtimeBeanName = ManagementFactory.getRuntimeMXBean().getName();
        if (runtimeBeanName == null) {
            return useFallbackPid();
        }

        // Bean name will have format PID@hostname so we try to parse the PID
        // portion
        int index = runtimeBeanName.indexOf("@");
        if (index < 0)
            return useFallbackPid();
        try {
            // Parse and cache for future reuse
            String pidData = runtimeBeanName.substring(0, index);
            myPid = Integer.parseInt(pidData);
            return myPid;
        } catch (NumberFormatException e) {
            // Invalid PID
            return useFallbackPid();
        }
    }

    private static int useFallbackPid() {
        // In the case where we can't determine our PID then treat ourselves as
        // no owner and cache for future use
        myPid = NO_OWNER;
        return myPid;
    }

    private Location location;

    public LocationLock(Location location) {
        if (location == null)
            throw new NullPointerException("Location cannot be null");
        this.location = location;
    }

    /**
     * Gets whether the location is lockable i.e. is it an on-disk location
     * where we can use a lock file to prevent multi-process access and the
     * potential data corruption that ensues
     * 
     * @return True if the location is lockable
     */
    public boolean canLock() {
        return !location.isMem();
    }

    /**
     * Gets whether the location is currently locked, use
     * 
     * @return
     */
    public boolean isLocked() {
        // Memory locations are never considered locked
        if (location.isMem())
            return false;

        return getOwner() != NO_OWNER;
    }

    /**
     * Gets whether the lock is owned by the current process
     * 
     * @return True if the location is locked and owned by the process, false
     *         otherwise
     */
    public boolean isOwned() {
        // Memory locations are never considered locked
        if (location.isMem())
            return false;

        int owner = getOwner();
        if (owner == NO_OWNER)
            return false;

        return owner == getPid();
    }

    /**
     * Gets the current owner of this locations lock.
     * 
     * @return Process ID of owner if locked, zero if the location cannot be
     *         locked or not currently locked
     */
    public int getOwner() {
        // Memory locations are never considered locked
        if (location.isMem())
            return NO_OWNER;

        File lockFile = getLockFile();
        if (lockFile.exists()) {
            checkLockFileForRead(lockFile);
            // Can read lock owner from the file
            try {
                String rawLockInfo = IO.readWholeFileAsUTF8(lockFile.getAbsolutePath());
                int owner = Integer.parseInt(rawLockInfo);
                return owner;
            } catch (IOException e) {
                throw new FileException("Unable to check TDB lock owner due to an IO error reading the lock file", e);
            } catch (NumberFormatException e) {
                throw new FileException("Unable to check TDB lock owner as the lock file contains invalid data", e);
            }
        } else {
            // No lock file so nobody owns the lock currently
            return NO_OWNER;
        }
    }

    /**
     * Gets whether the current JVM can obtain the lock
     * 
     * @return True if the lock can be obtained (or is already held), false
     *         otherwise
     */
    public boolean canObtain() {
        // Memory locations cannot be locked
        if (location.isMem())
            return false;

        int owner = this.getOwner();
        int pid = getPid();

        if (owner == NO_OWNER) {
            // Can obtain provided we have a valid PID
            if (pid != NO_OWNER)
                return true;
        } else if (owner == pid) {
            // Already held
            return true;
        }

        // Owned by another process, only obtainable if other process is dead
        if (!isAlive(owner))
            return true;

        // Otherwise not obtainable
        return false;
    }

    /**
     * Obtains the lock in order to prevent other JVMs using the location
     */
    public void obtain() {
        // Memory locations cannot be locked so nothing to do
        if (location.isMem())
            return;

        // Get current owner
        int owner = this.getOwner();
        if (owner == NO_OWNER) {
            // No owner currently so try to obtain the lock
            int pid = getPid();
            if (pid == NO_OWNER) {
                // In the case where we cannot obtain our PID then we cannot
                // obtain a lock
                return;
            }

            takeLock(pid);
        } else if (owner == getPid()) {
            // We already own the lock so nothing to do
        } else {
            // Someone other process potentially owns the lock on this location
            // Check if the owner is alive
            if (isAlive(owner))
                throw new TDBException(
                        "The location "
                                + location.getDirectoryPath()
                                + " is currently locked by PID "
                                + owner
                                + ".  TDB databases do not permit concurrent usage across JVMs so in order to prevent corruption you cannot open this location from the JVM that does not own the lock for the dataset");

            // Otherwise the previous owner is dead so we can take the lock
            takeLock(getPid());
        }
    }

    private void takeLock(int pid) {
        File lockFile = getLockFile();
        checkLockFileForWrite(lockFile);
        try {
            // Write our PID to the lock file
            BufferedWriter writer = new BufferedWriter(new FileWriter(lockFile));
            writer.write(Integer.toString(pid));
            writer.close();
        } catch (IOException e) {
            throw new TDBException("Failed to obtain a lock on the location " + location.getDirectoryPath(), e);
        }
    }

    /**
     * Releases the lock so that other JVMs can use the location
     */
    public void release() {
        // Memory locations cannot be locked so nothing to do
        if (location.isMem())
            return;

        int owner = this.getOwner();

        // Nobody owned the lock so nothing to do
        if (owner == NO_OWNER)
            return;

        // Some other process owns the lock so we can't release it
        if (owner != getPid())
            throw new TDBException("Cannot release the lock on location " + location.getDirectoryPath()
                    + " since this process does not own the lock");

        File lockFile = getLockFile();

        // No lock file exists so nothing to do
        if (!lockFile.exists())
            return;

        // Try and delete the lock file thereby releasing the lock
        if (!lockFile.delete())
            throw new TDBException("Failed to release the lock on location " + location.getDirectoryPath()
                    + ", it may be necessary to manually remove the lock file");

    }

    /**
     * Gets the lock file
     * 
     * @return Lock file
     */
    private File getLockFile() {
        return new File(location.getPath(LOCK_FILENAME));
    }

    /**
     * Checks if a lock file is valid throwing an exception if it is not
     * 
     * @param lockFile
     *            Lock file
     * @exception FileException
     *                Thrown if the lock file is invalid
     */
    private void checkLockFileForRead(File lockFile) {
        if (!lockFile.exists())
            return;

        if (!lockFile.isFile() || !lockFile.canRead()) {
            // Unable to read lock owner because it isn't a file or we don't
            // have read permission
            throw new FileException(
                    "Unable to check TDB lock owner for this location since the expected lock file is not a file/not readable");
        }
    }

    /**
     * Checks if a lock file is valid throwing an exception if it is not
     * 
     * @param lockFile
     *            Lock file
     * @exception FileException
     *                Thrown if the lock file is invalid
     */
    private void checkLockFileForWrite(File lockFile) {
        if (!lockFile.exists())
            return;

        if (!lockFile.isFile() || !lockFile.canWrite()) {
            // TODO What about read only file systems? Though I suspect TDB will
            // fail elsewhere in that case

            // Unable to read lock owner because it isn't a file or we don't
            // have read permission
            throw new FileException(
                    "Unable to check TDB lock owner for this location since the expected lock file is not a file/not writable");
        }
    }

    private static boolean isAlive(int pid) {
        String pidStr = Integer.toString(pid);
        Process p;
        try {
            if (SystemTDB.isWindows) {
                // Use the Windows tasklist utility
                ProcessBuilder builder = new ProcessBuilder("tasklist", "/FI", "PID eq " + pidStr);
                builder.redirectErrorStream(true);
                p = builder.start();
            } else {
                // Use the ps utility
                ProcessBuilder builder = new ProcessBuilder("ps", "-p", pidStr);
                builder.redirectErrorStream(true);
                p = builder.start();
            }

            // Run and read data from the process
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            List<String> data = new ArrayList<String>();
            String line = null;
            while ((line = reader.readLine()) != null) {
                data.add(line);
            }
            reader.close();

            // Expect a line to contain the PID to indicate the process is
            // alive
            for (String lineData : data) {
                if (lineData.contains(pidStr))
                    return true;
            }

            // Did not find any lines mentioning the PID so we can safely
            // assume that process is dead
            return false;
        } catch (IOException e) {
            // If any error running the process to check for the live process
            // then our check failed and for safety we assume the process is
            // alive

            // TODO Issue a warning here
            return true;
        }
    }
}
