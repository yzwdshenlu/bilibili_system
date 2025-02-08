package com.daVinci.bilibili.service;


import com.daVinci.bilibili.dao.DemoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @title: DemoService
 * @Author daVinci
 * @Date: 2025/1/20 17:18
 * @Version 1.0
 */
@Service
public class DemoService {
    @Autowired
    private DemoDao demoDao;

    public Long query(Long id){
        return demoDao.query(id);
    }
}
