package br.ufms.facom.alucar.view;

import br.ufms.facom.alucar.controller.ControladoraCliente;
import br.ufms.facom.alucar.controller.ValidacaoException;
import br.ufms.facom.alucar.dao.DAOException;
import br.ufms.facom.alucar.model.Cliente;
import br.ufms.facom.alucar.util.CpfUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tela de cadastro de clientes (RF03).
 *
 * Assim como a tela de usuarios, nao contem regra de negocio nem SQL: coleta
 * os dados como texto e entrega a ControladoraCliente, que decide o que e
 * valido.
 */
public class TelaCadastroCliente extends JFrame {

    private static final Color AZUL_CABECALHO = new Color(31, 95, 145);

    private static final DateTimeFormatter FORMATO_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ControladoraCliente controladora = new ControladoraCliente();
    private final ClienteTableModel tableModel = new ClienteTableModel();

    private JFormattedTextField campoCpf;
    private JTextField campoNome;
    private JFormattedTextField campoCnh;
    private JFormattedTextField campoValidadeCnh;
    private JFormattedTextField campoDataNascimento;
    private JFormattedTextField campoTelefone;
    private JTextField campoEmail;
    private JTextField campoEndereco;
    private JTextField campoBusca;
    private JCheckBox caixaMostrarInativos;
    private JTable tabelaClientes;
    private JLabel rotuloStatus;

    private boolean emModoEdicao = false;

    public TelaCadastroCliente() {
        super("Cadastro de Clientes - Alucar");
        montarInterface();
        carregarClientes();
    }

    private void montarInterface() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 8));

        add(criarCabecalho(), BorderLayout.NORTH);
        add(criarPainelCentral(), BorderLayout.CENTER);
        add(criarBarraStatus(), BorderLayout.SOUTH);

        setSize(820, 680);
        setMinimumSize(new Dimension(760, 600));
        setLocationRelativeTo(null);
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(AZUL_CABECALHO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel titulo = new JLabel("Cadastro de Clientes - Alucar");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.PLAIN, 14f));

        JLabel modulo = new JLabel("Modulo de atendimento");
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
        formulario.setBorder(BorderFactory.createTitledBorder("Dados do Cliente"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        campoCpf = criarCampoComMascara("###.###.###-##");
        campoNome = new JTextField(30);
        campoCnh = criarCampoComMascara("###########");
        campoValidadeCnh = criarCampoComMascara("##/##/####");
        campoDataNascimento = criarCampoComMascara("##/##/####");
        campoTelefone = criarCampoComMascara("(##) #####-####");
        campoEmail = new JTextField(24);
        campoEndereco = new JTextField(36);

        int linha = 0;

        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("CPF:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(campoCpf, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("Data de nascimento:"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(campoDataNascimento, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Nome:"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1;
        formulario.add(campoNome, c);
        c.gridwidth = 1;

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("CNH:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(campoCnh, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("Validade da CNH:"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(campoValidadeCnh, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Telefone:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(campoTelefone, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("E-mail:"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(campoEmail, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Endereco:"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1;
        formulario.add(campoEndereco, c);
        c.gridwidth = 1;

        linha++;
        c.gridx = 0; c.gridy = linha; c.gridwidth = 4;
        formulario.add(criarPainelBotoes(), c);

        return formulario;
    }

    private JFormattedTextField criarCampoComMascara(String mascara) {
        try {
            MaskFormatter formatador = new MaskFormatter(mascara);
            formatador.setPlaceholderCharacter(' ');
            return new JFormattedTextField(formatador);
        } catch (ParseException e) {
            // Mascara invalida e erro de programacao, nao de uso.
            throw new IllegalStateException("Mascara invalida: " + mascara, e);
        }
    }

    private JPanel criarPainelBotoes() {
        JPanel botoes = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 2, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JButton botaoSalvar = new JButton("Salvar");
        JButton botaoNovo = new JButton("Novo");
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
        painel.setBorder(BorderFactory.createTitledBorder("Clientes Cadastrados"));

        JPanel barraBusca = new JPanel(new BorderLayout(6, 0));
        barraBusca.setBorder(BorderFactory.createEmptyBorder(4, 6, 0, 6));

        campoBusca = new JTextField();
        campoBusca.addActionListener(e -> buscar());

        JButton botaoBuscar = new JButton("Buscar");
        botaoBuscar.addActionListener(e -> buscar());

        JButton botaoTodos = new JButton("Todos");
        botaoTodos.addActionListener(e -> {
            campoBusca.setText("");
            carregarClientes();
        });

        caixaMostrarInativos = new JCheckBox("Mostrar inativos");
        caixaMostrarInativos.addActionListener(e -> buscar());

        JPanel acoesBusca = new JPanel(new GridBagLayout());
        GridBagConstraints cb = new GridBagConstraints();
        cb.insets = new Insets(0, 4, 0, 0);
        cb.gridx = 0; acoesBusca.add(botaoBuscar, cb);
        cb.gridx = 1; acoesBusca.add(botaoTodos, cb);
        cb.gridx = 2; acoesBusca.add(caixaMostrarInativos, cb);

        barraBusca.add(new JLabel("Nome:"), BorderLayout.WEST);
        barraBusca.add(campoBusca, BorderLayout.CENTER);
        barraBusca.add(acoesBusca, BorderLayout.EAST);

        tabelaClientes = new JTable(tableModel);
        tabelaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaClientes.setRowHeight(22);
        tabelaClientes.getTableHeader().setReorderingAllowed(false);
        tabelaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormularioComSelecao();
            }
        });

        painel.add(barraBusca, BorderLayout.NORTH);
        painel.add(new JScrollPane(tabelaClientes), BorderLayout.CENTER);
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
        String cpf = campoCpf.getText();
        String nome = campoNome.getText();
        String cnh = campoCnh.getText();
        String validade = campoValidadeCnh.getText();
        String nascimento = campoDataNascimento.getText();
        String telefone = campoTelefone.getText();
        String email = campoEmail.getText();
        String endereco = campoEndereco.getText();

        try {
            if (emModoEdicao) {
                controladora.alterarCliente(cpf, nome, cnh, validade, nascimento,
                        telefone, email, endereco);
                exibirInformacao("Cliente alterado com sucesso.");
            } else {
                controladora.cadastrarCliente(cpf, nome, cnh, validade, nascimento,
                        telefone, email, endereco);
                exibirInformacao("Cliente cadastrado com sucesso.");
            }
            limparFormulario();
            carregarClientes();

        } catch (ValidacaoException e) {
            exibirAviso(e.getMessage());
        } catch (DAOException e) {
            exibirErro(e.getMessage());
        }
    }

    private void inativar() {
        Cliente selecionado = tableModel.getClienteEm(tabelaClientes.getSelectedRow());
        if (selecionado == null) {
            exibirAviso("Selecione um cliente na lista para inativar.");
            return;
        }

        int resposta = JOptionPane.showConfirmDialog(this,
                "Inativar o cliente " + selecionado.getNome() + "?\n\n"
                + "Ele deixara de aparecer na lista e nao podera iniciar novas\n"
                + "locacoes, mas o historico sera preservado.",
                "Alucar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controladora.inativarCliente(selecionado.getCpf());
            exibirInformacao("Cliente inativado com sucesso.");
            limparFormulario();
            carregarClientes();

        } catch (ValidacaoException e) {
            exibirAviso(e.getMessage());
        } catch (DAOException e) {
            exibirErro(e.getMessage());
        }
    }

    private void reativar() {
        Cliente selecionado = tableModel.getClienteEm(tabelaClientes.getSelectedRow());
        if (selecionado == null) {
            exibirAviso("Selecione um cliente na lista para reativar.\n\n"
                    + "Marque 'Mostrar inativos' para ve-los.");
            return;
        }

        try {
            controladora.reativarCliente(selecionado.getCpf());
            exibirInformacao("Cliente reativado com sucesso.");
            limparFormulario();
            carregarClientes();

        } catch (ValidacaoException e) {
            exibirAviso(e.getMessage());
        } catch (DAOException e) {
            exibirErro(e.getMessage());
        }
    }

    private void buscar() {
        boolean incluirInativos = caixaMostrarInativos.isSelected();

        try {
            List<Cliente> clientes =
                    controladora.buscarPorNome(campoBusca.getText(), incluirInativos);
            tableModel.setClientes(clientes);
            rotuloStatus.setText(clientes.size() + " cliente(s) encontrado(s).");

        } catch (DAOException e) {
            exibirErro(e.getMessage());
        }
    }

    private void carregarClientes() {
        boolean incluirInativos = caixaMostrarInativos != null
                && caixaMostrarInativos.isSelected();

        try {
            List<Cliente> clientes = controladora.listarClientes(incluirInativos);
            tableModel.setClientes(clientes);
            rotuloStatus.setText(clientes.size() + (incluirInativos
                    ? " cliente(s) no total, incluindo inativos."
                    : " cliente(s) ativo(s)."));

        } catch (DAOException e) {
            tableModel.setClientes(List.of());
            rotuloStatus.setText("Falha ao carregar a lista de clientes.");
            exibirErro(e.getMessage());
        }
    }

    private void preencherFormularioComSelecao() {
        Cliente selecionado = tableModel.getClienteEm(tabelaClientes.getSelectedRow());
        if (selecionado == null) {
            return;
        }

        campoCpf.setText(selecionado.getCpf());
        campoCpf.setEditable(false);
        campoNome.setText(selecionado.getNome());
        campoCnh.setText(selecionado.getCnh());
        campoValidadeCnh.setText(selecionado.getValidadeCnh().format(FORMATO_BR));
        campoDataNascimento.setText(selecionado.getDataNascimento().format(FORMATO_BR));
        campoTelefone.setText(selecionado.getTelefone() == null
                ? "" : selecionado.getTelefone().replaceAll("\\D", ""));
        campoEmail.setText(selecionado.getEmail() == null ? "" : selecionado.getEmail());
        campoEndereco.setText(selecionado.getEndereco() == null
                ? "" : selecionado.getEndereco());

        emModoEdicao = true;
        rotuloStatus.setText("Editando o cliente "
                + CpfUtil.formatar(selecionado.getCpf()) + ".");
    }

    private void limparFormulario() {
        campoCpf.setText("");
        campoCpf.setEditable(true);
        campoNome.setText("");
        campoCnh.setText("");
        campoValidadeCnh.setText("");
        campoDataNascimento.setText("");
        campoTelefone.setText("");
        campoEmail.setText("");
        campoEndereco.setText("");
        tabelaClientes.clearSelection();

        emModoEdicao = false;
        rotuloStatus.setText("Pronto para um novo cadastro.");
        campoCpf.requestFocusInWindow();
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
