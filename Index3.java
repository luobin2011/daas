import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;

import javax.sql.rowset.CachedRowSet;

import com.sun.rowset.CachedRowSetImpl;

/**
 * 
//request-->url
//url-->className
//ctx-->classField
//?m.invoke
//classField-->response
Object task = new URLClassLoader(new URL[]{
		new URL("file:/D:/Users/luob4/dev/eclipse-workspace/t/bin/")
		,new URL("file:/D:/Users/luob4/dev/jdk1.8.0_171/db/lib/derby.jar")
}).loadClass(request.getParameter("className")).newInstance();
for(String key: request.getParameterMap().keySet()) {
	try {
		task.getClass().getDeclaredField(key).set(task, request.getParameter(key));
	}catch(Exception ex1){
		try {
			task.getClass().getDeclaredField(key).set(task, request.getParameterValues(key));
		}catch(Exception ex2){
		}
	}
}
out.write(task.toString());
System.out.println(db("create table ttt (id int primary key)"));
//System.out.println(db("insert into xxx values (1,'xef')"));
//System.out.println(db("insert into xxx values (2,'fff')"));
//System.out.println(db("select * from xxx OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY"));
//System.out.println(db("show tables"));
//out.write(dodb(request).toString());
 * 
 * @author luob4
 *
 */
public class Index3 {

	public String q;

	public String[] d;

	@Override
	public String toString() {
		//top 10
		//up 10
		//down 10
		//back to start
//		d=a=id+10
//		d=f-k-k-k-k-f
//		d=t-f-f-f-f
//		d=t-f-f-f=a+b-c+10
//		q=a=b++=a=b--=a=b=a=b*=a=b*c*d
//
//		a=b+	>
//		a=b++	>=
//		a=b-	<
//		a=b--	<=
//		a=b		=
//		a=b*	like
//		a=b*c*d 	in
//
//		select d,d,d,d from a where q=v and q=v order by a asc limit 0,10
		//create table xxx (id int primary key);
		//alter table xxx add column ccc varchar(255);
		//tb-a-b-c-d-e-f-g
		return "select fff,fff,fff from tttt where qqq=vvv and qqq=vvv and qqq=vvv and id<maxid order by id desc";
	}
	
	/**
	 * 开发用的db方法
	 * @param sql
	 * @return
	 */
	public static Object db(String sql) {
		String protocol = "jdbc:derby:";
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		try {
			Class.forName(driver).newInstance();
			Connection conn = DriverManager.getConnection(protocol + "tt;create=true", null);
			if(sql.toLowerCase().indexOf("update")==0 || sql.toLowerCase().indexOf("insert")==0 
					|| sql.toLowerCase().indexOf("delete")==0 || sql.toLowerCase().indexOf("create")==0 
					|| sql.toLowerCase().indexOf("alter")==0 ) {
				return conn.prepareStatement(sql).executeUpdate();
			}
			else if(sql.toLowerCase().indexOf("select")==0 ) {
				CachedRowSet crs = new CachedRowSetImpl();  
				crs.populate(conn.prepareStatement(sql).executeQuery());
				ResultSetMetaData md = crs.getMetaData();
				Map r = new HashMap();
				for(int i=0, len=md.getColumnCount(); i<len; i++) {
					int idx = i+1;
					r.put(md.getColumnLabel(idx), crs.toCollection(idx));
				}
				return r;
			}
			else {
				CachedRowSet crs = new CachedRowSetImpl();  
				crs.populate(conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"}));
				ResultSetMetaData md = crs.getMetaData();
				Map r = new HashMap();
				for(int i=0, len=md.getColumnCount(); i<len; i++) {
					int idx = i+1;
					r.put(md.getColumnLabel(idx), crs.toCollection(idx));
				}
				return r;
			}
		} catch (Exception e) {
			return e.getMessage();
		} finally {
			
		}
	}
	
	public static final int HIGHEST_SPECIAL = '>';
	public static char[][] specialCharactersRepresentation = new char[HIGHEST_SPECIAL + 1][];
	static {
		specialCharactersRepresentation['&'] = "&amp;".toCharArray();
		specialCharactersRepresentation['<'] = "&lt;".toCharArray();
		specialCharactersRepresentation['>'] = "&gt;".toCharArray();
		specialCharactersRepresentation['"'] = "&#034;".toCharArray();
		specialCharactersRepresentation['\''] = "&#039;".toCharArray();
	}

	public static String escapeXml(String buffer) {
		int start = 0;
		int length = buffer.length();
		char[] arrayBuffer = buffer.toCharArray();
		StringBuffer escapedBuffer = null;

		for (int i = 0; i < length; i++) {
			char c = arrayBuffer[i];
			if (c <= HIGHEST_SPECIAL) {
				char[] escaped = specialCharactersRepresentation[c];
				if (escaped != null) {
					if (start == 0) {
						escapedBuffer = new StringBuffer(length + 5);
					}
					if (start < i) {
						escapedBuffer.append(arrayBuffer, start, i - start);
					}
					start = i + 1;
					escapedBuffer.append(escaped);
				}
			}
		}
		if (start == 0) {
			return buffer;
		}
		if (start < length) {
			escapedBuffer.append(arrayBuffer, start, length - start);
		}
		return escapedBuffer.toString();
	}
}
