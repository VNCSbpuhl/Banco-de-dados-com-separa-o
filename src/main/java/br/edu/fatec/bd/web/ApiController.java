package br.edu.fatec.bd.web;

import br.edu.fatec.bd.database.QueryResult;
import br.edu.fatec.bd.database.ReadRouter;
import br.edu.fatec.bd.service.DatabaseService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    private final DatabaseService database;
    private final ReadRouter router;

    @Value("${app.group-name}")
    private String group;

    public ApiController(DatabaseService database, ReadRouter router) {
        this.database = database;
        this.router   = router;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return mapOf("status", "ok", "grupo", group, "replicas", router.replicaCount());
    }

    @GetMapping("/pedidos/{id}")
    public ResponseEntity<Map<String, Object>> order(@PathVariable long id) {
        QueryResult<List<Map<String, Object>>> pedido = database.orderById(id);
        if (pedido.getData().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        QueryResult<List<Map<String, Object>>> itens = database.orderItems(id);
        return ResponseEntity.ok(mapOf(
            "replica_pedido", pedido.getReplica(),
            "replica_itens",  itens.getReplica(),
            "pedido",         pedido.getData().get(0),
            "itens",          itens.getData()
        ));
    }

    @GetMapping("/clientes/{id}/pedidos")
    public Map<String, Object> clientOrders(@PathVariable long id) {
        QueryResult<List<Map<String, Object>>> result = database.clientOrders(id);
        return mapOf("replica", result.getReplica(), "cliente_id", id, "pedidos", result.getData());
    }

    @GetMapping("/produtos/baixo-estoque")
    public Map<String, Object> lowStock(@RequestParam(defaultValue = "5") int limite) {
        QueryResult<List<Map<String, Object>>> result = database.lowStockProducts(limite);
        return mapOf("replica", result.getReplica(), "limite", limite, "produtos", result.getData());
    }

    @GetMapping("/relatorios/vendas")
    public Map<String, Object> salesReport() {
        QueryResult<List<Map<String, Object>>> result = database.salesReport();
        return mapOf("replica", result.getReplica(), "relatorio", result.getData().isEmpty() ? mapOf() : result.getData().get(0));
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }
}
