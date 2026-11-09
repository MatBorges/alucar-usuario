package br.ufms.facom.alucar.view;

import br.ufms.facom.alucar.controller.ControladoraLocacao;
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

public class TelaRealizarLocacao extends JFrame {

    private static final Color AZUL_CABECALHO = new Color(31, 95, 145);
    private static final String TODAS = "(todas)";
    private static final DateTimeFormatter FORMATO_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ControladoraLocacao controladora = new ControladoraLocacao();
    private final LocacaoTableModel tableModel = new LocacaoTableModel();

    private JComboBox<Cliente> comboCliente;
    private JComboBox<Veiculo> comboVeiculo;
    private JComboBox<Usuario> comboAtendente;
    private JFormattedTextField campoDataDevolucao;
    private JTextField campoCaucao;

    private JLabel rotuloInfoCliente;
    private JLabel rotuloInfoVeiculo;
    private JLabel rotuloEstimativa;

    private JComboBox<Object> comboFiltroStatus;
    private JTable tabelaLocacoes;
    private JLabel rotuloStatus;

    public TelaRealizarLocacao() {
        super("Realizar Locacao de Veiculos - Alucar");
        montarInterface();
        carregarCombos();
        consultarLocacoes();
    }

    private void montarInterface() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 8));

        add(criarCabecalho(), BorderLayout.NORTH);
        add(criarPainelCentral(), BorderLayout.CENTER);
        add(criarBarraStatus(), BorderLayout.SOUTH);

        setSize(1000, 740);
        setMinimumSize(new Dimension(920, 650));
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

        comboCliente = new JComboBox<>();
        comboVeiculo = new JComboBox<>();
        comboAtendente = new JComboBox<>();
        campoDataDevolucao = criarCampoComMascara("##/##/####");
        campoCaucao = new JTextField("500,00", 10);

        rotuloInfoCliente = new JLabel(" ");
        rotuloInfoCliente.setFont(rotuloInfoCliente.getFont().deriveFont(Font.PLAIN, 11f));

        rotuloInfoVeiculo = new JLabel(" ");
        rotuloInfoVeiculo.setFont(rotuloInfoVeiculo.getFont().deriveFont(Font.PLAIN, 11f));

        rotuloEstimativa = new JLabel("R$ 0,00");
        rotuloEstimativa.setFont(rotuloEstimativa.getFont().deriveFont(Font.BOLD, 13f));
        rotuloEstimativa.setForeground(new Color(0, 110, 60));

        comboCliente.addActionListener(e -> atualizarInfoCliente());
        comboVeiculo.addActionListener(e -> {
            atualizarInfoVeiculo();
            atualizarEstimativa();
        });
        campoDataDevolucao.addActionListener(e -> atualizarEstimativa());

        int linha = 0;

        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Cliente:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(comboCliente, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("Veiculo (Disponivel):"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(comboVeiculo, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Status CNH:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(rotuloInfoCliente, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("Diaria do Veiculo:"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(rotuloInfoVeiculo, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Atendente:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(comboAtendente, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("Previsao de Retorno:"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(campoDataDevolucao, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Caucao (R$):"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(campoCaucao, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("Valor Estimado:"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(rotuloEstimativa, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.gridwidth = 4;
        formulario.add(criarPainelBotoes(), c);

        return formulario;
    }

    private JPanel criarPainelBotoes() {
        JPanel botoes = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 2, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JButton botaoConfirmar = new JButton("Confirmar Locacao");
        JButton botaoCalcular = new JButton("Recalcular Estimativa");
        JButton botaoAtualizar = new JButton("Recarregar Dados");
        JButton botaoFechar = new JButton("Fechar");

        botaoConfirmar.addActionListener(e -> confirmarLocacao());
        botaoCalcular.addActionListener(e -> atualizarEstimativa());
        botaoAtualizar.addActionListener(e -> carregarCombos());
        botaoFechar.addActionListener(e -> dispose());

        c.gridx = 0; botoes.add(botaoConfirmar, c);
        c.gridx = 1; botoes.add(botaoCalcular, c);
        c.gridx = 2; botoes.add(botaoAtualizar, c);
        c.gridx = 3; botoes.add(botaoFechar, c);

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

        comboFiltroStatus = new JComboBox<>(new Object[]{TODAS, StatusLocacao.ATIVA, StatusLocacao.CONCLUIDA, StatusLocacao.CANCELADA});
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

    private void carregarCombos() {
        try {
            comboCliente.removeAllItems();
            List<Cliente> clientes = controladora.listarClientesAtivos();
            for (Cliente cl : clientes) {
                comboCliente.addItem(cl);
            }

            comboVeiculo.removeAllItems();
            List<Veiculo> veiculos = controladora.listarVeiculosDisponiveis();
            for (Veiculo v : veiculos) {
                comboVeiculo.addItem(v);
            }

            comboAtendente.removeAllItems();
            List<Usuario> atendentes = controladora.listarAtendentesAtivos();
            for (Usuario u : atendentes) {
                comboAtendente.addItem(u);
            }

            atualizarInfoCliente();
            atualizarInfoVeiculo();
            atualizarEstimativa();
            rotuloStatus.setText("Dados carregados com sucesso. Veiculos disponiveis: " + veiculos.size() + ".");

        } catch (DAOException e) {
            rotuloStatus.setText("Falha ao carregar dados do banco de dados.");
            exibirErro("Erro ao carregar dados para locacao: " + e.getMessage());
        }
    }

    private void atualizarInfoCliente() {
        Cliente c = (Cliente) comboCliente.getSelectedItem();
        if (c == null) {
            rotuloInfoCliente.setText("Nenhum cliente selecionado.");
            rotuloInfoCliente.setForeground(Color.DARK_GRAY);
            return;
        }

        LocalDate validade = c.getValidadeCnh();
        if (validade == null || validade.isBefore(LocalDate.now())) {
            rotuloInfoCliente.setText("CNH VENCIDA (" + (validade != null ? validade.format(FORMATO_BR) : "Nao informada") + ") - Impede locacao");
            rotuloInfoCliente.setForeground(Color.RED);
        } else {
            rotuloInfoCliente.setText("CNH Valida ate " + validade.format(FORMATO_BR));
            rotuloInfoCliente.setForeground(new Color(0, 110, 60));
        }
    }

    private void atualizarInfoVeiculo() {
        Veiculo v = (Veiculo) comboVeiculo.getSelectedItem();
        if (v == null) {
            rotuloInfoVeiculo.setText("Nenhum veiculo selecionado.");
            return;
        }

        rotuloInfoVeiculo.setText("Diaria: " + MoedaUtil.formatar(v.getValorDiaria())
                + " | Km atual: " + v.getKmAtual() + " km | Categoria: " + v.getCategoria().getNome());
    }

    private void atualizarEstimativa() {
        Veiculo v = (Veiculo) comboVeiculo.getSelectedItem();
        String dataTxt = campoDataDevolucao.getText();

        if (v == null || dataTxt == null || dataTxt.isBlank() || dataTxt.contains(" ")) {
            rotuloEstimativa.setText("R$ 0,00");
            return;
        }

        try {
            LocalDate dataRetorno = LocalDate.parse(dataTxt.trim(), FORMATO_BR);
            BigDecimal total = controladora.calcularEstimativa(v.getValorDiaria(), dataRetorno);
            rotuloEstimativa.setText(MoedaUtil.formatar(total));
        } catch (DateTimeParseException ignored) {
            rotuloEstimativa.setText("Data invalida");
        }
    }

    private void confirmarLocacao() {
        Cliente cliente = (Cliente) comboCliente.getSelectedItem();
        Veiculo veiculo = (Veiculo) comboVeiculo.getSelectedItem();
        Usuario atendente = (Usuario) comboAtendente.getSelectedItem();

        if (cliente == null) {
            exibirAviso("Selecione um cliente para prosseguir.");
            return;
        }
        if (veiculo == null) {
            exibirAviso("Selecione um veiculo disponivel para locacao.");
            return;
        }
        if (atendente == null) {
            exibirAviso("Selecione o atendente responsavel.");
            return;
        }

        String dataDevolucaoTexto = campoDataDevolucao.getText();
        String valorCaucaoTexto = campoCaucao.getText();

        try {
            Locacao locacao = controladora.realizarLocacao(
                    cliente.getCpf(),
                    veiculo.getPlaca(),
                    atendente.getMatricula(),
                    dataDevolucaoTexto,
                    valorCaucaoTexto
            );

            exibirInformacao("Locacao #" + locacao.getIdLocacao() + " realizada com sucesso!\n"
                    + "Veiculo " + veiculo.getPlaca() + " (" + veiculo.getModelo() + ") foi alterado para LOCADO.\n"
                    + "Valor estimado: " + MoedaUtil.formatar(locacao.getValorEstimado()));

            carregarCombos();
            consultarLocacoes();

        } catch (ValidacaoException e) {
            exibirAviso(e.getMessage());
        } catch (DAOException e) {
            exibirErro(e.getMessage());
        }
    }

    private void consultarLocacoes() {
        Object filtro = comboFiltroStatus.getSelectedItem();
        StatusLocacao statusFiltro = null;
        if (filtro instanceof StatusLocacao) {
            statusFiltro = (StatusLocacao) filtro;
        }

        try {
            List<Locacao> locacoes = controladora.listarPorStatus(statusFiltro);
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
                        + "O veiculo " + locacao.getVeiculo().getPlaca() + " retornara ao status DISPONIVEL.",
                "Confirmar Cancelamento",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (resposta == JOptionPane.YES_OPTION) {
            try {
                controladora.cancelarLocacao(locacao.getIdLocacao(), locacao.getVeiculo().getPlaca());
                exibirInformacao("Locacao cancelada com sucesso.");
                carregarCombos();
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
