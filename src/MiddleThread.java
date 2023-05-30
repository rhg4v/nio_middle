import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class MiddleThread implements Runnable {

    private SocketChannel socketChannel;
    private SocketChannel client;

    public MiddleThread() {

    }

    public MiddleThread(SocketChannel socketChannel, SocketChannel client) {
        this.socketChannel = socketChannel;
        this.client = client;
    }

    @Override
    public void run() {

        try {

            while (true) {
                ByteBuffer byteBuffer = setByteBuffer();
                readSocketChannel(byteBuffer);

                if (byteBuffer.position() == 0) {
                    continue;
                }

                System.out.println("receive: "+new String(byteBuffer.array(), StandardCharsets.UTF_8));
                writeSocketChannel(byteBuffer);
            }
        } catch (IOException e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }

    }

    private void readSocketChannel(ByteBuffer byteBuffer) throws IOException {
        client.read(byteBuffer);
    }

    private void writeSocketChannel(ByteBuffer byteBuffer) throws IOException {
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
        byteBuffer.clear();
    }

    private static ByteBuffer setByteBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(500);

        return byteBuffer;
    }

}
