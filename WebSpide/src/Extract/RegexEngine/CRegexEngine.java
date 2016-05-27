package Extract.RegexEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Copyright：2014
 * @Project：Spide
 * @Description：
 * @Class：Extract.RegexEngine.CRegexEngine
 * @author：Zhao Jietong
 * @Create：2014-9-18 上午9:06:52
 * @version V1.0
 */
public class CRegexEngine extends HashMap<String, ArrayList<CRegex>> {
	
	private static final long serialVersionUID = -8736671262718922186L;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void finalize() throws Throwable {
		Iterator iter = super.entrySet().iterator();
		while (iter.hasNext()) {
			ArrayList<CRegex> o = (ArrayList<CRegex>) ((Map.Entry) iter.next()).getValue();
			if (o != null) {
				o.clear();
			}
		}
		super.clear();
		super.finalize();
	}
	
	/*
	 * 设置正则表达式到引擎中
	 */
	public void set(String key, CRegex... regs) {
		ArrayList<CRegex> lst = super.get(key);
		if (lst == null) {
			lst = new ArrayList<CRegex>();
		}
		for (CRegex reg : regs) {
			lst.add(reg);
		}
		super.put(key, lst);
	}
	
	@SuppressWarnings("unchecked")
	public void set(String key, List<CRegex>... regss) {
		for (List<CRegex> regs : regss) {
			for (int i = 0; i < regs.size(); i++) {
				set(key, regs.get(i));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void set(String key, Object... regss) {
		for (Object regsx : regss) {
			String regsxType = regsx.getClass().getName();
			if (regsxType.equals("java.util.ArrayList")) {
				set(key, (List<CRegex>) regsx);
			}
			else if (regsxType.equals("Extract.RegexEngine.CRegex")) {
				set(key, (CRegex) regsx);
			}
			else if (regsxType.equals("java.util.String")) {
				set(key, new CRegex((String) regsx));
			}
		}
	}
	
	/*
	 * 获取引擎中的正则表达式
	 */
	public ArrayList<CRegex> get(String key) {
		return super.get(key);
	}
	
	public String toRegex(final String key, final String text) {
		final ArrayList<CRegex> regs = get(key);
		if (regs.size() < 0) return null;
		final String[] results = new String[regs.size()];
		ExecutorService pool = Executors.newFixedThreadPool(4);
		for (int r = 0; r < regs.size(); r++) {
			// System.out.println("/" + regs.get(r).getPattern().pattern().replaceAll("\\\\",
			// "\\\\\\\\") + "/");
			final int rr = r;
			pool.execute(new Runnable() {
				
				@Override
				public void run() {
					String _text = text;
					String result = "";
					String _result = "";
					CRegex regex = regs.get(rr);
					CRegex.IDeepSelect deepSelect = regex.getDeepSelect();
					CRegex.IDeal dataDeal = regex.getDataDeal();
					ArrayList<Integer> idxs = regex.getIdx();
					int size = idxs.size();
					do {
						Matcher m = regex.getPattern().matcher(_text);
						if (m.find() && m.groupCount() > 0) {
							try {
								// System.out.println("/" +
								// regex.getPattern().pattern().replaceAll("\\\\", "\\\\\\\\") +
								// "/");
								result = regex.getFormat();
								if (result != null && result.length() > 0) {
									for (int i = 0; i < size; i++) {
										result = result.replaceAll("\\$\\{" + idxs.get(i) + "\\}", m.group(idxs.get(i)));
									}
								}
								else {
									result = "";
									for (int i = 0; i < size; i++) {
										result += m.group(idxs.get(i));
									}
								}
								//
								if (dataDeal != null) {
									result = dataDeal.deal(result);
								}
								//
								if (deepSelect != null) {
									result = deepSelect.select(_result, result);
									_result = result;
									int pos = _text.indexOf(m.group(1));
									if (pos >= 0 && _text.length() > 0) {
										pos += m.group(1).length();
										if (pos == 0) pos++;
										_text = _text.substring(pos);
										continue;
									}
								}
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
						break;
					} while (deepSelect != null);
					results[rr] = result.trim();
				}
			});
		}
		pool.shutdown();
		try {
			pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
		}
		pool = null;
		// for (int i = 0; i < regs.size(); i++) {
		// System.out.println(key + " = " + results[i] + " /" +
		// regs.get(i).getRegex().replaceAll("\\\\", "\\\\\\\\") + "/");
		// System.out.println(key + " = " + results[i]);
		// }
		//
		CRegex.IDeepSelect deepSelect = regs.get(0).getDeepSelect();
		if (deepSelect == null) {
			for (int i = 0; i < regs.size(); i++) {
				if (results[i] != null && results[i].length() > 0) return results[i];
			}
		}
		String result = "";
		for (int i = 0; i < regs.size(); i++) {
			result = deepSelect.select(result, results[i]);
		}
		return result;
	}
	
	/*
	 * 测试正则表达式
	 */
	public static void TestRegex(String subString, String regexString, boolean deep) {
		TestRegex(subString, Pattern.compile(regexString, Pattern.MULTILINE), deep);
	}
	
	public static void TestRegex(String subString, CRegex regex, boolean deep) {
		String _text = subString;
		String result = "";
		String _result = "";
		CRegex.IDeepSelect deepSelect = regex.getDeepSelect();
		CRegex.IDeal dataDeal = regex.getDataDeal();
		ArrayList<Integer> idxs = regex.getIdx();
		int size = idxs.size();
		do {
			Matcher m = regex.getPattern().matcher(_text);
			if (m.find() && m.groupCount() > 0) {
				try {
					// System.out.println("\"" + regex.getPattern().pattern().replaceAll("\\\\",
					// "\\\\\\\\") + "\"");
					result = regex.getFormat();
					if (result != null && result.length() > 0) {
						for (int i = 0; i < size; i++) {
							result = result.replaceAll("\\$\\{" + idxs.get(i) + "\\}", m.group(idxs.get(i)));
						}
					}
					else {
						result = "";
						for (int i = 0; i < size; i++) {
							result += m.group(idxs.get(i));
						}
					}
					//
					if (dataDeal != null) {
						result = dataDeal.deal(result);
					}
					//
					if (deepSelect != null) {
						result = deepSelect.select(_result, result);
						_result = result;
						int pos = _text.indexOf(m.group(1));
						if (pos >= 0 && _text.length() > 0) {
							pos += m.group(1).length();
							if (pos == 0) pos++;
							_text = _text.substring(pos);
							continue;
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		} while (deepSelect != null);
		System.out.println(result + " /" + regex.getRegex().replaceAll("\\\\", "\\\\\\\\") + "/");
	}
	
	public static void TestRegex(String subString, List<CRegex> regs, boolean deep) {
		for (int r = regs.size() - 1; r >= 0; r--) {
			TestRegex(subString, regs.get(r), deep);
			System.out.println("-----");
		}
	}
	
	public static void TestRegex(String subString, Pattern pattern, boolean deep) {
		System.out.println("/" + pattern.pattern().replaceAll("\\\\", "\\\\\\\\") + "/");
		do {
			Matcher m = pattern.matcher(subString);
			if (m.find()) {
				for (int mi = 0; mi <= m.groupCount(); mi++) {
					System.out.println(mi + ". [" + m.group(mi) + "]");
				}
				if (deep) {
					int subIdx = subString.indexOf(m.group(1));
					if (subIdx > -1 && subString.length() > 0) {
						System.out.println("-----");
						if (subIdx == 0) subIdx++;
						subString = subString.substring(subIdx + m.group(1).length());
						continue;
					}
				}
			}
			else {
				System.out.println("Regex NOT MATCH!\n" + subString);
			}
			break;
		} while (true);
	}
}
