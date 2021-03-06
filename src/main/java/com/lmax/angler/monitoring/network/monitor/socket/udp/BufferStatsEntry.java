package com.lmax.angler.monitoring.network.monitor.socket.udp;

import com.lmax.angler.monitoring.network.monitor.socket.SocketIdentifier;

/**
 * Value object.
 */
final class BufferStatsEntry
{
    private long socketIdentifier;
    private long receiveQueueDepth;
    private long drops;
    private long inode;

    long getSocketIdentifier()
    {
        return socketIdentifier;
    }

    long getSocketInstanceIndentifier()
    {
        return SocketIdentifier.overlayInode(socketIdentifier, inode);
    }

    void setSocketIdentifier(final long socketIdentifier)
    {
        this.socketIdentifier = socketIdentifier;
    }

    void setInode(final long inode)
    {
        this.inode = inode;
    }

    long getReceiveQueueDepth()
    {
        return receiveQueueDepth;
    }

    void setReceiveQueueDepth(final long receiveQueueDepth)
    {
        this.receiveQueueDepth = receiveQueueDepth;
    }

    long getDrops()
    {
        return drops;
    }

    void setDrops(final long drops)
    {
        this.drops = drops;
    }

    long getInode()
    {
        return inode;
    }

    void reset()
    {
        socketIdentifier = 0L;
        receiveQueueDepth = 0L;
        drops = 0L;
        inode = 0L;
    }
}
