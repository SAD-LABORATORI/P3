
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class ClientGUI {

    JFrame frame = new JFrame("Chat Gràfic");
    JPanel input = new JPanel();
    JTextField textField = new JTextField(60);
    JTextArea messageArea = new JTextArea(30, 60);
    DefaultCaret caret = (DefaultCaret) messageArea.getCaret();
    DefaultListModel clients = new DefaultListModel();
    JList users = new JList((DefaultListModel) clients);
    JButton button = new JButton("ENVIAR");
    JButton nickButton = new JButton("CANVI DE NOM");
    JScrollPane usersListScroll = new JScrollPane(users);
    String nick;
    MySocket socket;
    ClientControl control;

    public ClientGUI() {
        messageArea.setEditable(false);
        messageArea.setBackground(Color.WHITE);
        messageArea.setLineWrap(true);

        users.setLayoutOrientation(JList.VERTICAL);
        users.setBackground(Color.LIGHT_GRAY);

        button.setPreferredSize(new Dimension(95, 30));

        button.addActionListener(new Send());
        nickButton.setPreferredSize(new Dimension(150,30));
        nickButton.addActionListener(new ChangeNick());
        textField.addActionListener(new Send());

        input.add(textField, "Center");
        input.add(button, "East");
        input.add(nickButton,"East");
        input.setBackground(Color.LIGHT_GRAY);

        frame.getContentPane().add(input, "South");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.getContentPane().add(usersListScroll, "East");

        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        frame.pack();
        
    }

    public void setCont(ClientControl c){
        control=c;
    }

    public String[] getIP_interficie(){
        String[] ip=JOptionPane.showInputDialog(frame, "Introdueix la IP:Port del servidor", "localhost:10000").split(":");
        return ip;
    }

    public String getNick() {
        return JOptionPane.showInputDialog(frame, "Escriu el teu nom:", "Josep");
    }
    public class Send implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String msg = textField.getText();
            if (!msg.isEmpty()) {
                control.Sender(msg);
                textField.setText("");
            }
        }
    }

    public class ChangeNick implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String nick = getNick();
            control.Sender('\u0001' +" " + nick);
        }
    }
}
