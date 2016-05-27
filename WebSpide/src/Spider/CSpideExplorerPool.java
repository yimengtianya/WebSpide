/**
 * @Title: CSpider.java
 * @Package Spider
 * @Description: TODO
 * @author
 * @date 2016-5-10 下午3:06:53
 * @version V1.0
 */
package Spider;

import java.util.HashMap;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.logging.log4j.Logger;

import Log.CLog;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * @Copyright：2016
 * @Project：WebSpide
 * @Description：
 * @Class：Spider.CSpider
 * @author：Zhao Jietong
 * @Create：2016-5-10 下午3:06:53
 * @version V1.0
 */
public class CSpideExplorerPool {
	
	private static Logger logger = CLog.getLogger();
	
	public static class PooledClientFactory {
		
		private GenericObjectPool<CSpideExplorer> clientPool = null;
		
		public PooledClientFactory(BrowserVersion explorer) {
			final PooledClientFactory _this = this;
			clientPool = new GenericObjectPool<CSpideExplorer>(new PooledObjectFactory<CSpideExplorer>() {
				
				@Override
				public void activateObject(PooledObject<CSpideExplorer> arg0) throws Exception {
				}
				
				@Override
				public void destroyObject(PooledObject<CSpideExplorer> arg0) throws Exception {
					CSpideExplorer client = arg0.getObject();
					client.close();
					client = null;
				}
				
				@Override
				public PooledObject<CSpideExplorer> makeObject() throws Exception {
					final CSpideExplorer client = new CSpideExplorer();
					client.setOwnerPooledClientFactory(_this);
					return new DefaultPooledObject<CSpideExplorer>(client);
				}
				
				@Override
				public void passivateObject(PooledObject<CSpideExplorer> arg0) throws Exception {
				}
				
				@Override
				public boolean validateObject(PooledObject<CSpideExplorer> arg0) {
					return false;
				}
			});
			// 是否启用后进先出, 默认true
			clientPool.setLifo(true);
			// 最大连接数
			clientPool.setMaxTotal(100);
			// 最大空闲连接数
			clientPool.setMaxIdle(10);
			// 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间, 默认-1
			clientPool.setMaxWaitMillis(1000L);
			// 对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断 (默认逐出策略)
			clientPool.setMinEvictableIdleTimeMillis(1800000);
			// 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
			clientPool.setTimeBetweenEvictionRunsMillis(-1);
			// 在空闲时检查有效性, 默认false
			clientPool.setTestWhileIdle(false);
			// 出借测试
			clientPool.setTestOnBorrow(false);
			// 归还测试
			clientPool.setTestOnReturn(false);
		}
		
		public CSpideExplorer getSpideExplorer() {
			try {
				return this.clientPool.borrowObject();
			}
			catch (Exception e) {
				logger.error(e);
			}
			return null;
		}
		
		protected void returnSpideExplorer(CSpideExplorer client) {
			try {
				this.clientPool.returnObject(client);
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	private final static HashMap<BrowserVersion, PooledClientFactory> instances = new HashMap<BrowserVersion, PooledClientFactory>();
	
	private CSpideExplorerPool(BrowserVersion explorer) {
	}
	
	@Override
	protected void finalize() throws Throwable {
		instances.clear();
		super.finalize();
	}
	
	public static PooledClientFactory getInstance(BrowserVersion explorer) {
		if (instances.containsKey(explorer)) {
			return instances.get(explorer);
		}
		else {
			PooledClientFactory instance = new PooledClientFactory(explorer);
			instances.put(explorer, instance);
			return instance;
		}
	}
}
