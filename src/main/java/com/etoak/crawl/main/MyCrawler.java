package com.etoak.crawl.main;

import com.etoak.crawl.link.LinkFilter;
import com.etoak.crawl.link.Links;
import com.etoak.crawl.page.Page;
import com.etoak.crawl.page.PageParserTool;
import com.etoak.crawl.page.RequestAndResponseTool;
import com.etoak.crawl.util.FileTool;
import org.jsoup.select.Elements;

import java.util.Set;

/**
 * @author hyq
 * @description 爬虫启动类
 */
public class MyCrawler {

    /**
     * 使用种子初始化 URL 队列
     *
     * @param seeds 种子 URL
     * @return
     */
    private void initCrawlerWithSeeds(String[] seeds) {
        for (int i = 0; i < seeds.length; i++) {
            Links.addUnvisitedUrlQueue(seeds[i]);
        }
    }


    /**
     * 抓取过程
     *
     * @param seeds
     * @return
     */
    public void crawling(String[] seeds) {

        //初始化 URL 队列
        initCrawlerWithSeeds(seeds);

        //定义过滤器，提取以 http://www.baidu.com 开头的链接
        LinkFilter filter = new LinkFilter() {
            public boolean accept(String url) {
                if (url.startsWith("https://mp.weixin.qq.com/s/PAlQ5w2tc_3dV_A8tr2gVg")) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        //循环条件：待抓取的链接不空且抓取的网页不多于 1000
        while (!Links.unVisitedUrlQueueIsEmpty() && Links.getVisitedUrlNum() <= 10) {

            //先从待访问的序列中取出第一个；
            String visitUrl = (String) Links.removeHeadOfUnVisitedUrlQueue();
            if (visitUrl == null) {
                continue;
            }

            //根据URL得到page;
            Page page = RequestAndResponseTool.sendRequestAndGetResponse(visitUrl);

            // 对page进行处理： 访问DOM的某个标签
            Elements es = PageParserTool.select(page, "p");
            if (!es.isEmpty()) {
                System.out.println("下面将打印所有 <p> 标签： ");
                System.out.println(es);
            }

            // 保存文件
            FileTool.saveToLocal(page);

            //将已经访问过的链接放入已访问的链接中；
            Links.addVisitedUrlSet(visitUrl);

            //得到超链接（获取指定标签内的 超链接）
            Set<String> links = PageParserTool.getLinks(page, "img");
            for (String link : links) {
                Links.addUnvisitedUrlQueue(link);
                System.out.println("新增爬取路径: " + link);
            }
        }
    }


    //main 方法入口
    public static void main(String[] args) {
        MyCrawler crawler = new MyCrawler();
        crawler.crawling(new String[]{"https://mp.weixin.qq.com/s/PAlQ5w2tc_3dV_A8tr2gVg"});
    }
}
