
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

    public static void main(String[] args) throws Exception {
        ClientGUI client = new ClientGUI();
        // El programa tanca quan apretem la x
        client.frame.setDefaultCloseOperation(3);//3 means EXIT_ON_CLOSE
        client.frame.setVisible(true);
        client.run();
    }

    private void run() throws IOException {
        String serverAddress[] = getIP();
        System.out.println("IP port:"+serverAddress[0]+":"+serverAddress[1]);
        this.socket = new MySocket(serverAddress[0], Integer.parseInt(serverAddress[1]));
        textField.requestFocus();
        while (true) {
            String line = socket.readLine();
            System.out.println(line);
            switch (line.charAt(0)) {
                case '\01':
                    nick = getNick();
                    socket.println('\u0001' +" " + nick);
                    break;
                case '\03':
                    messageArea.append(line.substring(1) + "\n");
                    break;
                case '\04':
                    clients.addElement(line.substring(1));
                    System.out.println(clients);
                    break;
                case '\05':
                    clients.removeElement(line.substring(1));
                    break;
                default:
            }
        }
    }

    private String[] getIP() {
        System.out.println("GET IP FUNCTION");
        String[] ip=JOptionPane.showInputDialog(frame, "Introdueix la IP:Port del servidor", "localhost:10000").split(":");
        System.out.println("CLOSE IP FUNCTION");
        return ip;
    }

    private String getNick() {
        return JOptionPane.showInputDialog(frame, "Escriu el teu nom:", "Josep");
    }

    public class Send implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String msg = textField.getText();
            if (!msg.isEmpty()) {
                socket.println(msg);
                textField.setText("");
            }
        }
    }

    public class ChangeNick implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String nick = getNick();
            socket.println('\u0001' +" " + nick);

        }
    }

}
