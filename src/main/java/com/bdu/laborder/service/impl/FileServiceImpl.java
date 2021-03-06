package com.bdu.laborder.service.impl;

import com.bdu.laborder.config.FileStorageProperties;
import com.bdu.laborder.exception.LabOrderException;
import com.bdu.laborder.mapper.UserMapper;
import com.bdu.laborder.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

/**
 * @Author Qi
 * @data 2021/4/29 10:45
 */
@Service
public class FileServiceImpl implements FileService {

    private final Path fileStorageLocation;
    @Autowired
    UserMapper userMapper;

    @Autowired
    public FileServiceImpl(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new LabOrderException("无法创建将存储上传文件的目录");
        }
    }

    @Override
    public String storeFile(MultipartFile file,String userId) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            if (fileName.contains("..")) {
                throw new LabOrderException();
            }
            // 获取文件后缀名
            String fileExt = getFileExt(fileName);
            //如果文件的后缀为空则不允许上传
            if (fileExt.isEmpty()) {
                throw new LabOrderException();
            }
            // 生产随机文件名
            String saveName = UUID.randomUUID().toString();
            // 获得保存文件名
            String saveFileName = saveName + '.' + fileExt;
            // 保存文件
            Path targetLocation = this.fileStorageLocation.resolve(saveFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            // 更改用户数据库头像
//            String fileDownloadUri = ServletUriComponentsBuilder
//                    .fromCurrentContextPath()
//                    .path("/downloadFile/")
//                    .path(saveFileName).toUriString();
//            userMapper.updateUserAvatar(userId,fileDownloadUri);
            return saveFileName;
        } catch (IOException ex) {
            throw new LabOrderException("无法存储文件 " + fileName + ". 请稍后再试！");
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new LabOrderException("没有找到文件" + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new LabOrderException("没有找到文件" + fileName);
        }
    }

    public static String getFileExt(String fileName) {
        int iIndex = fileName.lastIndexOf(".");
        if (iIndex < 0)
            return "";
        return fileName.substring(iIndex + 1).toLowerCase();
    }
}
