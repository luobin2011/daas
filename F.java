import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentSkipListMap;

public class F extends Socket {
	
	public static ServerSocket ss = null;
	static LinkedList<Socket> tasks = new LinkedList<Socket>();
	static LinkedList<Socket> tasks2 = new LinkedList<Socket>();
	static ConcurrentSkipListMap<String, Socket> objects = new ConcurrentSkipListMap<String, Socket>();
	static {
		try {
			ss = new ServerSocket(8886,1);
			ss.setSoTimeout(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		while(true) {
			loop();
		}
	}
	
	static void loop() {
		Socket t = tasks.poll();
		if(null==t) {
			LinkedList<Socket> tmptask = tasks;
			tasks=tasks2;
			tasks2=tmptask;
			accept();
		}
		else {
			scread(t);
		}
	}
	
	static void scread(Socket sc) {
		try {
			//System.out.println("try read "+scid(sc));
			byte[] bs = new byte[sc.getReceiveBufferSize()/4];//2048
			int rlen = sc.getInputStream().read(bs);
			if(rlen>0) {
				tasks2.offer(sc);
				System.out.println(String.format("from %s %s:\n%s", scid(sc),rlen, new String(bs, 0, rlen).replace("\r", "\\r").replace("\n","\\n")));
				if(bs[0]==(byte)'/' || bs[0]==(byte)'=') {
					int idx = -1;
					for(int i=0,len=bs.length; i<len; i++) {
						if(bs[i]==(byte)'=') {
							idx=i;
							break;
						}
					}
					if(idx>0) {
						String to = new String(bs, 0, idx);
						String msg = new String(bs, idx+1, rlen-idx-1);
						if(objects.containsKey(to)) {
							if(objects.get(to) instanceof F) {
								scwrite(objects.get(to), msg.getBytes());
							}
							else {
								scwrite(objects.get(to), (scid(sc)+"="+msg).getBytes());
							}
						}
						else {
							nsc(to);
						}
					}
					if(idx==0) {
						scwrite(sc, (scid(sc)+"="+objects.keySet().toString().replace(" ","")).getBytes());
					}
				}
			}
			else {
				System.out.println(String.format("close: %s", scid(sc)));
				objects.remove(scid(sc));
			}
		} catch (SocketTimeoutException e) {
			tasks2.offer(sc);
			//e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(String.format("close: %s", scid(sc)));
			objects.remove(scid(sc));
		}
	}
	
	static void nsc(String scid) {
		try {
			String[] ss = scid.split(":");
			Socket sc = new F();
			sc.connect(new InetSocketAddress(ss[0].replace("/", ""), Integer.parseInt(ss[1])), 3000);
			sc.setSoTimeout(1000);
			tasks.offer(sc);
			objects.put(scid(sc), sc);
			System.out.println(String.format("connect: %s", scid(sc)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static void scwrite(Socket sc, byte[] bs) {
		System.out.println(String.format("write %s %s:\n%s", scid(sc), bs.length, new String(bs).replace("\r", "\\r").replace("\n","\\n")));
		try {
			sc.getOutputStream().write(bs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static void accept() {
		try {
			//System.out.println(String.format("try accept:%s", System.currentTimeMillis()));
			Socket sc = ss.accept();
			sc.setSoTimeout(1000);
			tasks.offer(sc);
			objects.put(scid(sc), sc);
			System.out.println(String.format("accept: %s", scid(sc)));
		} catch (SocketTimeoutException e) {
			//e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static String scid(Socket sc) {
		return sc.getRemoteSocketAddress().toString();
	}
}
