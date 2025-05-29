package com.BTL.Springboot.controller;

import com.BTL.Springboot.dto.response.property.PropertyDto;
import com.BTL.Springboot.entity.Property;
import com.BTL.Springboot.entity.PropertyImage;
import com.BTL.Springboot.mapper.PropertyMapper;
import com.BTL.Springboot.service.PropertyImageService;
import com.BTL.Springboot.service.PropertyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/property-images")
@Slf4j
public class PropertyImageController {

    @Autowired
    private PropertyImageService propertyImageService;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private PropertyMapper mapper;

    @PostMapping("/upload")
    public ModelAndView uploadImages(@RequestParam("propertyId") Integer propertyId,
                                     @RequestParam("files") MultipartFile[] files,
                                     @RequestParam(value = "caption", required = false) String caption,
                                     @RequestParam(value = "isMain", defaultValue = "false") boolean isMain,
                                     RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();
        try {
            PropertyDto dto = propertyService.getPropertyById(propertyId);
            Property property = mapper.toEntity(dto);

            if (files == null || files.length == 0) {
                log.warn("No files uploaded for property ID {}", propertyId);
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất một file ảnh!");
                mav.setViewName("redirect:/properties/edit/" + propertyId);
                return mav;
            }

            int uploadedCount = 0;
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    log.info("Uploading file: {} (size: {} bytes) for property ID {}",
                            file.getOriginalFilename(), file.getSize(), propertyId);
                    String imageUrl = propertyImageService.uploadTempImage(property, file, caption, isMain);
                    uploadedCount++;
                    log.info("Uploaded file with URL: {}", imageUrl);
                } else {
                    log.warn("Skipping empty or null file for property ID {}", propertyId);
                }
            }

            if (uploadedCount == 0) {
                log.warn("No valid files were uploaded for property ID {}", propertyId);
                redirectAttributes.addFlashAttribute("errorMessage", "Không có file ảnh hợp lệ được tải lên!");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Đã tải " + uploadedCount + " ảnh lên - Hãy ấn lưu hình ảnh để cập nhật hình ảnh!");
                log.info("Uploaded {} images for property ID {}", uploadedCount, propertyId);
            }
        } catch (IOException e) {
            log.error("Error uploading images for property ID {}: {}", propertyId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tải ảnh lên: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error uploading images for property ID {}: {}", propertyId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi không xác định khi tải ảnh: " + e.getMessage());
        }
        mav.setViewName("redirect:/properties/edit/" + propertyId);
        return mav;
    }

    @PostMapping("/remove-temp")
    public ModelAndView removeTempImage(@RequestParam("propertyId") Integer propertyId,
                                        @RequestParam("imageUrl") String imageUrl,
                                        RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();
        List<String> hiddenImageUrls = new ArrayList<>();
        try {
            PropertyDto dto = propertyService.getPropertyById(propertyId);
            Property property = mapper.toEntity(dto);
            List<PropertyImage> allImages = propertyImageService.getImagesByProperty(property);

            // Đánh dấu là reload sau khi xóa tạm
            propertyImageService.setIsAfterRemoveTemp(propertyId, true);
            log.info("Marked isAfterRemoveTemp=true for property ID {} after remove-temp action", propertyId);

            if (imageUrl.contains("temp_")) {
                propertyImageService.deleteTempImage(imageUrl);
                redirectAttributes.addFlashAttribute("successMessage", "Ảnh vừa tải lên đã được xóa - Hãy ấn lưu hình ảnh để cập nhật!");
            } else {
                PropertyImage imageToRemove = allImages.stream()
                        .filter(img -> img.getImageUrl().equals(imageUrl))
                        .findFirst()
                        .orElse(null);
                if (imageToRemove != null) {
                    String newImageUrl = propertyImageService.deleteImage(imageToRemove);
                    hiddenImageUrls.add(newImageUrl);
                    redirectAttributes.addFlashAttribute("hiddenImageUrls", hiddenImageUrls);
                    redirectAttributes.addFlashAttribute("successMessage", "Ảnh đã được xoá - Hãy ấn lưu hình ảnh để cập nhật!");
                } else {
                    log.warn("Image not found in database: {}", imageUrl);
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy ảnh để xóa!");
                }
            }
        } catch (IOException e) {
            log.error("Error hiding image {}: {}", imageUrl, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa ảnh: " + e.getMessage());
        }
        log.info("Redirecting to edit page for property ID {} after removing image", propertyId);
        mav.setViewName("redirect:/properties/edit/" + propertyId);
        return mav;
    }

    @PostMapping("/save")
    public ModelAndView saveImages(@RequestParam("propertyId") Integer propertyId,
                                   @RequestParam(value = "imageUrls", required = false) List<String> imageUrls,
                                   @RequestParam(value = "imagesToDelete", required = false) List<String> imagesToDelete,
                                   RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();
        try {
            PropertyDto dto = propertyService.getPropertyById(propertyId);
            Property property = mapper.toEntity(dto);

            // Lưu ảnh được chọn
            propertyImageService.saveImages(property, imageUrls);

            // Xóa ảnh được đánh dấu
            List<String> imagesToRemove = imagesToDelete != null ? imagesToDelete : new ArrayList<>();
            if (!imagesToRemove.isEmpty()) {
                propertyImageService.deleteImagesFromList(property, imagesToRemove);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Lưu hình ảnh thành công!");
        } catch (Exception e) {
            log.error("Error saving images: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu hình ảnh: " + e.getMessage());
        }
        mav.setViewName("redirect:/properties/edit/" + propertyId);
        return mav;
    }

    @GetMapping("/img/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        try {
            Resource resource = propertyImageService.loadFileAsResource(fileName, null);
            String contentType = "image/jpeg";
            if (fileName.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (fileName.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            log.error("Error serving image: {}", fileName, e);
            return ResponseEntity.notFound().build();
        }
    }
}