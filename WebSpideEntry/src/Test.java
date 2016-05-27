import java.util.List;

import org.ansj.domain.Term;

import Algorithm.Segment.dic.CSegment;
import Extract.RegexEngine.CRegexEngine;
import RegexEngine.CRegexLib;

public class Test {
	
	public static void main(String[] args) {
		String html = "誉轩地产新推 剑桥郡佰利山 最后一间楼王四房 手 5月26日 21:12...次浏览 信息编号:991826900 免费发送到微信 价格:380万元 快速贷款 低利息快速贷款 快速 靠谱 小区名:雅居乐剑桥郡 房型:4室及以上 房间朝向:南 预约装修 >> 私信:私信留言 联系:1366249(广州)点击查看完整号码 联系时 请一定说明在百姓网看到的 谢谢 见面最安全 发现问题请举报 查看完整电话 13662499925 联系时请一定说明在百姓网看到的 谢谢 微信扫一扫 快速拨打电话 收藏 分享 举报 详情介绍 装修情况:精装修 建筑年代:2012年 面积:154平米 楼层:中层/30层 欢迎光临誉轩地产邓赖强网店 华南板块从事房产多年 熟悉地皮 房产相关业务 为您省钱是首要 我刚好专业 你刚好需要 欢迎来电咨询:微信同步 房子介绍:1.该房为剑桥郡佰利山东北向望江 154平方 采光充足 众多人喜欢的紧缺南向楼王户型 房子方正实用 开发商豪华装修 全新保养 业主情况:1.本人经过多次跟业主沟通落实 业主送家电(值10万) 是因为工作环境调动 本人亲自查看过 产权清晰过两年 无按揭 安全性高 看房随时约 到价即签 小区优势:1.享受地铁上盖的便利 享受小孩读书的高端教育 华南板块带医院的小区 五星级电影院 小王国...更多 欢迎光临誉轩地产邓赖强网店 华南板块从事房产多年 熟悉地皮 房产相关业务 为您省钱是首要 我刚好专业 你刚好需要 欢迎来电咨询:微信同步 房子介绍:1.该房为剑桥郡佰利山东北向望江 154平方 采光充足 众多人喜欢的紧缺南向楼王户型 房子方正实用 开发商豪华装修 全新保养 业主情况:1.本人经过多次跟业主沟通落实 业主送家电(值10万) 是因为工作环境调动 本人亲自查看过 产权清晰过两年 无按揭 安全性高 看房随时约 到价即签 小区优势:1.享受地铁上盖的便利 享受小孩读书的高端教育 华南板块带医院的小区 五星级电影院 小王国CBD商业街的小区 查看所有 12 张图片  ";
		CRegexEngine.TestRegex(html, CRegexLib.addressRegex("地处", ""), true);
		CRegexEngine.TestRegex(html, CRegexLib.addressRegex("临近", ""), true);
		CRegexEngine.TestRegex(html, CRegexLib.addressRegex("详情介绍", "地图及交通"), true);
		CRegexEngine.TestRegex(html, CRegexLib.addressRegex("", ""), true);
		List<Term> terms = CSegment.parse(html);
		System.out.println(terms);
	}
}
