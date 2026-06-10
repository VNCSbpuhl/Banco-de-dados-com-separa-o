package br.edu.fatec.bd.web;
import br.edu.fatec.bd.database.*;
import br.edu.fatec.bd.service.DatabaseService;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
public class ApiController{
 private final DatabaseService database;private final ReadRouter router;@Value("${app.group-name}")private String group;
 public ApiController(DatabaseService d,ReadRouter r){database=d;router=r;}
 @GetMapping("/health")public Map<String,Object> health(){return map("status","ok","grupo",group,"replicas",router.replicaCount());}
 @GetMapping("/pedidos/{id}")public ResponseEntity<Map<String,Object>> order(@PathVariable long id){QueryResult<List<Map<String,Object>>> p=database.orderById(id);if(p.getData().isEmpty())return ResponseEntity.notFound().build();QueryResult<List<Map<String,Object>>> i=database.orderItems(id);return ResponseEntity.ok(map("replica_pedido",p.getReplica(),"replica_itens",i.getReplica(),"pedido",p.getData().get(0),"itens",i.getData()));}
 @GetMapping("/clientes/{id}/pedidos")public Map<String,Object> client(@PathVariable long id){QueryResult<List<Map<String,Object>>> r=database.clientOrders(id);return map("replica",r.getReplica(),"cliente_id",id,"pedidos",r.getData());}
 @GetMapping("/produtos/baixo-estoque")public Map<String,Object> stock(@RequestParam(defaultValue="5")int limite){QueryResult<List<Map<String,Object>>> r=database.lowStockProducts(limite);return map("replica",r.getReplica(),"limite",limite,"produtos",r.getData());}
 @GetMapping("/relatorios/vendas")public Map<String,Object> report(){QueryResult<List<Map<String,Object>>> r=database.salesReport();return map("replica",r.getReplica(),"relatorio",r.getData().isEmpty()?map():r.getData().get(0));}
 private Map<String,Object> map(Object...v){Map<String,Object> m=new LinkedHashMap<>();for(int i=0;i<v.length;i+=2)m.put(String.valueOf(v[i]),v[i+1]);return m;}
}
