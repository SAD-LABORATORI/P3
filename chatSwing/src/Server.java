
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.IOException;
import java.util.*;


public class Server implements Runnable {
    private final int port;
    private ServerSocketChannel serversocket;
    private Selector selector;
    private ByteBuffer buf = ByteBuffer.allocate(256);
    private HashMap<SocketChannel, String> users;

    Server(int port) throws IOException {
        this.port = port;
        this.users = new HashMap<>();
        this.serversocket = ServerSocketChannel.open();
        this.serversocket.socket().bind(new InetSocketAddress(port));
        this.serversocket.configureBlocking(false);
        this.selector = Selector.open();
        this.serversocket.register(selector, SelectionKey.OP_ACCEPT);
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(10000);
        (new Thread(server)).start();
        while(true);
    }

    @Override 
    public void run() {
        try {
            System.out.println("Servidor obrint al port ----->" + this.port);
            Iterator<SelectionKey> iter;
            SelectionKey key;
            while(this.serversocket.isOpen()) {
                selector.select();
                iter=this.selector.selectedKeys().iterator();
                while(iter.hasNext()) {
                    key = iter.next();
                    iter.remove();
                    if(key.isAcceptable()) this.handleAccept(key);
                    if(key.isReadable()) this.handleRead(key);
                }
            }
        } catch(IOException e) {
            System.out.println("Error en el servidor de port"+ this.port);
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ByteBuffer welcomeBuf = ByteBuffer.wrap("Benvingut!\n".getBytes());
        SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
        String InetAddress = (new StringBuilder(sc.socket().getInetAddress().toString())).toString();
        String port_String = (new StringBuilder(sc.socket().getPort())).toString();
        String address = (new StringBuilder(InetAddress)).append(":").append(port_String).toString();

        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ, address);
        sc.write(welcomeBuf);
        welcomeBuf.rewind();
        this.users.put(sc, address);
        connectedClients(sc);  
        broadcast('\u0004'+" "+this.users.get(sc)+"\n");  
        sc.write(ByteBuffer.wrap(('\u0001'+" \n").getBytes()));  

        System.out.println("Connexió acceptada de: "+ this.users.get(sc));
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        StringBuilder constructorstrings = new StringBuilder();
        int read;

        buf.clear();
        while( (read = socketChannel.read(buf)) > 0 ) {
            buf.flip();
            byte[] bytes = new byte[buf.limit()];
            buf.get(bytes);
            constructorstrings.append(new String(bytes));
            buf.clear();
        }String missatge;
        if(read < 0) {
            missatge = this.users.get(socketChannel)+" ha sortit del chat\n";
            socketChannel.close();
            broadcast('\u0005'+" "+this.users.get(socketChannel)+"\n"); 
            this.users.remove(socketChannel);
        }else if(constructorstrings.toString().charAt(0) == '\u0001'){    
            String antic_nom = this.users.get(socketChannel);
            handleNickChanges(socketChannel, constructorstrings.toString().substring(1).replace("\n",""));
            missatge = "->El client " + antic_nom + " ha canviat el nom a: "+ this.users.get(socketChannel) +"\n";
        }else {
            missatge = this.users.get(socketChannel)+": "+ constructorstrings.toString().replace("\n","")+ "\n";
        }

        System.out.print(missatge);
        broadcast('\u0003' + " " +missatge);
    }

    //Per facilitar la feina cada cop que un usuari canvii de nom el borrarem i el tornarem a posar a la llista.
    private void handleNickChanges(SocketChannel sc, String nick) throws IOException{
        broadcast('\u0005' + " " + this.users.get(sc) + "\n");
        this.users.remove(sc);
        this.users.put(sc, nick);
        broadcast('\u0004'+ " " + this.users.get(sc) + "\n");
    }

    private void broadcast(String missatge) throws IOException {
        ByteBuffer msgBuf=ByteBuffer.wrap(missatge.getBytes());
        for(SelectionKey key : selector.keys()) {
            if(key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel socketchannel=(SocketChannel) key.channel();
                socketchannel.write(msgBuf);
                msgBuf.rewind();
            }
        }
    }

    private void connectedClients(SocketChannel sc) throws IOException {
        this.users.forEach((key, value) -> {
            if (value != this.users.get(sc)) {
                ByteBuffer msgBuf = ByteBuffer.wrap(('\u0004'+" "+value+"\n").getBytes());
                try{
                    sc.write(msgBuf);
                }catch(IOException e){
                    System.out.println("Error en la funció connectedClients");
                }
            }
        });
    }

}