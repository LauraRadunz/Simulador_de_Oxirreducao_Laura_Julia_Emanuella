import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

// forma: A notação da espécie (ex: "Zn(s)")
// tipo: "reduzida" (pode oxidar) ou "oxidada" (pode reduzir)
// formaOposta: A forma para a qual ela se transforma (ex: "Zn(s)" -> "Zn2+(aq)")

record informacoesEspecies(String forma, String tipo, String formaOposta) { // Grava as informações que forma inseridas em seus respectivos campos para depois adicionar na lista de espécies disponíveis
}

public class SimuladorQuimica {

    private static final Map<String, informacoesEspecies> especiesDisponiveis = new HashMap<>(); // Lista com todas as espécies que tem na lista

    static Scanner teclado = new Scanner(System.in);

    static { //Adiciona na lista da espécie, deve ser feito manualmente
        // Espécies de Zinco:
        especiesDisponiveis.put("Zn(s)", new informacoesEspecies("Zn(s)", "reduzida", "Zn2+(aq)")); 
        especiesDisponiveis.put("Zn2+(aq)", new informacoesEspecies("Zn2+(aq)", "oxidada", "Zn(s)"));

        // Espécies de Cobre:
        especiesDisponiveis.put("Cu(s)", new informacoesEspecies("Cu(s)", "reduzida", "Cu2+(aq)"));
        especiesDisponiveis.put("Cu2+(aq)", new informacoesEspecies("Cu2+(aq)", "oxidada", "Cu(s)"));

    }

    public static void main(String[] args) {
        System.out.println("\n--- ⚗️ Bem-vindo ao Simulador de Oxirredução Lalílla ⚗️ ---");
        iniciarSimulacao();
        teclado.close();
        System.out.println("--- Simulação Encerrada ---");
    }

    public static void iniciarSimulacao() { // Começa a simulação, com as perguntas

        while (true) {
            // Primeira Espécie:
            imprimirMenu(); // Imprime o menu com as telas disponíveis
            String resposta1 = lerString("Escolha a PRIMEIRA espécie (digite a forma, ex: Zn(s)): ");
            informacoesEspecies especie1 = especiesDisponiveis.get(resposta1); // Pega as informações da espécie escolhida para fazer a reação

            if (especie1 == null) { // Confere se foi inserido algo nulo
                imprimirErro("Espécie '" + resposta1 + "' inválida. Tente novamente.");
                continue; // Volta ao início do loop
            }

            // Segunda Espécie:
            String resposta2;
            informacoesEspecies especie2;
            while (true) {
                imprimirMenu();
                resposta2 = lerString("Escolha a SEGUNDA espécie (ex: Cu2+(aq)): ");
                especie2 = especiesDisponiveis.get(resposta2);

                if (especie2 == null) { // Começa a verificação pra ver se o que o usuário digitou foi válido, se não foi, pede novamente
                    imprimirErro("Espécie '" + resposta2 + "' inválida. Tente novamente.");
                } else {

                    // Validações das condições:

                    // 1. Não podem ser iguais
                    if (resposta1.equals(resposta2)) {
                        imprimirErro("Você não pode escolher a mesma espécie duas vezes.");
                    } else

                    // 2. Não podem ter o mesmo tipo (ambas reduzidas ou ambas oxidadas)
                    if (especie1.tipo().equals(especie2.tipo())) {
                        imprimirErro(
                                "Erro: Você deve escolher uma espécie reduzida (s) e uma oxidada (aq).\nAs espécies "
                                        + especie1.forma() + " e " + especie2.forma() + " são ambas " + especie1.tipo()
                                        + "s.");
                    } else {
                        break;
                    }
                }
            }

            informacoesEspecies especieQueOxida;
            informacoesEspecies especieQueReduz;

            // Identifica quem é quem
            // A espécie "reduzida" é a que PODE oxidar
            // A espécie "oxidada" é a que PODE reduzir
            if (especie1.tipo().equals("reduzida")) {
                especieQueOxida = especie1;
                especieQueReduz = especie2;
            } else {
                especieQueOxida = especie2;
                especieQueReduz = especie1;
            }

            // Pega as formas opostas (produtos da reação)
            String produtoOxidado = especieQueOxida.formaOposta();
            String produtoReduzido = especieQueReduz.formaOposta();

            // Imprime o resultado final
            System.out.println("\n========= ✅ RESULTADO DA REAÇÃO =========");
            System.out.println("A espécie que OXIDA (agente redutor) é: " + especieQueOxida.forma());
            System.out.println("A espécie que REDUZ (agente oxidante) é: " + especieQueReduz.forma());
            System.out.println("\nEquação Global de Oxirredução:");
            // Ex: Zn(s) + Cu2+(aq) -> Zn2+(aq) + Cu(s)
            System.out.println(especieQueOxida.forma() + " + " + especieQueReduz.forma() + " -> " + produtoOxidado
                    + " + " + produtoReduzido);
            System.out.println("=========================================\n");

            break; // Sai do loop, pois o programa foi concluído com sucesso
        }
    }

    private static void imprimirMenu() {
        System.out.println("\nEspécies Disponíveis:");
        for (String key : especiesDisponiveis.keySet()) {
            System.out.println(" - " + key);
        }
    }

    private static void imprimirErro(String mensagem) {
        System.out.println("\n----------------- ❌ ERRO ❌ -----------------");
        System.out.println(mensagem);
        System.out.println("----------------------------------------------\n");
    }

    public static String lerString(String pergunta) {
        System.out.print(pergunta);
        return teclado.nextLine();
    }
}