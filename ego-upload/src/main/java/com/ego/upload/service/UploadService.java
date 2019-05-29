package com.ego.upload.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/5/29
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@Slf4j
@Service
public class UploadService {

//    private static final Logger log = LoggerFactory.getLogger(UploadService.class);
    // 支持的文件类型
    private static final List<String> suffixes = Arrays.asList("image/png", "image/jpeg");

    public String upload(MultipartFile file) {

        //检查图片文件类型
        if(!suffixes.contains(file.getContentType()))
        {
            log.info("文件类型不匹配:{}", file.getContentType());
            return null;
        }

        //检查图片内容是否正确
        try {
            if(ImageIO.read(file.getInputStream())==null)
            {
                log.info("文件内容不正确.");
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        //保存到硬盘
        File dir = new File("d://images/");
        if(!dir.exists())
        {
            dir.mkdirs();
        }

        try {
            file.transferTo(new File("d://images/",file.getOriginalFilename()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "http://image.ego.com/"+file.getOriginalFilename();
    }
}
