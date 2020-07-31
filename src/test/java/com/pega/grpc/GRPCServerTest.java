
package com.pega.grpc;

import com.pega.grpc.grpctest.GRPCTestGrpc;
import com.pega.grpc.grpctest.HttpReq;
import com.pega.grpc.grpctest.HttpRes;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class GRPCServerTest {
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private GRPCServer server;
    private ManagedChannel inProcessChannel;

    @Before
    public void setUp() throws Exception {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        server = new GRPCServer(
                InProcessServerBuilder.forName(serverName).directExecutor(), 0);
        server.start();
        // Create a client channel and register for automatic graceful shutdown.
        inProcessChannel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void getPod() {
        GRPCTestGrpc.GRPCTestBlockingStub stub = GRPCTestGrpc.newBlockingStub(inProcessChannel);

        HttpReq req = HttpReq.newBuilder().build();
        HttpRes res = stub.getPodName(req);

        assertEquals("UNKNOWN", res.getPodname());
    }
}
