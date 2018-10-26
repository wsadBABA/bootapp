package com.bootapp.wopiServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;

@RestController
public class WopiServerController {

    Logger logger = LoggerFactory.getLogger(WopiServerController.class);

    private static final String CHARSET_UTF8 = "UTF-8";

    @Autowired
    private FtpFileService ftpFileService;

    /**
     * 获取文件信息
     * @param request
     * @param response
     * @return
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/files/{name}")
    public void getFileInfo(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        try  {
            // 获取文件名, 防止中文文件名乱码
            String fileName = URLDecoder.decode(uri.substring(uri.indexOf("wopi/files/") + 11), CHARSET_UTF8);
            FileInfo fileInfo = new FileInfo();
            if (fileName != null && fileName.length() > 0) {
                fileInfo  = ftpFileService.getFileInfo(fileName);
            }
            ObjectMapper mapper = new ObjectMapper();
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            String content = mapper.writeValueAsString(fileInfo);
            response.getWriter().write(content);
        } catch (Exception e) {
            logger.error("getFileInfo failed, errMsg: {}", e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 获取文件流
     * @param name
     * @param response
     */
    @GetMapping("/files/{name}/contents")
    public void getFile(@PathVariable String name, HttpServletResponse response) {
        OutputStream toClient = null;
        try {
            toClient = new BufferedOutputStream(response.getOutputStream());
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(name.getBytes(CHARSET_UTF8), "ISO-8859-1"));
            //response.addHeader("Content-Length", String.valueOf(10000));
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            ftpFileService.getFileContents(name,toClient);
            toClient.flush();
        } catch (Exception e) {
            logger.error("getFile failed, errMsg: {}", e.toString());
            e.printStackTrace();
        }finally {
            if(toClient != null){
                try {
                    toClient.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 保存更新文件
     * @param name
     * @param content
     */
    @PostMapping("/files/{name}/contents")
    public void postFile(@PathVariable(name = "name") String name, @RequestBody byte[] content) {
        ftpFileService.updateFile(name,content);
    }

}
