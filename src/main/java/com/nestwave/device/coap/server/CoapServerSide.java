/******************************************************************************
 * Copyright 2022 - NESTWAVE SAS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *****************************************************************************/
package com.nestwave.device.coap.server;

import com.nestwave.device.coap.resource.CoapAssistanceResource;
import com.nestwave.device.coap.resource.CoapNavigationResource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.Resource;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Slf4j
@Component
public class CoapServerSide {
    private final CoapNavigationResource coapNavigationResource;
    private final CoapAssistanceResource coapAssistanceResource;

    public CoapServerSide(CoapNavigationResource coapNavigationResource, CoapAssistanceResource coapAssistanceResource){
        this.coapNavigationResource = coapNavigationResource;
        this.coapAssistanceResource = coapAssistanceResource;
    }


    public void startServer() {
        CoapServer server = new CoapServer(populateNetworkConfig());
        CoapNavigationResource.NavigationResource navigationResource = coapNavigationResource.new NavigationResource(false);
        CoapAssistanceResource.GpsTime gpsTimeAssistanceResource = coapAssistanceResource.new GpsTime();

        server.add(navigationResource);
        server.add(gpsTimeAssistanceResource);
        ServerSideMessageDeliverer deliverer = new ServerSideMessageDeliverer(server.getRoot(), coapNavigationResource, coapAssistanceResource);
        server.setMessageDeliverer(deliverer);
        server.start();
    }

    private void executeResource (final Exchange exchange, final Resource resource) {
        Executor executor = resource.getExecutor ();
        if (executor != null) {
            executor.execute (new Runnable () {

                public void run () {
                    resource.handleRequest (exchange);
                }
            });
        } else {
            resource.handleRequest (exchange);
        }
    }

    private NetworkConfig populateNetworkConfig() {
        NetworkConfig net = NetworkConfig.createStandardWithoutFile()
                .setInt(NetworkConfig.Keys.HEALTH_STATUS_INTERVAL, 0)
                .setInt(NetworkConfig.Keys.ACK_TIMEOUT, 1000)
                .setInt(NetworkConfig.Keys.UDP_CONNECTOR_SEND_BUFFER, 0)
                .setDouble(NetworkConfig.Keys.ACK_TIMEOUT_SCALE, 2.0)
                .setBoolean(NetworkConfig.Keys.USE_RANDOM_MID_START, true)
                .setBoolean(NetworkConfig.Keys.BLOCKWISE_STRICT_BLOCK2_OPTION, false)
                .setLong(NetworkConfig.Keys.MAX_ACTIVE_PEERS, 150000)
                .setInt(NetworkConfig.Keys.PROTOCOL_STAGE_THREAD_COUNT, 12)
                .setLong(NetworkConfig.Keys.BLOCKWISE_STATUS_LIFETIME, 300000)
                .setLong(NetworkConfig.Keys.MAX_RESOURCE_BODY_SIZE, 8192)
                .setInt(NetworkConfig.Keys.HTTP_CACHE_SIZE, 32)
                .setInt(NetworkConfig.Keys.UDP_CONNECTOR_DATAGRAM_SIZE, 2048)
                .setInt(NetworkConfig.Keys.UDP_CONNECTOR_RECEIVE_BUFFER, 0)
                .setLong(NetworkConfig.Keys.MAX_TRANSMIT_WAIT, 93000)
                .setInt(NetworkConfig.Keys.NOTIFICATION_REREGISTRATION_BACKOFF, 2000)
                .setString(NetworkConfig.Keys.DEDUPLICATOR, "DEDUPLICATOR_MARK_AND_SWEEP")
                .setInt(NetworkConfig.Keys.TCP_NUMBER_OF_BULK_BLOCKS,2)
                .setInt(NetworkConfig.Keys.COAP_PORT,5683)
                .setString(NetworkConfig.Keys.MID_TRACKER, "GROUPED")
                .setBoolean(NetworkConfig.Keys.BLOCKWISE_ENTITY_TOO_LARGE_AUTO_FAILOVER, true)
                .setInt(NetworkConfig.Keys.NETWORK_STAGE_RECEIVER_THREAD_COUNT, 1)
                .setInt(NetworkConfig.Keys.COAP_SECURE_PORT, 5684)
                .setLong(NetworkConfig.Keys.HTTP_CACHE_RESPONSE_MAX_AGE, 86400)
                .setBoolean(NetworkConfig.Keys.USE_MESSAGE_OFFLOADING, false)
                .setLong(NetworkConfig.Keys.MULTICAST_BASE_MID, 65000)
                .setInt(NetworkConfig.Keys.HTTP_SERVER_SOCKET_BUFFER_SIZE, 8192)
                .setLong(NetworkConfig.Keys.EXCHANGE_LIFETIME, 247000)
                .setLong(NetworkConfig.Keys.TLS_HANDSHAKE_TIMEOUT, 10000)
                .setBoolean(NetworkConfig.Keys.DEDUPLICATOR_AUTO_REPLACE, true)
                .setInt(NetworkConfig.Keys.TCP_CONNECTION_IDLE_TIMEOUT, 10)
                .setInt(NetworkConfig.Keys.LEISURE, 5000)
                .setInt(NetworkConfig.Keys.HTTP_PORT, 8080)
                .setInt(NetworkConfig.Keys.DTLS_CONNECTION_ID_LENGTH, 10)
                .setLong(NetworkConfig.Keys.NOTIFICATION_CHECK_INTERVAL_TIME, 86400000)
                .setString(NetworkConfig.Keys.CONGESTION_CONTROL_ALGORITHM, "Cocoa")
                .setString(NetworkConfig.Keys.RESPONSE_MATCHING, "STRICT")
                .setInt(NetworkConfig.Keys.BLOCKWISE_STATUS_INTERVAL, 5000)
                .setInt(NetworkConfig.Keys.MID_TRACKER_GROUPS, 16)
                .setInt(NetworkConfig.Keys.TOKEN_SIZE_LIMIT,8)
                .setInt(NetworkConfig.Keys.NETWORK_STAGE_SENDER_THREAD_COUNT, 1)
                .setInt(NetworkConfig.Keys.TCP_WORKER_THREADS, 1)
                .setLong(NetworkConfig.Keys.SECURE_SESSION_TIMEOUT, 86400)
                .setLong(NetworkConfig.Keys.TCP_CONNECT_TIMEOUT, 10000)
                .setInt(NetworkConfig.Keys.MAX_RETRANSMIT, 4)
                .setInt(NetworkConfig.Keys.MAX_MESSAGE_SIZE, 1024)
                .setDouble(NetworkConfig.Keys.ACK_RANDOM_FACTOR, 1.5)
                .setInt(NetworkConfig.Keys.NSTART, 1)
                .setInt(NetworkConfig.Keys.PEERS_MARK_AND_SWEEP_MESSAGES, 64)
                .setLong(NetworkConfig.Keys.MAX_LATENCY, 100000)
                .setDouble(NetworkConfig.Keys.PROBING_RATE, 1.0)
                .setBoolean(NetworkConfig.Keys.USE_CONGESTION_CONTROL, false)
                .setLong(NetworkConfig.Keys.MAX_SERVER_RESPONSE_DELAY, 250000)
                .setLong(NetworkConfig.Keys.CROP_ROTATION_PERIOD, 247000)
                .setInt(NetworkConfig.Keys.MAX_PEER_INACTIVITY_PERIOD, 600)
                .setLong(NetworkConfig.Keys.UDP_CONNECTOR_OUT_CAPACITY, 2147483647)
                .setLong(NetworkConfig.Keys.UDP_CONNECTOR_OUT_CAPACITY, 30000)
                .setInt(NetworkConfig.Keys.PREFERRED_BLOCK_SIZE, 1024)
                .setLong(NetworkConfig.Keys.NON_LIFETIME, 145000)
                .setInt(NetworkConfig.Keys.NOTIFICATION_CHECK_INTERVAL_COUNT, 100)
                .setInt(NetworkConfig.Keys.MARK_AND_SWEEP_INTERVAL, 10000);

        return net;
    }

}
