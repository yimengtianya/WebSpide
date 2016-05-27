/**
 * @Title: CJobQueue.java
 * @Package Job
 * @Description: TODO
 * @author
 * @date 2016-5-11 下午3:34:20
 * @version V1.0
 */
package Job;

import java.io.File;

import org.apache.logging.log4j.Logger;
import org.dtools.ini.BasicIniFile;
import org.dtools.ini.IniFile;
import org.dtools.ini.IniFileReader;
import org.dtools.ini.IniSection;

import redis.clients.jedis.JedisPoolConfig;
import Log.CLog;

/**
 * @Copyright：2016
 * @Project：WebSpide
 * @Description：
 * @Class：Job.CJobQueue
 * @author：Zhao Jietong
 * @Create：2016-5-11 下午3:34:20
 * @version V1.0
 */
public class CJobQueueConfig {

	private static Logger   logger          = CLog.getLogger();
	private JedisPoolConfig jedisPoolConfig = null;
	private String          queueName       = "Q-WebSpide";
	private String          redisIP         = "localhost";
	private int             redisPort       = 6379;

	public CJobQueueConfig() {
		jedisPoolConfig = new JedisPoolConfig();
		// 是否启用后进先出, 默认true
		jedisPoolConfig.setLifo(true);
		// 最大连接数
		jedisPoolConfig.setMaxTotal(100);
		// 最大空闲连接数
		jedisPoolConfig.setMaxIdle(10);
		// 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间, 默认-1
		jedisPoolConfig.setMaxWaitMillis(1000L);
		// 对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断 (默认逐出策略)
		jedisPoolConfig.setMinEvictableIdleTimeMillis(1800000);
		// 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
		jedisPoolConfig.setTimeBetweenEvictionRunsMillis(-1);
		// 在空闲时检查有效性, 默认false
		jedisPoolConfig.setTestWhileIdle(false);
		// 出借测试
		jedisPoolConfig.setTestOnBorrow(false);
		// 归还测试
		jedisPoolConfig.setTestOnReturn(false);
	}

	public CJobQueueConfig(String configFile) {
		this();
		//
		File iniFile = new File(configFile);
		IniFile ini = new BasicIniFile(false);// 大小写不敏感
		IniFileReader reader = new IniFileReader(ini, iniFile);
		try {
			reader.read();
			for (int i = 0; i < ini.getNumberOfSections(); i++) {
				IniSection sec = ini.getSection(i);
				if (sec.getName().equals("QUEUE")) {
					queueName = sec.getItem("queueName").getValue();
					redisIP = sec.getItem("queueIP").getValue();
					redisPort = Integer.parseInt(sec.getItem("queuePort").getValue());
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
	}

	/**
	 * @return the queueName
	 */
	public String getQueueName() {
		return queueName;
	}

	/**
	 * @param queueName
	 *            the queueName to set
	 */
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getRedisIP() {
		return this.redisIP;
	}

	public void setRedisIP(String redisIP) {
		this.redisIP = redisIP;
	}

	public int getRedisPort() {
		return this.redisPort;
	}

	public void setRedisPort(int redisPort) {
		this.redisPort = redisPort;
	}

	public JedisPoolConfig getJedisPoolConfig() {
		return this.jedisPoolConfig;
	}
}
