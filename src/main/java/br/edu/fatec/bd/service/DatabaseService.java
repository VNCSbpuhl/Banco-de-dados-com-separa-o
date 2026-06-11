package br.edu.fatec.bd.service;

import br.edu.fatec.bd.config.DatabaseConfig.ReplicaJdbc;
import br.edu.fatec.bd.database.QueryResult;
import br.edu.fatec.bd.database.ReadRouter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class DatabaseService {

    private final JdbcTemplate writeJdbc;
    private final ReadRouter readRouter;
    private final TransactionTemplate transaction;

    @Value("${app.group-name}")
    private String groupName;

    public DatabaseService(JdbcTemplate writeJdbcTemplate, ReadRouter readRouter, TransactionTemplate transactionTemplate) {
        this.writeJdbc   = writeJdbcTemplate;
        this.readRouter  = readRouter;
        this.transaction = transactionTemplate;
    }

    public long createClient(String name, String email) {
        KeyHolder key = new GeneratedKeyHolder();
        writeJdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO cliente (nome, email, criado_por) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, groupName);
            return ps;
        }, key);
        return key.getKey().longValue();
    }

    public long createProduct(String description, String category, BigDecimal value, int stock) {
        KeyHolder key = new GeneratedKeyHolder();
        writeJdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO produto (descricao, categoria, valor, estoque, criado_por) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, description);
            ps.setString(2, category);
            ps.setBigDecimal(3, value);
            ps.setInt(4, stock);
            ps.setString(5, groupName);
            return ps;
        }, key);
        return key.getKey().longValue();
    }

    public QueryResult<List<Map<String, Object>>> randomClients() {
        return read("SELECT id, nome, email FROM cliente ORDER BY RAND() LIMIT 1");
    }

    public QueryResult<List<Map<String, Object>>> randomProducts(int limit) {
        return read("SELECT id, descricao, categoria, valor, estoque FROM produto WHERE estoque > 0 ORDER BY RAND() LIMIT ?", limit);
    }

    public Map<String, Object> createOrder(long clientId, List<OrderItemInput> items) {
        return transaction.execute(status -> {
            BigDecimal total = BigDecimal.ZERO;
            for (OrderItemInput item : items) {
                total = total.add(item.unitValue.multiply(BigDecimal.valueOf(item.quantity)));
            }

            KeyHolder key = new GeneratedKeyHolder();
            BigDecimal finalTotal = total;
            writeJdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO pedido (cliente_id, valor_total, status, criado_por) VALUES (?, ?, 'FINALIZADO', ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                ps.setLong(1, clientId);
                ps.setBigDecimal(2, finalTotal);
                ps.setString(3, groupName);
                return ps;
            }, key);

            long orderId = key.getKey().longValue();
            for (OrderItemInput item : items) {
                int changed = writeJdbc.update(
                    "UPDATE produto SET estoque = estoque - ? WHERE id = ? AND estoque >= ?",
                    item.quantity, item.productId, item.quantity
                );
                if (changed != 1) {
                    throw new IllegalStateException("Estoque insuficiente");
                }
                writeJdbc.update(
                    "INSERT INTO pedido_item (pedido_id, produto_id, quantidade, valor_unitario) VALUES (?, ?, ?, ?)",
                    orderId, item.productId, item.quantity, item.unitValue
                );
            }

            return mapOf("id", orderId, "cliente_id", clientId, "valor_total", total, "quantidade_itens", items.size());
        });
    }

    public QueryResult<List<Map<String, Object>>> orderById(long id) {
        return read(
            "SELECT p.id, p.cliente_id, c.nome AS cliente, p.valor_total, p.status, p.criado_em " +
            "FROM pedido p JOIN cliente c ON c.id = p.cliente_id WHERE p.id = ?", id
        );
    }

    public QueryResult<List<Map<String, Object>>> orderItems(long id) {
        return read(
            "SELECT pi.id, pi.pedido_id, pi.produto_id, pr.descricao AS produto, " +
            "pi.quantidade, pi.valor_unitario, pi.quantidade * pi.valor_unitario AS subtotal " +
            "FROM pedido_item pi JOIN produto pr ON pr.id = pi.produto_id WHERE pi.pedido_id = ? ORDER BY pi.id", id
        );
    }

    public QueryResult<List<Map<String, Object>>> clientOrders(long id) {
        return read(
            "SELECT id, valor_total, status, criado_em FROM pedido " +
            "WHERE cliente_id = ? ORDER BY criado_em DESC, id DESC LIMIT 5", id
        );
    }

    public QueryResult<List<Map<String, Object>>> lowStockProducts(int limit) {
        return read(
            "SELECT id, descricao, categoria, valor, estoque FROM produto " +
            "WHERE estoque <= ? ORDER BY estoque, descricao", limit
        );
    }

    public QueryResult<List<Map<String, Object>>> salesReport() {
        return read(
            "SELECT COUNT(*) AS quantidade_total_pedidos, " +
            "COALESCE(AVG(valor_total), 0) AS valor_medio_pedidos, " +
            "COALESCE(SUM(valor_total), 0) AS valor_total_vendido FROM pedido"
        );
    }

    private QueryResult<List<Map<String, Object>>> read(String sql, Object... params) {
        ReplicaJdbc replica = readRouter.nextReplica();
        return new QueryResult<>(replica.getHost(), replica.getJdbc().queryForList(sql, params));
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }

    public static class OrderItemInput {
        private final long productId;
        private final int quantity;
        private final BigDecimal unitValue;

        public OrderItemInput(long productId, int quantity, BigDecimal unitValue) {
            this.productId = productId;
            this.quantity  = quantity;
            this.unitValue = unitValue;
        }
    }
}
