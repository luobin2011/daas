import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class Index5 {

	public Class che;
	public Method dao;
	// a=b++=a=b--=a=b=a=b*=a=b*c*d //query
	// t+f=a-t=a-f=a-id=1 //insert/update
	public String q;
	// t-f-f-f=a+b-c+10 //query
	// a=id+10 //query
	// t+f=a-b-c-d-e-f //insert/update
	public String[] d;
	// sp =+-*

	String css = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><style type=\"text/css\">dt {\r\n" + 
			"    overflow: auto;\r\n" + 
			"position: fixed;"+
			"}\r\n" + 
			"\r\n" + 
			"dd {\r\n" + 
			"    width: 11em;\r\n" + 
			"    overflow: auto;\r\n" + 
			"min-height: 1.5em;"+
			"margin:0;"+
			"width:11em;"+
			"}\r\n" + 
			"dl{width:1000%;}"+
			"body * {\r\n" + 
			"    display: inline-block;\r\n" + 
			"}</style></head><body>";
	
	@Override
	public String toString() {
		StringBuilder sbe = new StringBuilder(css);
		if (null != d && d.length > 0) {
			for (String dr : d) {
				append("dl", dr, getM("q"), this, sbe);
			}
		}
		if(null==d && null==q) {
			append("dl", "", getM("q"), this, sbe);
		}
		sbe.append("</body></html>");
		return sbe.toString();
	}
	
	public Method getM(String name) {
		for(Method m: this.getClass().getDeclaredMethods()) {
			if(m.getName().equals(name)) {
				return m;
			}
		}
		return null;
	}
	
	
	public void q(String row, StringBuilder sbe) {
		Object r = d2o(row);
		if (r instanceof Map) {
			Map mr = (Map) r;
			for (Object key : mr.keySet()) {
				append("dt", key, sbe);
				sbe.append("<br/>");
				Collection cv = (Collection) mr.get(key);
				for (Iterator it = cv.iterator(); it.hasNext();) {
					append("dd", it.next(), sbe);
				}
				sbe.append("<br/>");
			}
		}
	}

	public void append(String label, Object value, StringBuilder sbe) {
		sbe.append("<").append(label).append(">").append(value).append("</").append(label).append(">");
	}
	public void append(String label, Object row, Method fn, Object holder, StringBuilder sbe) {
		sbe.append("<").append(label).append(">");
		try {
			fn.invoke(holder, row, sbe);
		} catch (Exception e) {
			e.printStackTrace();
		}
		sbe.append("</").append(label).append(">");
	}

	public Object d2o(String d) {
		String tb = "xxx";
		String columns = "id,fefef12,xxff12";
		String sql = "";
		if (d.contains("=")) {

		} else {
			if("".equals(d)){
				sql="tables";
			} 
			else {
				if (d.contains("-")) {

				} else {
					tb = d;
					columns = "*";
				}
				sql = "select " + columns + " from " + tb;
			}
		}
		try {
			System.out.println(String.format("===%s", sql));
			Object dr = dao.invoke(che, sql);
			if (dr instanceof String) {
				dao.invoke(che, "create table " + tb + " (id int primary key)");
				for (String col : columns.split(",")) {
					dao.invoke(che, "alter table " + tb + " add column " + col + " varchar(255) not null default ''");
				}
				dr = dao.invoke(che, sql);
			}
			return dr;
		} catch (Exception e) {
			return e;
		}
	}

}
