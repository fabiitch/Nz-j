package com.nz.nzj.thread.retry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryHandleTest {

    @Test
    void closeShouldDelegateToCancel() throws Exception {
        TestRetryHandle handle = new TestRetryHandle();

        handle.close();

        assertEquals(1, handle.cancelCallCount);
        assertTrue(handle.isCancelled());
    }

    private static final class TestRetryHandle implements RetryHandle {
        private int cancelCallCount;
        private boolean cancelled;

        @Override
        public void cancel() {
            cancelCallCount++;
            cancelled = true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }
    }
}
