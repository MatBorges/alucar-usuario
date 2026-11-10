package br.ufms.facom.alucar.view;

import br.ufms.facom.alucar.controller.Controladora;
import br.ufms.facom.alucar.controller.ValidacaoException;
import br.ufms.facom.alucar.dao.DAOException;
import br.ufms.facom.alucar.model.Cliente;
import br.ufms.facom.alucar.model.Locacao;
import br.ufms.facom.alucar.model.StatusLocacao;
import br.ufms.facom.alucar.model.Usuario;
import br.ufms.facom.alucar.model.Veiculo;
import br.ufms.facom.alucar.util.MoedaUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.text.MaskFormatter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Camada de Interface com o Usuario do caso de uso Realizar Locacao (RF08).
 *
 * A sequencia de interacao segue o DSS do caso de uso: o atendente identifica
 * o cliente pela CNH, o sistema valida a habilitacao e devolve os dados do
 * cliente, o atendente consulta os veiculos disponiveis, escolhe um, confirma
 * o endereco, informa a previsao de retorno e o caucao, e so entao efetua a
 * locacao. A tela nao contem regra de negocio: tudo passa pela Controladora.
 */
public class TelaRealizarLocacao extends JFrame {

    private static final Color AZUL_CABECALHO = new Color(31, 95, 145);
    private static final Color VERDE = new Color(0, 110, 60);
    private static final String TODAS = "(todas)";
    private static final DateTimeFormatter FORMATO_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Controladora controladora = new Controladora();
    private final LocacaoTableModel tableModel = new LocacaoTableModel();

    private JComboBox<Usuario> comboAtendente;
    private JFormattedTextField campoCnh;
    private JButton botaoValidarCnh;
    private JComboBox<Veiculo> comboVeiculo;
    private JTextField campoEndereco;
    private JFormattedTextField campoDataDevolucao;
    private JTextField campoCaucao;

    private JLabel rotuloInfoCliente;
    private JLabel rotuloInfoVeiculo;
    private JLabel rotuloEstimativa;

    private JComboBox<Object> comboFiltroStatus;
    private JTable tabelaLocacoes;
    private JLabel rotuloStatus;

    /** Cliente devolvido por validaCNH. Enquanto for nulo, nao ha locacao a efetuar. */
    private Cliente clienteValidado;

    /** Veiculo devolvido por buscarDadosVeiculo. */
    private Veiculo veiculoSelecionado;

    public TelaRealizarLocacao() {
        super("Realizar Locacao de Veiculos - Alucar");
        montarInterface();
        carregarDados();
        consultarLocacoes();
    }

    private void montarInterface() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 8));

        add(criarCabecalho(), BorderLayout.NORTH);
        add(criarPainelCentral(), BorderLayout.CENTER);
        add(criarBarraStatus(), BorderLayout.SOUTH);

        setSize(1000, 760);
        setMinimumSize(new Dimension(920, 670));
        setLocationRelativeTo(null);
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(AZUL_CABECALHO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel titulo = new JLabel("Realizar Locacao de Veiculos - Alucar");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.PLAIN, 14f));

        JLabel modulo = new JLabel("Modulo de Atendimento e Locacao (RF08)");
        modulo.setForeground(Color.WHITE);
        modulo.setFont(modulo.getFont().deriveFont(Font.PLAIN, 12f));

        cabecalho.add(titulo, BorderLayout.WEST);
        cabecalho.add(modulo, BorderLayout.EAST);
        return cabecalho;
    }

    private JPanel criarPainelCentral() {
        JPanel central = new JPanel(new BorderLayout(0, 10));
        central.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        central.add(criarFormulario(), BorderLayout.NORTH);
        central.add(criarPainelLista(), BorderLayout.CENTER);
        return central;
    }

    private JPanel criarFormulario() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createTitledBorder("Dados da Nova Locacao"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        comboAtendente = new JComboBox<>();
        campoCnh = criarCampoComMascara("###########");
        botaoValidarCnh = new JButton("Validar CNH");
        comboVeiculo = new JComboBox<>();
        campoEndereco = new JTextField(24);
        campoDataDevolucao = criarCampoComMascara("##/##/####");
        campoCaucao = new JTextField(10);

        campoCaucao.setText(MoedaUtil.formatar(controladora.getCaucaoMinima())
                .replace("R$", "").trim());

        rotuloInfoCliente = new JLabel("Informe a CNH do cliente e clique em Validar CNH.");
        rotuloInfoCliente.setFont(rotuloInfoCliente.getFont().deriveFont(Font.PLAIN, 11f));
        rotuloInfoCliente.setForeground(Color.DARK_GRAY);

        rotuloInfoVeiculo = new JLabel(" ");
        rotuloInfoVeiculo.setFont(rotuloInfoVeiculo.getFont().deriveFont(Font.PLAIN, 11f));

        rotuloEstimativa = new JLabel("R$ 0,00");
        rotuloEstimativa.setFont(rotuloEstimativa.getFont().deriveFont(Font.BOLD, 13f));
        rotuloEstimativa.setForeground(VERDE);

        comboAtendente.addActionListener(e -> autenticarAtendenteSelecionado());
        botaoValidarCnh.addActionListener(e -> validarCnh());
        campoCnh.addActionListener(e -> validarCnh());
        comboVeiculo.addActionListener(e -> selecionarVeiculo());
        campoDataDevolucao.addActionListener(e -> atualizarEstimativa());

        int linha = 0;

        // Pre-condicao do contrato: atendente autenticado.
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Atendente:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(comboAtendente, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("CNH do Cliente:"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(criarPainelCnh(), c);

        // DSS: dadosCliente = validaCNH(CNH)
        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Cliente:"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1;
        formulario.add(rotuloInfoCliente, c);
        c.gridwidth = 1;

        // Pos-condicao do contrato: o endereco do atendimento e registrado.
        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Endereco:"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1;
        formulario.add(campoEndereco, c);
        c.gridwidth = 1;

        // DSS: consultarVeiculosDisponiveis() / buscarDadosVeiculo(placa)
        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Veiculo (Disponivel):"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(comboVeiculo, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("Dados do Veiculo:"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(rotuloInfoVeiculo, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Previsao de Retorno:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(campoDataDevolucao, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("Valor Estimado:"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(rotuloEstimativa, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Caucao (R$):"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(campoCaucao, c);
        c.gridx = 2; c.gridwidth = 2; c.weightx = 1;
        formulario.add(criarRotuloCaucaoMinima(), c);
        c.gridwidth = 1;

        linha++;
        c.gridx = 0; c.gridy = linha; c.gridwidth = 4;
        formulario.add(criarPainelBotoes(), c);

        return formulario;
    }

    private JPanel criarPainelCnh() {
        JPanel painel = new JPanel(new BorderLayout(4, 0));
        painel.add(campoCnh, BorderLayout.CENTER);
        painel.add(botaoValidarCnh, BorderLayout.EAST);
        return painel;
    }

    private JLabel criarRotuloCaucaoMinima() {
        JLabel rotulo = new JLabel("Garantia minima exigida (RN03): "
                + MoedaUtil.formatar(controladora.getCaucaoMinima()));
        rotulo.setFont(rotulo.getFont().deriveFont(Font.PLAIN, 11f));
        rotulo.setForeground(Color.DARK_GRAY);
        return rotulo;
    }

    private JPanel criarPainelBotoes() {
        JPanel botoes = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 2, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JButton botaoConfirmar = new JButton("Confirmar Locacao");
        JButton botaoCalcular = new JButton("Recalcular Estimativa");
        JButton botaoLimpar = new JButton("Limpar Atendimento");
        JButton botaoAtualizar = new JButton("Recarregar Dados");
        JButton botaoFechar = new JButton("Fechar");

        botaoConfirmar.addActionListener(e -> confirmarLocacao());
        botaoCalcular.addActionListener(e -> atualizarEstimativa());
        botaoLimpar.addActionListener(e -> limparAtendimento());
        botaoAtualizar.addActionListener(e -> carregarDados());
        botaoFechar.addActionListener(e -> dispose());

        c.gridx = 0; botoes.add(botaoConfirmar, c);
        c.gridx = 1; botoes.add(botaoCalcular, c);
        c.gridx = 2; botoes.add(botaoLimpar, c);
        c.gridx = 3; botoes.add(botaoAtualizar, c);
        c.gridx = 4; botoes.add(botaoFechar, c);

        return botoes;
    }

    private JPanel criarPainelLista() {
        JPanel painel = new JPanel(new BorderLayout(0, 6));
        painel.setBorder(BorderFactory.createTitledBorder("Historico de Locacoes"));

        JPanel barraFiltros = new JPanel(new GridBagLayout());
        barraFiltros.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.anchor = GridBagConstraints.WEST;

        comboFiltroStatus = new JComboBox<>(new Object[]{
                TODAS, StatusLocacao.ATIVA, StatusLocacao.CONCLUIDA, StatusLocacao.CANCELADA});
        comboFiltroStatus.addActionListener(e -> consultarLocacoes());

        JButton botaoConsultar = new JButton("Consultar");
        botaoConsultar.addActionListener(e -> consultarLocacoes());

        JButton botaoCancelar = new JButton("Cancelar Locacao Selecionada");
        botaoCancelar.addActionListener(e -> cancelarLocacaoSelecionada());

        c.gridx = 0; barraFiltros.add(new JLabel("Filtrar por Status:"), c);
        c.gridx = 1; barraFiltros.add(comboFiltroStatus, c);
        c.gridx = 2; barraFiltros.add(botaoConsultar, c);
        c.gridx = 3; c.weightx = 1; c.anchor = GridBagConstraints.EAST;
        barraFiltros.add(botaoCancelar, c);

        tabelaLocacoes = new JTable(tableModel);
        tabelaLocacoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaLocacoes.setRowHeight(22);
        tabelaLocacoes.getTableHeader().setReorderingAllowed(false);

        painel.add(barraFiltros, BorderLayout.NORTH);
        painel.add(new JScrollPane(tabelaLocacoes), BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarBarraStatus() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createEmptyBorder(4, 12, 8, 12));

        rotuloStatus = new JLabel("Pronto.");
        rotuloStatus.setHorizontalAlignment(SwingConstants.LEFT);
        rotuloStatus.setFont(rotuloStatus.getFont().deriveFont(Font.PLAIN, 12f));

        barra.add(rotuloStatus, BorderLayout.CENTER);
        return barra;
    }

    private JFormattedTextField criarCampoComMascara(String mascara) {
        try {
            MaskFormatter formatador = new MaskFormatter(mascara);
            formatador.setPlaceholderCharacter(' ');
            return new JFormattedTextField(formatador);
        } catch (ParseException e) {
            throw new IllegalStateException("Mascara invalida: " + mascara, e);
        }
    }

    // ------------------------------------------------------------------
    // Eventos de sistema (um metodo por mensagem do DSS)
    // ------------------------------------------------------------------

    private void carregarDados() {
        try {
            comboAtendente.removeAllItems();
            List<Usuario> atendentes = controladora.listarAtendentesAtivos();
            for (Usuario u : atendentes) {
                comboAtendente.addItem(u);
            }

            // DSS: listaVeiculos = consultarVeiculosDisponiveis()
            comboVeiculo.removeAllItems();
            List<Veiculo> veiculos = controladora.consultarVeiculosDisponiveis();
            for (Veiculo v : veiculos) {
                comboVeiculo.addItem(v);
            }

            autenticarAtendenteSelecionado();
            selecionarVeiculo();
            rotuloStatus.setText("Dados carregados. Veiculos disponiveis: " + veiculos.size() + ".");

        } catch (DAOException e) {
            rotuloStatus.setText("Falha ao carregar dados do banco de dados.");
            exibirErro("Erro ao carregar dados para locacao: " + e.getMessage());
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

    /** DSS mensagem 2: dadosCliente = validaCNH(CNH) */
    private void validarCnh() {
        clienteValidado = null;
        try {
            clienteValidado = controladora.validaCNH(campoCnh.getText());

            rotuloInfoCliente.setText(clienteValidado.getNome()
                    + "  |  CNH valida ate "
                    + clienteValidado.getValidadeCnh().format(FORMATO_BR));
            rotuloInfoCliente.setForeground(VERDE);

            campoEndereco.setText(clienteValidado.getEndereco() == null
                    ? "" : clienteValidado.getEndereco());

            rotuloStatus.setText("Cliente identificado: " + clienteValidado.getNome() + ".");

        } catch (ValidacaoException e) {
            rotuloInfoCliente.setText("CNH invalida, locacao nao autorizada.");
            rotuloInfoCliente.setForeground(Color.RED);
            campoEndereco.setText("");
            rotuloStatus.setText("Validacao da CNH recusada.");
            exibirAviso(e.getMessage());

        } catch (DAOException e) {
            rotuloInfoCliente.setText(" ");
            exibirErro("Erro ao consultar o cliente: " + e.getMessage());
        }
    }

    /** DSS mensagem 5: dadosVeiculo = buscarDadosVeiculo(placa) */
    private void selecionarVeiculo() {
        veiculoSelecionado = null;
        Veiculo escolhido = (Veiculo) comboVeiculo.getSelectedItem();

        if (escolhido == null) {
            rotuloInfoVeiculo.setText("Nenhum veiculo selecionado.");
            rotuloInfoVeiculo.setForeground(Color.DARK_GRAY);
            atualizarEstimativa();
            return;
        }

        try {
            veiculoSelecionado = controladora.buscarDadosVeiculo(escolhido.getPlaca());

            rotuloInfoVeiculo.setText("Diaria: "
                    + MoedaUtil.formatar(veiculoSelecionado.getValorDiaria())
                    + "  |  Km atual: " + veiculoSelecionado.getKmAtual() + " km"
                    + "  |  Categoria: " + veiculoSelecionado.getCategoria().getNome());
            rotuloInfoVeiculo.setForeground(Color.BLACK);

        } catch (ValidacaoException e) {
            rotuloInfoVeiculo.setText(e.getMessage().split("\n")[0]);
            rotuloInfoVeiculo.setForeground(Color.RED);

        } catch (DAOException e) {
            exibirErro("Erro ao consultar o veiculo: " + e.getMessage());
        }

        atualizarEstimativa();
    }

    private void atualizarEstimativa() {
        String dataTexto = campoDataDevolucao.getText();

        if (veiculoSelecionado == null || dataTexto == null
                || dataTexto.isBlank() || dataTexto.contains(" ")) {
            rotuloEstimativa.setText("R$ 0,00");
            return;
        }

        try {
            LocalDate dataRetorno = LocalDate.parse(dataTexto.trim(), FORMATO_BR);
            BigDecimal total = controladora.calcularValorEstimado(veiculoSelecionado, dataRetorno);
            rotuloEstimativa.setText(MoedaUtil.formatar(total));
        } catch (DateTimeParseException ignored) {
            rotuloEstimativa.setText("Data invalida");
        }
    }

    /**
     * DSS mensagem 9:
     * efetuarLocacao(CNH, placa, endereco, dataDevolucaoPrevista, valorCaucao)
     */
    private void confirmarLocacao() {
        if (clienteValidado == null) {
            exibirAviso("Valide a CNH do cliente antes de efetuar a locacao.");
            return;
        }
        if (veiculoSelecionado == null) {
            exibirAviso("Selecione um veiculo disponivel para locacao.");
            return;
        }

        try {
            LocalDate dataDevolucao = controladora.converterDataDoFormulario(
                    campoDataDevolucao.getText(), "data de devolucao prevista");

            BigDecimal caucao = controladora.converterValorMonetario(
                    campoCaucao.getText(), "valor do caucao");

            Locacao locacao = controladora.efetuarLocacao(
                    clienteValidado.getCnh(),
                    veiculoSelecionado.getPlaca(),
                    campoEndereco.getText(),
                    dataDevolucao,
                    caucao
            );

            exibirInformacao("Locacao realizada.\n\n"
                    + "Locacao #" + locacao.getIdLocacao() + "\n"
                    + "Cliente: " + locacao.getCliente().getNome() + "\n"
                    + "Veiculo: " + locacao.getVeiculo().getPlaca()
                    + " (" + locacao.getVeiculo().getModelo() + ") passou para LOCADO\n"
                    + "Diarias: " + locacao.calcularQuantidadeDeDiarias() + "\n"
                    + "Valor estimado: " + MoedaUtil.formatar(locacao.getValorEstimado()));

            limparAtendimento();
            carregarDados();
            consultarLocacoes();

        } catch (ValidacaoException e) {
            exibirAviso(e.getMessage());
        } catch (DAOException e) {
            exibirErro(e.getMessage());
        }
    }

    private void limparAtendimento() {
        clienteValidado = null;
        veiculoSelecionado = null;

        campoCnh.setValue(null);
        campoEndereco.setText("");
        campoDataDevolucao.setValue(null);
        campoCaucao.setText(MoedaUtil.formatar(controladora.getCaucaoMinima())
                .replace("R$", "").trim());

        rotuloInfoCliente.setText("Informe a CNH do cliente e clique em Validar CNH.");
        rotuloInfoCliente.setForeground(Color.DARK_GRAY);
        rotuloEstimativa.setText("R$ 0,00");
        rotuloStatus.setText("Atendimento limpo.");
    }

    private void consultarLocacoes() {
        Object filtro = comboFiltroStatus.getSelectedItem();
        StatusLocacao statusFiltro = null;
        if (filtro instanceof StatusLocacao) {
            statusFiltro = (StatusLocacao) filtro;
        }

        try {
            List<Locacao> locacoes = controladora.listarLocacoesPorStatus(statusFiltro);
            tableModel.definirLocacoes(locacoes);
            rotuloStatus.setText(locacoes.size() + " locacao(oes) encontrada(s).");
        } catch (DAOException e) {
            exibirErro("Erro ao consultar locacoes: " + e.getMessage());
        }
    }

    private void cancelarLocacaoSelecionada() {
        int linha = tabelaLocacoes.getSelectedRow();
        if (linha < 0) {
            exibirAviso("Selecione uma locacao na tabela para cancelar.");
            return;
        }

        Locacao locacao = tableModel.obterLocacao(linha);
        if (locacao == null) {
            return;
        }

        if (locacao.getStatus() != StatusLocacao.ATIVA) {
            exibirAviso("Apenas locacoes com status ATIVA podem ser canceladas.");
            return;
        }

        int resposta = JOptionPane.showConfirmDialog(
                this,
                "Tem certeza que deseja cancelar a locacao #" + locacao.getIdLocacao() + "?\n"
                        + "O veiculo " + locacao.getVeiculo().getPlaca()
                        + " retornara ao status DISPONIVEL.",
                "Confirmar Cancelamento",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (resposta == JOptionPane.YES_OPTION) {
            try {
                controladora.cancelarLocacao(locacao.getIdLocacao(),
                        locacao.getVeiculo().getPlaca());
                exibirInformacao("Locacao cancelada com sucesso.");
                carregarDados();
                consultarLocacoes();
            } catch (DAOException e) {
                exibirErro("Erro ao cancelar locacao: " + e.getMessage());
            }
        }
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
