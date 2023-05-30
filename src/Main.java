import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws IOException {
        try (ServerSocketChannel middleSocketChannel = ServerSocketChannel.open();) {
            Selector selector = Selector.open();
            setSocketChannel(middleSocketChannel);
            middleSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {

                Iterator<SelectionKey> iterator = getSelectionKeyIterator(selector);

                while (iterator.hasNext()) {

                    SelectionKey key = iterator.next();

                    if (key.isAcceptable()) {

                        SocketChannel client = setSocketChanelForTransferSelectKey(key);
                        client.register(selector, SelectionKey.OP_READ);

                    } else if (key.isReadable()) {

                        try (SocketChannel socketChannel = SocketChannel.open()) {

                            setSocketChannel(socketChannel);
                            SocketChannel client = (SocketChannel) key.channel();
                            runMiddleThread(socketChannel, client);

                            while (true) {

                                ByteBuffer byteBuffer = setByteBuffer();
                                readSocket(socketChannel, byteBuffer);
                                System.out.println("go: "+new String(byteBuffer.array(), StandardCharsets.UTF_8));
                                writeSocket(client, byteBuffer);
                            }
                        }
                    }
                }

                iterator.remove();
            }
        }
    }

    private static void writeSocket(SocketChannel client, ByteBuffer byteBuffer) throws IOException {
        byteBuffer.flip();
        client.write(byteBuffer);
        byteBuffer.clear();
    }

    private static void readSocket(SocketChannel socketChannel, ByteBuffer byteBuffer) throws IOException {
        socketChannel.read(byteBuffer);
    }

    private static ByteBuffer setByteBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(500);

        return byteBuffer;
    }

    private static void runMiddleThread(SocketChannel socketChannel, SocketChannel client) {
        Thread thread;
        thread = new Thread(new MiddleThread(socketChannel, client));
        thread.start();
    }

    private static void setSocketChannel(SocketChannel socketChannel) throws IOException {
        socketChannel.bind(new InetSocketAddress(9000));
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 8083));
    }

    private static SocketChannel setSocketChanelForTransferSelectKey(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannelAcceptable = (ServerSocketChannel) key.channel();
        SocketChannel client = serverSocketChannelAcceptable.accept();
        client.configureBlocking(false);

        return client;
    }

    private static Iterator<SelectionKey> getSelectionKeyIterator(Selector selector) throws IOException {
        selector.select();
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();

        return iterator;
    }

    private static void setSocketChannel(ServerSocketChannel middleSocketChannel) throws IOException {
        middleSocketChannel.bind(new InetSocketAddress("localhost", 8080));
        middleSocketChannel.configureBlocking(false);
    }

}
