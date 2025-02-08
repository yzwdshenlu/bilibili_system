package com.daVinci.bilibili.api;

import com.daVinci.bilibili.domain.JsonResponse;
import com.daVinci.bilibili.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.api
 * @Author: daVinci
 * @CreateTime: 2025-02-04  15:16
 * @Description: 文件controller
 * @Version: 1.0
 */
@RestController
public class FileApi {
    @Autowired
    private FileService fileService;

    /**
     * 对需要上传的文件进行md5加密
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping("/md5files")
    public JsonResponse<String> getFileMD5(MultipartFile file) throws Exception{
        String fileMD5 = fileService.getFileMD5(file);
        return new JsonResponse<>(fileMD5);
    }

    /**
     * 分片上传，且实现秒传
     * @param slice
     * @param fileMd5
     * @param sliceNo
     * @param totalSliceNo
     * @return
     * @throws Exception
     */
    @PutMapping("/file-slices")
    public JsonResponse<String> uploadFileBySlices(MultipartFile slice,String fileMd5,Integer sliceNo,Integer totalSliceNo) throws Exception{
        String filePath = fileService.uploadFileBySlices(slice,fileMd5,sliceNo,totalSliceNo);
        return new JsonResponse<>(filePath);
    }
}
