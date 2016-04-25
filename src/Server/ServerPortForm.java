package Server;

import javax.swing.*;
import javax.swing.UIManager.*;

/**
 * Created by erickchandra on 4/25/16.
 */
public class ServerPortForm {
    private JTextField textField1;
    private JButton OKButton;
    private JPanel panelMain;

    public static void main(String[] args) {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }

        JFrame frame = new JFrame("ServerPortForm");
        frame.setContentPane(new ServerPortForm().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setTitle("Werewolf Server");
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
