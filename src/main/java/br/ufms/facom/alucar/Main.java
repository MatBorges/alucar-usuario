package br.ufms.facom.alucar;

import br.ufms.facom.alucar.view.TelaPrincipal;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;


public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}


        SwingUtilities.invokeLater(() -> new TelaPrincipal().setVisible(true));
    }
}
