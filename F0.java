import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class F0 extends Socket  {
	static int ctime = 15;
	static Map<String, LinkedList<Object>> mm = new HashMap<String, LinkedList<Object>>();
	static Map<String, String> chain = new HashMap<String, String>();
	public static void main(String[] args) {
		chain.put("open", "accept");
		chain.put("accept", "read");
		chain.put("read", "write");
		chain.put("write", "read");
		LinkedList<Object> ct =  getMM(cm());
		ct.offer(getM("accept"));
		ct.offer(getM("read"));
		ct.offer(getM("write"));
		getMM("open").offer(8888);
		invoke(getM("open"));
		while(true) {
			try {
				Method m = (Method) ct.poll();
				invoke(m);
				ct.offer(m);
			}catch(Exception ex) {ex.printStackTrace();}
		}
	}
	public static void invoke(Method m) {
		try {
			LinkedList<Object> mt =  getMM(m.getName());
			Object mp = mt.poll();
			if(null!=mp) {
				Object r = m.invoke(null, mp, mt);
				String nm = chain.get(m.getName());
				if(r!=null && nm!=null) {
					getMM(nm).offer(r);
					sysout(m.getName(),"----",mp,"--",nm,"==",r);
					meminfo();
				}
			}
		}catch(Exception ex) {ex.printStackTrace();}
	}
	public static Object open(Object task, LinkedList<Object> mt) {
		try {
			sysout("bind:",task);
			ServerSocket ss = new ServerSocket(Integer.parseInt(task.toString()));
			ss.setSoTimeout(ctime);
			return ss;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static Object accept(Object task, LinkedList<Object> mt) {
		try {
			ServerSocket ss = (ServerSocket)task;
			//sysout(String.format("try accept:%s %s", ss.getLocalPort(), System.currentTimeMillis()));
			Socket sc = ss.accept();mt.offer(task);
			sysout("accept:",sc);
			sc.setSoTimeout(1000);
			return sc;
		} catch (SocketTimeoutException e) {
			//e.printStackTrace();
			mt.offer(task);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static Map<Socket, StringBuilder> smem = new HashMap<Socket, StringBuilder>();
	static Map<Socket, Socket> smap = new HashMap<Socket, Socket>();
	public static Object read(Object task, LinkedList<Object> mt) {
		try {
			Socket sc = (Socket)task;
			//sysout("try read ",scid(sc));
			byte[] bs;
			if(sc instanceof F0) {
				bs = new byte[sc.getReceiveBufferSize()/2];//4096
			}
			else {
				bs = new byte[100];//2048
			}
			int rlen = sc.getInputStream().read(bs);
			if(rlen>0) {
				StringBuilder req = smem.get(sc);
				if(req==null) {
					req = new StringBuilder();
				}
				int bidx = req.length();
				for(int i=0; i<rlen; i++) {
					req.append((char)bs[i]);
				}
				sysout(String.format("from %s %s:\n%s", sc,rlen, req.substring(bidx).replace("\r", "r").replace("\n","n")));
				if(isReadEnd(req, !(sc instanceof F0))) {
					sysout("read finish: " , sc);
					smem.put(sc, req);
					return sc;
				}
				else {
					smem.put(sc, req);
					mt.offer(task);
				}
			}
			else {
				sysout("close:",task);
				sc.close();
			}
		} catch (SocketTimeoutException e) {
			//e.printStackTrace();
			mt.offer(task);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
	}
	public static Object write(Object task, LinkedList<Object> mt) {
		try {
			Socket sc = (Socket)task;
			StringBuilder req = smem.get(sc);
			Socket target = smap.get(sc);
			if(target==null) {
				target = new F0();
				target.connect(new InetSocketAddress("localhost", 8080), 3000);
				smap.put(target, sc);
			}
			else {
			}
			byte[] bs = req.toString().getBytes();
			target.getOutputStream().write(bs);
			if(!(target instanceof F0)) {
				smap.remove(task);
				smem.remove(task);
				smem.remove(target);
				target.close();
				sc.close();
			}
			sysout("write:",bs.length,":",target);
			return target;
		} catch (SocketTimeoutException e) {
			//e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
		return null;
	}
	
	
	public static boolean isReadEnd(StringBuilder in, boolean isReq) {
		boolean r = true;
		if(isReq && in.indexOf("GET")==0) {
			if(in.length()>=7 && in.indexOf("\r\n\r\n")==in.length()-4) {
				r=true;
			}
			else {
				r=false;
			}
		}
		else if(!isReq && in.indexOf("HTTP/")==0){
			int hidx = in.indexOf("\r\n\r\n");
			if(hidx>0) {
				String head = in.substring(0, hidx);
				if(head.indexOf("Transfer-Encoding: chunked")>0) {
					if(in.length()>=7 && in.toString().endsWith("0\r\n\r\n")) {
						r=true;
					}
					else {
						r=false;
					}
				}
				else {
					String lflag = "Content-Length: ";
					int lidx = head.indexOf(lflag);
					if(lidx>0) {
						int len = Integer.parseInt(in.substring(lidx+lflag.length(), in.indexOf("\r\n", lidx)));
						if(len==in.length()-hidx-4) {
							r=true;
						}
						else {
							r=false;
						}
					}
				}
			}
		}
		return r;
	}
	
	public static String cm() {
		return Thread.currentThread().getStackTrace()[2].getMethodName();
	}
	public static Method getM(String mname) {
		for(Method m: F0.class.getDeclaredMethods()) {
			if(m.getName().equals(mname)) {
				return m;
			}
		}
		return null;
	}
	public static LinkedList<Object> getMM(String mname) {
		LinkedList<Object> mv = mm.get(mname);
		if(mv==null) {
			mv = new LinkedList<Object>();
			mm.put(mname, mv);
		}
		return mv;
	}
	public static void meminfo() {
		boolean p = false;
		if(!p) return;
		for(Field f: F0.class.getDeclaredFields()) {
			try {
				sysout(f,":",f.get(null));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public static void sysout(Object... obj) {
		System.out.println(Arrays.toString(obj));
	}
}
