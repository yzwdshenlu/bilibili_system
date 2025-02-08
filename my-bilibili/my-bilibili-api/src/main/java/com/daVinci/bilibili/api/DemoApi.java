package com.daVinci.bilibili.api;

import com.daVinci.bilibili.service.DemoService;
import com.daVinci.bilibili.service.util.FastDFSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @title: DemoApi
 * @Author daVinci
 * @Date: 2025/1/20 17:20
 * @Version 1.0
 */
@RestController
public class DemoApi {
    @Autowired
    private DemoService demoService;
    @Autowired
    private FastDFSUtil fastDFSUtil;

    @GetMapping("/query")
    public Long query(Long id){
        return demoService.query(id);
    }

    @GetMapping("/slices")
    public void slices(MultipartFile file) throws Exception{
        fastDFSUtil.convertFileToSlices(file);
    }
}
