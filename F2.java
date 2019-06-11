import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class F2 {
	static Map<SocketChannel, SocketChannel> smap = new HashMap<SocketChannel, SocketChannel>();
	static ByteBuffer buf = ByteBuffer.allocate(2048);
	public static void main(String[] args) throws Exception {
		Selector st = Selector.open();
		ServerSocketChannel ss = ServerSocketChannel.open();
		ss.configureBlocking(false);
		ss.register(st, SelectionKey.OP_ACCEPT);
		ss.socket().bind(new InetSocketAddress(8888));
		
		while (true) {
			if(st.select()==0) {
				continue;
			}
			Set selectedKeys = st.selectedKeys();
			Iterator keyIterator = selectedKeys.iterator();
			while (keyIterator.hasNext()) {
				SelectionKey key = (SelectionKey) keyIterator.next();
				keyIterator.remove();
				if (key.isAcceptable()) {
					SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
					clientChannel.configureBlocking(false);
					//clientChannel.register(st, SelectionKey.OP_READ);

					System.out.println("accept:" + clientChannel);
					
					SocketChannel sc = SocketChannel.open();
					sc.configureBlocking(false);
					sc.register(st, SelectionKey.OP_CONNECT);
					sc.connect(new InetSocketAddress("cn.bing.com", 443));
					smap.put(clientChannel, sc);
					smap.put(sc, clientChannel);
				} else if (key.isConnectable()) {
					SocketChannel sc = (SocketChannel) key.channel();
					if(!sc.finishConnect()) {
						continue;
					}
					else {
						System.out.println("connect:" + sc);
						sc.register(st, SelectionKey.OP_READ);
						smap.get(sc).register(st, SelectionKey.OP_READ);
					}
				} else if (key.isReadable()) {
					SocketChannel sc = (SocketChannel) key.channel();
					System.out.println("read:" + sc);
					buf.clear();
					int rlen = sc.read(buf);
					if(rlen>0) {
						System.out.println("write:" + smap.get(sc));
						System.out.println(rlen);
						buf.flip();
						while (buf.hasRemaining()) {
							System.out.println(smap.get(sc).write(buf));
						}
					}
				} else if (key.isWritable()) {
					// a channel is ready for writing
				}
			}
		}
	}
}
