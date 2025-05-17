package com.BTL.Springboot.service.impl;

import com.BTL.Springboot.entity.Property;
import com.BTL.Springboot.entity.PropertyImage;
import com.BTL.Springboot.repository.PropertyImageRepository;
import com.BTL.Springboot.service.PropertyImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PropertyImageServiceImpl implements PropertyImageService {

    @Autowired
    private PropertyImageRepository propertyImageRepository;

    private static final String UPLOAD_DIR = "E:\\IntelliJ\\SpringBoot\\uploads\\";
    private static final String TEMP_PREFIX = "temp_";
    private static final String HIDDEN_PREFIX = "hidden_";
    private static final String IMAGE_URL_PREFIX = "/img/";

    // Map để lưu tempImageUrls cho mỗi propertyId
    private static final Map<Integer, List<String>> tempImageUrlsMap = new HashMap<>();
    private static final Map<Integer, Integer> reloadCountMap = new HashMap<>();
    // Map để theo dõi xem một property vừa trải qua hành động xóa tạm hay không
    private static final Map<Integer, Boolean> isAfterRemoveTempMap = new ConcurrentHashMap<>();
    // Map để theo dõi thời gian của lần xóa tạm cuối cùng cho mỗi property
    private static final Map<Integer, Long> lastRemoveTempTimeMap = new ConcurrentHashMap<>();

    @Override
    public List<PropertyImage> getImagesByProperty(Property property) {
        List<PropertyImage> images = propertyImageRepository.findByProperty(property);
        log.info("Fetched images for property ID {}: {}", property.getPropertyId(), images);
        return images;
    }

    @Override
    public List<String> getTempImagesByProperty(Property property) {
        List<String> tempImages = tempImageUrlsMap.getOrDefault(property.getPropertyId(), new ArrayList<>());
        log.info("Fetched temp images for property ID {}: {}", property.getPropertyId(), tempImages);
        return tempImages;
    }

    @Override
    public String uploadTempImage(Property property, MultipartFile file, String caption, boolean isMain) throws IOException {
        if (file == null || file.isEmpty()) {
            log.error("Cannot upload temp image: file is null or empty for property ID {}", property.getPropertyId());
            throw new IOException("File is null or empty");
        }
        String fileName = uploadSingleFile(file, true);
        String imageUrl = IMAGE_URL_PREFIX + fileName;
        log.info("Uploaded temp image for property ID {}: {}", property.getPropertyId(), imageUrl);

        // Lưu imageUrl vào tempImageUrlsMap
        List<String> tempImages = tempImageUrlsMap.computeIfAbsent(property.getPropertyId(), k -> new ArrayList<>());
        tempImages.add(imageUrl);
        tempImageUrlsMap.put(property.getPropertyId(), tempImages);
        log.info("Added temp image to map for property ID {}: {}", property.getPropertyId(), imageUrl);

        // Kiểm tra file có thực sự được lưu
        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        if (!Files.exists(filePath)) {
            log.error("File was not saved to disk: {}", filePath);
            throw new IOException("Failed to save file to disk: " + fileName);
        }
        return imageUrl;
    }

    @Override
    public PropertyImage uploadImage(Property property, MultipartFile file, String caption, boolean isMain) throws IOException {
        String fileName = uploadSingleFile(file, false);
        PropertyImage image = new PropertyImage();
        image.setProperty(property);
        image.setImageUrl(IMAGE_URL_PREFIX + fileName);
        image.setCaption(caption);
        image.setIsMain(isMain);
        image.setCreatedAt(LocalDateTime.now());
        return propertyImageRepository.save(image);
    }

    @Override
    public void saveImages(Property property, List<String> imageUrls) throws IOException {
        List<PropertyImage> existingImages = propertyImageRepository.findByProperty(property);

        // Xóa ảnh ẩn vĩnh viễn
        List<PropertyImage> hiddenImages = existingImages.stream()
                .filter(img -> img.getImageUrl().startsWith(IMAGE_URL_PREFIX + HIDDEN_PREFIX))
                .toList();
        for (PropertyImage hiddenImage : hiddenImages) {
            String fileName = hiddenImage.getImageUrl().substring(IMAGE_URL_PREFIX.length());
            if (isImageInUploads(fileName)) {
                // Trường hợp 1: Ảnh trong uploads
                Path filePath = Paths.get(UPLOAD_DIR + fileName);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("Permanently deleted hidden image file: {}", filePath);
                }
            } else {
                // Trường hợp 2: Ảnh không trong uploads, chỉ xóa bản ghi database
                log.info("Permanently deleting hidden image from database (non-uploaded): {}", hiddenImage.getImageUrl());
            }
            propertyImageRepository.delete(hiddenImage);
            log.info("Permanently deleted hidden image from database: {}", hiddenImage.getImageUrl());
        }

        // Lưu ảnh được chọn
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String url : imageUrls) {
                boolean exists = existingImages.stream()
                        .anyMatch(img -> img.getImageUrl().equals(url) && !url.startsWith(IMAGE_URL_PREFIX + HIDDEN_PREFIX));
                if (!exists && url.startsWith(IMAGE_URL_PREFIX + TEMP_PREFIX)) {
                    String fileName = url.substring(IMAGE_URL_PREFIX.length());
                    String newFileName = fileName.substring(TEMP_PREFIX.length());
                    if (isImageInUploads(fileName)) {
                        // Trường hợp 1: Ảnh trong uploads
                        Path oldPath = Paths.get(UPLOAD_DIR + fileName);
                        Path newPath = Paths.get(UPLOAD_DIR + newFileName);
                        if (Files.exists(oldPath)) {
                            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                            log.info("Renamed temp image from {} to {}", fileName, newFileName);
                        }
                    } else {
                        // Trường hợp 2: Ảnh không trong uploads, chỉ cập nhật database
                        log.info("Saving non-uploaded temp image to database: {}", url);
                    }
                    PropertyImage newImage = new PropertyImage();
                    newImage.setProperty(property);
                    newImage.setImageUrl(IMAGE_URL_PREFIX + newFileName);
                    newImage.setCaption("Uploaded Image");
                    newImage.setIsMain(false);
                    newImage.setCreatedAt(LocalDateTime.now());
                    propertyImageRepository.save(newImage);
                }
            }
        }

        // Xóa tempImageUrls sau khi lưu
        tempImageUrlsMap.remove(property.getPropertyId());
        log.info("Cleared tempImageUrls for property ID {}", property.getPropertyId());
    }

    @Override
    public String deleteImage(PropertyImage image) throws IOException {
        String imageUrl = image.getImageUrl();
        String fileName = imageUrl.substring(IMAGE_URL_PREFIX.length());
        String hiddenFileName = HIDDEN_PREFIX + fileName;

        if (isImageInUploads(fileName)) {
            // Trường hợp 1: Ảnh trong uploads
            Path oldPath = Paths.get(UPLOAD_DIR + fileName);
            Path newPath = Paths.get(UPLOAD_DIR + hiddenFileName);
            if (Files.exists(oldPath)) {
                Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("Moved image to hidden: {} -> {}", fileName, hiddenFileName);
            }
        } else {
            // Trường hợp 2: Ảnh không trong uploads, chỉ cập nhật database
            log.info("Image not in uploads, marking as hidden in database: {}", imageUrl);
        }

        // Cập nhật database cho cả hai trường hợp
        image.setImageUrl(IMAGE_URL_PREFIX + hiddenFileName);
        propertyImageRepository.save(image);
        log.info("Marked image as hidden in database: {}", image.getImageUrl());
        return image.getImageUrl();
    }

    @Override
    public void deleteTempImage(String imageUrl) {
        try {
            String fileName = imageUrl.substring(IMAGE_URL_PREFIX.length());
            if (imageUrl.contains("temp_") && isImageInUploads(fileName)) {
                // Trường hợp 1: Ảnh tạm trong uploads
                Path filePath = Paths.get(UPLOAD_DIR + fileName);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("Deleted temp image file: {}", filePath);
                }
            } else {
                // Trường hợp 2: Ảnh không trong uploads hoặc không phải ảnh tạm, đánh dấu hidden trong database
                PropertyImage image = propertyImageRepository.findByImageUrl(imageUrl);
                if (image != null) {
                    String hiddenFileName = HIDDEN_PREFIX + fileName;
                    image.setImageUrl(IMAGE_URL_PREFIX + hiddenFileName);
                    propertyImageRepository.save(image);
                    log.info("Marked temp image as hidden in database for non-uploaded image: {}", imageUrl);
                } else {
                    log.warn("Temp image not found in database: {}", imageUrl);
                }
            }
            // Xóa imageUrl khỏi tempImageUrlsMap
            tempImageUrlsMap.values().forEach(list -> list.remove(imageUrl));
            log.info("Removed temp image from map: {}", imageUrl);
        } catch (Exception e) {
            log.error("Error deleting temp image: {}", imageUrl, e);
        }
    }

    @Override
    public void deleteImagesFromList(Property property, List<String> imageUrlsToDelete) {
        if (imageUrlsToDelete != null && !imageUrlsToDelete.isEmpty()) {
            List<PropertyImage> existingImages = propertyImageRepository.findByProperty(property);
            for (String url : imageUrlsToDelete) {
                PropertyImage imageToDelete = existingImages.stream()
                        .filter(img -> img.getImageUrl().equals(url))
                        .findFirst()
                        .orElse(null);
                if (imageToDelete != null) {
                    try {
                        String fileName = url.substring(IMAGE_URL_PREFIX.length());
                        String hiddenFileName = HIDDEN_PREFIX + fileName;
                        if (isImageInUploads(fileName)) {
                            // Trường hợp 1: Ảnh trong uploads
                            Path filePath = Paths.get(UPLOAD_DIR + fileName);
                            Path hiddenPath = Paths.get(UPLOAD_DIR + hiddenFileName);
                            if (Files.exists(filePath)) {
                                Files.move(filePath, hiddenPath, StandardCopyOption.REPLACE_EXISTING);
                                log.info("Moved image to hidden: {} -> {}", fileName, hiddenFileName);
                            }
                        } else {
                            // Trường hợp 2: Ảnh không trong uploads, chỉ cập nhật database
                            log.info("Marking non-uploaded image as hidden in database: {}", url);
                        }
                        imageToDelete.setImageUrl(IMAGE_URL_PREFIX + hiddenFileName);
                        propertyImageRepository.save(imageToDelete);
                        log.info("Marked image as hidden in database: {}", url);
                    } catch (IOException e) {
                        log.error("Error moving image to hidden: {}", url, e);
                        String errorFileName = url.substring(IMAGE_URL_PREFIX.length());
                        imageToDelete.setImageUrl(IMAGE_URL_PREFIX + HIDDEN_PREFIX + errorFileName);
                        propertyImageRepository.save(imageToDelete);
                        log.info("Marked image as hidden in database despite file error: {}", url);
                    }
                }
            }
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName, String directory) throws MalformedURLException {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new MalformedURLException("File not found or not readable: " + fileName);
        } catch (MalformedURLException e) {
            log.error("Error loading file: {}", fileName, e);
            throw e;
        }
    }

    private String uploadSingleFile(MultipartFile file, boolean isTemp) throws IOException {
        if (file == null || file.isEmpty()) {
            log.error("Cannot upload file: file is null or empty");
            throw new IOException("File is null or empty");
        }

        Path folder = Paths.get(UPLOAD_DIR);
        try {
            Files.createDirectories(folder);
            if (!Files.isWritable(folder)) {
                log.error("Upload directory is not writable: {}", UPLOAD_DIR);
                throw new IOException("Upload directory is not writable: " + UPLOAD_DIR);
            }
        } catch (IOException e) {
            log.error("Error creating directory: {}", UPLOAD_DIR, e);
            throw new IOException("Could not create directory: " + UPLOAD_DIR, e);
        }

        String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString();
        if (isTemp) {
            fileName = TEMP_PREFIX + fileName;
        }
        if (fileExtension != null) {
            fileName = fileName + "." + fileExtension;
        } else {
            fileName = fileName + ".jpg";
        }

        Path filePath = folder.resolve(fileName);
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Uploaded file: {} to path: {}", fileName, filePath);
        } catch (IOException e) {
            log.error("Error uploading file: {} to path: {}", fileName, filePath, e);
            throw new IOException("Could not upload file: " + fileName, e);
        }

        return fileName;
    }

    // Phương thức kiểm tra xem ảnh có nằm trong thư mục uploads hay không
    private boolean isImageInUploads(String fileName) {
        // Kiểm tra nếu fileName là URL bên ngoài (bắt đầu bằng http:// hoặc https://)
        if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
            log.info("Image {} is an external URL, not in uploads", fileName);
            return false;
        }
        try {
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            boolean exists = Files.exists(filePath);
            log.info("Checking if image {} is in uploads: {}", fileName, exists);
            return exists;
        } catch (Exception e) {
            log.error("Error checking if image {} is in uploads: {}", fileName, e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> restoreImagesFromHidden(Property property) {
        int reloadCount = getReloadCount(property.getPropertyId());
        boolean isAfterRemoveTemp = isAfterRemoveTempOperation(property.getPropertyId());
        List<String> restoredUrls = new ArrayList<>();

        log.info("Current reloadCount: {}, isAfterRemoveTemp: {} for property ID {}",
                reloadCount, isAfterRemoveTemp, property.getPropertyId());

        if (reloadCount == 2 && !isAfterRemoveTemp) {
            List<PropertyImage> hiddenImages = propertyImageRepository.findByProperty(property)
                    .stream()
                    .filter(img -> img.getImageUrl().startsWith(IMAGE_URL_PREFIX + HIDDEN_PREFIX))
                    .toList();

            for (PropertyImage image : hiddenImages) {
                String fileName = image.getImageUrl().substring(IMAGE_URL_PREFIX.length());
                String restoredFileName = fileName.substring(HIDDEN_PREFIX.length());
                if (isImageInUploads(fileName)) {
                    // Trường hợp 1: Ảnh trong uploads
                    Path hiddenPath = Paths.get(UPLOAD_DIR + fileName);
                    Path restoredPath = Paths.get(UPLOAD_DIR + restoredFileName);
                    if (Files.exists(hiddenPath)) {
                        try {
                            Files.move(hiddenPath, restoredPath, StandardCopyOption.REPLACE_EXISTING);
                            log.info("Restored hidden image: {} -> {}", fileName, restoredFileName);
                        } catch (IOException e) {
                            log.error("Error restoring hidden image: {}", fileName, e);
                            continue;
                        }
                    }
                } else {
                    // Trường hợp 2: Ảnh không trong uploads, chỉ cập nhật database
                    log.info("Restoring non-uploaded hidden image in database: {} -> {}", fileName, restoredFileName);
                }
                image.setImageUrl(IMAGE_URL_PREFIX + restoredFileName);
                propertyImageRepository.save(image);
                restoredUrls.add(image.getImageUrl());
                log.info("Restored image in database: {}", image.getImageUrl());
            }

            updateReloadCount(property.getPropertyId(), 0);
            log.info("Reset reloadCount to 0 after restoration for property ID {}", property.getPropertyId());
            System.out.println("Biến đếm count: " + reloadCount);
        } else {
            if (isAfterRemoveTemp) {
                updateReloadCount(property.getPropertyId(), 2);
                log.info("Set reloadCount to 1 after temporary removal for property ID {}", property.getPropertyId());
                System.out.println("Biến đếm count (after temp remove): 1");
            } else {
                updateReloadCount(property.getPropertyId(), reloadCount + 1);
                log.info("Increased reloadCount to {} for property ID {}", reloadCount + 1, property.getPropertyId());
                System.out.println("Biến đếm count: " + (reloadCount + 1));
            }
        }

        setIsAfterRemoveTemp(property.getPropertyId(), false);
        return restoredUrls;
    }

    @Override
    public void setIsAfterRemoveTemp(Integer propertyId, boolean isAfterRemoveTemp) {
        if (isAfterRemoveTemp) {
            lastRemoveTempTimeMap.put(propertyId, System.currentTimeMillis());
        }
        isAfterRemoveTempMap.put(propertyId, isAfterRemoveTemp);
        log.info("Set isAfterRemoveTemp for property ID {}: {}", propertyId, isAfterRemoveTemp);
    }

    private int getReloadCount(Integer propertyId) {
        int count = reloadCountMap.getOrDefault(propertyId, 0);
        log.info("Get reloadCount for property ID {}: {}", propertyId, count);
        return count;
    }

    private void updateReloadCount(Integer propertyId, int count) {
        log.info("Update reloadCount for property ID {}: {}", propertyId, count);
        reloadCountMap.put(propertyId, count);
    }

    public void clearTempImages(Integer propertyId) {
        List<String> tempImages = tempImageUrlsMap.getOrDefault(propertyId, new ArrayList<>());
        for (String imageUrl : tempImages) {
            try {
                String fileName = imageUrl.substring(IMAGE_URL_PREFIX.length());
                if (isImageInUploads(fileName)) {
                    Path filePath = Paths.get(UPLOAD_DIR + fileName);
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                        log.info("Deleted temp image file during cleanup: {}", filePath);
                    }
                }
            } catch (IOException e) {
                log.error("Error deleting temp image file during cleanup: {}", imageUrl, e);
            }
        }
        tempImageUrlsMap.remove(propertyId);
        log.info("Cleared tempImageUrls for property ID {}", propertyId);
    }

    private boolean isAfterRemoveTempOperation(Integer propertyId) {
        boolean isAfterRemoveTemp = isAfterRemoveTempMap.getOrDefault(propertyId, false);
        Long lastRemoveTime = lastRemoveTempTimeMap.getOrDefault(propertyId, 0L);
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastRemoveTime;
        boolean isRecentRemove = timeDiff < 2000;

        log.info("Check isAfterRemoveTemp for property ID {}: flag={}, timeDiff={}, isRecentRemove={}",
                propertyId, isAfterRemoveTemp, timeDiff, isRecentRemove);

        return isAfterRemoveTemp || isRecentRemove;
    }
}