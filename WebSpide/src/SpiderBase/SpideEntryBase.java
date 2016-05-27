package SpiderBase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import net.sf.json.JSONObject;

import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;
import Extract.Json.CJson;
import Job.CJobService4WorkerConfig;
import Job.IJobConsole;
import Log.CLog;
import PageParser.CPageParse;
import Spider.CAdvanceSpideExplorer;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @Copyright：2016
 * @Project：WebSpideEntry
 * @Description：
 * @Class：.MyJob1
 * @author：Zhao Jietong
 * @Create：2016-5-18 上午10:19:07
 * @version V1.0
 */
public abstract class SpideEntryBase extends CPageParse implements IJobConsole {
	
	private class _JobCounter {
		
		int jobNum = 0;
	}
	
	private final _JobCounter jobCounter = new _JobCounter();
	private boolean           isStop     = false;
	
	protected class Paras {
		
		public CJobService4WorkerConfig spideConfig  = null; // ini配置中的SPIDE项
		public Jedis                    spideLogEdis = null; // 记录该URL完成的页数
		public String                   path         = null; // 该作业的class位置
		public String                   jobname      = null; // 该作业的class名称
		public String                   url          = null; // 该作业处理的URL
		public ArrayList<String>        spideParas   = null; // 该作业在sjob文件中配置的参数
		
		@SuppressWarnings("unchecked")
		public Paras(Object... arg0) {
			spideConfig = (CJobService4WorkerConfig) arg0[0];
			spideLogEdis = (Jedis) arg0[1];
			path = (String) arg0[2];
			jobname = (String) arg0[3];
			url = (String) arg0[4];
			spideParas = (ArrayList<String>) arg0[5];
		}
	}
	
	protected Logger   logger = CLog.getLogger();
	protected Paras    paras  = null;
	private HashSet<?> links  = null;
	
	public SpideEntryBase() {
	}
	
	protected void init() {
	}
	
	@Override
	public boolean run(final Object... arg0) {
		isStop = false;
		//
		paras = new Paras(arg0);
		init();
		final String key = paras.jobname + "@" + paras.url;
		//
		CAdvanceSpideExplorer explorer = new CAdvanceSpideExplorer(BrowserVersion.CHROME);
		HtmlPage page = explorer.getPage(paras.url, paras.spideConfig.getAttempt(), paras.spideConfig.getAttemptMS());
		//
		if (page == null) {
			explorer.close();
			explorer = null;
			logger.warn("Can't open " + " [" + key + "]");
			return false;
		}
		//
		int pageNum = 0;
		try {
			String json = paras.spideLogEdis.get(key);
			if (json != null) {
				CJson argJson = new CJson(json);
				argJson.process();
				String jpageNum = argJson.query("./page").toString();
				String jurl = argJson.query("./url").toString();
				argJson = null;
				try {
					pageNum = Integer.parseInt(jpageNum);
				}
				catch (Exception e) {
				}
				if (paras.url.equals(jurl)) {
					if (pageNum > 0) {
						logger.info("Jump Page " + pageNum + " [" + key + "]");
						for (int p = 1; p <= pageNum && page != null; p++) {
							page = nextPage(page, p);
						}
					}
				}
				else {
					paras.url = jurl;
					logger.info("Jump Page " + pageNum + " [" + key + "] -> [" + paras.url + "]");
					page = explorer.getPage(paras.url, paras.spideConfig.getAttempt(), paras.spideConfig.getAttemptMS());
				}
			}
		}
		catch (Exception e) {
		}
		explorer.close();
		explorer = null;
		//
		JSONObject json = new JSONObject();
		while (page != null) {
			logger.info(paras.jobname + "(" + page.getUrl().toString() + ")");
			pageNum++;
			final int finalpageNum = pageNum;
			final HtmlPage finalpage = page;
			int threadNum = setThreadNum(finalpage, paras.spideParas);
			links = setLinks(finalpage, finalpageNum);
			logger.info(page.getUrl().toString() + " sub links : " + links.size());
			if (links != null && links.size() > 0) {
				if (links.size() < threadNum) threadNum = links.size();
				jobCounter.jobNum = threadNum;
				for (final Object linkItem : links) {
					if (linkItem == null) continue;
					while (jobCounter.jobNum <= 0 && !isStop) {
						sleep(50);
					}
					if (isStop) break;
					//
					jobCounter.jobNum--;
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							FutureTask<Object> task = new FutureTask<Object>(new Callable<Object>() {
								
								@Override
								public Object call() throws Exception {
									try {
										parsePage(finalpage, linkItem, finalpageNum);
									}
									catch (Exception e) {
										logger.error(e);
									}
									jobCounter.jobNum++;
									return null;
								}
							});
							try {
								new Thread(task).start();
								task.get(paras.spideConfig.getTimeOut(), TimeUnit.SECONDS);
							}
							catch (Exception e) {
								logger.error(e);
								jobCounter.jobNum++;
							}
							task = null;
						}
					}).start();
				}
				links.clear();
				links = null;
				while (jobCounter.jobNum < threadNum && !isStop) {
					sleep(50);
				}
			}
			else {
				jobCounter.jobNum = 0;
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						FutureTask<Object> task = new FutureTask<Object>(new Callable<Object>() {
							
							@Override
							public Object call() throws Exception {
								try {
									parsePage(finalpage, null, finalpageNum);
								}
								catch (Exception e) {
									logger.error(e);
								}
								jobCounter.jobNum++;
								return null;
							}
						});
						try {
							new Thread(task).start();
							task.get(paras.spideConfig.getTimeOut(), TimeUnit.SECONDS);
						}
						catch (Exception e) {
							logger.error(e);
							jobCounter.jobNum++;
						}
						task = null;
					}
				}).start();
				while (jobCounter.jobNum < 1) {
					sleep(50);
				}
			}
			//
			json.clear();
			json.put("page", finalpageNum);
			json.put("url", page.getUrl().toString());
			paras.spideLogEdis.set(key, json.toString());
			//
			if (isStop) break;
			//
			logger.info("[ " + page.getUrl().toString() + " ] -> to next page ...");
			page = nextPage(finalpage, finalpageNum);
			if (page != null) {
				logger.info("the next page is [ " + page.getUrl().toString() + " ] ");
			}
			else {
				logger.info("No next page");
			}
		}
		json = null;
		paras.spideParas = null;
		paras.spideLogEdis.del(key);
		return true;
	}
	
	protected void stop() {
		isStop = true;
	}
	
	protected void sleep(long ms) {
		try {
			Thread.sleep(ms);
		}
		catch (Exception e) {
		}
	}
	
	protected int setThreadNum(HtmlPage page, ArrayList<String> paras) {
		return 1;
	}
	
	protected HashSet<?> setLinks(HtmlPage page, int pageNum) {
		return null;
	}
	
	protected HtmlPage nextPage(HtmlPage page, int pageNum) {
		return null;
	}
	
	protected abstract void parsePage(HtmlPage Mainpage, Object linkItem, int pageNum);
}
