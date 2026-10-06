package br.ufms.facom.alucar.view;

import br.ufms.facom.alucar.controller.ControladoraUsuario;
import br.ufms.facom.alucar.controller.ValidacaoException;
import br.ufms.facom.alucar.dao.DAOException;
import br.ufms.facom.alucar.model.TipoUsuario;
import br.ufms.facom.alucar.model.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
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
import java.util.List;

/**
 * Tela de cadastro de usuarios (RF04).
 *
 * Responsabilidade unica: coletar dados, exibir resultados e mensagens.
 * Nenhuma regra de negocio e nenhuma instrucao SQL aparecem aqui - tudo e
 * delegado a ControladoraUsuario.
 */
public class TelaCadastroUsuario extends JFrame {

    private static final Color AZUL_CABECALHO = new Color(31, 95, 145);

    private final ControladoraUsuario controladora = new ControladoraUsuario();
    private final UsuarioTableModel tableModel = new UsuarioTableModel();

    private JTextField campoMatricula;
    private JFormattedTextField campoCpf;
    private JTextField campoNome;
    private JTextField campoLogin;
    private JPasswordField campoSenha;
    private JPasswordField campoConfirmacao;
    private JComboBox<TipoUsuario> comboTipo;
    private JCheckBox caixaMostrarInativos;
    private JTable tabelaUsuarios;
    private JLabel rotuloStatus;

    private boolean emModoEdicao = false;

    public TelaCadastroUsuario() {
        super("Cadastro de Usuarios - Alucar");
        montarInterface();
        carregarUsuarios();
    }

    private void montarInterface() {
        // DISPOSE, e nao EXIT: fechar esta tela nao encerra o sistema, apenas
        // devolve o usuario a TelaPrincipal.
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 8));

        add(criarCabecalho(), BorderLayout.NORTH);
        add(criarPainelCentral(), BorderLayout.CENTER);
        add(criarBarraStatus(), BorderLayout.SOUTH);

        setSize(880, 660);
        setMinimumSize(new Dimension(820, 600));
        setLocationRelativeTo(null);
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(AZUL_CABECALHO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel titulo = new JLabel("Cadastro de Usuarios - Alucar");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.PLAIN, 14f));

        JLabel modulo = new JLabel("Modulo administrativo");
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
        formulario.setBorder(BorderFactory.createTitledBorder("Dados do Usuario"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        campoMatricula = new JTextField(14);
        campoCpf = criarCampoComMascara("###.###.###-##");
        campoNome = new JTextField(28);
        campoLogin = new JTextField(16);
        campoSenha = new JPasswordField(16);
        campoConfirmacao = new JPasswordField(16);
        comboTipo = new JComboBox<>(TipoUsuario.values());

        int linha = 0;

        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Matricula:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(campoMatricula, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("CPF:"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(campoCpf, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Nome:"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1;
        formulario.add(campoNome, c);
        c.gridwidth = 1;

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Login:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(campoLogin, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("Tipo:"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(comboTipo, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0;
        formulario.add(new JLabel("Senha:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(campoSenha, c);
        c.gridx = 2; c.weightx = 0;
        formulario.add(new JLabel("Confirmar senha:"), c);
        c.gridx = 3; c.weightx = 1;
        formulario.add(campoConfirmacao, c);

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
        JButton botaoExcluir = new JButton("Inativar");
        JButton botaoReativar = new JButton("Reativar");
        JButton botaoFechar = new JButton("Fechar");

        botaoSalvar.addActionListener(e -> salvar());
        botaoNovo.addActionListener(e -> limparFormulario());
        botaoExcluir.addActionListener(e -> inativar());
        botaoReativar.addActionListener(e -> reativar());
        botaoFechar.addActionListener(e -> dispose());

        c.gridx = 0; botoes.add(botaoSalvar, c);
        c.gridx = 1; botoes.add(botaoNovo, c);
        c.gridx = 2; botoes.add(botaoExcluir, c);
        c.gridx = 3; botoes.add(botaoReativar, c);
        c.gridx = 4; botoes.add(botaoFechar, c);

        return botoes;
    }

    private JPanel criarPainelLista() {
        JPanel painel = new JPanel(new BorderLayout(0, 6));
        painel.setBorder(BorderFactory.createTitledBorder("Usuarios Cadastrados"));

        JPanel barraFiltro = new JPanel(new BorderLayout());
        barraFiltro.setBorder(BorderFactory.createEmptyBorder(4, 6, 0, 6));

        caixaMostrarInativos = new JCheckBox("Mostrar inativos");
        caixaMostrarInativos.addActionListener(e -> carregarUsuarios());

        JButton botaoAtualizar = new JButton("Atualizar lista");
        botaoAtualizar.addActionListener(e -> carregarUsuarios());

        barraFiltro.add(caixaMostrarInativos, BorderLayout.WEST);
        barraFiltro.add(botaoAtualizar, BorderLayout.EAST);

        tabelaUsuarios = new JTable(tableModel);
        tabelaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaUsuarios.setRowHeight(22);
        tabelaUsuarios.getTableHeader().setReorderingAllowed(false);
        tabelaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormularioComSelecao();
            }
        });

        painel.add(barraFiltro, BorderLayout.NORTH);
        painel.add(new JScrollPane(tabelaUsuarios), BorderLayout.CENTER);
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
        String matricula = campoMatricula.getText();
        String cpf = campoCpf.getText();
        String nome = campoNome.getText();
        String login = campoLogin.getText();
        String senha = new String(campoSenha.getPassword());
        String confirmacao = new String(campoConfirmacao.getPassword());
        TipoUsuario tipo = (TipoUsuario) comboTipo.getSelectedItem();

        try {
            if (emModoEdicao) {
                controladora.alterarUsuario(matricula, cpf, nome, login, senha,
                        confirmacao, tipo);
                exibirInformacao("Usuario alterado com sucesso.");
            } else {
                controladora.cadastrarUsuario(matricula, cpf, nome, login, senha,
                        confirmacao, tipo);
                exibirInformacao("Usuario cadastrado com sucesso.");
            }
            limparFormulario();
            carregarUsuarios();

        } catch (ValidacaoException e) {
            exibirAviso(e.getMessage());
        } catch (DAOException e) {
            exibirErro(e.getMessage());
        }
    }

    private void inativar() {
        Usuario selecionado = tableModel.getUsuarioEm(tabelaUsuarios.getSelectedRow());
        if (selecionado == null) {
            exibirAviso("Selecione um usuario na lista para inativar.");
            return;
        }

        int resposta = JOptionPane.showConfirmDialog(this,
                "Inativar o usuario " + selecionado.getNome() + "?\n\n"
                + "Ele deixara de aparecer na lista e nao podera mais acessar o\n"
                + "sistema, mas o historico de locacoes sera preservado.",
                "Alucar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controladora.inativarUsuario(selecionado.getMatricula());
            exibirInformacao("Usuario inativado com sucesso.");
            limparFormulario();
            carregarUsuarios();

        } catch (ValidacaoException e) {
            exibirAviso(e.getMessage());
        } catch (DAOException e) {
            exibirErro(e.getMessage());
        }
    }

    private void reativar() {
        Usuario selecionado = tableModel.getUsuarioEm(tabelaUsuarios.getSelectedRow());
        if (selecionado == null) {
            exibirAviso("Selecione um usuario na lista para reativar.\n\n"
                    + "Marque 'Mostrar inativos' para ve-los.");
            return;
        }

        try {
            controladora.reativarUsuario(selecionado.getMatricula());
            exibirInformacao("Usuario reativado com sucesso.");
            limparFormulario();
            carregarUsuarios();

        } catch (ValidacaoException e) {
            exibirAviso(e.getMessage());
        } catch (DAOException e) {
            exibirErro(e.getMessage());
        }
    }

    private void carregarUsuarios() {
        boolean incluirInativos = caixaMostrarInativos != null
                && caixaMostrarInativos.isSelected();

        try {
            List<Usuario> usuarios = controladora.listarUsuarios(incluirInativos);
            tableModel.setUsuarios(usuarios, controladora.getPrazoExpiracaoSenha());
            rotuloStatus.setText(usuarios.size() + (incluirInativos
                    ? " usuario(s) no total, incluindo inativos."
                    : " usuario(s) ativo(s)."));

        } catch (DAOException e) {
            tableModel.setUsuarios(List.of(), controladora.getPrazoExpiracaoSenha());
            rotuloStatus.setText("Falha ao carregar a lista de usuarios.");
            exibirErro(e.getMessage());
        }
    }

    private void preencherFormularioComSelecao() {
        Usuario selecionado = tableModel.getUsuarioEm(tabelaUsuarios.getSelectedRow());
        if (selecionado == null) {
            return;
        }

        campoMatricula.setText(selecionado.getMatricula());
        campoMatricula.setEditable(false);
        campoCpf.setText(selecionado.getCpf() == null ? "" : selecionado.getCpf());
        campoNome.setText(selecionado.getNome());
        campoLogin.setText(selecionado.getLogin());
        campoSenha.setText("");
        campoConfirmacao.setText("");
        comboTipo.setSelectedItem(selecionado.getTipoUsuario());

        emModoEdicao = true;

        int prazo = controladora.getPrazoExpiracaoSenha();
        String avisoSenha = selecionado.senhaExpirada(prazo)
                ? " A SENHA DESTE USUARIO ESTA EXPIRADA."
                : "";
        rotuloStatus.setText("Editando o usuario " + selecionado.getMatricula()
                + ". Deixe a senha em branco para mante-la." + avisoSenha);
    }

    private void limparFormulario() {
        campoMatricula.setText("");
        campoMatricula.setEditable(true);
        campoCpf.setText("");
        campoNome.setText("");
        campoLogin.setText("");
        campoSenha.setText("");
        campoConfirmacao.setText("");
        comboTipo.setSelectedIndex(0);
        tabelaUsuarios.clearSelection();

        emModoEdicao = false;
        rotuloStatus.setText("Pronto para um novo cadastro.");
        campoMatricula.requestFocusInWindow();
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
