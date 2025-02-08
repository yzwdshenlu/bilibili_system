package com.daVinci.bilibili.service;

import com.daVinci.bilibili.dao.FileDao;
import com.daVinci.bilibili.domain.File;
import com.daVinci.bilibili.service.util.FastDFSUtil;
import com.daVinci.bilibili.service.util.MD5Util;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;


/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.service
 * @Author: daVinci
 * @CreateTime: 2025-02-04  15:18
 * @Description: 文件Service
 * @Version: 1.0
 */
@Service
public class FileService {

    @Autowired
    private FileDao fileDao;

    @Autowired
    private FastDFSUtil fastDFSUtil;

    /**
     * 文件上传，已经实现秒传
     * @param slice
     * @param fileMD5
     * @param sliceNo
     * @param totalSliceNo
     * @return
     * @throws Exception
     */
    public String uploadFileBySlices(MultipartFile slice,
                                     String fileMD5,
                                     Integer sliceNo,
                                     Integer totalSliceNo) throws Exception {
        // 判断数据库中是否已经存在该文件
        File dbFileMD5 = fileDao.getFileByMD5(fileMD5);
        if(dbFileMD5 != null){
            return dbFileMD5.getUrl(); // 已存在，则直接从数据库中返回，即秒传
        }
        String url = fastDFSUtil.uploadFileBySlices(slice, fileMD5, sliceNo, totalSliceNo); // 不存在，则正常分片上传
        if(!StringUtil.isNullOrEmpty(url)){
            dbFileMD5 = new File();
            dbFileMD5.setCreateTime(new Date());
            dbFileMD5.setMd5(fileMD5);
            dbFileMD5.setUrl(url);
            dbFileMD5.setType(fastDFSUtil.getFileType(slice));
            fileDao.addFile(dbFileMD5); // 将上传的文件信息保存到数据库
        }
        return url;
    }

    /**
     * 获取文件的MD5加密
     * @param file
     * @return
     * @throws Exception
     */
    public String getFileMD5(MultipartFile file) throws Exception {
        return MD5Util.getFileMD5(file);
    }

}
