/*
 * %W% %E%
 * 
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright notice, 
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * 
 * Neither the name of Oracle or the names of contributors may 
 * be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL 
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST 
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY 
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, 
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

/*
 * %W% %E%
 */

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.IOException;

/**
 * This Deadlock class demonstrates the capability of performing
 * deadlock detection programmatically within the application using
 * the java.lang.management API.
 *
 * See ThreadMonitor.java for the use of java.lang.management.ThreadMXBean
 * API.
 */
public class Deadlock {
    public static void main(String[] argv) {
        Deadlock dl = new Deadlock();
 
        // Now find deadlock
        ThreadMonitor monitor = new ThreadMonitor();
        boolean found = false;
        while (!found) {
            found = monitor.findDeadlock();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.exit(1);
            }
        }

        System.out.println("\nPress <Enter> to exit this Deadlock program.\n");
        waitForEnterPressed();
    }


    private CyclicBarrier barrier = new CyclicBarrier(6);
    public Deadlock() {
        DeadlockThread[] dThreads = new DeadlockThread[6];

        Monitor a = new Monitor("a");
        Monitor b = new Monitor("b");
        Monitor c = new Monitor("c");
        dThreads[0] = new DeadlockThread("MThread-1", a, b);
        dThreads[1] = new DeadlockThread("MThread-2", b, c);
        dThreads[2] = new DeadlockThread("MThread-3", c, a);

        Lock d = new ReentrantLock();
        Lock e = new ReentrantLock();
        Lock f = new ReentrantLock();

        dThreads[3] = new DeadlockThread("SThread-4", d, e);
        dThreads[4] = new DeadlockThread("SThread-5", e, f);
        dThreads[5] = new DeadlockThread("SThread-6", f, d);
                                                                                
        // make them daemon threads so that the test will exit
        for (int i = 0; i < 6; i++) {
            dThreads[i].setDaemon(true);
            dThreads[i].start();
        }
    }

    class DeadlockThread extends Thread {
        private Lock lock1 = null;
        private Lock lock2 = null;
        private Monitor mon1 = null;
        private Monitor mon2 = null;
        private boolean useSync;

        DeadlockThread(String name, Lock lock1, Lock lock2) {
            super(name);
            this.lock1 = lock1;
            this.lock2 = lock2;
            this.useSync = true;
        }
        DeadlockThread(String name, Monitor mon1, Monitor mon2) {
            super(name);
            this.mon1 = mon1;
            this.mon2 = mon2;
            this.useSync = false;
        }
        public void run() {
            if (useSync) {
                syncLock();
            } else {
                monitorLock();
            }
        }
        private void syncLock() {
            lock1.lock();
            try {
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.exit(1);
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                goSyncDeadlock();
            } finally {
                lock1.unlock();
            }
        }
        private void goSyncDeadlock() {
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
                System.exit(1);
            }
            lock2.lock();
            throw new RuntimeException("should not reach here.");
        }
        private void monitorLock() {
            synchronized (mon1) {
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.exit(1);
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                goMonitorDeadlock();
            }
        }
        private void goMonitorDeadlock() {
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
                System.exit(1);
            }
            synchronized (mon2) {
                throw new RuntimeException(getName() + " should not reach here.");
            }
        }
    }

    class Monitor {
        String name; 
        Monitor(String name) {
            this.name = name;
        }
    }

    private static void waitForEnterPressed() {
        try {
            boolean done = false;
            while (!done) {
                char ch = (char) System.in.read();
                if (ch<0||ch=='\n') {
                    done = true;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
