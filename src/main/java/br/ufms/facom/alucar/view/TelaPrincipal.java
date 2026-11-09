package br.ufms.facom.alucar.view;

import br.ufms.facom.alucar.dao.ConexaoBD;
import br.ufms.facom.alucar.dao.DAOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;


public class TelaPrincipal extends JFrame {

    private static final Color AZUL_CABECALHO = new Color(31, 95, 145);
    private static final Color VERDE_OK = new Color(0, 110, 60);
    private static final Color AMBAR_ALERTA = new Color(160, 95, 0);
    private static final Color VERMELHO_ERRO = new Color(165, 30, 30);

    private JLabel rotuloConexao;

    public TelaPrincipal() {
        super("Alucar - Sistema de Gestao de Locadora");
        montarInterface();
        verificarConexao();
    }

    private void montarInterface() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 10));

        setJMenuBar(criarBarraDeMenu());
        add(criarCabecalho(), BorderLayout.NORTH);
        add(criarPainelDeAtalhos(), BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);

        setSize(560, 540);
        setMinimumSize(new Dimension(500, 500));
        setLocationRelativeTo(null);
    }

    private JMenuBar criarBarraDeMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu menuCadastros = new JMenu("Cadastros");

        JMenuItem itemClientes = new JMenuItem("Clientes");
        itemClientes.addActionListener(e -> abrirCadastroClientes());

        JMenuItem itemUsuarios = new JMenuItem("Usuarios");
        itemUsuarios.addActionListener(e -> abrirCadastroUsuarios());

        JMenuItem itemCategorias = new JMenuItem("Categorias de Veiculo");
        itemCategorias.addActionListener(e -> abrirCadastroCategorias());

        JMenuItem itemVeiculos = new JMenuItem("Veiculos");
        itemVeiculos.addActionListener(e -> abrirCadastroVeiculos());

        menuCadastros.add(itemClientes);
        menuCadastros.add(itemUsuarios);
        menuCadastros.addSeparator();
        menuCadastros.add(itemCategorias);
        menuCadastros.add(itemVeiculos);

        JMenu menuSistema = new JMenu("Sistema");

        JMenuItem itemTestarConexao = new JMenuItem("Testar conexao");
        itemTestarConexao.addActionListener(e -> verificarConexao());

        JMenuItem itemSair = new JMenuItem("Sair");
        itemSair.addActionListener(e -> System.exit(0));

        menuSistema.add(itemTestarConexao);
        menuSistema.addSeparator();
        menuSistema.add(itemSair);

        barra.add(menuCadastros);

        JMenu menuLocacao = new JMenu("Locacao");
        JMenuItem itemRealizarLocacao = new JMenuItem("Realizar Locacao");
        itemRealizarLocacao.addActionListener(e -> abrirRealizarLocacao());
        menuLocacao.add(itemRealizarLocacao);
        barra.add(menuLocacao);

        barra.add(menuSistema);
        return barra;
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(AZUL_CABECALHO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel titulo = new JLabel("Alucar");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.PLAIN, 18f));

        JLabel subtitulo = new JLabel("Sistema de Gestao de Locadora de Veiculos");
        subtitulo.setForeground(Color.WHITE);
        subtitulo.setFont(subtitulo.getFont().deriveFont(Font.PLAIN, 12f));

        cabecalho.add(titulo, BorderLayout.WEST);
        cabecalho.add(subtitulo, BorderLayout.EAST);
        return cabecalho;
    }

    private JPanel criarPainelDeAtalhos() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 0, 8, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;


//        BOTOES DO MENU

        JButton botaoLocacao = new JButton("Realizar Locacao");
        botaoLocacao.setPreferredSize(new Dimension(240, 44));
        botaoLocacao.addActionListener(e -> abrirRealizarLocacao());

        JButton botaoClientes = new JButton("Cadastro de Clientes");
        botaoClientes.setPreferredSize(new Dimension(240, 44));
        botaoClientes.addActionListener(e -> abrirCadastroClientes());

        JButton botaoVeiculos = new JButton("Cadastro de Veiculos");
        botaoVeiculos.setPreferredSize(new Dimension(240, 44));
        botaoVeiculos.addActionListener(e -> abrirCadastroVeiculos());

        JButton botaoCategorias = new JButton("Categorias de Veiculo");
        botaoCategorias.setPreferredSize(new Dimension(240, 44));
        botaoCategorias.addActionListener(e -> abrirCadastroCategorias());

        JButton botaoUsuarios = new JButton("Cadastro de Usuarios");
        botaoUsuarios.setPreferredSize(new Dimension(240, 44));
        botaoUsuarios.addActionListener(e -> abrirCadastroUsuarios());

        c.gridy = 0; painel.add(botaoLocacao, c);
        c.gridy = 1; painel.add(botaoClientes, c);
        c.gridy = 2; painel.add(botaoVeiculos, c);
        c.gridy = 3; painel.add(botaoCategorias, c);
        c.gridy = 4; painel.add(botaoUsuarios, c);

        return painel;
    }

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout(0, 4));
        rodape.setBorder(BorderFactory.createEmptyBorder(4, 12, 10, 12));

        rotuloConexao = new JLabel("Verificando conexao com o banco...");
        rotuloConexao.setHorizontalAlignment(SwingConstants.CENTER);
        rotuloConexao.setFont(rotuloConexao.getFont().deriveFont(Font.PLAIN, 12f));

        JLabel rotuloGrupo = new JLabel("Grupo 3");
        rotuloGrupo.setHorizontalAlignment(SwingConstants.CENTER);
        rotuloGrupo.setFont(rotuloGrupo.getFont().deriveFont(Font.PLAIN, 11f));

        rodape.add(rotuloConexao, BorderLayout.NORTH);
        rodape.add(rotuloGrupo, BorderLayout.SOUTH);
        return rodape;
    }

    /**
     * Abre e fecha uma conexao apenas para descobrir qual banco respondeu.
     *
     * Roda em SwingWorker porque conectar a um servidor na nuvem leva
     * centenas de milissegundos (e segundos, se o cluster estiver hibernando).
     * Na Event Dispatch Thread isso congelaria a janela.
     */
    private void verificarConexao() {
        rotuloConexao.setText("Verificando conexao..");
        rotuloConexao.setForeground(Color.GRAY);

        new SwingWorker<String, Void>() {

            private boolean houveFalha = false;
            private boolean emContingencia = false;

            @Override
            protected String doInBackground() {
                try (Connection ignorada = ConexaoBD.obterConexao()) {
                    emContingencia = ConexaoBD.estaUsandoContingencia();
                    return "Conectado a " + ConexaoBD.descreverConexaoAtiva();

                } catch (Exception e) {
                    houveFalha = true;
                    String detalhe = (e instanceof DAOException)
                            ? primeiraLinha(e.getMessage())
                            : e.getMessage();
                    return "Sem conexao com o banco: " + detalhe;
                }
            }

            @Override
            protected void done() {
                try {
                    rotuloConexao.setText(get());
                } catch (Exception e) {
                    rotuloConexao.setText("Nao foi possivel verificar a conexao.");
                    houveFalha = true;
                }

                if (houveFalha) {
                    rotuloConexao.setForeground(VERMELHO_ERRO);
                } else if (emContingencia) {
                    rotuloConexao.setForeground(AMBAR_ALERTA);
                } else {
                    rotuloConexao.setForeground(VERDE_OK);
                }
            }
        }.execute();
    }


    private String primeiraLinha(String mensagem) {
        if (mensagem == null) {
            return "causa desconhecida";
        }
        int quebra = mensagem.indexOf('\n');
        return quebra < 0 ? mensagem : mensagem.substring(0, quebra);
    }

    private void abrirCadastroClientes() {
        new TelaCadastroCliente().setVisible(true);
    }

    private void abrirCadastroUsuarios() {
        new TelaCadastroUsuario().setVisible(true);
    }

    private void abrirCadastroCategorias() {
        new TelaCadastroCategoria().setVisible(true);
    }

    private void abrirCadastroVeiculos() {
        new TelaCadastroVeiculo().setVisible(true);
    }

    private void abrirRealizarLocacao() {
        new TelaRealizarLocacao().setVisible(true);
    }
}
