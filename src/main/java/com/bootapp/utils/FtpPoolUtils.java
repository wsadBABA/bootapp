package com.bootapp.utils;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.ObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;

public class FtpPoolUtils {

    private static Logger logger = LoggerFactory.getLogger(FtpPoolUtils.class);

    /**
     * ftpClient连接池初始化标志
     */
    private static volatile boolean hasInit = false;

    /**
     * ftpClient连接池
     */
    private static ObjectPool<FTPClient> ftpClientPool;

    /**
     * 初始化ftpClientPool
     *
     * @param ftpClientPool
     */
    public static void init(ObjectPool<FTPClient> ftpClientPool) {
        if (!hasInit) {
            synchronized (FtpPoolUtils.class) {
                if (!hasInit) {
                    FtpPoolUtils.ftpClientPool = ftpClientPool;
                    hasInit = true;
                }
            }
        }
    }
    /**
     * 获取ftpClient
     *
     * @return
     */
    public static FTPClient getFtpClient() {
        checkFtpClientPoolAvailable();
        FTPClient ftpClient = null;
        Exception ex = null;
        // 获取连接最多尝试3次
        for (int i = 0; i < 3; i++) {
            try {
                ftpClient = ftpClientPool.borrowObject();
                ftpClient.changeWorkingDirectory("/");
                break;
            } catch (Exception e) {
                ex = e;
            }
        }
        if (ftpClient == null) {
            throw new RuntimeException("Could not get a ftpClient from the pool", ex);
        }
        return ftpClient;
    }

    /**
     * 释放ftpClient
     */
    public static void releaseFtpClient(FTPClient ftpClient) {
        if (ftpClient == null) {
            return;
        }
        try {
            ftpClientPool.returnObject(ftpClient);
        } catch (Exception e) {
            logger.error("Could not return the ftpClient to the pool", e.getMessage());
            if (ftpClient.isAvailable()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException io) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 检查ftpClientPool是否可用
     */
    public static void checkFtpClientPoolAvailable() {
        Assert.state(hasInit, "FTP未启用或连接失败！");
    }

}
