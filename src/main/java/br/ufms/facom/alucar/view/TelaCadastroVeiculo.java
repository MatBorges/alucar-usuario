package br.ufms.facom.alucar.view;

import br.ufms.facom.alucar.controller.ControladoraVeiculo;
import br.ufms.facom.alucar.controller.ValidacaoException;
import br.ufms.facom.alucar.dao.DAOException;
import br.ufms.facom.alucar.model.CategoriaVeiculo;
import br.ufms.facom.alucar.model.StatusVeiculo;
import br.ufms.facom.alucar.model.Veiculo;
import br.ufms.facom.alucar.util.MoedaUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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


public class TelaCadastroVeiculo extends JFrame {

    private static final Color AZUL_CABECALHO = new Color(31, 95, 145);
    private static final String TODOS = "(todos)";

    private final ControladoraVeiculo controladora = new ControladoraVeiculo();
    private final VeiculoTableModel tableModel = new VeiculoTableModel();

    private JTextField campoPlaca;
    private JTextField campoRenavam;
    private JTextField campoMarca;
    private JTextField campoModelo;
    private JTextField campoAno;
    private JTextField campoKm;
    private JTextField campoValorDiaria;
    private JComboBox<CategoriaVeiculo> comboCategoria;
    private JComboBox<StatusVeiculo> comboStatus;

    private JTextField filtroMarca;
    private JTextField filtroModelo;
    private JComboBox<Object> filtroCategoria;
    private JComboBox<Object> filtroStatus;

    private JTable tabelaVeiculos;
    private JLabel rotuloStatus;

    private boolean emModoEdicao = false;

    public TelaCadastroVeiculo() {
        super("Cadastro de Veiculos - Alucar");
        montarInterface();
        carregarCategorias();
        consultar();
    }

    private void montarInterface() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 8));

        add(criarCabecalho(), BorderLayout.NORTH);
        add(criarPainelCentral(), BorderLayout.CENTER);
        add(criarBarraStatus(), BorderLayout.SOUTH);

        setSize(980, 720);
        setMinimumSize(new Dimension(900, 640));
        setLocationRelativeTo(null);
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(AZUL_CABECALHO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel titulo = new JLabel("Cadastro de Veiculos - Alucar");
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
        formulario.setBorder(BorderFactory.createTitledBorder("Dados do Veiculo"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        campoPlaca = new JTextField(10);
        campoRenavam = new JTextField(14);
        campoMarca = new JTextField(18);
        campoModelo = new JTextField(18);
        campoAno = new JTextField(6);
        campoKm = new JTextField(10);
        campoValorDiaria = new JTextField(10);
        comboCategoria = new JComboBox<>();
        comboStatus = new JComboBox<>(StatusVeiculo.definiveisNoCadastro());

        int linha = 0;

        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Placa:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(campoPlaca, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("RENAVAM:"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(campoRenavam, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Marca:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(campoMarca, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("Modelo:"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(campoModelo, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Ano de fabricacao:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(campoAno, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("Quilometragem atual:"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(campoKm, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Categoria:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(comboCategoria, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("Valor da diaria (R$):"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(campoValorDiaria, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Status:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(comboStatus, c);
        c.gridx = 2; c.gridwidth = 2; c.weightx = 1;
        formulario.add(criarAvisoDaRegra(), c);
        c.gridwidth = 1;

        linha++;
        c.gridx = 0; c.gridy = linha; c.gridwidth = 4;
        formulario.add(criarPainelBotoes(), c);

        return formulario;
    }

    private JLabel criarAvisoDaRegra() {
        JLabel aviso = new JLabel("RN04: acima de "
                + Veiculo.KM_LIMITE_DEPRECIACAO + " km ou "
                + Veiculo.ANOS_LIMITE_DEPRECIACAO + " anos, o veiculo e desativado.");
        aviso.setFont(aviso.getFont().deriveFont(Font.PLAIN, 11f));
        aviso.setForeground(Color.DARK_GRAY);
        return aviso;
    }

    private JPanel criarPainelBotoes() {
        JPanel botoes = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 2, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JButton botaoSalvar = new JButton("Salvar");
        JButton botaoNovo = new JButton("Novo");
        JButton botaoFechar = new JButton("Fechar");

        botaoSalvar.addActionListener(e -> salvar());
        botaoNovo.addActionListener(e -> limparFormulario());
        botaoFechar.addActionListener(e -> dispose());

        c.gridx = 0; botoes.add(botaoSalvar, c);
        c.gridx = 1; botoes.add(botaoNovo, c);
        c.gridx = 2; botoes.add(botaoFechar, c);

        return botoes;
    }

    private JPanel criarPainelLista() {
        JPanel painel = new JPanel(new BorderLayout(0, 6));
        painel.setBorder(BorderFactory.createTitledBorder("Frota"));

        painel.add(criarBarraDeFiltros(), BorderLayout.NORTH);

        tabelaVeiculos = new JTable(tableModel);
        tabelaVeiculos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaVeiculos.setRowHeight(22);
        tabelaVeiculos.getTableHeader().setReorderingAllowed(false);
        tabelaVeiculos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormularioComSelecao();
            }
        });

        painel.add(new JScrollPane(tabelaVeiculos), BorderLayout.CENTER);
        return painel;
    }

//    Filtros
    private JPanel criarBarraDeFiltros() {
        JPanel barra = new JPanel(new GridBagLayout());
        barra.setBorder(BorderFactory.createEmptyBorder(4, 6, 0, 6));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        filtroMarca = new JTextField(10);
        filtroModelo = new JTextField(10);
        filtroCategoria = new JComboBox<>(new Object[] {TODOS});
        filtroStatus = new JComboBox<>(montarOpcoesDeStatus());

        filtroMarca.addActionListener(e -> consultar());
        filtroModelo.addActionListener(e -> consultar());
        filtroCategoria.addActionListener(e -> consultar());
        filtroStatus.addActionListener(e -> consultar());

        JButton botaoConsultar = new JButton("Consultar");
        botaoConsultar.addActionListener(e -> consultar());

        JButton botaoLimpar = new JButton("Limpar filtros");
        botaoLimpar.addActionListener(e -> limparFiltros());

        int x = 0;
        c.gridx = x++; c.weightx = 0; barra.add(new JLabel("Marca:"), c);
        c.gridx = x++; c.weightx = 1; barra.add(filtroMarca, c);
        c.gridx = x++; c.weightx = 0; barra.add(new JLabel("Modelo:"), c);
        c.gridx = x++; c.weightx = 1; barra.add(filtroModelo, c);
        c.gridx = x++; c.weightx = 0; barra.add(new JLabel("Categoria:"), c);
        c.gridx = x++; c.weightx = 1; barra.add(filtroCategoria, c);
        c.gridx = x++; c.weightx = 0; barra.add(new JLabel("Status:"), c);
        c.gridx = x++; c.weightx = 1; barra.add(filtroStatus, c);
        c.gridx = x++; c.weightx = 0; barra.add(botaoConsultar, c);
        c.gridx = x; c.weightx = 0; barra.add(botaoLimpar, c);

        return barra;
    }

    private Object[] montarOpcoesDeStatus() {
        StatusVeiculo[] todos = StatusVeiculo.values();
        Object[] opcoes = new Object[todos.length + 1];
        opcoes[0] = TODOS;
        System.arraycopy(todos, 0, opcoes, 1, todos.length);
        return opcoes;
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

    private void carregarCategorias() {
        try {
            List<CategoriaVeiculo> categorias = controladora.listarCategoriasAtivas();

            comboCategoria.removeAllItems();
            for (CategoriaVeiculo categoria : categorias) {
                comboCategoria.addItem(categoria);
            }

            filtroCategoria.removeAllItems();
            filtroCategoria.addItem(TODOS);
            for (CategoriaVeiculo categoria : categorias) {
                filtroCategoria.addItem(categoria.getNome());
            }

            if (categorias.isEmpty()) {
                exibirAviso("Nenhuma categoria ativa cadastrada.\n\n"
                        + "Cadastre ao menos uma categoria antes de incluir veiculos.");
            }

        } catch (DAOException e) {
            exibirErro(e.getMessage());
        }
    }

    private void salvar() {
        String placa = campoPlaca.getText();
        String renavam = campoRenavam.getText();
        String marca = campoMarca.getText();
        String modelo = campoModelo.getText();
        String ano = campoAno.getText();
        String km = campoKm.getText();
        String valorDiaria = campoValorDiaria.getText();
        StatusVeiculo status = (StatusVeiculo) comboStatus.getSelectedItem();

        CategoriaVeiculo categoria = (CategoriaVeiculo) comboCategoria.getSelectedItem();
        String nomeCategoria = categoria == null ? null : categoria.getNome();


        String motivo = controladora.verificarDepreciacao(ano, km);
        if (motivo != null && status != StatusVeiculo.DESATIVADO) {
            int resposta = JOptionPane.showConfirmDialog(this,
                    "Este veiculo atingiu o limite de depreciacao:\n  " + motivo + ".\n\n"
                    + "Pela RN04 ele sera gravado como DESATIVADO e nao podera\n"
                    + "receber novas locacoes. Confirma?",
                    "Alucar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (resposta != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            if (emModoEdicao) {
                controladora.alterarVeiculo(placa, renavam, modelo, marca, ano, km,
                        valorDiaria, status, nomeCategoria);
                exibirInformacao("Veiculo alterado com sucesso.");
            } else {
                controladora.cadastrarVeiculo(placa, renavam, modelo, marca, ano, km,
                        valorDiaria, status, nomeCategoria);
                exibirInformacao("Veiculo cadastrado com sucesso.");
            }
            limparFormulario();
            consultar();

        } catch (ValidacaoException e) {
            exibirAviso(e.getMessage());
        } catch (DAOException e) {
            exibirErro(e.getMessage());
        }
    }

    private void consultar() {
        String marca = filtroMarca.getText();
        String modelo = filtroModelo.getText();

        Object categoriaSelecionada = filtroCategoria.getSelectedItem();
        String categoria = TODOS.equals(categoriaSelecionada) || categoriaSelecionada == null
                ? null : categoriaSelecionada.toString();

        Object statusSelecionado = filtroStatus.getSelectedItem();
        StatusVeiculo status = statusSelecionado instanceof StatusVeiculo valor
                ? valor : null;

        try {
            List<Veiculo> veiculos =
                    controladora.consultarVeiculos(marca, modelo, categoria, status);
            tableModel.setVeiculos(veiculos);
            rotuloStatus.setText(veiculos.size() + " veiculo(s) encontrado(s).");

        } catch (DAOException e) {
            tableModel.setVeiculos(List.of());
            rotuloStatus.setText("Falha ao consultar a frota.");
            exibirErro(e.getMessage());
        }
    }

    private void limparFiltros() {
        filtroMarca.setText("");
        filtroModelo.setText("");
        filtroCategoria.setSelectedItem(TODOS);
        filtroStatus.setSelectedItem(TODOS);
        consultar();
    }

    private void preencherFormularioComSelecao() {
        Veiculo selecionado = tableModel.getVeiculoEm(tabelaVeiculos.getSelectedRow());
        if (selecionado == null) {
            return;
        }

        campoPlaca.setText(selecionado.getPlaca());
        campoPlaca.setEditable(false);
        campoRenavam.setText(selecionado.getRenavam());
        campoMarca.setText(selecionado.getMarca());
        campoModelo.setText(selecionado.getModelo());
        campoAno.setText(String.valueOf(selecionado.getAnoFabricacao()));
        campoKm.setText(String.valueOf(selecionado.getKmAtual()));
        campoValorDiaria.setText(MoedaUtil.formatarSemSimbolo(selecionado.getValorDiaria()));

        selecionarCategoria(selecionado.getCategoria());
        selecionarStatus(selecionado.getStatus());

        emModoEdicao = true;

        String aviso = selecionado.atingiuLimiteDepreciacao()
                ? " Atingiu o limite da RN04: " + selecionado.descreverMotivoDaDepreciacao() + "."
                : "";
        rotuloStatus.setText("Editando o veiculo " + selecionado.getPlaca() + "." + aviso);
    }

    private void selecionarCategoria(CategoriaVeiculo categoria) {
        if (categoria == null) {
            return;
        }
        for (int i = 0; i < comboCategoria.getItemCount(); i++) {
            if (comboCategoria.getItemAt(i).getNome().equals(categoria.getNome())) {
                comboCategoria.setSelectedIndex(i);
                return;
            }
        }

        comboCategoria.addItem(categoria);
        comboCategoria.setSelectedItem(categoria);
    }


    private void selecionarStatus(StatusVeiculo status) {
        for (int i = 0; i < comboStatus.getItemCount(); i++) {
            if (comboStatus.getItemAt(i) == status) {
                comboStatus.setSelectedIndex(i);
                return;
            }
        }
        comboStatus.addItem(status);
        comboStatus.setSelectedItem(status);
    }

    private void limparFormulario() {
        campoPlaca.setText("");
        campoPlaca.setEditable(true);
        campoRenavam.setText("");
        campoMarca.setText("");
        campoModelo.setText("");
        campoAno.setText("");
        campoKm.setText("");
        campoValorDiaria.setText("");
        if (comboCategoria.getItemCount() > 0) {
            comboCategoria.setSelectedIndex(0);
        }
        comboStatus.setSelectedItem(StatusVeiculo.DISPONIVEL);
        tabelaVeiculos.clearSelection();

        emModoEdicao = false;
        rotuloStatus.setText("Pronto para um novo cadastro.");
        campoPlaca.requestFocusInWindow();
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
