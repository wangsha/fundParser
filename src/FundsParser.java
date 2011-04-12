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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FundsParser {
	public static int TICKER_CLOSE = 0;
	public static int TICKER_OPEN = 1;

	boolean debug = true;
	String baseurl = "http://funds.money.hexun.com/fundsdata/open/summarize/data2.aspx?code=";
	String esimu_product_list = "http://www.esimu.com/smproduct_list.php?&page=";
	String esimu_url = "http://www.esimu.com/";
	LinkedList<String> timeslots = new LinkedList<String>();
	LinkedList<Ticker> oticker = new LinkedList<Ticker>();
	LinkedList<Ticker> cticker = new LinkedList<Ticker>();
	String ooutput = "ticker_open_output.csv";
	String coutput = "ticker_close_output.csv";

	void addTicker(int mode, String id) {
		if (mode == TICKER_CLOSE) {
			cticker.add(new Ticker(id));
		} else {
			oticker.add(new Ticker(id));
		}
	}

	void addTimeSlots(String slot) {
		timeslots.add(slot);
	}

	void parse(int mode) throws Exception {

		URL url;
		Iterator<Ticker> it;
		if (mode == TICKER_CLOSE) {
			it = cticker.iterator();

		} else {
			it = oticker.iterator();
		}

		while (it.hasNext()) {
			try {
				Ticker ticker = it.next();
				if (debug)
					System.out.println("Begin " + ticker.id);
				url = new URL(baseurl.concat(ticker.id));
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(url.openStream()));
				String line;

				boolean reachTable = false;

				while ((line = reader.readLine()) != null) {
					if (!reachTable) {
						if (line.contains("<tr bgcolor=\"F0F9FF\">")
								|| line.contains("<tr bgcolor=\"f4f4f4\">")) {
							reachTable = true;
						}
					}

					if (reachTable && line.contains("</tbody>")) {
						break;
					}
					if (line.contains("<tr bgcolor=\"F0F9FF\">")
							|| line.contains("<tr bgcolor=\"f4f4f4\">")) {

						// Read lines
						String publishDate = reader.readLine()
								.substring(32, 43);// publish date
						String netVal = reader.readLine().substring(21, 27);// unit
						// net
						// value
						String cumuVal = reader.readLine();// cumulative
						String[] dates = publishDate.split("-");
						if (debug)
							System.out.println(dates[0] + "|" + dates[1] + "|"
									+ dates[2] + "|" + netVal + "|" + cumuVal);
						String year = dates[0].substring(3, 5);
						String slot = timeToString(dates[1], dates[2], year);
						ticker.insertData(slot, netVal);
						reader.readLine(); // skip </tr> tag
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("exceptin caught");
				e.printStackTrace();
			}
		}
		// return module;

	}

	void writeFile(int mode) throws Exception {
		FileWriter writer;
		LinkedList<Ticker> tickers;
		if (mode == TICKER_CLOSE) {
			writer = new FileWriter(coutput);
			tickers = cticker;

		} else {
			writer = new FileWriter(ooutput);
			tickers = oticker;
		}

		// write first line
		writer.append(",");
		Iterator<Ticker> it = tickers.iterator();
		int i = 0;
		while (it.hasNext()) {
			i++;
			writer.append(it.next().id + ",");
		}
		writer.append("\n");

		Iterator<String> sit = timeslots.iterator();
		while (sit.hasNext()) {
			String slot = sit.next();
			writer.append(slot + ",");

			Iterator<Ticker> tit = tickers.iterator();
			while (tit.hasNext()) {

				String val = tit.next().getData(slot);
				if (val == null) {
					writer.append(",");
				} else {
					writer.append(val + ",");
				}
			}
			writer.append("\n");

		}
		writer.flush();
		writer.close();
	}

	public void getESimuFundHistory(LinkedList<String> urls) {
		try {
			FileWriter writer = new FileWriter("pe_output.xls");
			writer
					.append("<html><head><meta http-equiv=Content-Type content=\"text/html;"
							+ " charset=utf-8\"></head><body><table border=\"1\" >");
			while (!urls.isEmpty()) {

				String name = urls.poll();
				URL url = new URL(esimu_url.concat(
						"history_net.php?Product_name=").concat(name));
				String dates = "<tr><td><a href=\"".concat(url.toString()).concat("\">")
						.concat(URLDecoder.decode(name, "GBK")).concat(
								"</a></td></tr><tr><td>日期</td>");

				String netvals = "<tr><td>单位净值</td>";
				String accvals = "<tr><td>累计净值</td>";
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(url.openStream(), "GBK"));
				String line;
				boolean reachTable = false;
				while ((line = reader.readLine()) != null) {
					if (line.contains("hot_table")) {
						reachTable = true;
						// skip header
						for (int i = 0; i < 6; i++) {
							line = reader.readLine();
						}
					}
					if (line.contains("<td>") && reachTable) {

						String time = line.replace('-', '/');
						String netval = reader.readLine();
						String accval = reader.readLine();
						if (debug)
							System.out.println(time + "," + netval + ","
									+ accval);
						dates = dates.concat(time);
						netvals = netvals.concat(netval);
						accvals = accvals.concat(accval);
						// skip last column
						reader.readLine();
					}

				}// finish a fund
				writer.append(dates);
				writer.append("</tr>");
				writer.append(netvals);
				writer.append("</tr>");
				writer.append(accvals);
				writer.append("</tr><tr></tr>");
			}
			writer.append("</table>");
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	public LinkedList<String> getESimuProductList() {
		LinkedList<String> urls = new LinkedList<String>();
		try {
			int pagenum = 1;
			boolean hasdata = true;
			while (hasdata) {
				hasdata = false;
				URL url = new URL(esimu_product_list.concat(String.valueOf(pagenum++)));

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(url.openStream(), "GBK"));

				String line;
				boolean reachTable = false;
				while ((line = reader.readLine()) != null) {
					if (line.contains("sm_table smcompany_table rank_right_td")) {
						reachTable = true;
					}
					if (line.contains("smproduct.php?Product_name=")
							&& reachTable) {
						Pattern pattern = Pattern
								.compile("<a[^>]*?href\\s*=\\s*((\'|\")(.*?)(\'|\"))[^>]*?(?!/)>");
						Matcher matcher = pattern.matcher(line);
						while (matcher.find()) {
							int len = matcher.group(1).length();
							// extract fund name
							String fund_name = matcher.group(1).substring(28,
									len - 1);
							if (!fund_name.contains("Yxzcg")) {
								hasdata = true;
								urls.add(URLEncoder.encode(fund_name, "GBK"));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return urls;
	}


	public String timeToString(String month, String date, String year) {
		int mth = Integer.parseInt(month);
		int day = Integer.parseInt(date);
		int yr = Integer.parseInt(year);

		if (mth > 12)
			System.out.println("month > 12");
		if (yr > 99)
			System.out.println("year > 99");

		String slot = String.valueOf(mth) + "/" + String.valueOf(day) + "/"
				+ year;
		return slot;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		FundsParser fparser = new FundsParser();
		
		//read pe funds data
		LinkedList<String> products = fparser.getESimuProductList();
		System.out.println("total number of funds: "+ products.size());
		fparser.getESimuFundHistory(products);
		try {
			// get current directory
			String filepath = new File(".").getCanonicalPath();

			// Read time slots
			BufferedReader in = new BufferedReader(new FileReader(filepath
					.concat("/timeslots.csv")));

			String strLine;
			while ((strLine = in.readLine()) != null) {
				String[] dates = strLine.split("/", 3);
				String slot = fparser.timeToString(dates[0], dates[1], dates[2]
						.substring(0, 2));
				fparser.addTimeSlots(slot);
			}

			// read close tickers, must be in 6 digit format
			in = new BufferedReader(new FileReader(filepath
					.concat("/ticker_close.csv")));
			while ((strLine = in.readLine()) != null) {
				try {
					Integer.parseInt(strLine);
					fparser.addTicker(TICKER_CLOSE, strLine.trim());
				} catch (Exception e) {
				}
			}

			// read open tickers, must be in 6 digit format
			in = new BufferedReader(new FileReader(filepath
					.concat("/ticker_open.csv")));
			while ((strLine = in.readLine()) != null) {
				try {
					Integer.parseInt(strLine);
					fparser.addTicker(TICKER_OPEN, strLine.trim());
				} catch (Exception e) {
				}
			}
			//parse data
			fparser.parse(TICKER_CLOSE);
			fparser.writeFile(TICKER_CLOSE);
			fparser.parse(TICKER_OPEN);
			fparser.writeFile(TICKER_OPEN);
			System.out.println("DONE");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
