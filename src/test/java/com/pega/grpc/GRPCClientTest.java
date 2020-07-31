
package com.pega.grpc;

import com.pega.grpc.grpctest.GRPCTestGrpc;
import com.pega.grpc.grpctest.HttpReq;
import com.pega.grpc.grpctest.HttpRes;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.grpc.util.MutableHandlerRegistry;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(JUnit4.class)
public class GRPCClientTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private static final String POD_NAME = "pod123";
    private final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();

    private GRPCClient client;

    @Before
    public void setUp() throws Exception {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();
        // Use a mutable service registry for later registering the service impl for each test case.
        grpcCleanup.register(InProcessServerBuilder.forName(serverName)
                .fallbackHandlerRegistry(serviceRegistry).directExecutor().build().start());
        client = new GRPCClient(grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build()));
    }

    @Test
    public void getPodName() {

        GRPCTestGrpc.GRPCTestImplBase getPodImpl =
                new GRPCTestGrpc.GRPCTestImplBase() {
                    @Override
                    public void getPodName(HttpReq point, StreamObserver<HttpRes> responseObserver) {
                        responseObserver.onNext(HttpRes.newBuilder().setPodname(POD_NAME).build());
                        responseObserver.onCompleted();
                    }
                };
        serviceRegistry.addService(getPodImpl);


        assertEquals(POD_NAME, client.getPod());
    }

}
