# Atividade de SOLID

Sistema de delivery em Java

## Sobre o sistema

O sistema representa um delivery: cria pedidos, guarda os dados e mostra os produtos e o valor total. Também inclui pagamentos por cartão, PIX e dinheiro, agendamento de entregas e notificações por e-mail, SMS e WhatsApp.

O código foi feito em Java 17 ou superior. Os pagamentos e as mensagens são simulações no console, e os dados ficam na memória enquanto o programa está aberto.

## 1. SRP — Responsabilidade única

Cada classe fica responsável por uma parte do pedido. Assim, uma mudança na forma de mostrar os dados não interfere na forma de salvar.

Pedido guarda os produtos e calcula o total. PedidoService cria o pedido e chama os serviços de armazenamento e notificação. RepositorioPedidosEmMemoria salva os pedidos, e ApresentadorPedido mostra as informações no console.

Por exemplo, para trocar o armazenamento em memória por um banco de dados, é possível criar outra implementação de RepositorioPedidos sem mudar a lógica de criação do pedido.

## 2. OCP — Aberto para extensão, fechado para modificação

As formas de pagamento seguem a interface FormaPagamento, que possui o método pagar. Cada classe cuida de um tipo: PagamentoCartao, PagamentoPix ou PagamentoDinheiro.

ProcessadorPagamento recebe a forma escolhida e chama esse método. Para adicionar pagamento por voucher, basta criar uma classe que implemente FormaPagamento e passar um objeto dela ao processador. A escolha do novo pagamento entra na configuração do programa, sem alterar o código do processador.

## 3. LSP — Substituição de Liskov

Pizza, Hamburguer e Bebida são subclasses de Produto. Todas têm nome, preço e descrição, e podem ser usadas onde o programa espera um Produto.

Isso aparece na lista de itens do pedido, que mistura os três tipos. O cálculo do total usa getPreco() em todos eles, sem precisar tratar pizza, hambúrguer ou bebida de forma diferente. As subclasses mantêm o mesmo comportamento esperado da classe base, com nome válido e preço não negativo.

## 4. ISP — Segregação de interfaces

As interfaces foram separadas por função: Notificador envia mensagens, GeradorRelatorios gera o relatório, GerenciadorPedidos cria pedidos e GerenciadorEntregas agenda as entregas.

Cada classe implementa apenas a interface de que precisa. Por exemplo, RelatorioPedidos implementa GeradorRelatorios, enquanto EntregaService implementa GerenciadorEntregas. Dessa forma, uma classe de entrega não precisa ter métodos de relatório que não vai usar.

## 5. DIP — Inversão de dependência

PedidoService recebe RepositorioPedidos e Notificador pelo construtor. Ele usa essas interfaces sem escolher diretamente qual repositório ou canal de mensagem será utilizado.

A escolha fica em Main. Para mudar de e-mail para SMS ou WhatsApp, basta passar outro notificador ao criar o serviço. As três classes implementam Notificador, então PedidoService continua funcionando com o mesmo código.

## 6. Como executar

Salve o código em Main.java. Com o JDK 17 ou superior instalado, abra o terminal na pasta do arquivo e execute:

```sh
java Main.java
```

Outra opção é compilar primeiro e depois executar:

```sh
javac Main.java
java Main
```

O exemplo cria três pedidos, cada um com uma pizza de R$ 40,00, um hambúrguer de R$ 25,00 e uma bebida de R$ 7,00. Cada pedido soma R$ 72,00. Ao executar, aparecem os três pagamentos, os três canais de notificação e os agendamentos das entregas.

No fim, o relatório mostra:

```text
Pedidos: 3 | Valor total: R$ 216.00
```
