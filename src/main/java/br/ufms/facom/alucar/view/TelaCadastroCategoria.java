package br.ufms.facom.alucar.view;

import br.ufms.facom.alucar.controller.ControladoraCategoriaVeiculo;
import br.ufms.facom.alucar.controller.ValidacaoException;
import br.ufms.facom.alucar.dao.DAOException;
import br.ufms.facom.alucar.model.CategoriaVeiculo;
import br.ufms.facom.alucar.util.MoedaUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;


public class TelaCadastroCategoria extends JFrame {

    private static final Color AZUL_CABECALHO = new Color(31, 95, 145);

    private final ControladoraCategoriaVeiculo controladora =
            new ControladoraCategoriaVeiculo();
    private final CategoriaVeiculoTableModel tableModel = new CategoriaVeiculoTableModel();

    private JTextField campoNome;
    private JTextArea campoDescricao;
    private JTextField campoValorBase;
    private JCheckBox caixaMostrarInativas;
    private JTable tabelaCategorias;
    private JLabel rotuloStatus;

    private boolean emModoEdicao = false;

    public TelaCadastroCategoria() {
        super("Cadastro de Categorias de Veiculo - Alucar");
        montarInterface();
        carregarCategorias();
    }

    private void montarInterface() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 8));

        add(criarCabecalho(), BorderLayout.NORTH);
        add(criarPainelCentral(), BorderLayout.CENTER);
        add(criarBarraStatus(), BorderLayout.SOUTH);

        setSize(760, 600);
        setMinimumSize(new Dimension(700, 540));
        setLocationRelativeTo(null);
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(AZUL_CABECALHO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel titulo = new JLabel("Categorias de Veiculo - Alucar");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.PLAIN, 14f));

        JLabel modulo = new JLabel("Modulo de frota");
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
        formulario.setBorder(BorderFactory.createTitledBorder("Dados da Categoria"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        campoNome = new JTextField(24);
        campoValorBase = new JTextField(12);

        campoDescricao = new JTextArea(3, 40);
        campoDescricao.setLineWrap(true);
        campoDescricao.setWrapStyleWord(true);
        campoDescricao.setFont(campoNome.getFont());
        JScrollPane painelDescricao = new JScrollPane(campoDescricao);

        int linha = 0;

        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Nome:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(campoNome, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("Valor base da diaria (R$):"), c);
        c.gridx = 3; c.weightx = 0;
        formulario.add(campoValorBase, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        formulario.add(new JLabel("Descricao:"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        formulario.add(painelDescricao, c);
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

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

        JButton botaoSalvar = new JButton("Salvar");
        JButton botaoNovo = new JButton("Nova");
        JButton botaoInativar = new JButton("Inativar");
        JButton botaoReativar = new JButton("Reativar");
        JButton botaoFechar = new JButton("Fechar");

        botaoSalvar.addActionListener(e -> salvar());
        botaoNovo.addActionListener(e -> limparFormulario());
        botaoInativar.addActionListener(e -> inativar());
        botaoReativar.addActionListener(e -> reativar());
        botaoFechar.addActionListener(e -> dispose());

        c.gridx = 0; botoes.add(botaoSalvar, c);
        c.gridx = 1; botoes.add(botaoNovo, c);
        c.gridx = 2; botoes.add(botaoInativar, c);
        c.gridx = 3; botoes.add(botaoReativar, c);
        c.gridx = 4; botoes.add(botaoFechar, c);

        return botoes;
    }

    private JPanel criarPainelLista() {
        JPanel painel = new JPanel(new BorderLayout(0, 6));
        painel.setBorder(BorderFactory.createTitledBorder("Categorias Cadastradas"));

        JPanel barraFiltro = new JPanel(new BorderLayout());
        barraFiltro.setBorder(BorderFactory.createEmptyBorder(4, 6, 0, 6));

        caixaMostrarInativas = new JCheckBox("Mostrar inativas");
        caixaMostrarInativas.addActionListener(e -> carregarCategorias());

        JButton botaoAtualizar = new JButton("Atualizar lista");
        botaoAtualizar.addActionListener(e -> carregarCategorias());

        barraFiltro.add(caixaMostrarInativas, BorderLayout.WEST);
        barraFiltro.add(botaoAtualizar, BorderLayout.EAST);

        tabelaCategorias = new JTable(tableModel);
        tabelaCategorias.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaCategorias.setRowHeight(22);
        tabelaCategorias.getTableHeader().setReorderingAllowed(false);
        tabelaCategorias.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormularioComSelecao();
            }
        });

        painel.add(barraFiltro, BorderLayout.NORTH);
        painel.add(new JScrollPane(tabelaCategorias), BorderLayout.CENTER);
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

    private void salvar() {
        String nome = campoNome.getText();
        String descricao = campoDescricao.getText();
        String valorBase = campoValorBase.getText();

        try {
            if (emModoEdicao) {
                controladora.alterarCategoria(nome, descricao, valorBase);
                exibirInformacao("Categoria alterada com sucesso.");
            } else {
                controladora.cadastrarCategoria(nome, descricao, valorBase);
                exibirInformacao("Categoria cadastrada com sucesso.");
            }
            limparFormulario();
            carregarCategorias();

        } catch (ValidacaoException e) {
            exibirAviso(e.getMessage());
        } catch (DAOException e) {
            exibirErro(e.getMessage());
        }
    }

    private void inativar() {
        CategoriaVeiculo selecionada =
                tableModel.getCategoriaEm(tabelaCategorias.getSelectedRow());
        if (selecionada == null) {
            exibirAviso("Selecione uma categoria na lista para inativar.");
            return;
        }

        String aviso = "";
        try {
            int quantidade = controladora.contarVeiculosNaCategoria(selecionada.getNome());
            if (quantidade > 0) {
                aviso = "\n\nAtencao: " + quantidade + " veiculo(s) pertence(m) a esta "
                        + "categoria.\nEles continuarao validos, mas a categoria deixara de\n"
                        + "aparecer para novos cadastros.";
            }
        } catch (DAOException e) {
            // A contagem e apenas informativa; a inativacao segue mesmo sem ela.
        }

        int resposta = JOptionPane.showConfirmDialog(this,
                "Inativar a categoria '" + selecionada.getNome() + "'?" + aviso,
                "Alucar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controladora.inativarCategoria(selecionada.getNome());
            exibirInformacao("Categoria inativada com sucesso.");
            limparFormulario();
            carregarCategorias();

        } catch (ValidacaoException e) {
            exibirAviso(e.getMessage());
        } catch (DAOException e) {
            exibirErro(e.getMessage());
        }
    }

    private void reativar() {
        CategoriaVeiculo selecionada =
                tableModel.getCategoriaEm(tabelaCategorias.getSelectedRow());
        if (selecionada == null) {
            exibirAviso("Selecione uma categoria na lista para reativar.\n\n"
                    + "Marque 'Mostrar inativas' para ve-las.");
            return;
        }

        try {
            controladora.reativarCategoria(selecionada.getNome());
            exibirInformacao("Categoria reativada com sucesso.");
            limparFormulario();
            carregarCategorias();

        } catch (ValidacaoException e) {
            exibirAviso(e.getMessage());
        } catch (DAOException e) {
            exibirErro(e.getMessage());
        }
    }

    private void carregarCategorias() {
        boolean incluirInativas = caixaMostrarInativas != null
                && caixaMostrarInativas.isSelected();

        try {
            List<CategoriaVeiculo> categorias =
                    controladora.listarCategorias(incluirInativas);
            tableModel.setCategorias(categorias);
            rotuloStatus.setText(categorias.size() + (incluirInativas
                    ? " categoria(s) no total, incluindo inativas."
                    : " categoria(s) ativa(s)."));

        } catch (DAOException e) {
            tableModel.setCategorias(List.of());
            rotuloStatus.setText("Falha ao carregar a lista de categorias.");
            exibirErro(e.getMessage());
        }
    }

    private void preencherFormularioComSelecao() {
        CategoriaVeiculo selecionada =
                tableModel.getCategoriaEm(tabelaCategorias.getSelectedRow());
        if (selecionada == null) {
            return;
        }

        campoNome.setText(selecionada.getNome());
        // O nome e chave primaria e e referenciado pelos veiculos: nao pode
        // ser alterado depois do cadastro.
        campoNome.setEditable(false);
        campoDescricao.setText(selecionada.getDescricao() == null
                ? "" : selecionada.getDescricao());
        campoValorBase.setText(MoedaUtil.formatarSemSimbolo(selecionada.getValorBaseDiaria()));

        emModoEdicao = true;
        rotuloStatus.setText("Editando a categoria '" + selecionada.getNome()
                + "'. O nome nao pode ser alterado.");
    }

    private void limparFormulario() {
        campoNome.setText("");
        campoNome.setEditable(true);
        campoDescricao.setText("");
        campoValorBase.setText("");
        tabelaCategorias.clearSelection();

        emModoEdicao = false;
        rotuloStatus.setText("Pronto para um novo cadastro.");
        campoNome.requestFocusInWindow();
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
