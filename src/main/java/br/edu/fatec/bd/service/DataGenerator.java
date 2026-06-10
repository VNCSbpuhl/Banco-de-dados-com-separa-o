package br.edu.fatec.bd.service;
import br.edu.fatec.bd.database.QueryResult;
import br.edu.fatec.bd.service.DatabaseService.OrderItemInput;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
public class DataGenerator{
 private final DatabaseService database;private final Random random=new Random();
 private final List<String> names=Arrays.asList("Ana Silva","Carlos Santos","Carla Lima","Daniel Souza","Eduarda Costa","Felipe Oliveira");
 private final List<ProductSeed> products=Arrays.asList(new ProductSeed("Notebook","Informatica","3499.90"),new ProductSeed("Mouse sem fio","Perifericos","119.90"),new ProductSeed("Teclado mecanico","Perifericos","289.90"),new ProductSeed("Monitor 24 polegadas","Monitores","899.90"));
 @Value("${app.generator.enabled}") private boolean enabled;
 public DataGenerator(DatabaseService d){database=d;}
 @Scheduled(fixedDelayString="${app.generator.interval-ms}") public void generateCycle(){if(!enabled)return;try{long token=System.currentTimeMillis();String name=names.get(random.nextInt(names.size()));String email=name.toLowerCase().replace(" ",".")+"."+token+"@exemplo.com";long clientId=database.createClient(name,email);System.out.printf("[WRITE primaria] Cliente id=%d nome=%s email=%s%n",clientId,name,email);ProductSeed seed=products.get(random.nextInt(products.size()));long productId=database.createProduct(seed.description+" "+token%100000,seed.category,seed.value,random.nextInt(26)+5);System.out.printf("[WRITE primaria] Produto id=%d%n",productId);Thread.sleep(500);QueryResult<List<Map<String,Object>>> clients=database.randomClients();QueryResult<List<Map<String,Object>>> found=database.randomProducts(random.nextInt(3)+1);if(clients.getData().isEmpty()||found.getData().isEmpty())return;Map<String,Object> client=clients.getData().get(0);List<OrderItemInput> items=new ArrayList<>();for(Map<String,Object> p:found.getData()){int max=Math.min(3,((Number)p.get("estoque")).intValue());items.add(new OrderItemInput(((Number)p.get("id")).longValue(),random.nextInt(max)+1,new BigDecimal(p.get("valor").toString())));}Map<String,Object> order=database.createOrder(((Number)client.get("id")).longValue(),items);System.out.printf("[WRITE primaria] Pedido %s%n",order);System.out.printf("[READ] Pedido %s%n",database.orderById(((Number)order.get("id")).longValue()).getData());System.out.printf("[READ] Relatorio %s%n",database.salesReport().getData());}catch(Exception e){System.err.println("[ERRO NO CICLO] "+e.getMessage());}}
 private static class ProductSeed{final String description,category;final BigDecimal value;ProductSeed(String d,String c,String v){description=d;category=c;value=new BigDecimal(v);}}
}
