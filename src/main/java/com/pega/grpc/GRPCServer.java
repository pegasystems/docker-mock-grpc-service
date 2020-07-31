
package com.pega.grpc;

import com.pega.grpc.grpctest.GRPCTestGrpc;
import com.pega.grpc.grpctest.HttpReq;
import com.pega.grpc.grpctest.HttpRes;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class GRPCServer {
    public static final String POD_NAME_ENV_VARIABLE = "MY_POD_NAME";
    public static final String POD_NAME_DEFAULT = "UNKNOWN";

    public static final int DEFAULT_PORT = 8980;

    private static final Logger logger = Logger.getLogger(GRPCServer.class.getName());

    private final int port;
    private final Server server;

    public GRPCServer(int port) throws IOException {
        this(ServerBuilder.forPort(port), port);
    }

    public GRPCServer(ServerBuilder<?> serverBuilder, int port) {
        this.port = port;
        server = serverBuilder.addService(new GRPCTestService())
                .build();
    }

    /** Start serving requests. */
    public void start() throws IOException {
        server.start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    GRPCServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    /** Stop serving requests and shutdown resources. */
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main method.  This comment makes the linter happy.
     */
    public static void main(String[] args) throws Exception {
        GRPCServer server = new GRPCServer(DEFAULT_PORT);
        server.start();
        server.blockUntilShutdown();
    }

    private static class GRPCTestService extends GRPCTestGrpc.GRPCTestImplBase {

        @Override
        public void getPodName(HttpReq request, StreamObserver<HttpRes> responseObserver) {
            responseObserver.onNext(checkHttp());
            responseObserver.onCompleted();
        }

        private HttpRes checkHttp() {
            String podName = System.getenv(POD_NAME_ENV_VARIABLE);
            if(podName == null){
                podName = POD_NAME_DEFAULT;
            }
            return HttpRes.newBuilder().setPodname(podName).build();
        }
    }
}
