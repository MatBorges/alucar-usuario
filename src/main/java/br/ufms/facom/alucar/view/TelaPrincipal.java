package br.ufms.facom.alucar.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Janela principal do sistema.
 *
 * Serve apenas como ponto de navegacao: cada opcao abre a tela do caso de uso
 * correspondente. Nenhuma regra de negocio passa por aqui.
 */
public class TelaPrincipal extends JFrame {

    private static final Color AZUL_CABECALHO = new Color(31, 95, 145);

    public TelaPrincipal() {
        super("Alucar - Sistema de Gestao de Locadora");
        montarInterface();
    }

    private void montarInterface() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 10));

        setJMenuBar(criarBarraDeMenu());
        add(criarCabecalho(), BorderLayout.NORTH);
        add(criarPainelDeAtalhos(), BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);

        setSize(520, 340);
        setMinimumSize(new Dimension(460, 300));
        setLocationRelativeTo(null);
    }

    private JMenuBar criarBarraDeMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu menuCadastros = new JMenu("Cadastros");

        JMenuItem itemClientes = new JMenuItem("Clientes");
        itemClientes.addActionListener(e -> abrirCadastroClientes());

        JMenuItem itemUsuarios = new JMenuItem("Usuarios");
        itemUsuarios.addActionListener(e -> abrirCadastroUsuarios());

        menuCadastros.add(itemClientes);
        menuCadastros.add(itemUsuarios);

        JMenu menuSistema = new JMenu("Sistema");
        JMenuItem itemSair = new JMenuItem("Sair");
        itemSair.addActionListener(e -> System.exit(0));
        menuSistema.add(itemSair);

        barra.add(menuCadastros);
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

        JButton botaoClientes = new JButton("Cadastro de Clientes");
        botaoClientes.setPreferredSize(new Dimension(240, 44));
        botaoClientes.addActionListener(e -> abrirCadastroClientes());

        JButton botaoUsuarios = new JButton("Cadastro de Usuarios");
        botaoUsuarios.setPreferredSize(new Dimension(240, 44));
        botaoUsuarios.addActionListener(e -> abrirCadastroUsuarios());

        c.gridy = 0; painel.add(botaoClientes, c);
        c.gridy = 1; painel.add(botaoUsuarios, c);

        return painel;
    }

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBorder(BorderFactory.createEmptyBorder(4, 12, 10, 12));

        JLabel rotulo = new JLabel("Grupo 3 - Analise e Projeto de Software OO");
        rotulo.setHorizontalAlignment(SwingConstants.CENTER);
        rotulo.setFont(rotulo.getFont().deriveFont(Font.PLAIN, 11f));

        rodape.add(rotulo, BorderLayout.CENTER);
        return rodape;
    }

    private void abrirCadastroClientes() {
        new TelaCadastroCliente().setVisible(true);
    }

    private void abrirCadastroUsuarios() {
        new TelaCadastroUsuario().setVisible(true);
    }
}
