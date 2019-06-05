package com.ego.goods.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/6/5
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@Slf4j
@Service
public class GoodsHtmlService {

    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    @Autowired
    private TemplateEngine templateEngine;

    public void createHtml(Map<String, Object> modelMap, Long id) {
        executorService.submit(()-> {
            Context context = new Context();
            context.setVariables(modelMap);
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(new File("D:\\ide\\nginx-1.15.9\\html\\item\\" + id + ".html"));
                templateEngine.process("item", context, writer);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                log.error("页面静态化出错：{}"+e,id);
            } finally {
                writer.close();
            }
        });
    }
}
