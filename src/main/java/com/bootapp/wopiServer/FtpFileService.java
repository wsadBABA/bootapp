package com.bootapp.wopiServer;

import com.bootapp.utils.FtpPoolUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

@Service
public class FtpFileService {

    private static final Logger logger = LoggerFactory.getLogger(FtpFileService.class);

    @Value("${file.path}")
    private String filePath;

    /**
     * 获取文档信息
     * @param fileName
     * @return
     * @throws Exception
     */
    public FileInfo getFileInfo(String fileName){
        FileInfo fileInfo = new FileInfo();
        FTPClient ftpClient = FtpPoolUtils.getFtpClient();
        InputStream inputStream = null;
        try {
            //filePath + fileName
            FTPFile[] ftpFiles = ftpClient.listFiles(filePath + fileName);
            if(ftpFiles != null && ftpFiles.length > 0){
                FTPFile file = ftpFiles[0];
                inputStream = ftpClient.retrieveFileStream(fileName);
                fileInfo.setSize(file.getSize());
                fileInfo.setBaseFileName(file.getName());
                fileInfo.setOwnerId("admin");
                fileInfo.setSha256(getHash256(inputStream));
                fileInfo.setVersion(file.getTimestamp().getTimeInMillis());
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                ftpClient.completePendingCommand();
            }catch (Exception e){

            }
            FtpPoolUtils.releaseFtpClient(ftpClient);
        }
        return fileInfo;
    }

    /**
     * 获取文档内容字节
     * @param fileName
     * @return
     * @throws Exception
     */
    public OutputStream getFileContents(String fileName, OutputStream outputStream) {
        FTPClient client = FtpPoolUtils.getFtpClient();
        try {
            client.retrieveFile(filePath + fileName,outputStream);
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            FtpPoolUtils.releaseFtpClient(client);
        }
        return outputStream;
    }

    /**
     * 更新文档
     * @param fileName
     * @param fileContents
     * @return
     */
    public boolean updateFile(String fileName,byte[] fileContents){
        FTPClient ftpClient = FtpPoolUtils.getFtpClient();
        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(fileContents);
            ftpClient.storeFile(filePath + fileName,inputStream);
            logger.info("文件 {} 更新成功!",fileName);
        }catch (Exception e){
            logger.error("文件 {} 更新失败！",filePath + fileName);
            e.printStackTrace();
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            FtpPoolUtils.releaseFtpClient(ftpClient);
        }
        return true;
    }

    /**
     * 获取文件的SHA-256值
     * @param fileInputStream
     * @return
     */
    private String getHash256(InputStream fileInputStream){
        String value = "";
        try {
            byte[] buffer = new byte[1024];
            int numRead;
            // 返回实现指定摘要算法的 MessageDigest 对象
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            do {
                numRead = fileInputStream.read(buffer);
                if (numRead > 0) {
                    // 更新摘要
                    digest.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            value = new String(Base64.encodeBase64(digest.digest()));
        }catch (Exception e){
            throw new RuntimeException("获取文件摘要算法错误",e);
        }
        return value;
    }

}
