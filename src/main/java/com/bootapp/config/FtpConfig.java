package com.bootapp.config;

import com.bootapp.utils.FtpPoolUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@Configuration
@ConditionalOnClass({ GenericObjectPool.class, FTPClient.class })
@ConditionalOnProperty(value = "ftp.enabled", havingValue = "true")
@EnableConfigurationProperties(FtpConfigProperties.class)
public class FtpConfig {

    private static final Logger logger = LoggerFactory.getLogger(FtpConfig.class);

    private ObjectPool<FTPClient> pool;

    public FtpConfig(FtpConfigProperties props) {
        // 默认最大连接数与最大空闲连接数都为8，最小空闲连接数为0
        // 其他未设置属性使用默认值，可根据需要添加相关配置
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(60000);
        poolConfig.setSoftMinEvictableIdleTimeMillis(50000);
        poolConfig.setTimeBetweenEvictionRunsMillis(30000);
        pool = new GenericObjectPool<>(new FtpClientPooledObjectFactory(props), poolConfig);
        preLoadingFtpClient(props.getInitialSize(), poolConfig.getMaxIdle());
        // 初始化ftp工具类中的ftpClientPool
        FtpPoolUtils.init(pool);
    }

    /**
     * 预先加载FTPClient连接到对象池中
     * @param initialSize 初始化连接数
     * @param maxIdle 最大空闲连接数
     */
    private void preLoadingFtpClient(Integer initialSize, int maxIdle) {
        if (initialSize == null || initialSize <= 0) {
            return;
        }
        int size = Math.min(initialSize.intValue(), maxIdle);
        for (int i = 0; i < size; i++) {
            try {
                pool.addObject();
            } catch (Exception e) {
                logger.error("预先加载ftpclient失败...",e);
            }
        }
    }

    @PreDestroy
    public void destroy() {
        if (pool != null) {
            pool.close();
        }
    }

}