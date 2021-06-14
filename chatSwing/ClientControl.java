import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class ClientControl {
    String nick;
    MySocket socket;
    ClientGUI interficie;

    public ClientControl() {
        this.interficie=new ClientGUI();
        interficie.frame.setDefaultCloseOperation(3);
        this.interficie.frame.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        ClientControl client = new ClientControl();
        client.run();
    }

    private void run() throws IOException {
        String serverAddress[] = getIP();
        System.out.println("IP port:"+serverAddress[0]+":"+serverAddress[1]);
        this.socket = new MySocket(serverAddress[0], Integer.parseInt(serverAddress[1]));
        this.interficie.setCont(this);
        this.interficie.textField.requestFocus();
        while (true) {
            String line = socket.readLine();
            switch (line.charAt(0)) {
                case '\01':
                    nick = interficie.getNick();
                    this.socket.println('\u0001' +" " + nick);
                    break;
                case '\03':
                    System.out.println(line.substring(1));
                    this.interficie.messageArea.append(line.substring(1) + "\n");
                    break;
                case '\04':
                    this.interficie.clients.addElement(line.substring(1));
                    break;
                case '\05':
                    this.interficie.clients.removeElement(line.substring(1));
                    break;
                default:
            }
        }
    }

    public String[] getIP(){ 
        return this.interficie.getIP_interficie();
    }

    public void Sender(String s){
        socket.println(s);
    }


}
