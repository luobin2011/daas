import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class F2 {
	static Map<SocketChannel, SocketChannel> smap = new HashMap<SocketChannel, SocketChannel>();
	static ByteBuffer buf = ByteBuffer.allocate(4086);
	public static void main(String[] args) {
		try {
			main2(args);
		}catch(Exception ex) {
			System.err.println(System.currentTimeMillis());
			ex.printStackTrace();
		}
	}
	static Selector st = null;
	static ServerSocketChannel ss = null;
	public static void init() throws Exception {
		st = Selector.open();
		ss = ServerSocketChannel.open();
		ss.configureBlocking(false);
		ss.register(st, SelectionKey.OP_ACCEPT);
		ss.socket().bind(new InetSocketAddress(8888));
	}
	public static int stselect() {
		try {
			return st.select();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	public static SocketChannel a(SelectionKey key) {
		SocketChannel clientChannel = null;
		try {
			clientChannel = ((ServerSocketChannel) key.channel()).accept();
			clientChannel.configureBlocking(false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("accept:" + clientChannel);
		return clientChannel;
	}
	public static SocketChannel finishConnect(SelectionKey key) {
		SocketChannel sc = (SocketChannel) key.channel();
		try {
			if(sc.finishConnect()) {
				return sc;
			}
		} catch (Exception e) {
			e.printStackTrace();
			cnm(sc);
		}
		return null;
	}
	public static int rw(SelectionKey key) {
		SocketChannel sc = (SocketChannel) key.channel();
		buf.clear();
		int rlen = 0;
		try {
			rlen = sc.read(buf);
		}catch(Exception ex) {
			System.err.println(sc);
			System.err.println(ex.getMessage());
			cnm(sc);
		}
		return rlen;
	}
	public static void cnm(SocketChannel sc) {
		try {
			sc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			smap.get(sc).close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		smap.remove(smap.remove(sc));
		System.out.println(smap);
	}
	public static void nm(SocketChannel csc,int count) {
		if(csc==null)return;
		if(count<=0) return;
		SocketChannel sc = null;
		try {
			sc = SocketChannel.open();
			sc.configureBlocking(false);
			sc.register(st, SelectionKey.OP_CONNECT);
			sc.connect(new InetSocketAddress("localhost", 8080));
		} catch (Exception e) {
			e.printStackTrace();
			if(null!=sc) {
				try {
					sc.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				sc = null;
			}
			nm(csc,--count);
		}
		if(null!=sc) {
			smap.put(csc, sc);
			smap.put(sc, csc);
		}
	}
	public static void rr(SocketChannel sc) {
		if(sc==null)return;
		try {
			System.out.println("connect:" + sc);
			sc.register(st, SelectionKey.OP_READ);
			smap.get(sc).register(st, SelectionKey.OP_READ);
		} catch (Exception e) {
			e.printStackTrace();
			cnm(sc);
		}
	}
	public static void main2(String[] args) {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		int sr = 0;
		while (true) {
			sr = stselect();
			if(sr<0) {
				return;
			}
			if(sr==0) {
				continue;
			}
			Iterator keyIterator = st.selectedKeys().iterator();
			while (keyIterator.hasNext()) {
				SelectionKey key = (SelectionKey) keyIterator.next();
				keyIterator.remove();
				if (key.isAcceptable()) {
					nm(a(key),1);
				} else if (key.isConnectable()) {
					 rr(finishConnect(key));
				} else if (key.isReadable()) {
					dw(rw(key),key);
				} else if (key.isWritable()) {
					// a channel is ready for writing
				}
			}
		}
	}
	public static void dw(int rlen,SelectionKey key) {
		SocketChannel sc = (SocketChannel) key.channel();
		if(rlen>0) {
			System.out.println("read:" + sc);
			System.out.println(rlen);
			System.out.println(System.currentTimeMillis()+":write:" + smap.get(sc));
			buf.flip();
			byte[] bs = Arrays.copyOfRange(buf.array(), 0, buf.limit());
			System.out.println(Arrays.toString(bs));
			while (buf.hasRemaining()) {
				try {
					System.out.println(smap.get(sc).write(buf));
				} catch (Exception e) {
					e.printStackTrace();
					cnm(sc);
				}
			}
		}
		if(rlen<0) {
			cnm(sc);
		}
	}
}
