package br.ufms.facom.alucar;

import br.ufms.facom.alucar.view.TelaPrincipal;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;


public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Mantem o look and feel padrao do Java caso o do sistema falhe.
        }

        // Toda criacao e manipulacao de componentes Swing deve ocorrer na
        // Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> new TelaPrincipal().setVisible(true));
    }
}
