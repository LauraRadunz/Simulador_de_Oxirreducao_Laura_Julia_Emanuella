import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

// --- 1. ESTRUTURA DE DADOS ---
record informacoesEspecies(String forma, String tipo, String formaOposta, double potencial) {
    // Método auxiliar para pegar só o símbolo (ex: "Zn(s)" -> "Zn")
    public String getSimbolo() {
        return forma.replaceAll("\\(s\\)|\\(aq\\)|\\d+\\+|\\+", "");
    }
    
    // Método auxiliar para pegar a carga do íon (ex: "Zn2+(aq)" -> "Zn2+")
    public String getSimboloIon() {
        return formaOposta.replaceAll("\\(aq\\)|\\(s\\)", "");
    }
}

public class SimuladorSwing extends JFrame {

    // --- BANCO DE DADOS ---
    private static final Map<Integer, informacoesEspecies> bancoDeEspecies = new HashMap<>();

    static {
        bancoDeEspecies.put(1, new informacoesEspecies("Li(s)", "reduzida", "Li+(aq)", -3.04));
        bancoDeEspecies.put(2, new informacoesEspecies("Li+(aq)", "oxidada", "Li(s)", -3.04));
        bancoDeEspecies.put(3, new informacoesEspecies("Mg(s)", "reduzida", "Mg2+(aq)", -2.37));
        bancoDeEspecies.put(4, new informacoesEspecies("Mg2+(aq)", "oxidada", "Mg(s)", -2.37));
        bancoDeEspecies.put(5, new informacoesEspecies("Al(s)", "reduzida", "Al3+(aq)", -1.66));
        bancoDeEspecies.put(6, new informacoesEspecies("Al3+(aq)", "oxidada", "Al(s)", -1.66));
        bancoDeEspecies.put(7, new informacoesEspecies("Zn(s)", "reduzida", "Zn2+(aq)", -0.76));
        bancoDeEspecies.put(8, new informacoesEspecies("Zn2+(aq)", "oxidada", "Zn(s)", -0.76));
        bancoDeEspecies.put(9, new informacoesEspecies("Fe(s)", "reduzida", "Fe2+(aq)", -0.44));
        bancoDeEspecies.put(10, new informacoesEspecies("Fe2+(aq)", "oxidada", "Fe(s)", -0.44));
        bancoDeEspecies.put(11, new informacoesEspecies("Ni(s)", "reduzida", "Ni2+(aq)", -0.25));
        bancoDeEspecies.put(12, new informacoesEspecies("Ni2+(aq)", "oxidada", "Ni(s)", -0.25));
        bancoDeEspecies.put(13, new informacoesEspecies("Pb(s)", "reduzida", "Pb2+(aq)", -0.13));
        bancoDeEspecies.put(14, new informacoesEspecies("Pb2+(aq)", "oxidada", "Pb(s)", -0.13));
        bancoDeEspecies.put(15, new informacoesEspecies("Cu(s)", "reduzida", "Cu2+(aq)", +0.34));
        bancoDeEspecies.put(16, new informacoesEspecies("Cu2+(aq)", "oxidada", "Cu(s)", +0.34));
        bancoDeEspecies.put(17, new informacoesEspecies("Ag(s)", "reduzida", "Ag+(aq)", +0.80));
        bancoDeEspecies.put(18, new informacoesEspecies("Ag+(aq)", "oxidada", "Ag(s)", +0.80));
        bancoDeEspecies.put(19, new informacoesEspecies("Au(s)", "reduzida", "Au3+(aq)", +1.50));
        bancoDeEspecies.put(20, new informacoesEspecies("Au3+(aq)", "oxidada", "Au(s)", +1.50));
    }

    // --- COMPONENTES ---
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private JLabel lblStatus;
    private JTextArea areaResultado;
    private PainelPilha painelDesenho;
    private JButton btnReiniciar;

    private informacoesEspecies primeiraEscolha = null;
    private informacoesEspecies segundaEscolha = null;

    public SimuladorSwing() {
        setTitle("Simulador de Oxirredução (Pilha de Daniell)");
        setSize(1000, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. Topo
        JPanel panelTopo = new JPanel();
        panelTopo.setBackground(new Color(40, 40, 40));
        lblStatus = new JLabel("Clique na tabela para selecionar a PRIMEIRA espécie.");
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 16));
        panelTopo.add(lblStatus);
        add(panelTopo, BorderLayout.NORTH);

        // 2. Esquerda (Tabela)
        JPanel panelEsquerda = new JPanel(new BorderLayout());
        panelEsquerda.setBorder(BorderFactory.createTitledBorder("Tabela de Espécies"));
        
        String[] colunas = {"ID", "Espécie"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        Map<Integer, informacoesEspecies> mapOrdenado = new TreeMap<>(bancoDeEspecies);
        for (Map.Entry<Integer, informacoesEspecies> entry : mapOrdenado.entrySet()) {
            modeloTabela.addRow(new Object[]{entry.getKey(), entry.getValue().forma()});
        }

        tabela = new JTable(modeloTabela);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tabela.setRowHeight(25);
        
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (primeiraEscolha != null && segundaEscolha != null) return;
                int linha = tabela.getSelectedRow();
                if (linha != -1) {
                    int id = (int) tabela.getValueAt(linha, 0);
                    processarSelecao(id);
                }
            }
        });

        JScrollPane scrollTabela = new JScrollPane(tabela);
        scrollTabela.setPreferredSize(new Dimension(200, 0));
        panelEsquerda.add(scrollTabela, BorderLayout.CENTER);
        
        btnReiniciar = new JButton("Nova Simulação");
        btnReiniciar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnReiniciar.setEnabled(false);
        btnReiniciar.addActionListener(e -> reiniciarSimulacao());
        panelEsquerda.add(btnReiniciar, BorderLayout.SOUTH);
        
        add(panelEsquerda, BorderLayout.WEST);

        // 3. Centro (Desenho)
        painelDesenho = new PainelPilha();
        painelDesenho.setBorder(BorderFactory.createTitledBorder("Esquema da Pilha"));
        painelDesenho.setBackground(Color.WHITE);
        add(painelDesenho, BorderLayout.CENTER);

        // 4. Baixo (Texto e Créditos)
        JPanel panelSul = new JPanel(new BorderLayout());
        
        areaResultado = new JTextArea(4, 40);
        areaResultado.setEditable(false);
        
        // Estilo
        areaResultado.setFont(new Font("Monospaced", Font.BOLD, 20));
        areaResultado.setBackground(new Color(240, 240, 240));
        areaResultado.setForeground(new Color(255, 20, 147)); // DeepPink (Rosa Choque)

        JScrollPane scrollRes = new JScrollPane(areaResultado);
        scrollRes.setBorder(BorderFactory.createTitledBorder("Dados da Reação"));
        
        panelSul.add(scrollRes, BorderLayout.CENTER);

        // Label de Créditos
        JLabel lblCreditos = new JLabel("Desenvolvido por: Laura Radünz Pedro, Julia Miranda Lima e Emanuella Bedim Zaniolo");
        lblCreditos.setHorizontalAlignment(SwingConstants.RIGHT);
        lblCreditos.setFont(new Font("SansSerif", Font.ITALIC, 15));
        lblCreditos.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelSul.add(lblCreditos, BorderLayout.SOUTH);

        add(panelSul, BorderLayout.SOUTH);
    }

    private void processarSelecao(int idSelecionado) {
        informacoesEspecies selecaoAtual = bancoDeEspecies.get(idSelecionado);

        if (primeiraEscolha == null) {
            primeiraEscolha = selecaoAtual;
            lblStatus.setText("<html>Selecionado: <b>" + primeiraEscolha.forma() + "</b>. Agora escolha a SEGUNDA espécie.</html>");
            lblStatus.setForeground(new Color(255, 200, 0));
            tabela.clearSelection();
        } else {
            if (selecaoAtual.forma().equals(primeiraEscolha.forma())) {
                JOptionPane.showMessageDialog(this, "Você escolheu a mesma espécie duas vezes!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (primeiraEscolha.formaOposta().equals(selecaoAtual.forma())) {
                 JOptionPane.showMessageDialog(this, "Mesmo elemento químico (" + primeiraEscolha.getSimbolo() + "). Escolha metais diferentes.", "Erro Químico", JOptionPane.ERROR_MESSAGE);
                 return;
            }
            if (primeiraEscolha.tipo().equals(selecaoAtual.tipo())) {
                JOptionPane.showMessageDialog(this, "Conflito de Tipo! Você precisa de um Metal Sólido e um Íon Aquoso.", "Erro de Estado", JOptionPane.ERROR_MESSAGE);
                return;
            }

            segundaEscolha = selecaoAtual;
            finalizarSimulacao();
        }
    }

    private void finalizarSimulacao() {
        lblStatus.setText("Simulação Concluída! Lâmpada Acesa.");
        lblStatus.setForeground(new Color(0, 150, 0));
        btnReiniciar.setEnabled(true);
        tabela.setEnabled(false);

        informacoesEspecies anodoOxida;
        informacoesEspecies catodoReduz;

        // Determina quem oxida e quem reduz (Comparando os metais)
        informacoesEspecies metal1 = primeiraEscolha.tipo().equals("reduzida") ? primeiraEscolha : bancoDeEspecies.get(getKeyByValue(primeiraEscolha.formaOposta()));
        informacoesEspecies metal2 = segundaEscolha.tipo().equals("reduzida") ? segundaEscolha : bancoDeEspecies.get(getKeyByValue(segundaEscolha.formaOposta()));

        if (metal1.potencial() > metal2.potencial()) {
            catodoReduz = metal1;
            anodoOxida = metal2;
        } else {
            catodoReduz = metal2;
            anodoOxida = metal1;
        }

        double ddp = catodoReduz.potencial() - anodoOxida.potencial();
        String equacao = anodoOxida.forma() + " + " + catodoReduz.formaOposta() + " -> " + anodoOxida.formaOposta() + " + " + catodoReduz.forma();

        StringBuilder sb = new StringBuilder();
        sb.append(" EQUAÇÃO GLOBAL: ").append(equacao).append("\n");
        sb.append(String.format(" DDP DA PILHA: %.2f V", ddp));
        
        areaResultado.setText(sb.toString());

        painelDesenho.setDadosPilha(catodoReduz, anodoOxida);
    }
    
    private int getKeyByValue(String forma) {
        for (Map.Entry<Integer, informacoesEspecies> entry : bancoDeEspecies.entrySet()) {
            if (entry.getValue().forma().equals(forma)) return entry.getKey();
        }
        return -1;
    }

    private void reiniciarSimulacao() {
        primeiraEscolha = null;
        segundaEscolha = null;
        areaResultado.setText("");
        lblStatus.setText("Clique na tabela para selecionar a PRIMEIRA espécie.");
        lblStatus.setForeground(Color.WHITE);
        tabela.setEnabled(true);
        tabela.clearSelection();
        btnReiniciar.setEnabled(false);
        painelDesenho.resetar();
    }

    // --- PAINEL DE DESENHO DA PILHA ---
    class PainelPilha extends JPanel {
        private informacoesEspecies catodoEsq; 
        private informacoesEspecies anodoDir;  
        private boolean desenhar = false;

        public void setDadosPilha(informacoesEspecies catodo, informacoesEspecies anodo) {
            this.catodoEsq = catodo;
            this.anodoDir = anodo;
            this.desenhar = true;
            repaint();
        }

        public void resetar() {
            this.desenhar = false;
            repaint();
        }
        
        private Color getCorMetal(String simbolo) {
            return switch (simbolo) {
                case "Cu" -> new Color(184, 115, 51);  
                case "Zn" -> new Color(140, 140, 150); 
                case "Au" -> new Color(255, 215, 0);   
                case "Ag" -> new Color(220, 220, 220); 
                case "Mg" -> new Color(200, 200, 200); 
                case "Fe" -> new Color(100, 100, 100); 
                case "Li" -> new Color(180, 180, 180); 
                case "Al" -> new Color(190, 190, 190); 
                case "Ni" -> new Color(150, 150, 150); 
                case "Pb" -> new Color(100, 100, 110); 
                default -> Color.GRAY;
            };
        }
        
        private Color getCorSolucao(String simbolo) {
             if (simbolo.equals("Cu")) return new Color(135, 206, 250); 
             if (simbolo.equals("Ni")) return new Color(144, 238, 144); 
             return new Color(225, 245, 255); 
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (!desenhar) {
                g2.setColor(Color.LIGHT_GRAY);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 20));
                FontMetrics fm = g2.getFontMetrics();
                String msg = "Selecione os elementos para visualizar a pilha.";
                g2.drawString(msg, (getWidth() - fm.stringWidth(msg))/2, getHeight()/2);
                return;
            }

            int w = getWidth();
            int h = getHeight();
            int centroY = h / 2 + 10;
            int bequerW = 180;
            int bequerH = 220;
            int offsetEsq = w/2 - 250; 
            int offsetDir = w/2 + 70;

            // --- 1. Copos (Béqueres) e Solução ---
            drawBequer(g2, offsetEsq, centroY - 100, bequerW, bequerH, getCorSolucao(catodoEsq.getSimbolo()));
            drawBequer(g2, offsetDir, centroY - 100, bequerW, bequerH, getCorSolucao(anodoDir.getSimbolo()));

            // --- 2. Eletrodos ---
            g2.setColor(getCorMetal(catodoEsq.getSimbolo()));
            g2.fillRect(offsetEsq + 60, centroY - 80, 60, 180);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(offsetEsq + 60, centroY - 80, 60, 180);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 24));
            g2.drawString(catodoEsq.getSimbolo(), offsetEsq + 75, centroY + 20);

            g2.setColor(getCorMetal(anodoDir.getSimbolo()));
            g2.fillRect(offsetDir + 60, centroY - 80, 60, 180); 
            g2.setColor(Color.BLACK);
            g2.drawRect(offsetDir + 60, centroY - 80, 60, 180);
            g2.setColor(Color.WHITE);
            g2.drawString(anodoDir.getSimbolo(), offsetDir + 75, centroY + 20);

            // --- 3. Ponte Salina ---
            g2.setColor(new Color(230, 230, 220)); 
            g2.setStroke(new BasicStroke(25, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(w/2 - 90, centroY - 60, 180, 150, 0, 180); 
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawArc(w/2 - 102, centroY - 60, 204, 170, 0, 180); 
            g2.drawArc(w/2 - 78, centroY - 60, 156, 130, 0, 180);  
            g2.setColor(Color.WHITE);
            g2.fillOval(w/2 - 102, centroY + 15, 24, 24);
            g2.fillOval(w/2 + 78, centroY + 15, 24, 24);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("ponte salina", w/2 - 35, centroY - 5);

            // --- 4. Fios e Lâmpada ---
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3));
            
            // Fio Esquerda
            int lampX = w/2 - 25;
            int lampY = centroY - 230;

            g2.drawLine(offsetEsq + 90, centroY - 80, offsetEsq + 90, centroY - 180); 
            g2.drawLine(offsetEsq + 90, centroY - 180, w/2 - 30, centroY - 180); 
            
            // Fio Direita
            g2.drawLine(offsetDir + 90, centroY - 80, offsetDir + 90, centroY - 180); 
            g2.drawLine(offsetDir + 90, centroY - 180, w/2 + 30, centroY - 180);

            // LÂMPADA ACESA
            g2.setColor(Color.YELLOW);
            g2.fillOval(lampX, lampY, 50, 50); 
            g2.setColor(Color.ORANGE); 
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(lampX, lampY, 50, 50); 
            g2.setColor(Color.ORANGE);
            g2.drawLine(w/2, lampY - 10, w/2, lampY - 25);
            g2.drawLine(w/2 - 30, lampY + 10, w/2 - 45, lampY);
            g2.drawLine(w/2 + 30, lampY + 10, w/2 + 45, lampY);
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(lampX + 10, lampY + 48, 30, 20);
            g2.setColor(Color.BLACK);
            g2.drawString("lâmpada acesa", w/2 - 45, lampY - 30);

            // --- 5. Informações e Elétrons ---
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            // Seta Esquerda
            g2.drawString("e-", offsetEsq + 200, centroY - 190);
            drawArrow(g2, offsetEsq + 150, centroY - 180, offsetEsq + 100, centroY - 180);
            // Seta Direita
            g2.drawString("e-", offsetDir + 5, centroY - 190);
            drawArrow(g2, offsetDir + 80, centroY - 180, offsetDir + 30, centroY - 180);

            // Textos Descritivos
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            // LADO ESQUERDO
            g2.setColor(Color.BLACK);
            String txtMetEsq = catodoEsq.forma() + " metálico";
            g2.drawString(txtMetEsq, offsetEsq + 30, centroY - 260);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString("Cátodo", offsetEsq + 65, centroY - 240);
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.drawString("(polo positivo", offsetEsq + 55, centroY - 220);
            g2.drawString("onde ocorre a", offsetEsq + 55, centroY - 205);
            g2.drawString("redução)", offsetEsq + 70, centroY - 190);
            // LADO DIREITO
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            String txtMetDir = anodoDir.forma() + " metálico";
            g2.drawString(txtMetDir, offsetDir + 30, centroY - 260);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString("Ânodo", offsetDir + 65, centroY - 240);
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.drawString("(polo negativo", offsetDir + 55, centroY - 220);
            g2.drawString("onde ocorre a", offsetDir + 55, centroY - 205);
            g2.drawString("oxidação)", offsetDir + 70, centroY - 190);

            // ÍONS
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(catodoEsq.getSimboloIon(), offsetEsq + 20, centroY + 80);
            g2.drawString("SO4 2-", offsetEsq + 110, centroY + 60);
            g2.drawString(anodoDir.getSimboloIon(), offsetDir + 120, centroY + 100);
            g2.drawString("SO4 2-", offsetDir + 30, centroY + 80);
        }

        private void drawBequer(Graphics2D g2, int x, int y, int w, int h, Color corLiq) {
            // Vidro
            g2.setColor(new Color(240, 248, 255, 100));
            g2.fillRoundRect(x, y, w, h, 30, 30);
            // Liquido
            g2.setColor(corLiq);
            g2.fillRoundRect(x + 5, y + 80, w - 10, h - 85, 25, 25);
            g2.fillRect(x + 5, y + 80, w - 10, 20); 
            g2.setColor(corLiq.darker());
            g2.drawOval(x + 5, y + 70, w - 10, 20); 
            
            // Contorno Bequer
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(x, y, w, h, 30, 30);
            g2.drawOval(x, y - 10, w, 20); 
        }

        private void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
            g2.drawLine(x1, y1, x2, y2);
            int arrowSize = 6;
            if (x1 < x2) { 
                 g2.fillPolygon(new int[]{x2, x2 - arrowSize, x2 - arrowSize}, new int[]{y2, y2 - arrowSize, y2 + arrowSize}, 3);
            } else { 
                 g2.fillPolygon(new int[]{x2, x2 + arrowSize, x2 + arrowSize}, new int[]{y2, y2 - arrowSize, y2 + arrowSize}, 3);
            }
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new SimuladorSwing().setVisible(true));
    }
}
