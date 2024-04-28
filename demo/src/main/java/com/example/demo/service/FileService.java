package com.example.demo.service;

import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


@Component
public class FileService {

    @Autowired
    S3Uploader s3Uploader;

    @Autowired
    UserRepository userRepository;

    //function 1: save String and throw to function 3
    public void saveUserImage(MultipartFile picture_file, HttpServletRequest request) {
        if (picture_file != null && !picture_file.isEmpty()) {
            // Get fileName
            String fileName = picture_file.getOriginalFilename();
            // Construct base Url
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            // Save files and get full url
            String campaignFileValue = saveMultipartFile(picture_file, baseUrl);
            // Save Image to db
//            saveCampaignImageInfo(campaignFileValue);
        }
    }

    //function 2 -(1): One file
    public String saveMultipartFile(MultipartFile file, String baseUrl) {
        if (file != null && !file.isEmpty()) {
            try {
                // 路徑
                String baseDir = System.getProperty("user.dir");
                String relativeDir = "uploads/";
                String fileName = file.getOriginalFilename();
                String filePath = baseDir + File.separator + relativeDir + "product" + "-" + fileName;

                s3Uploader.saveMultipartFileToFile(file, "uploads/product" + "-" + fileName);
//                file.transferTo(new File(filePath));

                String imageUrl = baseUrl + "/" + relativeDir + "product" + "-" + fileName;
                return imageUrl;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    //function 3: call Dao to insert data
//    public void saveCampaignImageInfo(String campaignFileValue) {
//        userRepository.save(campaignFileValue);
//    }


}
