/**
 * Copyright (c) 2011-2012
 * Wang Sha
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * 1. The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * 2. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
