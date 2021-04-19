package com.offcn.content.controller;

import com.offcn.entity.Result;
import com.offcn.utils.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    @Value("${FILE_SERVER_PATH}")
    private String FILE_SERVER_PATH;

    @RequestMapping("/upload")
    public Result uploadFile(MultipartFile file) {
        //1.获得文件的全名称   1.jpg --> jpg
        String fileName = file.getOriginalFilename();
        //2.根据名称截取文件扩展名
        String extName = fileName.substring(fileName.lastIndexOf(".") + 1);
        try {
            //3.实例化上传文件的工具类
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:fdfs_client.conf");
            //4.使用工具类中上传方法完成上传操作
            String path = fastDFSClient.uploadFile(file.getBytes(), extName);
            //5.将图片路径返回到前端
            String url = FILE_SERVER_PATH + path;
            return new Result(true, url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上传失败");
        }


    }
}
