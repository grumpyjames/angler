package com.epickrram.monitoring.network.monitor.util;

import org.agrona.collections.LongHashSet;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public final class CurrentProcessSocketInodeRetriever implements Consumer<LongHashSet>
{
    private static final String SOCKET_PREFIX = "socket:[";
    private static final int SOCKET_PREFIX_LENGTH = SOCKET_PREFIX.length();
    private static final long NOT_A_SOCKET = Long.MIN_VALUE;

    @Override
    public void accept(final LongHashSet targetForOwnedSocketInodes)
    {
        targetForOwnedSocketInodes.clear();
        try
        {
            Files.list(Paths.get("/proc/self/fd")).
                    filter(Files::isSymbolicLink).
                    mapToLong(CurrentProcessSocketInodeRetriever::socketLinkInode).
                    filter(inode -> inode != NOT_A_SOCKET).
                    forEach(targetForOwnedSocketInodes::add);
        }
        catch(final IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private static long socketLinkInode(final Path validSymbolicLink)
    {
        try
        {
            final Path link = Files.readSymbolicLink(validSymbolicLink);
            final String linkName = link.getFileName().toString();
            if(linkName.startsWith(SOCKET_PREFIX))
            {
                return Long.parseLong(linkName.substring(SOCKET_PREFIX_LENGTH, linkName.length() - 1));
            }
            return NOT_A_SOCKET;
        }
        catch (IOException e)
        {
            return NOT_A_SOCKET;
        }
    }
}
