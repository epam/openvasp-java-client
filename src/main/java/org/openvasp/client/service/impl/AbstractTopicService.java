package org.openvasp.client.service.impl;

import lombok.NonNull;
import lombok.val;
import org.openvasp.client.api.whisper.WhisperApi;
import org.openvasp.client.api.whisper.WhisperIOException;
import org.openvasp.client.api.whisper.model.ShhMessage;
import org.openvasp.client.api.whisper.model.ShhNewMessageFilterRequest;
import org.openvasp.client.api.whisper.model.ShhPostRequest;
import org.openvasp.client.common.Tuple2;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.model.EncryptionType;
import org.openvasp.client.model.Topic;
import org.slf4j.Logger;

import java.io.InterruptedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * @author Olexandr_Bilovol@epam.com
 */
abstract class AbstractTopicService implements AutoCloseable {

    private static final BigInteger TTL = BigInteger.valueOf(60);
    private static final BigInteger POW_TIME = BigInteger.valueOf(20);
    private static final BigDecimal POW_TARGET = BigDecimal.valueOf(2.01);

    private static final long POLL_TIMEOUT = 1000;

    private static final int RUNNING = 1;
    private static final int SHUTDOWN = 2;
    private static final int TERMINATED = 3;

    private final WhisperApi whisper;

    private final Thread pollingThread;
    private final AtomicInteger state = new AtomicInteger(RUNNING);
    private final ReentrantLock stateCtl = new ReentrantLock();
    private final Condition termination = stateCtl.newCondition();

    AbstractTopicService(
            @NonNull final WhisperApi whisper,
            @NonNull final String pollingThreadLoopName) {

        this.whisper = whisper;
        this.pollingThread = new Thread(this::topicPollingLoop, pollingThreadLoopName);
    }

    @Override
    public void close() {
        if (!checkState(TERMINATED)) {
            pollingThread.interrupt();
        }
    }

    public void shutdown() {
        stateCtl.lock();
        try {
            state.compareAndSet(RUNNING, SHUTDOWN);
        } finally {
            stateCtl.unlock();
        }
    }

    public boolean waitForTermination(final long msTimeout) {
        stateCtl.lock();
        try {
            return state.get() == TERMINATED || termination.await(msTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            return false;
        } finally {
            stateCtl.unlock();
        }
    }

    abstract Logger log();

    void send(
            @NonNull final Topic topic,
            @NonNull final EncryptionType encType,
            @NonNull final String key,
            @NonNull final String payload) {

        switch (encType) {
            case ASSYMETRIC: {
                val request = ShhPostRequest.builder()
                        .pubKey(key)
                        .topic(topic.getData())
                        .payload(payload)
                        .ttl(TTL)
                        .powTarget(POW_TARGET)
                        .powTime(POW_TIME)
                        .build();
                whisper.post(request);
                break;
            }

            case SYMMETRIC: {
                val symKeyId = whisper.addSymKey(key);
                val request = ShhPostRequest.builder()
                        .symKeyId(symKeyId)
                        .topic(topic.getData())
                        .payload(payload)
                        .ttl(TTL)
                        .powTarget(POW_TARGET)
                        .powTime(POW_TIME)
                        .build();
                whisper.post(request);
                break;
            }
        }
    }

    void startPolling() {
        pollingThread.start();
    }

    Tuple2<String, String> createWhisperMessageFilter(
            @NonNull final Topic topic,
            @NonNull final EncryptionType encType,
            @NonNull final String key) {

        final String keyId, filterId;
        switch (encType) {
            case ASSYMETRIC: {
                keyId = whisper.addPrivateKey(key);
                filterId = whisper.newMessageFilter(ShhNewMessageFilterRequest.builder()
                        .privateKeyId(keyId)
                        .topics(Collections.singletonList(topic.getData()))
                        .build());
                break;
            }

            case SYMMETRIC: {
                keyId = whisper.addSymKey(key);
                filterId = whisper.newMessageFilter(ShhNewMessageFilterRequest.builder()
                        .symKeyId(keyId)
                        .topics(Collections.singletonList(topic.getData()))
                        .build());
                break;
            }

            default:
                // encType cannot be null, so it is impossible to get there
                // But because the Java compiler does not know that, it requires
                // initialization of final 'keyId' and 'filterId'.
                // The exception is just a workaround for the situation
                throw new VaspException("It's impossible to get here");
        }

        return Tuple2.of(keyId, filterId);
    }

    void deleteWhisperMessageFilter(
            @NonNull final EncryptionType encType,
            @NonNull final String keyId,
            @NonNull final String filterId) {

        if (filterId != null) {
            whisper.deleteMessageFilter(filterId);
        }

        if (keyId != null) {
            switch (encType) {
                case ASSYMETRIC:
                    whisper.deleteKeyPair(keyId);
                    break;

                case SYMMETRIC:
                    whisper.deleteSymKey(keyId);
                    break;
            }
        }
    }

    Stream<ShhMessage> getWhisperMessages(@NonNull final String filterId) {
        return whisper.getFilterMessages(filterId).stream();
    }

    abstract void pollMessages();

    private void topicPollingLoop() {
        log().debug("{} started", Thread.currentThread().getName());
        try {
            while (!Thread.interrupted() && checkState(RUNNING)) {
                pollMessages();
                Thread.sleep(POLL_TIMEOUT);
            }
        } catch (InterruptedException ex) {
            // Do nothing, just exit
        } catch (WhisperIOException ex) {
            val cause = ex.getCause();
            if (!(cause instanceof InterruptedIOException)) {
                log().error(formatErrorMessage(), ex);
            }
        } catch (Exception ex) {
            log().error(formatErrorMessage(), ex);
        } finally {
            setTerminatedState();
            log().debug("{} terminated", Thread.currentThread().getName());
        }
    }

    private boolean checkState(final int expectedState) {
        stateCtl.lock();
        try {
            return state.get() == expectedState;
        } finally {
            stateCtl.unlock();
        }
    }

    private void setTerminatedState() {
        stateCtl.lock();
        try {
            state.set(TERMINATED);
            termination.signalAll();
        } finally {
            stateCtl.unlock();
        }
    }

    private String formatErrorMessage() {
        return String.format("Error in %s.topicPollingLoop", getClass().getSimpleName());
    }

}
