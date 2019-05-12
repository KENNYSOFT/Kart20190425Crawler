package kr.KENNYSOFT.Kart20190425Crawler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Kart20190425Crawler
{
	public static JSONParser jsonParser = new JSONParser();
	public static final Map<Long, Integer> pcs = Map.ofEntries(Map.entry(1945l, 3), Map.entry(1946l, 500), Map.entry(1951l, 5), Map.entry(1952l, 200), Map.entry(1957l, 20), Map.entry(1958l, 400), Map.entry(1926l, 50), Map.entry(1927l, 2), Map.entry(1928l, 100), Map.entry(1929l, 50), Map.entry(1930l, 50), Map.entry(1931l, 1), Map.entry(1932l, 50), Map.entry(1933l, 1), Map.entry(1934l, 2), Map.entry(1936l, 3), Map.entry(1937l, 50), Map.entry(1938l, 1), Map.entry(1939l, 100), Map.entry(1940l, 200), Map.entry(1942l, 5));
	public static final String ENC = "YOUR_ENC";
	public static final String KENC = "YOUR_KENC";
	public static final String NPP = "YOUR_NPP";
	
	public static void main(String[] args) throws Exception
	{
		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
		HttpCookie cookie1 = new HttpCookie("ENC", ENC);
		cookie1.setDomain("nexon.com");
		cookie1.setPath("/");
		cookie1.setVersion(0);
		cookieManager.getCookieStore().add(new URI("http://kart.nexon.com/"), cookie1);
		HttpCookie cookie2 = new HttpCookie("KENC", KENC);
		cookie2.setDomain("kart.nexon.com");
		cookie2.setPath("/");
		cookie2.setVersion(0);
		cookieManager.getCookieStore().add(new URI("http://kart.nexon.com/"), cookie2);
		HttpCookie cookie3 = new HttpCookie("NPP", NPP);
		cookie3.setDomain("nexon.com");
		cookie3.setPath("/");
		cookie3.setVersion(0);
		cookieManager.getCookieStore().add(new URI("http://kart.nexon.com/"), cookie3);
		CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get("coupon.csv")), CSVFormat.DEFAULT.withHeader("\uFEFF아이템", "수량", "당첨일시", "유효기간", "쿠폰번호", "사용 여부"));
		int pages = 1;
		for (int i = 1; i <= pages; ++i)
		{
			JSONObject object = null;
			boolean completed;
			do
			{
				completed = true;
				try
				{
					object = list(i);
				}
				catch (HttpRetryException e)
				{
					cookieManager.getCookieStore().add(new URI("http://kart.nexon.com/"), cookie1);
					cookieManager.getCookieStore().add(new URI("http://kart.nexon.com/"), cookie2);
					cookieManager.getCookieStore().add(new URI("http://kart.nexon.com/"), cookie3);
					completed = false;
				}
			} while (!completed);
			pages = ((int) (long) object.get("TotalCount") + 5) / 6;
			JSONArray array = (JSONArray) object.get("Data");
			for (Object obj : array)
			{
				JSONObject item = (JSONObject) obj;
				csvPrinter.printRecord(item.get("strItemName"), pcs.containsKey(item.get("n4ItemNo")) ? pcs.get(item.get("n4ItemNo")) : "-", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.valueOf(((String) item.get("dtCreate")).replaceAll("[^\\d]", "")))), "2019-05-22", item.get("strCouponSN"), "X");
			}
		}
		csvPrinter.flush();
		csvPrinter.close();
	}
	
	public static JSONObject list(int page) throws Exception
	{
		HttpURLConnection conn = (HttpURLConnection) new URL("http://kart.nexon.com/Events/2019/0425/Ajax.aspx").openConnection();
		Map<String, String> parameters = new HashMap<>();
		parameters.put("strType", "list");
		parameters.put("PageNo", String.valueOf(page));
		StringJoiner sj = new StringJoiner("&");
		for (Entry<String, String> entry : parameters.entrySet()) sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
		byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
		conn.setRequestMethod("POST");
		conn.setFixedLengthStreamingMode(out.length);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Referer", "http://kart.nexon.com/Events/2019/0425/Event.aspx");
		conn.setDoOutput(true);
		DataOutputStream os = new DataOutputStream(conn.getOutputStream());
		os.write(out);
		os.flush();
		os.close();
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		StringBuffer response = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null) response.append(line);
		br.close();
		conn.disconnect();
		System.out.println(response);
		return (JSONObject) jsonParser.parse(response.toString());
	}
}