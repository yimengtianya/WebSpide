/**
 * @Title: CJobQueue.java
 * @Package Job
 * @Description: TODO
 * @author
 * @date 2016-5-11 下午3:34:20
 * @version V1.0
 */
package Job;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @Copyright：2016
 * @Project：WebSpide
 * @Description：
 * @Class：Job.CJobQueue
 * @author：Zhao Jietong
 * @Create：2016-5-11 下午3:34:20
 * @version V1.0
 */
@SuppressWarnings("deprecation")
public class CJobQueue {
	
	public final static int  QUEUE_INDEX_JOB    = 0;   // 作业队列
	public final static int  QUEUE_INDEX_RESULT = 1;   // 完成的作业队列
	public final static int  QUEUE_INDEX_FAIL   = 2;   // 失败的作业队列
	public final static int  MDB_INDEX_LOG      = 3;   // 已经完成的进度，那个URL完成了多少页
	public final static int  MDB_INDEX_SERVER   = 4;   // Service运行状态
	public final static int  MDB_INDEX_RUNNING  = 5;   // 正在运行的作业集合
	//
	private static JedisPool pool               = null;
	private String           queueName          = "";
	
	public CJobQueue(CJobQueueConfig config) {
		if (pool == null) {
			pool = new JedisPool(config.getJedisPoolConfig(), config.getRedisIP(), config.getRedisPort());
		}
		this.queueName = config.getQueueName();
	}
	
	@Override
	protected void finalize() throws Throwable {
		pool.close();
		super.finalize();
	}
	
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
	public String getQueueName() {
		return this.queueName;
	}
	
	public void runSaveService() {
		final Jedis jedis = pool.getResource();
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
				}
				catch (InterruptedException e) {
				}
				jedis.save();
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
	
	public Jedis getJedis(int queueIdx) {
		Jedis jedis = pool.getResource();
		jedis.select(queueIdx);
		return jedis;
	}
	
	public void returnJedis(Jedis jedis) {
		pool.returnResource(jedis);
	}
	
	public void empty() {
		Jedis jedis = pool.getResource();
		jedis.flushAll();
		pool.returnResource(jedis);
	}
	
	public void empty(int queueIdx) {
		Jedis jedis = getJedis(queueIdx);
		jedis.flushDB();
		pool.returnResource(jedis);
	}
	
	public Long length(int queueIdx) {
		Jedis jedis = getJedis(queueIdx);
		Long len = jedis.llen(queueName);
		pool.returnResource(jedis);
		return len;
	}
	
	public void addJob(int queueIdx, String... jobs) {
		Jedis jedis = getJedis(queueIdx);
		jedis.rpush(queueName, jobs);
		pool.returnResource(jedis);
	}
	
	public String getJob(int queueIdx) {
		Jedis jedis = getJedis(queueIdx);
		String str = jedis.lpop(queueName);
		pool.returnResource(jedis);
		return str;
	}
}
