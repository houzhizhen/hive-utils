package org.apache.commons.pool.impl;

import org.apache.commons.pool.PoolableObjectFactory;

public class TestGenericObjectPool {
    GenericObjectPool pool;
    public void setUp() throws Exception {

        pool = new GenericObjectPool(new SimpleFactory());
    }

    public void tearDown() throws Exception {
        pool.clear();
        pool.close();
        pool = null;
    }
    public void testConcurrentBorrowAndEvict() throws Exception {

        pool.setMaxActive(1);
        pool.addObject();

        for( int i=0; i<5000; i++) {
            ConcurrentBorrowAndEvictThread one =
                    new ConcurrentBorrowAndEvictThread(true);
            ConcurrentBorrowAndEvictThread two =
                    new ConcurrentBorrowAndEvictThread(false);

            one.start();
            two.start();
            one.join();
            two.join();

            pool.returnObject(one.obj);

            if (i % 10 == 0) {
                System.out.println(i/10);
            }
        }
    }

    private class ConcurrentBorrowAndEvictThread extends Thread {
        private boolean borrow;
        public Object obj;

        public ConcurrentBorrowAndEvictThread(boolean borrow) {
            this.borrow = borrow;
        }

        public void run() {
            try {
                if (borrow) {
                    obj = pool.borrowObject();
                } else {
                    pool.evict();
                }
            } catch (Exception e) { /* Ignore */}
        }
    }

    public static void main(String[] args) throws Exception {
        TestGenericObjectPool test = new TestGenericObjectPool();
        test.setUp();
        test.testConcurrentBorrowAndEvict();
        test.tearDown();
    }
    public class SimpleFactory implements PoolableObjectFactory {
        public SimpleFactory() {
            this(true);
        }
        public SimpleFactory(boolean valid) {
            this(valid,valid);
        }
        public SimpleFactory(boolean evalid, boolean ovalid) {
            evenValid = evalid;
            oddValid = ovalid;
        }
        public synchronized void setValid(boolean valid) {
            setEvenValid(valid);
            setOddValid(valid);
        }
        public synchronized void setEvenValid(boolean valid) {
            evenValid = valid;
        }
        public synchronized void setOddValid(boolean valid) {
            oddValid = valid;
        }
        public synchronized void setThrowExceptionOnPassivate(boolean bool) {
            exceptionOnPassivate = bool;
        }
        public synchronized void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }
        public synchronized void setDestroyLatency(long destroyLatency) {
            this.destroyLatency = destroyLatency;
        }
        public synchronized void setMakeLatency(long makeLatency) {
            this.makeLatency = makeLatency;
        }
        public synchronized void setValidateLatency(long validateLatency) {
            this.validateLatency = validateLatency;
        }
        public Object makeObject() {
            final long waitLatency;
            synchronized(this) {
                activeCount++;
                if (activeCount > maxActive) {
                    throw new IllegalStateException(
                            "Too many active instances: " + activeCount);
                }
                waitLatency = makeLatency;
            }
            if (waitLatency > 0) {
                doWait(waitLatency);
            }
            final int counter;
            synchronized(this) {
                counter = makeCounter++;
            }
            return String.valueOf(counter);
        }
        public void destroyObject(Object obj) throws Exception {
            final long waitLatency;
            final boolean hurl;
            synchronized(this) {
                waitLatency = destroyLatency;
                hurl = exceptionOnDestroy;
            }
            if (waitLatency > 0) {
                doWait(waitLatency);
            }
            synchronized(this) {
                activeCount--;
            }
            if (hurl) {
                throw new Exception();
            }
        }
        public boolean validateObject(Object obj) {
            final boolean validate;
            final boolean evenTest;
            final boolean oddTest;
            final long waitLatency;
            final int counter;
            synchronized(this) {
                validate = enableValidation;
                evenTest = evenValid;
                oddTest = oddValid;
                counter = validateCounter++;
                waitLatency = validateLatency;
            }
            if (waitLatency > 0) {
                doWait(waitLatency);
            }
            if (validate) {
                return counter%2 == 0 ? evenTest : oddTest;
            }
            else {
                return true;
            }
        }
        public void activateObject(Object obj) throws Exception {
            final boolean hurl;
            final boolean evenTest;
            final boolean oddTest;
            final int counter;
            synchronized(this) {
                hurl = exceptionOnActivate;
                evenTest = evenValid;
                oddTest = oddValid;
                counter = validateCounter++;
            }
            if (hurl) {
                if (!(counter%2 == 0 ? evenTest : oddTest)) {
                    throw new Exception();
                }
            }
        }
        public void passivateObject(Object obj) throws Exception {
            final boolean hurl;
            synchronized(this) {
                hurl = exceptionOnPassivate;
            }
            if (hurl) {
                throw new Exception();
            }
        }
        int makeCounter = 0;
        int validateCounter = 0;
        int activeCount = 0;
        boolean evenValid = true;
        boolean oddValid = true;
        boolean exceptionOnPassivate = false;
        boolean exceptionOnActivate = false;
        boolean exceptionOnDestroy = false;
        boolean enableValidation = true;
        long destroyLatency = 0;
        long makeLatency = 0;
        long validateLatency = 0;
        int maxActive = Integer.MAX_VALUE;

        public synchronized boolean isThrowExceptionOnActivate() {
            return exceptionOnActivate;
        }

        public synchronized void setThrowExceptionOnActivate(boolean b) {
            exceptionOnActivate = b;
        }

        public synchronized void setThrowExceptionOnDestroy(boolean b) {
            exceptionOnDestroy = b;
        }

        public synchronized boolean isValidationEnabled() {
            return enableValidation;
        }

        public synchronized void setValidationEnabled(boolean b) {
            enableValidation = b;
        }

        public synchronized int getMakeCounter() {
            return makeCounter;
        }

        private void doWait(long latency) {
            try {
                Thread.sleep(latency);
            } catch (InterruptedException ex) {
                // ignore
            }
        }
    }
}
