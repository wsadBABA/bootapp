package com.bootapp.config;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpClientPooledObjectFactory implements PooledObjectFactory<FTPClient> {

    private static final Logger logger = LoggerFactory.getLogger(FtpClientPooledObjectFactory.class);

    private FtpConfigProperties props;

    public FtpClientPooledObjectFactory(FtpConfigProperties props) {
        this.props = props;
    }

    @Override
    public PooledObject<FTPClient> makeObject() throws Exception {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(props.getHost(), props.getPort());
            ftpClient.login(props.getUsername(), props.getPassword());
            ftpClient.setControlEncoding(props.getEncoding());
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            return new DefaultPooledObject<>(ftpClient);
        } catch (Exception e) {
            logger.error("无法建立与FTP服务器的连接，错误原：{}",e.getMessage());
            if (ftpClient.isAvailable()) {
                ftpClient.disconnect();
            }
            ftpClient = null;
            throw new Exception("建立FTP连接失败", e);
        }
    }

    @Override
    public void destroyObject(PooledObject<FTPClient> p) throws Exception {
        FTPClient ftpClient = getObject(p);
        if (ftpClient != null && ftpClient.isConnected()) {
            ftpClient.disconnect();
        }
    }

    @Override
    public boolean validateObject(PooledObject<FTPClient> p) {
        FTPClient ftpClient = getObject(p);
        if (ftpClient == null || !ftpClient.isConnected()) {
            return false;
        }
        return true;
    }

    @Override
    public void activateObject(PooledObject<FTPClient> p) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<FTPClient> p) throws Exception {

    }

    private FTPClient getObject(PooledObject<FTPClient> p) {
        if (p == null || p.getObject() == null) {
            return null;
        }
        return p.getObject();
    }

}
