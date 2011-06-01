package test ;
import java.nio.ByteBuffer;

import org.junit.Test;


public class ByteBufferTest {
    //@Test
    public void direct0() {
        testDirect("direct0");
    }
    @Test
    public void heap1() {
        testHeap("heap1");
    }

    @Test
    public void direct1() {
        testDirect("direct1");
    }
    
    @Test
    public void heap2() {
        testHeap("heap2");
    }

    @Test
    public void direct2() {
        testDirect("direct2");
    }

    private void testHeap(String name) {
        ByteBuffer buf = ByteBuffer.allocate(2048);
        long startTime = System.currentTimeMillis();
        for (int i = 1048576; i > 0; i --) {
            buf.clear();
            while (buf.hasRemaining()) {
                buf.getInt(buf.position());
                buf.putInt((byte) 0);
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println(name + ": " + (endTime - startTime));
    }
    
    private void testDirect(String name) {
        ByteBuffer buf = ByteBuffer.allocateDirect(2048);
        long startTime = System.currentTimeMillis();
        for (int i = 1048576; i > 0; i --) {
            buf.clear();
            while (buf.hasRemaining()) {
                buf.getInt(buf.position());
                buf.putInt((byte) 0);
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println(name + ": " + (endTime - startTime));
    }

}