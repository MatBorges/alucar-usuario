package br.ufms.facom.alucar.view;

import br.ufms.facom.alucar.controller.Controladora;
import br.ufms.facom.alucar.controller.ValidacaoException;
import br.ufms.facom.alucar.dao.DAOException;
import br.ufms.facom.alucar.model.Locacao;
import br.ufms.facom.alucar.model.Usuario;
import br.ufms.facom.alucar.model.ValoresDaDevolucao;
import br.ufms.facom.alucar.util.MoedaUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Camada de Interface com o Usuario do caso de uso Registrar Devolucao (RF09).
 *
 * A sequencia segue o DSS: o atendente informa a placa, o sistema devolve os
 * dados da locacao ativa, o atendente informa a quilometragem final e a
 * vistoria, pede o calculo dos valores e so entao confirma ou cancela.
 * Nenhuma regra de negocio mora aqui.
 */
public class TelaRegistrarDevolucao extends JFrame {

    private static final Color AZUL_CABECALHO = new Color(31, 95, 145);
    private static final Color VERDE = new Color(0, 110, 60);
    private static final Color VERMELHO = new Color(170, 30, 30);

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Controladora controladora = new Controladora();

    private JComboBox<Usuario> comboAtendente;
    private JTextField campoPlaca;
    private JButton botaoBuscar;

    private JLabel rotuloCliente;
    private JLabel rotuloVeiculo;
    private JLabel rotuloPeriodo;
    private JLabel rotuloKmInicial;
    private JLabel rotuloCaucao;

    private JTextField campoKmFinal;
    private JTextArea campoVistoria;
    private JTextField campoCustoAvarias;

    private JLabel rotuloDiarias;
    private JLabel rotuloAtraso;
    private JLabel rotuloMulta;
    private JLabel rotuloAvaria;
    private JLabel rotuloValorFinal;
    private JLabel rotuloCaucaoDevolver;
    private JLabel rotuloValorAPagar;
    private JLabel rotuloDestinoVeiculo;

    private JButton botaoCalcular;
    private JButton botaoConfirmar;
    private JButton botaoCancelar;

    private JLabel rotuloStatus;

    /** Locacao devolvida por buscaLocacaoAtiva. Sem ela nao ha devolucao. */
    private Locacao locacaoAtiva;

    public TelaRegistrarDevolucao() {
        super("Registrar Devolucao de Veiculo - Alucar");
        montarInterface();
        carregarAtendentes();
        habilitarCamposDaDevolucao(false);
    }

    private void montarInterface() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 8));

        add(criarCabecalho(), BorderLayout.NORTH);
        add(criarPainelCentral(), BorderLayout.CENTER);
        add(criarBarraStatus(), BorderLayout.SOUTH);

        setSize(980, 720);
        setMinimumSize(new Dimension(900, 660));
        setLocationRelativeTo(null);
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(AZUL_CABECALHO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel titulo = new JLabel("Registrar Devolucao de Veiculo - Alucar");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.PLAIN, 14f));

        JLabel modulo = new JLabel("Modulo de Atendimento e Locacao (RF09)");
        modulo.setForeground(Color.WHITE);
        modulo.setFont(modulo.getFont().deriveFont(Font.PLAIN, 12f));

        cabecalho.add(titulo, BorderLayout.WEST);
        cabecalho.add(modulo, BorderLayout.EAST);
        return cabecalho;
    }

    private JPanel criarPainelCentral() {
        JPanel central = new JPanel(new BorderLayout(0, 10));
        central.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        JPanel topo = new JPanel(new BorderLayout(0, 8));
        topo.add(criarPainelIdentificacao(), BorderLayout.NORTH);
        topo.add(criarPainelVistoria(), BorderLayout.CENTER);

        central.add(topo, BorderLayout.NORTH);
        central.add(criarPainelValores(), BorderLayout.CENTER);
        central.add(criarPainelBotoes(), BorderLayout.SOUTH);
        return central;
    }

    private JPanel criarPainelIdentificacao() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("1. Identificacao da Locacao"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        comboAtendente = new JComboBox<>();
        comboAtendente.addActionListener(e -> autenticarAtendenteSelecionado());

        campoPlaca = new JTextField(10);
        campoPlaca.addActionListener(e -> buscarLocacaoAtiva());

        botaoBuscar = new JButton("Buscar Locacao");
        botaoBuscar.addActionListener(e -> buscarLocacaoAtiva());

        rotuloCliente = criarRotuloDeDado();
        rotuloVeiculo = criarRotuloDeDado();
        rotuloPeriodo = criarRotuloDeDado();
        rotuloKmInicial = criarRotuloDeDado();
        rotuloCaucao = criarRotuloDeDado();

        int linha = 0;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        painel.add(new JLabel("Atendente:"), c);
        c.gridx = 1; c.weightx = 1;
        painel.add(comboAtendente, c);
        c.gridx = 2; c.weightx = 0;
        painel.add(new JLabel("Placa do Veiculo:"), c);
        c.gridx = 3; c.weightx = 1;
        painel.add(criarPainelPlaca(), c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        painel.add(new JLabel("Cliente:"), c);
        c.gridx = 1; c.weightx = 1;
        painel.add(rotuloCliente, c);
        c.gridx = 2; c.weightx = 0;
        painel.add(new JLabel("Veiculo:"), c);
        c.gridx = 3; c.weightx = 1;
        painel.add(rotuloVeiculo, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        painel.add(new JLabel("Periodo:"), c);
        c.gridx = 1; c.weightx = 1;
        painel.add(rotuloPeriodo, c);
        c.gridx = 2; c.weightx = 0;
        painel.add(new JLabel("Km na Retirada:"), c);
        c.gridx = 3; c.weightx = 1;
        painel.add(rotuloKmInicial, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        painel.add(new JLabel("Caucao Retida:"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1;
        painel.add(rotuloCaucao, c);

        return painel;
    }

    private JPanel criarPainelPlaca() {
        JPanel painel = new JPanel(new BorderLayout(4, 0));
        painel.add(campoPlaca, BorderLayout.CENTER);
        painel.add(botaoBuscar, BorderLayout.EAST);
        return painel;
    }

    private JPanel criarPainelVistoria() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("2. Quilometragem e Vistoria"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        campoKmFinal = new JTextField(10);
        campoCustoAvarias = new JTextField("0,00", 10);

        campoVistoria = new JTextArea(3, 40);
        campoVistoria.setLineWrap(true);
        campoVistoria.setWrapStyleWord(true);

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        painel.add(new JLabel("Km Final:"), c);
        c.gridx = 1; c.weightx = 1;
        painel.add(campoKmFinal, c);
        c.gridx = 2; c.weightx = 0;
        painel.add(new JLabel("Custo das Avarias (R$):"), c);
        c.gridx = 3; c.weightx = 1;
        painel.add(campoCustoAvarias, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        painel.add(new JLabel("Vistoria:"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        painel.add(new JScrollPane(campoVistoria), c);

        return painel;
    }

    private JPanel criarPainelValores() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("3. Valores da Devolucao"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 6, 3, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        rotuloDiarias = criarRotuloDeDado();
        rotuloAtraso = criarRotuloDeDado();
        rotuloMulta = criarRotuloDeDado();
        rotuloAvaria = criarRotuloDeDado();
        rotuloCaucaoDevolver = criarRotuloDeDado();
        rotuloDestinoVeiculo = criarRotuloDeDado();

        rotuloValorFinal = new JLabel("R$ 0,00");
        rotuloValorFinal.setFont(rotuloValorFinal.getFont().deriveFont(Font.BOLD, 13f));

        rotuloValorAPagar = new JLabel("R$ 0,00");
        rotuloValorAPagar.setFont(rotuloValorAPagar.getFont().deriveFont(Font.BOLD, 15f));
        rotuloValorAPagar.setForeground(VERDE);

        int linha = 0;
        adicionarLinhaDeValor(painel, c, linha++, "Diarias utilizadas:", rotuloDiarias,
                "Dias de atraso:", rotuloAtraso);
        adicionarLinhaDeValor(painel, c, linha++, "Multa por atraso (RN06):", rotuloMulta,
                "Custo das avarias:", rotuloAvaria);
        adicionarLinhaDeValor(painel, c, linha++, "Valor final da locacao:", rotuloValorFinal,
                "Caucao a devolver (RN03):", rotuloCaucaoDevolver);
        adicionarLinhaDeValor(painel, c, linha++, "Veiculo passa para:", rotuloDestinoVeiculo,
                "TOTAL A PAGAR:", rotuloValorAPagar);

        return painel;
    }

    private void adicionarLinhaDeValor(JPanel painel, GridBagConstraints c, int linha,
                                       String r1, JLabel v1, String r2, JLabel v2) {
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        painel.add(new JLabel(r1), c);
        c.gridx = 1; c.weightx = 1;
        painel.add(v1, c);
        c.gridx = 2; c.weightx = 0;
        painel.add(new JLabel(r2), c);
        c.gridx = 3; c.weightx = 1;
        painel.add(v2, c);
    }

    private JPanel criarPainelBotoes() {
        JPanel botoes = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        botaoCalcular = new JButton("Calcular Valores");
        botaoConfirmar = new JButton("Confirmar Devolucao");
        botaoCancelar = new JButton("Cancelar Devolucao");
        JButton botaoFechar = new JButton("Fechar");

        botaoCalcular.addActionListener(e -> calcularValorFinal());
        botaoConfirmar.addActionListener(e -> confirmarDevolucao());
        botaoCancelar.addActionListener(e -> cancelarDevolucao());
        botaoFechar.addActionListener(e -> dispose());

        c.gridx = 0; botoes.add(botaoCalcular, c);
        c.gridx = 1; botoes.add(botaoConfirmar, c);
        c.gridx = 2; botoes.add(botaoCancelar, c);
        c.gridx = 3; botoes.add(botaoFechar, c);

        return botoes;
    }

    private JPanel criarBarraStatus() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createEmptyBorder(4, 12, 8, 12));

        rotuloStatus = new JLabel("Informe a placa do veiculo devolvido.");
        rotuloStatus.setHorizontalAlignment(SwingConstants.LEFT);
        rotuloStatus.setFont(rotuloStatus.getFont().deriveFont(Font.PLAIN, 12f));

        barra.add(rotuloStatus, BorderLayout.CENTER);
        return barra;
    }

    private JLabel criarRotuloDeDado() {
        JLabel rotulo = new JLabel("-");
        rotulo.setFont(rotulo.getFont().deriveFont(Font.PLAIN, 12f));
        return rotulo;
    }

    // ------------------------------------------------------------------
    // Eventos de sistema (um metodo por mensagem do DSS)
    // ------------------------------------------------------------------

    private void carregarAtendentes() {
        try {
            comboAtendente.removeAllItems();
            List<Usuario> atendentes = controladora.listarAtendentesAtivos();
            for (Usuario u : atendentes) {
                comboAtendente.addItem(u);
            }
            autenticarAtendenteSelecionado();
        } catch (DAOException e) {
            exibirErro("Erro ao carregar os atendentes: " + e.getMessage());
        }
    }

    private void autenticarAtendenteSelecionado() {
        Usuario atendente = (Usuario) comboAtendente.getSelectedItem();
        if (atendente == null) {
            return;
        }
        try {
            controladora.autenticarAtendente(atendente.getMatricula());
        } catch (ValidacaoException e) {
            exibirAviso(e.getMessage());
        } catch (DAOException e) {
            exibirErro("Erro ao identificar o atendente: " + e.getMessage());
        }
    }

    /** DSS mensagem 2: dadosLocacao = buscaLocacaoAtiva(placa) */
    private void buscarLocacaoAtiva() {
        limparValores();
        locacaoAtiva = null;

        try {
            locacaoAtiva = controladora.buscaLocacaoAtiva(campoPlaca.getText());

            rotuloCliente.setText(locacaoAtiva.getCliente().getNome());
            rotuloVeiculo.setText(locacaoAtiva.getVeiculo().getPlaca() + "  "
                    + locacaoAtiva.getVeiculo().getMarca() + " "
                    + locacaoAtiva.getVeiculo().getModelo()
                    + "  |  diaria " + MoedaUtil.formatar(locacaoAtiva.getVeiculo().getValorDiaria()));

            rotuloPeriodo.setText("retirada em "
                    + locacaoAtiva.getDataRetirada().format(FORMATO_DATA_HORA)
                    + "   |   devolucao prevista para "
                    + locacaoAtiva.getDataDevolucaoPrevista().format(FORMATO_DATA));

            rotuloKmInicial.setText(locacaoAtiva.getKmInicial() + " km");
            rotuloCaucao.setText(MoedaUtil.formatar(locacaoAtiva.getValorCaucao()));

            campoKmFinal.setText(String.valueOf(locacaoAtiva.getKmInicial()));
            habilitarCamposDaDevolucao(true);
            campoKmFinal.requestFocusInWindow();

            rotuloStatus.setText("Locacao #" + locacaoAtiva.getIdLocacao()
                    + " encontrada. Informe a quilometragem final e a vistoria.");

        } catch (ValidacaoException e) {
            limparDadosDaLocacao();
            habilitarCamposDaDevolucao(false);
            rotuloStatus.setText("Nenhuma locacao ativa para esse veiculo.");
            exibirAviso(e.getMessage());

        } catch (DAOException e) {
            exibirErro("Erro ao buscar a locacao: " + e.getMessage());
        }
    }

    /** DSS mensagem 5: valores = calculaValorFinal(placa, kmFinal, custoAvarias) */
    private void calcularValorFinal() {
        if (naoHaLocacaoSelecionada()) {
            return;
        }
        try {
            ValoresDaDevolucao valores = controladora.calculaValorFinal(
                    locacaoAtiva.getVeiculo().getPlaca(),
                    lerKmFinal(),
                    lerCustoAvarias());

            exibirValores(valores);
            rotuloStatus.setText("Valores calculados. Confira e confirme a devolucao.");

        } catch (ValidacaoException e) {
            limparValores();
            exibirAviso(e.getMessage());
        } catch (DAOException e) {
            exibirErro(e.getMessage());
        }
    }

    private void exibirValores(ValoresDaDevolucao v) {
        rotuloDiarias.setText(v.getDiariasUtilizadas() + " diaria(s)");

        rotuloAtraso.setText(v.houveAtraso()
                ? v.getDiasDeAtraso() + " dia(s) de atraso"
                : "no prazo");
        rotuloAtraso.setForeground(v.houveAtraso() ? VERMELHO : VERDE);

        rotuloMulta.setText(MoedaUtil.formatar(v.getMultaPorAtraso()));
        rotuloMulta.setForeground(v.houveAtraso() ? VERMELHO : Color.BLACK);

        rotuloAvaria.setText(MoedaUtil.formatar(v.getCustoAvaria()));
        rotuloValorFinal.setText(MoedaUtil.formatar(v.getValorFinal()));

        rotuloCaucaoDevolver.setText(MoedaUtil.formatar(v.getValorCaucaoADevolver())
                + (v.getValorCaucaoRetido().signum() > 0
                   ? "   (retido: " + MoedaUtil.formatar(v.getValorCaucaoRetido()) + ")"
                   : ""));

        rotuloValorAPagar.setText(MoedaUtil.formatar(v.getValorAPagar()));
        rotuloDestinoVeiculo.setText(v.getSituacaoDestinoDoVeiculo().getDescricao().toUpperCase());
    }

    /** DSS mensagem 9: registrarDevolucao(placa, kmFinal, custoAvarias, descricao) */
    private void confirmarDevolucao() {
        if (naoHaLocacaoSelecionada()) {
            return;
        }
        try {
            int kmFinal = lerKmFinal();
            BigDecimal custoAvarias = lerCustoAvarias();

            ValoresDaDevolucao previa = controladora.calculaValorFinal(
                    locacaoAtiva.getVeiculo().getPlaca(), kmFinal, custoAvarias);

            int resposta = JOptionPane.showConfirmDialog(this,
                    "Confirmar a devolucao do veiculo "
                            + locacaoAtiva.getVeiculo().getPlaca() + "?\n\n"
                            + "Total a pagar: " + MoedaUtil.formatar(previa.getValorAPagar()) + "\n"
                            + "Caucao a devolver: "
                            + MoedaUtil.formatar(previa.getValorCaucaoADevolver()) + "\n"
                            + "O veiculo passara para "
                            + previa.getSituacaoDestinoDoVeiculo().getDescricao().toUpperCase() + ".",
                    "Confirmar Devolucao",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (resposta != JOptionPane.YES_OPTION) {
                return;
            }

            Locacao locacao = controladora.registrarDevolucao(
                    locacaoAtiva.getVeiculo().getPlaca(),
                    kmFinal,
                    custoAvarias,
                    campoVistoria.getText());

            exibirInformacao("Devolucao registrada.\n\n"
                    + "Locacao #" + locacao.getIdLocacao() + " concluida\n"
                    + "Veiculo " + locacao.getVeiculo().getPlaca() + " passou para "
                    + locacao.getVeiculo().getStatus().getDescricao().toUpperCase() + "\n"
                    + "Quilometragem rodada: " + locacao.getQuilometragemRodada() + " km\n"
                    + "Valor final: " + MoedaUtil.formatar(locacao.getValorFinal()));

            limparAtendimento();
            rotuloStatus.setText("Devolucao registrada com sucesso.");

        } catch (ValidacaoException e) {
            exibirAviso(e.getMessage());
        } catch (DAOException e) {
            exibirErro(e.getMessage());
        }
    }

    /** DSS mensagem 7: cancelarDevolucao() */
    private void cancelarDevolucao() {
        if (locacaoAtiva == null) {
            limparAtendimento();
            return;
        }
        int resposta = JOptionPane.showConfirmDialog(this,
                "Cancelar a devolucao em andamento?\n"
                        + "Nada sera gravado e a locacao continuara ATIVA.",
                "Cancelar Devolucao",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (resposta == JOptionPane.YES_OPTION) {
            controladora.cancelarDevolucao();
            limparAtendimento();
            exibirInformacao("Devolucao cancelada.");
            rotuloStatus.setText("Devolucao cancelada. A locacao continua ATIVA.");
        }
    }

    // ------------------------------------------------------------------
    // Apoio da tela
    // ------------------------------------------------------------------

    private boolean naoHaLocacaoSelecionada() {
        if (locacaoAtiva == null) {
            exibirAviso("Busque primeiro a locacao ativa pela placa do veiculo.");
            return true;
        }
        return false;
    }

    private int lerKmFinal() throws ValidacaoException {
        String texto = campoKmFinal.getText();
        if (texto == null || texto.isBlank()) {
            throw new ValidacaoException("Informe a quilometragem final do veiculo.");
        }
        try {
            return Integer.parseInt(texto.trim().replaceAll("[.\\s]", ""));
        } catch (NumberFormatException e) {
            throw new ValidacaoException("A quilometragem final deve ser um numero inteiro.");
        }
    }

    private BigDecimal lerCustoAvarias() throws ValidacaoException {
        String texto = campoCustoAvarias.getText();
        if (texto == null || texto.isBlank()) {
            return BigDecimal.ZERO;
        }
        return controladora.converterValorMonetario(texto, "custo das avarias");
    }

    private void habilitarCamposDaDevolucao(boolean habilitado) {
        campoKmFinal.setEnabled(habilitado);
        campoVistoria.setEnabled(habilitado);
        campoCustoAvarias.setEnabled(habilitado);
        botaoCalcular.setEnabled(habilitado);
        botaoConfirmar.setEnabled(habilitado);
        botaoCancelar.setEnabled(habilitado);
    }

    private void limparDadosDaLocacao() {
        rotuloCliente.setText("-");
        rotuloVeiculo.setText("-");
        rotuloPeriodo.setText("-");
        rotuloKmInicial.setText("-");
        rotuloCaucao.setText("-");
    }

    private void limparValores() {
        rotuloDiarias.setText("-");
        rotuloAtraso.setText("-");
        rotuloAtraso.setForeground(Color.BLACK);
        rotuloMulta.setText("-");
        rotuloMulta.setForeground(Color.BLACK);
        rotuloAvaria.setText("-");
        rotuloValorFinal.setText("R$ 0,00");
        rotuloCaucaoDevolver.setText("-");
        rotuloValorAPagar.setText("R$ 0,00");
        rotuloDestinoVeiculo.setText("-");
    }

    private void limparAtendimento() {
        locacaoAtiva = null;
        campoPlaca.setText("");
        campoKmFinal.setText("");
        campoVistoria.setText("");
        campoCustoAvarias.setText("0,00");
        limparDadosDaLocacao();
        limparValores();
        habilitarCamposDaDevolucao(false);
    }

    private void exibirInformacao(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Alucar", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exibirAviso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Alucar", JOptionPane.WARNING_MESSAGE);
    }

    private void exibirErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Alucar", JOptionPane.ERROR_MESSAGE);
    }
}
