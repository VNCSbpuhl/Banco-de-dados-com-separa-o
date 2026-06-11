package br.edu.fatec.bd.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class DatabaseConfig {

    @Value("${app.database.name}")     private String database;
    @Value("${app.database.port}")     private int port;
    @Value("${app.database.user}")     private String user;
    @Value("${app.database.password}") private String password;
    @Value("${app.database.primary-host}") private String primaryHost;
    @Value("${app.database.replica-hosts}") private String replicaHosts;
    @Value("${app.database.use-ssl}")  private boolean useSsl;

    private HikariDataSource buildDataSource(String host, String poolName, boolean readOnly) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database
            + "?useSSL=" + useSsl + "&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setPoolName(poolName);
        config.setMaximumPoolSize(10);
        config.setReadOnly(readOnly);
        return new HikariDataSource(config);
    }

    @Bean
    public DataSource primaryDataSource() {
        return buildDataSource(primaryHost, "mysql-primary-write", false);
    }

    @Bean
    public JdbcTemplate writeJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public ReplicaRegistry replicaRegistry() {
        List<String> hosts = Arrays.stream(replicaHosts.split(","))
            .map(String::trim)
            .filter(h -> !h.isEmpty())
            .collect(Collectors.toList());

        if (hosts.isEmpty()) {
            throw new IllegalArgumentException("Configure DB_REPLICA_HOSTS");
        }

        List<ReplicaJdbc> list = new ArrayList<>();
        for (int i = 0; i < hosts.size(); i++) {
            String host = hosts.get(i);
            list.add(new ReplicaJdbc(host, new JdbcTemplate(buildDataSource(host, "mysql-replica-read-" + i, true))));
        }
        return new ReplicaRegistry(list);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    public static class ReplicaJdbc {
        private final String host;
        private final JdbcTemplate jdbc;

        public ReplicaJdbc(String host, JdbcTemplate jdbc) {
            this.host = host;
            this.jdbc = jdbc;
        }

        public String getHost() {
            return host;
        }

        public JdbcTemplate getJdbc() {
            return jdbc;
        }
    }

    public static class ReplicaRegistry {
        private final List<ReplicaJdbc> replicas;

        public ReplicaRegistry(List<ReplicaJdbc> replicas) {
            this.replicas = replicas;
        }

        public List<ReplicaJdbc> getReplicas() {
            return replicas;
        }
    }
}
