import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class F1 {
	public static void main(String[] args) throws Exception {
		Selector st = Selector.open();
		SocketChannel sc = SocketChannel.open();
		sc.configureBlocking(false);
		sc.register(st, SelectionKey.OP_CONNECT);
		sc.connect(new InetSocketAddress("cn.bing.com", 443));
		
		long ast = System.currentTimeMillis();
		while (true) {
			int readyChannels = st.select();
			System.out.println("timeused:"+(System.currentTimeMillis()-ast));
			if((System.currentTimeMillis()-ast)>2000) {
				sc.close();
			}
			if (readyChannels == 0)
				continue;
			Set selectedKeys = st.selectedKeys();
			Iterator keyIterator = selectedKeys.iterator();
			while (keyIterator.hasNext()) {
				SelectionKey key = (SelectionKey) keyIterator.next();
				keyIterator.remove();
				p(key);
				if (key.isAcceptable()) {
					// a connection was accepted by a ServerSocketChannel.
				} else if (key.isConnectable()) {
					w((SocketChannel) key.channel(), "GET / HTTP/1.1\r\nHost: cn.bing.com\r\n\r\n");
					key.interestOps(SelectionKey.OP_READ);
				} else if (key.isReadable()) {
					if(r((SocketChannel) key.channel())<0) {
						key.channel().close();
					};
				} else if (key.isWritable()) {
					// a channel is ready for writing
				}
			}
		}
	}

	public static int r(SocketChannel sc) throws Exception {
		ByteBuffer readBuff = ByteBuffer.allocate(1024);
		readBuff.clear();
		int rlen = sc.read(readBuff);
		System.out.println(rlen);
		System.out.println(new String(readBuff.array()));
		return rlen;
	}
	
	public static byte[] hexStringToBytes(String hexString) {  
	    if (hexString == null || hexString.equals("")) {  
	        return null;  
	    }  
	    hexString = hexString.toUpperCase();  
	    int length = hexString.length() / 2;  
	    char[] hexChars = hexString.toCharArray();  
	    byte[] d = new byte[length];  
	    for (int i = 0; i < length; i++) {  
	        int pos = i * 2;  
	        d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));  
	    }  
	    return d;  
	}  
	private static byte charToByte(char c) {  
	    return (byte) "0123456789ABCDEF".indexOf(c);  
	}  


	public static void w(SocketChannel sc, String newData) throws Exception {
		if(!sc.finishConnect()) {return;}
        dw(sc, hexStringToBytes("160303000101"));  
	}
	
	public static void dw(SocketChannel sc, byte[] bytes) throws Exception {
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		while (buf.hasRemaining()) {
			System.out.println(sc.write(buf));
		}
	}
	
	public static void p(SelectionKey key) throws Exception {
		boolean back = true;
		if(back) return;
		System.out.println(key);
		System.out.println(key.channel());
		if(!key.isValid()) return;
		for(Method m: SelectionKey.class.getDeclaredMethods()) {
			if(m.getName().startsWith("is")) {
				//if((Boolean)m.invoke(key, null)) System.out.println(m.getName()+":"+m.invoke(key, null));
			}
		}
	}
}
