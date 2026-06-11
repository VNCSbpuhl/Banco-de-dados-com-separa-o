package br.edu.fatec.bd.database;

import br.edu.fatec.bd.config.DatabaseConfig.ReplicaJdbc;
import br.edu.fatec.bd.config.DatabaseConfig.ReplicaRegistry;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class ReadRouter {

    private final List<ReplicaJdbc> replicas;
    private final AtomicInteger next = new AtomicInteger();

    public ReadRouter(ReplicaRegistry registry) {
        replicas = registry.getReplicas();
    }

    public ReplicaJdbc nextReplica() {
        return replicas.get(Math.floorMod(next.getAndIncrement(), replicas.size()));
    }

    public int replicaCount() {
        return replicas.size();
    }
}
