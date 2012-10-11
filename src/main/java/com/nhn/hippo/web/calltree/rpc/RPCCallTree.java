package com.nhn.hippo.web.calltree.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.profiler.common.dto.thrift.Span;

/**
 * Call Tree
 *
 * @author netspider
 */
public class RPCCallTree {

    private final String PREFIX_CLIENT = "CLIENT:";

    private final Map<String, RPC> rpcs = new HashMap<String, RPC>();
    private final Map<String, String> spanIdToRPCId = new HashMap<String, String>();
    private final Map<String, RPCRequest> requests = new HashMap<String, RPCRequest>();
    private final List<Span> spans = new ArrayList<Span>();

    private boolean isBuilt = false;

    public void addSpan(Span span) {
        /**
         * make RPCs
         */
        // TODO: 여기에서 이러지말고 수집할 때 처음부터 table에 저장해둘 수 있나??
        RPC rpc = new RPC(span.getAgentID(), span.getServiceName(), span.getName(), span.isTerminal());

        // TODO: remove this later.
        if (rpc.getId().contains("mysql:jdbc:") || rpc.getId().contains("favicon")) {
            return;
        }

        if (!rpcs.containsKey(rpc.getId())) {
            rpcs.put(rpc.getId(), rpc);
        }
        spanIdToRPCId.put(String.valueOf(span.getSpanID()), rpc.getId());

        // TODO remove client node
        if (span.getParentSpanId() == -1) {
            RPC client = new RPC(PREFIX_CLIENT + span.getAgentID(), span.getServiceName(), span.getName(), false);
            rpcs.put(client.getId(), client);
            spanIdToRPCId.put(PREFIX_CLIENT + span.getSpanID(), client.getId());
        }

        /**
         * Preparing makes link (requests)
         */
        spans.add(span);
    }

    public RPCCallTree build() {
        if (isBuilt)
            return this;

        int i = 0;
        for (Entry<String, RPC> entry : rpcs.entrySet()) {
            entry.getValue().setSequence(i++);
        }

        for (Span span : spans) {
            String from = String.valueOf(span.getParentSpanId());
            String to = String.valueOf(span.getSpanID());

            RPC fromRPC = rpcs.get(spanIdToRPCId.get(from));
            RPC toRPC = rpcs.get(spanIdToRPCId.get(to));

            if (fromRPC == null) {
                fromRPC = rpcs.get(spanIdToRPCId.get(PREFIX_CLIENT + to));
            }

            RPCRequest request = new RPCRequest(fromRPC, toRPC);
            if (requests.containsKey(request.getId())) {
                requests.get(request.getId()).increaseCallCount();
            } else {
                requests.put(request.getId(), request);
            }
        }

        isBuilt = true;
        return this;
    }

    public Collection<RPC> getNodes() {
        return this.rpcs.values();
    }

    public Collection<RPCRequest> getLinks() {
        return this.requests.values();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("RPCS=").append(rpcs);
        sb.append("\n");
        sb.append("REQUESTS=").append(requests.values());

        return sb.toString();
    }
}
