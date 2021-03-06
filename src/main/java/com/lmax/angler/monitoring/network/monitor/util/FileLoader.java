package com.lmax.angler.monitoring.network.monitor.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Utility for repeated reads of a file using a ByteBuffer to avoid allocating unnecessary objects.
 */
public final class FileLoader
{
    private final Path path;
    private ByteBuffer buffer;
    private FileChannel fileChannel;

    public FileLoader(final Path path, final int initialBufferCapacity)
    {
        this.path = path;
        buffer = ByteBuffer.allocateDirect(initialBufferCapacity);
    }

    /**
     * Visit the data contained within the specified path, passing it to
     * the supplied file handler as it is seen.
     */
    public void run(final FileHandler fileHandler)
    {
        try
        {
            if (fileChannel == null)
            {
                fileChannel = FileChannel.open(path, StandardOpenOption.READ);
            }
            else
            {
                fileChannel.position(0);
            }

            int read = fileChannel.read(buffer);
            while (read > 0)
            {
                buffer.flip();
                fileHandler.handleData(buffer, 0, read);
                buffer.clear();

                read = fileChannel.read(buffer);
            }
            fileHandler.noFurtherData();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
