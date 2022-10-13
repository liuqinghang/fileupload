package com.ruoyi.web.controller.video;

import com.ruoyi.common.core.domain.AjaxResult;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Controller
@RequestMapping(value = "/video")
public class VideoController {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private VideoService storageService;

    @GetMapping("/video_upload")
    public String videoUpload(){
        return "/demo/video/video_upload";
    }

    /**
     * 秒传判断，断点判断
     *
     * @return
     */
    @RequestMapping(value = "checkFileMd5", method = RequestMethod.POST)
    @ResponseBody
    public Object checkFileMd5(String md5) throws IOException {
        Object processingObj = stringRedisTemplate.opsForHash().get(Constants.FILE_UPLOAD_STATUS, md5);
        if (processingObj == null) {
//            return new ResultVo(ResultStatus.NO_HAVE);
            return AjaxResult.success("101");
        }
        String processingStr = processingObj.toString();
        boolean processing = Boolean.parseBoolean(processingStr);
        String value = stringRedisTemplate.opsForValue().get(Constants.FILE_MD5_KEY + md5);
        if (processing) {
            return AjaxResult.success("100");
        } else {
            File confFile = new File(value);
            byte[] completeList = FileUtils.readFileToByteArray(confFile);
            List<String> missChunkList = new LinkedList<>();
            for (int i = 0; i < completeList.length; i++) {
                if (completeList[i] != Byte.MAX_VALUE) {
                    missChunkList.add(i + "");
                }
            }
//            return new ResultVo<>(ResultStatus.ING_HAVE, missChunkList);
            return AjaxResult.success("102", missChunkList);
        }
    }

    /**
     * 上传文件
     *
     * @param param
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult fileUpload(MultipartFileParam param, HttpServletRequest request) {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        String fileUrl = "";
        if (isMultipart) {
//            logger.info("上传文件start。");
            try {
                // 方法1
//                storageService.uploadFileRandomAccessFile(param);
                // 方法2 这个更快点
                fileUrl = storageService.uploadFileByMappedByteBuffer(param);
            } catch (IOException e) {
                e.printStackTrace();
//                logger.error("文件上传失败。{}", param.toString());
            }
//            logger.info("上传文件end。");
        }
        return AjaxResult.success(fileUrl);
    }
}
