package com.BTL.Springboot.service;

import com.BTL.Springboot.entity.Property;
import com.BTL.Springboot.entity.PropertyImage;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public interface PropertyImageService {

    List<PropertyImage> getImagesByProperty(Property property);

    List<String> getTempImagesByProperty(Property property);

    PropertyImage uploadImage(Property property, MultipartFile file, String caption, boolean isMain) throws IOException;

    String uploadTempImage(Property property, MultipartFile file, String caption, boolean isMain) throws IOException;

    void saveImages(Property property, List<String> imageUrls) throws IOException;

    String deleteImage(PropertyImage image) throws IOException;

    void deleteTempImage(String imageUrl);

    void deleteImagesFromList(Property property, List<String> imageUrlsToDelete);

    List<String> restoreImagesFromHidden(Property property);

    Resource loadFileAsResource(String fileName, String directory) throws MalformedURLException;

    void setIsAfterRemoveTemp(Integer propertyId, boolean isAfterRemoveTemp);
}