package com.epickrram.monitoring.network;

import java.net.SocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.LongConsumer;
import java.util.function.LongUnaryOperator;

import static java.util.concurrent.TimeUnit.SECONDS;

public final class Experiment
{
    private final LongUnaryOperator sendingDelayCalculator;
    private final LongConsumer transmitLatencyHandler;
    private final TimeUnit experimentRuntimeUnit;
    private final long experimentRuntimeDuration;
    private final SocketAddress address;

    public Experiment(
            final LongUnaryOperator sendingDelayCalculator,
            final LongConsumer transmitLatencyHandler,
            final SocketAddress address,
            final TimeUnit experimentRuntimeUnit,
            final long experimentRuntimeDuration)
    {
        this.sendingDelayCalculator = sendingDelayCalculator;
        this.transmitLatencyHandler = transmitLatencyHandler;
        this.address = address;
        this.experimentRuntimeUnit = experimentRuntimeUnit;
        this.experimentRuntimeDuration = experimentRuntimeDuration;
    }

    void execute()
    {
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);
        final MessageFactory messageFactory = new MessageFactory();
        final UnicastReceiver receiver = new UnicastReceiver(address, transmitLatencyHandler);
        final UnicastSender sender = new UnicastSender(address, sendingDelayCalculator, messageFactory::prepare);
        final KernelBufferDepthMonitor bufferDepthMonitor = new KernelBufferDepthMonitor(address);
        final SoftIrqHandlerTimeSqueezeMonitor timeSqueezeMonitor = new SoftIrqHandlerTimeSqueezeMonitor();

        executorService.submit(receiver::receiveLoop);
        executorService.submit(sender::sendLoop);

        executorService.scheduleAtFixedRate(() -> {
            bufferDepthMonitor.report();
            timeSqueezeMonitor.report();
        }, 1L, 1L, SECONDS);


        LockSupport.parkNanos(experimentRuntimeUnit.toNanos(experimentRuntimeDuration));

        executorService.shutdownNow();
        try
        {
            if(!executorService.awaitTermination(10, TimeUnit.SECONDS))
            {
                System.err.println("Failed to shutdown executor, threads did not exit.");
            }
        }
        catch (InterruptedException e)
        {
            System.err.println("Interrupted while waiting for threads to exit.");
        }
    }
}
