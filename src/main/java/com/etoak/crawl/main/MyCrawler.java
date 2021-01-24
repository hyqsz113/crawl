package com.etoak.crawl.main;

import com.etoak.crawl.link.LinkFilter;
import com.etoak.crawl.link.Links;
import com.etoak.crawl.page.Page;
import com.etoak.crawl.page.PageParserTool;
import com.etoak.crawl.page.RequestAndResponseTool;
import com.etoak.crawl.util.FileTool;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
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
     * @param seeds   抓取网址
     * @param limit   限制数量
     * @param baseUrl 过滤url
     */
    public void crawling(String[] seeds, Integer limit, String baseUrl) {

        //初始化 URL 队列
        initCrawlerWithSeeds(seeds);

        //定义过滤器，提取以 http://www.baidu.com 开头的链接
        LinkFilter filter = new LinkFilter() {
            public boolean accept(String url) {
                if (url.startsWith("https://mp.weixin.qq.com/")) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        //循环条件：待抓取的链接不空且抓取的网页不多于 1000
        while (!Links.unVisitedUrlQueueIsEmpty() && Links.getVisitedUrlNum() <= limit) {

            //先从待访问的序列中取出 头部第一个；
            String visitUrl = (String) Links.removeHeadOfUnVisitedUrlQueue();
            if (visitUrl == null) {
                continue;
            }

            boolean baseStatus = visitUrl.equals(baseUrl);

            //根据URL得到page;
            Page page = RequestAndResponseTool.sendRequestAndGetResponse(visitUrl);

            // 首页 a标签， 非首页 p标签
            String cssSelector = baseStatus ? "a" : "p";
            // 对page进行处理： 访问DOM的某个标签
            Elements es = PageParserTool.select(page, cssSelector);
            if (!es.isEmpty()) {
                System.out.println("下面将打印所有 <" + cssSelector + ">标签： ");
                System.out.println(es);
            }

            if (baseStatus) {
                // baseUrl 保存文件夹
                FileTool.mkdir(es);
            }


            //将已经访问过的链接放入已访问的链接中；
            Links.addVisitedUrlSet(visitUrl);

            //得到超链接（获取指定标签内的 超链接）
            Set<String> links;
            if (baseStatus) {
                // 首页
                links = PageParserTool.getLinks(page, "a[href]");
            } else {
                // 非首页 取图片
                links = PageParserTool.getLinks(page, "img");
            }
            // 保存文件

            for (String link : links) {
                Links.addUnvisitedUrlQueue(link);
                if (!baseStatus) {
                    Page linkPage = RequestAndResponseTool.sendRequestAndGetResponse(link);
                    // 保存文件
                    FileTool.saveToLocal(linkPage, visitUrl);
                }
                System.out.println("新增爬取路径: " + link);
            }
        }
    }


    //main 方法入口
    public static void main(String[] args) {
        // 爬虫对象
        MyCrawler crawler = new MyCrawler();
        // 首页
        String baseUrl = "https://mp.weixin.qq.com/s/1JS1-sak1ijAVN7gxHSf4A";
        List<String> list = new ArrayList<String>();
        list.add(baseUrl);
        // 限制爬取网页数量
        Integer limit = 1000;
        // 开始爬虫
        crawler.crawling(getStringArray(list), limit, baseUrl);
    }

    /**
     * 集合 转化为 数组
     *
     * @param list
     * @return
     */
    private static String[] getStringArray(List<String> list) {
        String[] strings = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            strings[i] = list.get(i);
        }
        return strings;
    }
}
