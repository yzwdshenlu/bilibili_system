package com.daVinci.bilibili.service.util;

import com.daVinci.bilibili.domain.exception.ConditionException;
import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.service.util
 * @Author: daVinci
 * @CreateTime: 2025-02-04  11:27
 * @Description: FastDFS工具类
 * @Version: 1.0
 */
@Component
public class FastDFSUtil {

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private AppendFileStorageClient appendFileStorageClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String DEFAULT_GROUP = "group1";
    private static final String PATH_KEY = "path-key:";
    private static final String UPLOADED_SIZE_KEY = "uploaded-size-key:";
    private static final String UPLOADED_NO_KEY = "uploaded-no-key:";
    private static final int SLICE_SIZE = 1024 * 1024 * 2;

    /**
     * 获取文件类型
     *
     * @param file
     * @return
     */
    public String getFileType(MultipartFile file) {
        if (file == null) {
            throw new ConditionException("非法文件!");
        }
        String fileName = file.getOriginalFilename();
        int index = fileName.lastIndexOf("."); // 获取文件名中最后一个.作为分隔符
        return fileName.substring(index + 1);
    }

    /**
     * 上传普通文件
     *
     * @param file
     * @return
     * @throws Exception
     */
    public String uploadCommonFile(MultipartFile file) throws Exception {
        Set<MetaData> metaDataSet = new HashSet<>(); // 元数据，这里不额外添加了
        String fileType = this.getFileType(file);
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), fileType, metaDataSet);
        return storePath.getPath();
    }

    /**
     * 上传可以断点续传的文件
     *
     * @param file
     * @return
     * @throws Exception
     */
    public String uploadAppenderFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        String fileType = this.getFileType(file);
        StorePath storePath = appendFileStorageClient.uploadAppenderFile(DEFAULT_GROUP, file.getInputStream(), file.getSize(), fileType);
        return storePath.getPath();
    }

    /**
     * 继续上一个分片上传文件
     * 使用modify而不是append的好处是它可以对应位置修改，避免出现重传
     *
     * @param file
     * @param filePath
     * @param offset
     * @throws Exception
     */
    public void modifyAppenderFile(MultipartFile file, String filePath, long offset) throws Exception {
        appendFileStorageClient.modifyFile(DEFAULT_GROUP, filePath, file.getInputStream(), file.getSize(), offset);
    }


    /**
     * 分片上传文件
     *
     * @param file
     * @param fileMd5
     * @param sliceNo
     * @param totalSliceNo
     * @return
     * @throws Exception
     */
    public String uploadFileBySlices(MultipartFile file, String fileMd5, Integer sliceNo, Integer totalSliceNo) throws Exception {
        if (file == null || sliceNo == null || totalSliceNo == null) {
            throw new ConditionException("参数异常!");
        }
        String pathKey = PATH_KEY + fileMd5; // 文件上传的路径
        String uploadedSizeKey = UPLOADED_SIZE_KEY + fileMd5; // 用于计算偏移量
        String uploadedNoKey = UPLOADED_NO_KEY + fileMd5; // 用于比对什么时候上传成功

        String uploadedSizeStr = redisTemplate.opsForValue().get(uploadedSizeKey);
        Long uploadedSize = 0L;
        if (!StringUtil.isNullOrEmpty(uploadedSizeStr)) {
            uploadedSize = Long.valueOf(uploadedSizeStr); // 获取当前文件传了多少分片
        }
        String fileType = this.getFileType(file);
        if (sliceNo == 1) { // 第一个分片上传
            String path = this.uploadAppenderFile(file);
            if (StringUtil.isNullOrEmpty(path)) {
                throw new ConditionException("上传失败!");
            }
            redisTemplate.opsForValue().set(pathKey, path); // 存储上传的文件路径
            redisTemplate.opsForValue().set(uploadedNoKey, "1");
        } else { // 非第一个分片上传
            String filePath = redisTemplate.opsForValue().get(pathKey);
            if (StringUtil.isNullOrEmpty(filePath)) {
                throw new ConditionException("上传失败!");
            }
            this.modifyAppenderFile(file, filePath, uploadedSize);
            redisTemplate.opsForValue().increment(uploadedNoKey); // No加一
        }
        uploadedSize += file.getSize(); // 更新文件历史上传大小
        redisTemplate.opsForValue().set(uploadedSizeKey, String.valueOf(uploadedSize));
        // 如果所有分片全部上传完毕，则清空redis中的key和value
        String uploadedNoStr = redisTemplate.opsForValue().get(uploadedNoKey);
        Integer uploadedNo = Integer.valueOf(uploadedNoStr);
        String resultPath = "";
        if (uploadedNo.equals(totalSliceNo)) {
            resultPath = redisTemplate.opsForValue().get(pathKey); // 将文件上传路径暂存
            List<String> keyList = Arrays.asList(uploadedNoKey, pathKey, uploadedSizeKey);
            redisTemplate.delete(keyList);
        }
        return resultPath;
    }

    public void convertFileToSlices(MultipartFile multipartFile) throws Exception {
        String fileName = multipartFile.getOriginalFilename();
        String fileType = this.getFileType(multipartFile);
        File file = this.multipartFileToFile(multipartFile); // 转换为Java原生文件类型
        long fileLength = file.length();
        int count = 1;
        for (int i = 0; i < fileLength; i += SLICE_SIZE) { // 每次增加一个分片的大小
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(i); // 定位到分片的起始位置
            byte[] bytes = new byte[SLICE_SIZE];
            int len = randomAccessFile.read(bytes); // len的作用是最后一次分片的大小可能小于SLICE_SIZE
            String path = "D:\\jobs\\code\\my-bilibili" + count + "." + fileType;
            File slice = new File(path);
            FileOutputStream fos = new FileOutputStream(slice);
            fos.write(bytes, 0, len); // 写入磁盘
            fos.close();
            randomAccessFile.close();
            count++;
        }
        file.delete(); // 删除临时文件
    }

    public File multipartFileToFile(MultipartFile multipartFile) throws Exception {
        String originalFilename = multipartFile.getOriginalFilename();
        String[] fileName = originalFilename.split("\\."); // 分割成文件名和文件类型
        File file = File.createTempFile(fileName[0], "." + fileName[1]);
        multipartFile.transferTo(file);
        return file;
    }

    /**
     * 删除文件
     *
     * @param filePath
     */
    public void deleteFile(String filePath) {
        fastFileStorageClient.deleteFile(filePath);
    }

    @Value("${fdfs.http.storage-addr}")
    private String httpFdfsStorageAddr;

    /**
     * 使用Http协议向文件服务器请求视频，需要转发前端的请求头，自行构建响应头，然后将请求头，响应头以及文件的全局路径通过Http协议发送给文件服务器(HttpUtil)
     * @param request
     * @param response
     * @param path
     * @throws Exception
     */
    public void viewVideoOnlineBySlices(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String path) throws Exception {
        FileInfo fileInfo = fastFileStorageClient.queryFileInfo(DEFAULT_GROUP, path); // 根据相对路径获取文件信息
        long totalFileSize = fileInfo.getFileSize(); // 获取文件大小
        String url = httpFdfsStorageAddr + path; // 文件服务器ip与相对路径进行拼接

        // 转发请求头
        Enumeration<String> headerNames = request.getHeaderNames(); // 获取请求头的名字
        Map<String, Object> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) { // 将所有请求头的内容放入所有变量
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }
        // 构建contentRange请求头(分片的特殊参数)
        String rangeStr = request.getHeader("Range"); // 从请求头中获取分片范围参数
        String[] range;
        if (StringUtil.isNullOrEmpty(rangeStr)) {
            rangeStr = "bytes=0-" + (totalFileSize - 1);
        }
        range = rangeStr.split("bytes=|-");
        long begin = 0; // 起始位置
        if (range.length >= 2) { // 只有起始位置没有结束位置
            begin = Long.parseLong(range[1]);
        }
        long end = totalFileSize - 1;
        if (range.length >= 3) { // 起始位置和结束位置都有
            end = Long.parseLong(range[2]);
        }
        long len = (end - begin) + 1; // 分片的长度

        // 构建响应头
        String contentRange = "bytes " + begin + "-" + end + "/" + totalFileSize;
        response.setHeader("Content-Range", contentRange);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Type", "video/mp4");
        response.setContentLength((int) len);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        HttpUtil.get(url, headers, response);
    }
}
