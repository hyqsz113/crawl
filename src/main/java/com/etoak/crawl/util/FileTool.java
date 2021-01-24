package com.etoak.crawl.util;


import com.etoak.crawl.page.Page;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/*  本类主要是 下载那些已经访问过的文件*/
public class FileTool {

    private static String dirPath;

    private static Map<String, String> urlFileNameMap;

    /**
     * getMethod.getResponseHeader("Content-Type").getValue()
     * 根据 URL 和网页类型生成需要保存的网页的文件名，去除 URL 中的非文件名字符
     */
    private static String getFileNameByUrl(String url, String contentType) {
        //去除 http://
        url = url.substring(7);
        //text/html 类型
        if (contentType.indexOf("html") != -1) {
            url = url.replaceAll("[\\?/:*|<>\"]", "_") + ".html";
            return url;
        }
        //如 application/pdf 类型
        else {
            return url.replaceAll("[\\?/:*|<>\"]", "_") + "." +
                    contentType.substring(contentType.lastIndexOf("/") + 1);
        }
    }

    /**
     * 生成目录
     **/
    public static void mkdir(Elements es) {
        if (es == null) {
            return;
        }
        int count = 0;
        urlFileNameMap = new HashMap<String, String>();

        for (Element item : es) {
            /*if (count >= 10) {
                return;
            }*/
            if (item.childNodes().size() == 0) {
                continue;
            }
            String fileName = item.childNode(0).attr("text").trim();
            // 文件夹名字长度不够 3
            if (fileName.length() <= 3) {
                continue;
            }
            // 第三个字
            String third = fileName.substring(2, 3);
            // 第四个字
            String fourth = fileName.substring(3, 4);
            boolean jump = "章".equals(third) || "章".equals(fourth);
            if (!jump) {
                continue;
            }

            dirPath = "E:\\pachong\\" + fileName + "\\";

            // 保存 文件夹名字 与 网址的关系
            Attributes attributes = item.attributes();
            String hrefKey = attributes.get("href");

            String url = getPath(hrefKey);
            urlFileNameMap.put(url, dirPath);

            File fileDir = new File(dirPath);
            if (!fileDir.exists()) {
                fileDir.mkdir();
            }
            count++;
        }
        System.out.println(urlFileNameMap.toString());

    }

    private static String getPath(String url) {
        String sub = "&chksm";
        int stop = url.indexOf(sub);
        return url.substring(0, stop);
    }

    /**
     * 保存网页字节数组到本地文件，filePath 为要保存的文件的相对地址
     */

    public static void saveToLocal(Page page, String visitUrl) {
        System.out.println(urlFileNameMap.toString());

        String urlKey = getPath(visitUrl);
        // 根据 visitUrl 匹配 文件夹
        String path = urlFileNameMap.get(urlKey);
        if (path == null) {
            return;
        }
        String fileName = getFileNameByUrl(page.getUrl(), page.getContentType());
        String filePath = path + fileName;

        // 获取数据
        byte[] data = page.getContent();
        try {
            //Files.lines(Paths.get("D:\\jd.txt"), StandardCharsets.UTF_8).forEach(System.out::println);
            DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(filePath)));
            // 写出
            for (int i = 0; i < data.length; i++) {
                out.write(data[i]);
            }
            out.flush();
            out.close();
            System.out.println("文件：" + fileName + "已经被存储在" + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
