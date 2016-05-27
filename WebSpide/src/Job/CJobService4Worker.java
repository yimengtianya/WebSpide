/**
 * @Title: CJobWorker.java
 * @Package Job
 * @Description: TODO
 * @author
 * @date 2016-5-11 下午4:00:14
 * @version V1.0
 */
package Job;

import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;
import Log.CLog;

/**
 * @Copyright：2016
 * @Project：WebSpide
 * @Description：
 * @Class：Job.CJobWorker
 * @author：Zhao Jietong
 * @Create：2016-5-11 下午4:00:14
 * @version V1.0
 */
public class CJobService4Worker {

	private static Logger logger = CLog.getLogger();

	private class _JobCounter {

		int jobNum = 0;
	}

	private final _JobCounter        jobCounter = new _JobCounter();
	private CJobQueue                queue      = null;
	private IJobWorker               worker     = null;
	private CJobService4WorkerConfig config     = null;

	public CJobService4Worker(CJobQueue queue, CJobService4WorkerConfig config, IJobWorker worker) {
		this.queue = queue;
		this.worker = worker;
		this.config = config;
		queue.empty(CJobQueue.MDB_INDEX_RUNNING);
	}

	public void run(final boolean daemon) {
		jobCounter.jobNum = config.getJobNum();
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					while (queue.length(CJobQueue.QUEUE_INDEX_JOB) <= 0 || jobCounter.jobNum <= 0) {
						sleep(50);
					}
					jobCounter.jobNum--;
					runConsole(daemon);
					if (!daemon) break;
				}
			}
		}).start();
	}

	private void runConsole(final boolean daemon) {
		final String jobString = queue.getJob(CJobQueue.QUEUE_INDEX_JOB);
		final Jedis mdb_running = queue.getJedis(CJobQueue.MDB_INDEX_RUNNING);
		if (mdb_running.exists(jobString)) {
			logger.info("Duplicate running " + jobString);
			return;
		}
		mdb_running.set(jobString, "1");
		//
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					if (worker.execute(jobString) && daemon) {
						queue.addJob(CJobQueue.QUEUE_INDEX_RESULT, jobString);
						logger.info("Job SUCCESS and return QUEUE_INDEX_RESULT : " + jobString);
					}
					else {
						queue.addJob(CJobQueue.QUEUE_INDEX_FAIL, jobString);
						logger.warn("Job FALSE : " + jobString);
					}
				}
				catch (Exception e) {
					logger.warn(e);
				}
				mdb_running.del(jobString);
				jobCounter.jobNum++;
			}
		}).start();
	}

	public void sleep(long ms) {
		try {
			Thread.sleep(ms);
		}
		catch (Exception e) {
		}
	}
}
