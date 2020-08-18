package io.forestframework.testsupport.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class FreePortFinderTest {
    @Test
    public void canBindToFoundPort() throws Exception {
        int port = FreePortFinder.findFreeLocalPort();
        ServerSocket serverSocket = new ServerSocket(port);
        assertTrue(serverSocket.isBound());
    }

    @Test
    public void overTheLimitPortAllocationShouldFail() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> FreePortFinder.findFreeLocalPort(FreePortFinder.MAX_PORT_NUMBER + 1));
    }

    @Test
    public void canBindToLocalHostToFoundPort() throws Exception {
        int port = FreePortFinder.findFreeLocalPort(InetAddress.getLocalHost());
        ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getLocalHost());
        assertTrue(serverSocket.isBound());
    }

    @Test
    public void canBindToMultipleHostsToFoundPort() throws Exception {
        int port = FreePortFinder.findFreeLocalPortOnAddresses(null, InetAddress.getLocalHost());
        ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getLocalHost());
        assertTrue(serverSocket.isBound());
    }

    @Test
    public void canBindToMultipleHostsMultipleAllocations() throws Exception {
        int port = FreePortFinder.findFreeLocalPortOnAddresses(null, InetAddress.getLocalHost());
        int nextPort = FreePortFinder.findFreeLocalPortOnAddresses(null, InetAddress.getLocalHost());
        assertTrue(nextPort > port);
    }
}
