package System.RunEnvProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 采集内存使用率
 */
public class MemUsage {

	private static Logger   log      = LogManager.getLogger(MemUsage.class);
	private static MemUsage INSTANCE = new MemUsage();

	private MemUsage() {
	}

	public static MemUsage getInstance() {
		return INSTANCE;
	}

	/**
	 * Purpose:采集内存使用率
	 * 
	 * @param args
	 * @return float,内存使用率,小于1
	 */
	public float get() {
		log.info("开始收集memory使用率");
		float memUsage = 0.0f;
		Process pro = null;
		Runtime r = Runtime.getRuntime();
		try {
			String command = "cat /proc/meminfo";
			pro = r.exec(command);
			BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			String line = null;
			int count = 0;
			long totalMem = 0, freeMem = 0;
			while ((line = in.readLine()) != null) {
				log.info(line);
				String[] memInfo = line.split("\\s+");
				if (memInfo[0].startsWith("MemTotal")) {
					totalMem = Long.parseLong(memInfo[1]);
				}
				if (memInfo[0].startsWith("MemFree")) {
					freeMem = Long.parseLong(memInfo[1]);
				}
				memUsage = 1 - (float) freeMem / (float) totalMem;
				log.info("本节点内存使用率为: " + memUsage);
				if (++count == 2) {
					break;
				}
			}
			in.close();
			pro.destroy();
		}
		catch (IOException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			log.error("MemUsage发生InstantiationException. " + e.getMessage());
			log.error(sw.toString());
		}
		return memUsage;
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		while (true) {
			System.out.println(MemUsage.getInstance().get());
			Thread.sleep(5000);
		}
	}
}
