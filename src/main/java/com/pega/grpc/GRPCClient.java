
package com.pega.grpc;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Message;
import com.pega.grpc.grpctest.GRPCTestGrpc;
import com.pega.grpc.grpctest.GRPCTestGrpc.GRPCTestBlockingStub;
import com.pega.grpc.grpctest.GRPCTestGrpc.GRPCTestStub;
import com.pega.grpc.grpctest.HttpReq;
import com.pega.grpc.grpctest.HttpRes;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test client for a GRPC service.
 *
 */
public class GRPCClient {
    private static final Logger logger = Logger.getLogger(GRPCClient.class.getName());

    private static final String TARGET_ENV_VAR = "TARGET";
    private static final int MAX_ALLOWED_TIMES_SAME_POD_CAN_BE_USED = 3;

    private final GRPCTestBlockingStub blockingStub;
    private final GRPCTestStub asyncStub;

    public GRPCClient(Channel channel) {
        blockingStub = GRPCTestGrpc.newBlockingStub(channel);
        asyncStub = GRPCTestGrpc.newStub(channel);
    }

    public String getPod() {
        HttpReq req = HttpReq.newBuilder().build();
        HttpRes res;

         res = blockingStub.getPodName(req);
         System.out.println("Response pod " + res.getPodname());
         return res.getPodname();
    }

    //Verify that our infrastructure lets us make requests on different
    //GRPC server pods
    public void assertCanTalkToDifferentPods(){
        String previousPod = "";
        int previousPodCount = 0;
        for(int i = 0; i < 100; i++) {
            String currentPod = getPod();
            if(currentPod.equals(previousPod)){
                if(++previousPodCount > MAX_ALLOWED_TIMES_SAME_POD_CAN_BE_USED) {
                    throw new IllegalStateException("Connected to pod " + previousPod + " more than " + MAX_ALLOWED_TIMES_SAME_POD_CAN_BE_USED + " in a row");
                }
            }else{
                previousPod = currentPod;
                previousPodCount = 0;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {

        String target = System.getenv(TARGET_ENV_VAR);
        if(target == null){
            target = "localhost:" + GRPCServer.DEFAULT_PORT;
        }

        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        try {
            GRPCClient client = new GRPCClient(channel);

            client.assertCanTalkToDifferentPods();

        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private void info(String msg, Object... params) {
        logger.log(Level.INFO, msg, params);
    }

    private void warning(String msg, Object... params) {
        logger.log(Level.WARNING, msg, params);
    }

}
