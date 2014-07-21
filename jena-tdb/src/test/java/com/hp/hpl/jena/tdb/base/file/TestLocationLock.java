package com.hp.hpl.jena.tdb.base.file;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.hp.hpl.jena.tdb.StoreConnection;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.sys.ProcessUtils;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/**
 * Tests for {@link LocationLock}
 */
public class TestLocationLock {

    private static boolean negativePidsTreatedAsAlive = false;
    
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    
    @BeforeClass
    public static void setup() {
        SystemTDB.DiskLocationMultiJvmUsagePrevention = true;
        negativePidsTreatedAsAlive = ProcessUtils.negativePidsTreatedAsAlive();
    }
    
    @AfterClass
    public static void teardown() {
        SystemTDB.DiskLocationMultiJvmUsagePrevention = false;
    }

    @Test
    public void location_lock_mem() {
        Location mem = Location.mem();
        LocationLock lock = mem.getLock();
        Assert.assertFalse(lock.canLock());
        Assert.assertFalse(lock.isLocked());
        Assert.assertFalse(lock.isOwned());
        Assert.assertFalse(lock.canObtain());
    }

    @Test
    public void location_lock_dir_01() {
        Location dir = new Location(tempDir.getRoot().getAbsolutePath());
        LocationLock lock = dir.getLock();
        Assert.assertTrue(lock.canLock());
        Assert.assertFalse(lock.isLocked());
        Assert.assertFalse(lock.isOwned());
        Assert.assertTrue(lock.canObtain());

        // Try to obtain the lock
        lock.obtain();
        Assert.assertTrue(lock.isLocked());
        Assert.assertTrue(lock.isOwned());

        // Release the lock
        lock.release();
        Assert.assertFalse(lock.isLocked());
        Assert.assertFalse(lock.isOwned());
    }

    @Test
    public void location_lock_dir_02() throws IOException {
        Assume.assumeTrue(negativePidsTreatedAsAlive);

        Location dir = new Location(tempDir.getRoot().getAbsolutePath());
        LocationLock lock = dir.getLock();
        Assert.assertTrue(lock.canLock());
        Assert.assertFalse(lock.isLocked());
        Assert.assertFalse(lock.isOwned());
        Assert.assertTrue(lock.canObtain());

        // Write a fake PID to the lock file
        BufferedWriter writer = new BufferedWriter(new FileWriter(
                dir.getPath("tdb.lock")));
        writer.write(Integer.toString(-1234)); // Fake PID that would never be
                                               // valid
        writer.close();
        Assert.assertTrue(lock.isLocked());
        Assert.assertFalse(lock.isOwned());
        Assert.assertFalse(lock.canObtain());
    }

    @Test
    public void location_lock_dir_03() {
        Location dir = new Location(tempDir.getRoot().getAbsolutePath());
        LocationLock lock = dir.getLock();
        Assert.assertTrue(lock.canLock());
        Assert.assertFalse(lock.isLocked());
        Assert.assertFalse(lock.isOwned());
        Assert.assertTrue(lock.canObtain());

        // Creating a StoreConnection on the location will obtain the lock
        StoreConnection.make(dir);
        Assert.assertTrue(lock.isLocked());
        Assert.assertTrue(lock.isOwned());
        Assert.assertTrue(lock.canObtain());

        // Releasing the connection releases the lock
        StoreConnection.release(dir);
        Assert.assertFalse(lock.isLocked());
        Assert.assertFalse(lock.isOwned());
        Assert.assertTrue(lock.canObtain());
    }

    @Test(expected = TDBException.class)
    public void location_lock_dir_error_01() throws IOException {
        Assume.assumeTrue(negativePidsTreatedAsAlive);

        Location dir = new Location(tempDir.getRoot().getAbsolutePath());
        LocationLock lock = dir.getLock();
        Assert.assertTrue(lock.canLock());
        Assert.assertFalse(lock.isLocked());
        Assert.assertFalse(lock.isOwned());
        Assert.assertTrue(lock.canObtain());

        // Write a fake PID to the lock file
        BufferedWriter writer = new BufferedWriter(new FileWriter(
                dir.getPath("tdb.lock")));
        writer.write(Integer.toString(-1234)); // Fake PID that would never be
                                               // valid
        writer.close();
        Assert.assertTrue(lock.isLocked());
        Assert.assertFalse(lock.isOwned());

        // Attempting to obtain the lock should now error
        Assert.assertFalse(lock.canObtain());
        lock.obtain();
    }

    @Test(expected = TDBException.class)
    public void location_lock_dir_error_02() throws IOException {
        Assume.assumeTrue(negativePidsTreatedAsAlive);

        Location dir = new Location(tempDir.getRoot().getAbsolutePath());
        LocationLock lock = dir.getLock();
        Assert.assertTrue(lock.canLock());
        Assert.assertFalse(lock.isLocked());
        Assert.assertFalse(lock.isOwned());
        Assert.assertTrue(lock.canObtain());

        // Write a fake PID to the lock file
        BufferedWriter writer = new BufferedWriter(new FileWriter(
                dir.getPath("tdb.lock")));
        writer.write(Integer.toString(-1234)); // Fake PID that would never be
                                               // valid
        writer.close();
        Assert.assertTrue(lock.isLocked());
        Assert.assertFalse(lock.isOwned());

        // Attempting to release a lock we don't own should error
        Assert.assertFalse(lock.canObtain()); // Returns true on Jenkins
        lock.release();
    }

    @Test(expected = TDBException.class)
    public void location_lock_dir_error_03() throws IOException {
        Assume.assumeTrue(negativePidsTreatedAsAlive);
        
        Location dir = new Location(tempDir.getRoot().getAbsolutePath());
        LocationLock lock = dir.getLock();
        Assert.assertTrue(lock.canLock());
        Assert.assertFalse(lock.isLocked());
        Assert.assertFalse(lock.isOwned());
        Assert.assertTrue(lock.canObtain());

        // Write a fake PID to the lock file
        BufferedWriter writer = new BufferedWriter(new FileWriter(
                dir.getPath("tdb.lock")));
        writer.write(Integer.toString(-1234)); // Fake PID that would never be
                                               // valid
        writer.close();
        Assert.assertTrue(lock.isLocked());
        Assert.assertFalse(lock.isOwned());

        // Attempting to create a connection on this location should error
        StoreConnection.make(dir); // Doesn't error on Jenkins
    }
}
