import java.math.BigDecimal;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        RepositorioPedidos repositorio = new RepositorioPedidosEmMemoria();
        List<Produto> produtos = List.of(
            new Pizza(), new Hamburguer(), new Bebida()
        );
        FormaPagamento[] pagamentos = {
            new PagamentoCartao(), new PagamentoPix(), new PagamentoDinheiro()
        };
        Notificador[] notificadores = {
            new NotificadorEmail(), new NotificadorSms(),
            new NotificadorWhatsApp()
        };
        String[] destinatarios = {
            "cliente@example.com", "+5500000000000", "+5500000000000"
        };
        ApresentadorPedido apresentador = new ApresentadorPedido();
        ProcessadorPagamento processador = new ProcessadorPagamento();
        GerenciadorEntregas entregas = new EntregaService();

        for (int i = 0; i < pagamentos.length; i++) {
            GerenciadorPedidos servico = new PedidoService(
                repositorio, notificadores[i]
            );
            Pedido pedido = servico.criarPedido(produtos, destinatarios[i]);
            apresentador.exibir(pedido);
            processador.processar(pedido, pagamentos[i]);
            entregas.agendar(pedido, "Rua Exemplo, 100");
            System.out.println();
        }
        GeradorRelatorios relatorio = new RelatorioPedidos(repositorio);
        System.out.println(relatorio.gerar());
    }
}

// LSP: todo produto oferece nome, preco e descricao com o mesmo contrato.
abstract class Produto {
    private final String nome;
    private final BigDecimal preco;

    protected Produto(String nome, String preco) {
        this.nome = Objects.requireNonNull(nome);
        this.preco = new BigDecimal(preco);
        if (nome.isBlank() || this.preco.signum() < 0) {
            throw new IllegalArgumentException("Nome ou preco invalido.");
        }
    }
    public String getNome() { return nome; }
    public BigDecimal getPreco() { return preco; }
    public String descricao() { return nome + " - R$ " + preco; }
}

class Pizza extends Produto {
    public Pizza() { super("Pizza de mussarela", "40.00"); }
}
class Hamburguer extends Produto {
    public Hamburguer() { super("Hamburguer", "25.00"); }
}
class Bebida extends Produto {
    public Bebida() { super("Refrigerante", "7.00"); }
}

record Pedido(UUID id, List<Produto> produtos) {
    public Pedido {
        Objects.requireNonNull(id);
        produtos = List.copyOf(produtos);
        if (produtos.isEmpty()) {
            throw new IllegalArgumentException("Pedido sem produtos.");
        }
    }
    public BigDecimal total() {
        return produtos.stream().map(Produto::getPreco)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

// SRP e DIP: a persistencia tem sua propria abstracao e implementacao.
interface RepositorioPedidos {
    void salvar(Pedido pedido);
    List<Pedido> listar();
}
class RepositorioPedidosEmMemoria implements RepositorioPedidos {
    private final Map<UUID, Pedido> pedidos = new LinkedHashMap<>();
    public void salvar(Pedido pedido) { pedidos.put(pedido.id(), pedido); }
    public List<Pedido> listar() { return List.copyOf(pedidos.values()); }
}

// ISP: cada interface representa uma capacidade especifica.
interface Notificador {
    void enviar(String destinatario, String mensagem);
}
interface GerenciadorPedidos {
    Pedido criarPedido(List<Produto> produtos, String destinatario);
}
interface GeradorRelatorios {
    String gerar();
}
interface GerenciadorEntregas {
    void agendar(Pedido pedido, String endereco);
}

// DIP: o servico recebe abstracoes pelo construtor.
class PedidoService implements GerenciadorPedidos {
    private final RepositorioPedidos repositorio;
    private final Notificador notificador;

    public PedidoService(RepositorioPedidos repositorio,
                         Notificador notificador) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.notificador = Objects.requireNonNull(notificador);
    }
    public Pedido criarPedido(List<Produto> produtos, String destinatario) {
        Pedido pedido = new Pedido(UUID.randomUUID(), produtos);
        repositorio.salvar(pedido);
        notificador.enviar(destinatario, "Pedido " + pedido.id() + " criado.");
        return pedido;
    }
}

// SRP: a apresentacao fica fora da entidade e do repositorio.
class ApresentadorPedido {
    public void exibir(Pedido pedido) {
        System.out.println("Pedido: " + pedido.id());
        pedido.produtos().forEach(p -> System.out.println(p.descricao()));
        System.out.println("Total: R$ " + pedido.total());
    }
}

class RelatorioPedidos implements GeradorRelatorios {
    private final RepositorioPedidos repositorio;
    public RelatorioPedidos(RepositorioPedidos repositorio) {
        this.repositorio = repositorio;
    }
    public String gerar() {
        List<Pedido> pedidos = repositorio.listar();
        BigDecimal total = pedidos.stream().map(Pedido::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return "Pedidos: " + pedidos.size() + " | Valor total: R$ " + total;
    }
}
class EntregaService implements GerenciadorEntregas {
    private final Map<UUID, String> entregas = new LinkedHashMap<>();
    public void agendar(Pedido pedido, String endereco) {
        if (endereco == null || endereco.isBlank()) {
            throw new IllegalArgumentException("Endereco obrigatorio.");
        }
        entregas.put(pedido.id(), endereco);
        System.out.println("Entrega agendada para: " + endereco);
    }
}

// OCP: novos meios implementam a interface sem mudar o processador.
interface FormaPagamento {
    void pagar(BigDecimal valor);
}
class PagamentoCartao implements FormaPagamento {
    public void pagar(BigDecimal valor) {
        System.out.println("[SIMULACAO] Cartao: R$ " + valor);
    }
}
class PagamentoPix implements FormaPagamento {
    public void pagar(BigDecimal valor) {
        System.out.println("[SIMULACAO] PIX: R$ " + valor);
    }
}
class PagamentoDinheiro implements FormaPagamento {
    public void pagar(BigDecimal valor) {
        System.out.println("[SIMULACAO] Dinheiro: R$ " + valor);
    }
}
class ProcessadorPagamento {
    public void processar(Pedido pedido, FormaPagamento forma) {
        forma.pagar(pedido.total());
    }
}

// Estes adaptadores simulam os envios, sem usar servicos externos.
class NotificadorEmail implements Notificador {
    public void enviar(String destinatario, String mensagem) {
        System.out.println("[SIMULACAO] Email para "
                + destinatario + ": " + mensagem);
    }
}
class NotificadorSms implements Notificador {
    public void enviar(String destinatario, String mensagem) {
        System.out.println("[SIMULACAO] SMS para "
                + destinatario + ": " + mensagem);
    }
}
class NotificadorWhatsApp implements Notificador {
    public void enviar(String destinatario, String mensagem) {
        System.out.println("[SIMULACAO] WhatsApp para "
                + destinatario + ": " + mensagem);
    }
}
