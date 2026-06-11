package br.edu.fatec.bd.service;

import br.edu.fatec.bd.database.QueryResult;
import br.edu.fatec.bd.service.DatabaseService.OrderItemInput;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataGenerator {

    private final DatabaseService database;
    private final Random random = new Random();

    private final List<String> names = Arrays.asList(
        "Ana Silva", "Carlos Santos", "Carla Lima",
        "Daniel Souza", "Eduarda Costa", "Felipe Oliveira"
    );

    private final List<ProductSeed> products = Arrays.asList(
        new ProductSeed("Notebook",            "Informatica",  "3499.90"),
        new ProductSeed("Mouse sem fio",       "Perifericos",  "119.90"),
        new ProductSeed("Teclado mecanico",    "Perifericos",  "289.90"),
        new ProductSeed("Monitor 24 polegadas","Monitores",    "899.90")
    );

    @Value("${app.generator.enabled}")
    private boolean enabled;

    public DataGenerator(DatabaseService d) {
        database = d;
    }

    @Scheduled(fixedDelayString = "${app.generator.interval-ms}")
    public void generateCycle() {
        if (!enabled) return;
        try {
            long token = System.currentTimeMillis();

            // 1. Cadastro de cliente
            String name  = names.get(random.nextInt(names.size()));
            String email = name.toLowerCase().replace(" ", ".") + "." + token + "@exemplo.com";
            long clientId = database.createClient(name, email);
            System.out.printf("[WRITE primaria] Cliente id=%d | nome=%s | email=%s%n", clientId, name, email);

            // 2. Cadastro de produto
            ProductSeed seed    = products.get(random.nextInt(products.size()));
            String descricao    = seed.description + " " + token % 100000;
            int estoque         = random.nextInt(26) + 5;
            long productId      = database.createProduct(descricao, seed.category, seed.value, estoque);
            System.out.printf("[WRITE primaria] Produto id=%d | descricao=%s | valor=%.2f | estoque=%d%n",
                productId, descricao, seed.value.doubleValue(), estoque);

            Thread.sleep(500);

            // 3. Criacao de pedido
            QueryResult<List<Map<String, Object>>> clients = database.randomClients();
            QueryResult<List<Map<String, Object>>> found   = database.randomProducts(random.nextInt(3) + 1);
            if (clients.getData().isEmpty() || found.getData().isEmpty()) return;

            Map<String, Object> client = clients.getData().get(0);
            List<OrderItemInput> items = new ArrayList<>();
            for (Map<String, Object> p : found.getData()) {
                int max = Math.min(3, ((Number) p.get("estoque")).intValue());
                items.add(new OrderItemInput(
                    ((Number) p.get("id")).longValue(),
                    random.nextInt(max) + 1,
                    new BigDecimal(p.get("valor").toString())
                ));
            }

            Map<String, Object> order = database.createOrder(((Number) client.get("id")).longValue(), items);
            long orderId = ((Number) order.get("id")).longValue();
            System.out.printf("[WRITE primaria] Pedido id=%d | cliente=%s | total=%.2f | itens=%d%n",
                orderId, client.get("nome"), ((Number) order.get("valor_total")).doubleValue(), items.size());

            // 4.1 - Buscar pedido por ID
            QueryResult<List<Map<String, Object>>> pedido = database.orderById(orderId);
            System.out.printf("[READ %s] Pedido: %s%n", pedido.getReplica(), pedido.getData());

            // 4.2 - Buscar itens do pedido
            QueryResult<List<Map<String, Object>>> itens = database.orderItems(orderId);
            System.out.printf("[READ %s] Itens: %s%n", itens.getReplica(), itens.getData());

            // 4.3 - Historico do cliente
            QueryResult<List<Map<String, Object>>> historico = database.clientOrders(((Number) client.get("id")).longValue());
            System.out.printf("[READ %s] Historico: %s%n", historico.getReplica(), historico.getData());

            // 4.4 - Relatorio agregado
            QueryResult<List<Map<String, Object>>> relatorio = database.salesReport();
            System.out.printf("[READ %s] Relatorio: %s%n", relatorio.getReplica(), relatorio.getData());

        } catch (Exception e) {
            System.err.println("[ERRO NO CICLO] " + e.getMessage());
        }
    }

    private static class ProductSeed {
        final String description, category;
        final BigDecimal value;

        ProductSeed(String d, String c, String v) {
            description = d;
            category    = c;
            value       = new BigDecimal(v);
        }
    }
}
