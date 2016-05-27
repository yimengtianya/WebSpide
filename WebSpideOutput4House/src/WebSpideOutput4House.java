import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.Logger;
import org.dtools.ini.BasicIniFile;
import org.dtools.ini.IniFile;
import org.dtools.ini.IniFileReader;
import org.dtools.ini.IniSection;

import redis.clients.jedis.Jedis;
import Extract.Json.CJson;
import Job.CJobQueue;
import Job.CJobQueueConfig;
import Job.CSpideVersion;
import Log.CLog;

public class WebSpideOutput4House {
	
	public final static int QUEUE_INDEX_OUTPUT  = 0;
	public final static int MDB_INDEX_DUPLICATE = 1;
	
	public static enum Status {
		STATUS_OK, STATUS_ERROR, STATUS_EXIST
	}
	
	private final static Logger logger   = CLog.getLogger();
	private String              driver   = null;
	private String              url      = null;
	private String              dbname   = null;
	private String              user     = null;
	private String              password = null;
	private Connection          con      = null;
	
	public WebSpideOutput4House(String configFile) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		File iniFile = new File(configFile);
		IniFile ini = new BasicIniFile(false);// 大小写不敏感
		IniFileReader reader = new IniFileReader(ini, iniFile);
		try {
			reader.read();
			for (int i = 0; i < ini.getNumberOfSections(); i++) {
				IniSection sec = ini.getSection(i);
				if (sec.getName().equals("DB")) {
					driver = sec.getItem("driver").getValue();
					url = sec.getItem("url").getValue();
					dbname = sec.getItem("dbname").getValue();
					user = sec.getItem("user").getValue();
					password = sec.getItem("password").getValue();
				}
			}
		}
		catch (Exception e) {
			logger.warn(e);
		}
		finally {
			reader = null;
			ini = null;
			iniFile = null;
		}
		//
		openDB();
	}
	
	private void openDB() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Class.forName(driver).newInstance();
		con = DriverManager.getConnection(url + dbname, user, password);
	}
	
	private void closeDB() {
		try {
			con.close();
		}
		catch (Exception e) {
		}
		con = null;
	}
	
	@Override
	protected void finalize() throws Throwable {
		closeDB();
		super.finalize();
	}
	
	private String decode(String v1, String v2) {
		if (v1 == null || v1.length() <= 0) return v2;
		return v1;
	}
	
	public Status insert(CJobQueue outputQueue) {
		String strJson = outputQueue.getJob(WebSpideOutput4House.QUEUE_INDEX_OUTPUT);
		CJson datajson = new CJson(strJson);
		datajson.process();
		int retry = 5;
		while (retry > 0) {
			try {
				String address = datajson.query("./address").toString().replaceAll("\'", "");
				String tab_name = "";
				String sql_select = "select distinct lower(ct.state_py) py from tab_china_city ct where substr(ct.state,1,2)=substr('" + address.substring(0, Math.min(6, address.length())) + "',1,2) or (strpos(ct.city,'市')>0 and substr(ct.city,1,2)=substr('" + address
				                .substring(0, Math.min(6, address.length())) + "',1,2))";
				Statement stm = con.createStatement();
				ResultSet rs = stm.executeQuery(sql_select);
				if (rs.next()) {
					tab_name = "_" + rs.getString("py");
				}
				String sql_insert = "INSERT INTO public.tab_second_house" + tab_name + " (release_time, price, price_unit, house_property, house_style, house_struct, house_decoration, house_class, build_class, usage_area, " + "build_area, build_name, build_time_year, build_face, build_layer, build_max_layer, address_city, address, develop_company, property_costs, " + "property_company, mortgage_down_payment, monthly, url, url_cofrom, web_in_uid, postcode, coordinate, elevation, cofrom) VALUES " + "(?::TIMESTAMP, to_number(?,'999999.999'), to_number(?,'999999.999'), ?, ?, ?, ?, ?, ?, to_number(?,'999999.999')," + " to_number(?,'999999.999'), ?, to_number(?,'9999'), ?, to_number(?,'99999'), to_number(?,'99999'), ?, ?, ?, ?, " + "?, to_number(?,'999999.999'), to_number(?,'999999.999'), ?, ?, ?, ?, POINT(to_number(?,'9999.9999999'),to_number(?,'9999.9999999')), to_number(?,'9999.9999999'), ?)";
				PreparedStatement st = con.prepareStatement(sql_insert);
				st.setString(1, datajson.query("./release_time").toString());
				st.setString(2, decode(datajson.query("./price").toString(), "0"));
				st.setString(3, decode(datajson.query("./price_unit").toString(), "0"));
				st.setString(4, datajson.query("./house_property").toString());
				st.setString(5, datajson.query("./house_style").toString());
				st.setString(6, datajson.query("./house_struct").toString());
				st.setString(7, datajson.query("./house_decoration").toString());
				st.setString(8, datajson.query("./house_class").toString());
				st.setString(9, datajson.query("./build_class").toString());
				st.setString(10, decode(datajson.query("./usage_area").toString(), "0"));
				st.setString(11, decode(datajson.query("./build_area").toString(), "0"));
				st.setString(12, datajson.query("./build_name").toString());
				st.setString(13, decode(datajson.query("./build_time_year").toString(), "0"));
				st.setString(14, datajson.query("./build_face").toString());
				st.setString(15, decode(datajson.query("./build_layer").toString(), "0"));
				st.setString(16, decode(datajson.query("./build_max_layer").toString(), "0"));
				st.setString(17, datajson.query("./address_city").toString());
				st.setString(18, datajson.query("./address").toString());
				st.setString(19, datajson.query("./develop_company").toString());
				st.setString(20, datajson.query("./property_costs").toString());
				st.setString(21, datajson.query("./property_company").toString());
				st.setString(22, decode(datajson.query("./mortgage_down_payment").toString(), "0"));
				st.setString(23, decode(datajson.query("./monthly").toString(), "0"));
				st.setString(24, datajson.query("./url").toString());
				st.setString(25, datajson.query("./url_cofrom").toString());
				st.setString(26, datajson.query("./web_in_uid").toString());
				st.setString(27, "");
				st.setString(28, decode(datajson.query("./longitude").toString(), "0"));
				st.setString(29, decode(datajson.query("./latitude").toString(), "0"));
				st.setString(30, decode(datajson.query("./elevation").toString(), "0"));
				st.setString(31, datajson.query("./style").toString());
				st.executeUpdate();
				//
				System.out.println(datajson.getJson());
				datajson = null;
				//
				return Status.STATUS_OK;
			}
			catch (SQLException e) {
				if (e.getSQLState().equals("08003")) {
					try {
						closeDB();
						Thread.sleep(5000);
						openDB();
						retry--;
						continue;
					}
					catch (Exception e2) {
						logger.warn(e2);
					}
				}
				else if (!e.getSQLState().equals("23505")) {
					logger.warn(e);
					return Status.STATUS_ERROR;
				}
				else {
					String key = datajson.query("./url_cofrom").toString();
					Jedis dupJeds = outputQueue.getJedis(WebSpideOutput4House.MDB_INDEX_DUPLICATE);
					int dup = 1;
					if (dupJeds.exists(key)) {
						dup += Integer.parseInt(dupJeds.get(key));
					}
					dupJeds.set(key, "" + dup);
					outputQueue.returnJedis(dupJeds);
					return Status.STATUS_EXIST;
				}
			}
		}
		return Status.STATUS_ERROR;
	}
	
	public static void main(String[] args) {
		if (args.length <= 0) {
			CSpideVersion.printVersion("WebSpideOutput4House");
			System.out.println("java -jar WebSpideOutput4House.jar <-c inifile>");
			System.out.println("option:");
			System.out.println("       -c <ini file> : config file.");
			System.out.println("       -stop         : stop server.");
			return;
		}
		//
		String iniFileName = "";
		boolean stop = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-c")) {
				iniFileName = args[i + 1];
				i++;
			}
			else if (args[i].equals("-stop")) {
				stop = true;
			}
		}
		CLog.getLogger().info("Begin [ WebSpideOutput4House ]");
		//
		final WebSpideOutput4House output4House;
		try {
			output4House = new WebSpideOutput4House(iniFileName);
		}
		catch (Exception e) {
			logger.error(e);
			return;
		}
		//
		CJobQueueConfig jobQueueConfig = new CJobQueueConfig(iniFileName);
		final CJobQueue outputQueue = new CJobQueue(jobQueueConfig);
		outputQueue.setQueueName(jobQueueConfig.getQueueName() + "-OUTPUT");
		if (stop) {
		}
		else {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					while (true) {
						while (outputQueue.length(CJobQueue.QUEUE_INDEX_JOB) <= 0) {
							try {
								Thread.sleep(50);
							}
							catch (InterruptedException e) {
							}
						}
						//
						try {
							WebSpideOutput4House.Status status = WebSpideOutput4House.Status.STATUS_OK;
							status = output4House.insert(outputQueue);
							if (status == WebSpideOutput4House.Status.STATUS_OK) {
							}
							else if (status == WebSpideOutput4House.Status.STATUS_EXIST) {
							}
							else if (status == WebSpideOutput4House.Status.STATUS_ERROR) {
							}
						}
						catch (Exception e) {
							logger.warn(e);
							return;
						}
					}
				}
			}).start();
		}
	}
}
