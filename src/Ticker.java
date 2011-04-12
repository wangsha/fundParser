import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Ticker {
	public String id;
	private Map<String, String> mapping = new HashMap<String, String>();
	
	Ticker(String id) {
		this.id = id;
	}
	
	public void insertData(String time, String value) {
		mapping.put(time, value);
	}
	
	public String getData(String time) {
		return mapping.get(time);
	}
	
	public String toString() {
		String str = new String(id);
		Iterator<String> it = mapping.keySet().iterator();
		while(it.hasNext()) {
			String key = it.next();
			str = str.concat("["+key+","+mapping.get(key)+"]");
		}
		return str;
	}
}
