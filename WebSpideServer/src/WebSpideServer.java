// jconsole
/*
 * -Xverify:none -Xms1024M -Xmx1024M -Xmn600M -XX:PermSize=96M -XX:MaxPermSize=96M -Xss1M -XX:ParallelGCThreads=2 -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+UseAdaptiveSizePolicy -XX:CMSFullGCsBeforeCompaction=5
 * -XX:CMSInitiatingOccupancyFraction=85 -XX:MaxTenuringThreshold=0 -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false
 */
import Job.CJobQueue;
import Job.CJobQueueConfig;
import Job.CJobService4Server;
import Job.CJobService4ServerConfig;
import Job.CSpideVersion;
import Log.CLog;

/**
 * @Copyright：2016
 * @Project：WebSpide
 * @Description：
 * @Class：.WebSpideServer
 * @author：Zhao Jietong
 * @Create：2016-5-17 下午4:36:17
 * @version V1.0
 */
public class WebSpideServer {
	
	/**
	 * @Title: main
	 * @Description: TODO
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length <= 0) {
			CSpideVersion.printVersion("WebSpideServer");
			System.out.println("java -jar WebSpideServer.jar <-c inifile> [-keep] [-stop]");
			System.out.println("option:");
			System.out.println("       -c <ini file> : config file.");
			System.out.println("       -keep         : keep on queue.");
			System.out.println("       -force        : force to ignore spided and restart.");
			System.out.println("       -stop         : stop server.");
			return;
		}
		//
		String iniFileName = "";
		boolean keep = false;
		boolean force = false;
		boolean stop = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-c")) {
				iniFileName = args[i + 1];
				i++;
			}
			else if (args[i].equals("-keep")) {
				keep = true;
			}
			else if (args[i].equals("-force")) {
				force = true;
			}
			else if (args[i].equals("-stop")) {
				stop = true;
			}
		}
		CLog.getLogger().info("WebSpideServer" + (keep ? "(keep)" : "") + (force ? "(force)" : ""));
		//
		CJobQueueConfig jobQueueConfig = new CJobQueueConfig(iniFileName);
		CJobQueue jobQueue = new CJobQueue(jobQueueConfig);
		jobQueue.runSaveService();
		CJobService4ServerConfig jobService4ServerConfig = new CJobService4ServerConfig(iniFileName);
		CJobService4Server jobService4Server = new CJobService4Server(jobQueue, jobService4ServerConfig);
		if (force) {
			jobService4Server.ignoreSpided();
		}
		if (stop) {
			jobService4Server.stop(!keep);
		}
		else {
			jobService4Server.run(!keep);
		}
	}
}
