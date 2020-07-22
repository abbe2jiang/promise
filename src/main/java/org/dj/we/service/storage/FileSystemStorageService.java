package org.dj.we.service.storage;

import org.dj.we.properties.StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public Path store(MultipartFile file, Path path,String fileName) throws StorageException {

        try {

            Path dir = rootLocation.resolve(path);
            if (Files.notExists(dir)) {
                Files.createDirectories(dir);
            }

            Path relativePath = path.resolve(fileName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, this.rootLocation.resolve(relativePath),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            return relativePath;
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + fileName, e);
        }
    }


    @Override
    public Resource load(Path file) throws StorageFileNotFoundException {
        Path filePath = rootLocation.resolve(file);
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filePath);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filePath, e);
        }
    }

    @Override
    public Path getPath(Path path){
        return rootLocation.resolve(path);
    }

}
