/**
 * @Title: CAdvanceSpideExplorer.java
 * @Package Spider
 * @Description: TODO
 * @author
 * @date 2016-5-10 下午5:09:25
 * @version V1.0
 */
package Spider;

import org.apache.logging.log4j.Logger;

import Log.CLog;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @Copyright：2016
 * @Project：WebSpide
 * @Description：
 * @Class：Spider.CAdvanceSpideExplorer
 * @author：Zhao Jietong
 * @Create：2016-5-10 下午5:09:25
 * @version V1.0
 */
public class CAdvanceSpideExplorer {
	
	private static Logger logger        = CLog.getLogger();
	CSpideExplorer        spideExplorer = null;
	
	public CAdvanceSpideExplorer(BrowserVersion explorer) {
		spideExplorer = CSpideExplorerPool.getInstance(BrowserVersion.CHROME).getSpideExplorer();
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
	
	public void close() {
		spideExplorer.close();
		spideExplorer = null;
	}
	
	public CSpideExplorer getExplorer() {
		return spideExplorer;
	}
	
	public HtmlPage getPage(String url) {
		return getPage(url, 0, 0);
	}
	
	public HtmlPage getPage(String url, int retry, long ms) {
		HtmlPage page = null;
		while (retry >= 0) {
			retry--;
			try {
				page = spideExplorer.getPage(url);
				WebResponse response = page.getWebResponse();
				if (response.getStatusCode() == 503) {
					logger.info(url + " => Retry " + retry);
					CSpideExplorer.sleep(ms);
				}
				else {
					break;
				}
			}
			catch (Exception e) {
				if (retry < 0) logger.warn(e);
			}
		}
		return page;
	}
}
