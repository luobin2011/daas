import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
//request-->url
//url-->className
//ctx-->classField
//?m.invoke
//classField-->response
Object task = new URLClassLoader(new URL[]{new URL("file:/D:/Users/luob4/dev/eclipse-workspace/t/bin/")}).loadClass(request.getParameter("className")).newInstance();
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
 * @author luob4
 *
 */
public class Index2 {

	public String in;

	public String out;

	public String[] a$b;
	public String[] a$c;
	public String[] b$b;
	public String[] b$c;

	@Override
	public String toString() {
		Elem root = new Elem(0, null, null);
		Map<String, Map<String, String[]>> psmap = new HashMap<String, Map<String, String[]>>();
		root.sbe.append("<dl>");
		for (Field f : this.getClass().getDeclaredFields()) {
			StringBuilder sbe = root.sbe;
			try {
				Object v = f.get(this);
				if (v instanceof String) {
					sbe.append("<dt>").append(f.getName()).append("</dt>");
					sbe.append("<dd>").append(escapeXml((String) v)).append("</dd>");
				} else if (v instanceof String[]) {
					String okey = f.getName().split("\\$")[0];
					if (!psmap.containsKey(okey)) {
						sbe.append("<dt>").append(okey).append("</dt>");
						sbe.append("<dd>");
						int len = sbe.length();
						Map<String, String[]> vmap = new HashMap<String, String[]>();
						psmap.put(okey, vmap);
						root.pslist.add(new Elem(len, okey, vmap));
						sbe.append("</dd>");
					}
					psmap.get(okey).put(f.getName(), (String[]) v);
				}
			} catch (Exception e) {

			}
		}
		root.sbe.append("</dl>");
		StringBuilder fsbe = new StringBuilder();
		int idx = 0;
		for (Elem row : root.pslist) {
			StringBuilder sbe = row.sbe;
			sbe.append("<dl>");
			for (String key : row.value.keySet()) {
				String[] ks = key.split("\\$");
				String[] vs = row.value.get(key);
				sbe.append("<dt>").append(ks[1]).append("</dt>");
				for (String v : vs) {
					sbe.append("<dd>").append(escapeXml(v)).append("</dd>");
				}
			}
			row.sbe.append("</dl>");
			fsbe.append(root.sbe.substring(idx, row.idx));
			fsbe.append(row.sbe);
			idx = row.idx;
		}
		fsbe.append(root.sbe.substring(idx));
		return fsbe.toString();
	}

	class Elem {
		int idx;
		String key;
		Map<String, String[]> value;
		StringBuilder sbe = new StringBuilder();
		List<Elem> pslist = new ArrayList<Elem>();

		public Elem(int idx, String key, Map<String, String[]> value) {
			super();
			this.idx = idx;
			this.key = key;
			this.value = value;
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
