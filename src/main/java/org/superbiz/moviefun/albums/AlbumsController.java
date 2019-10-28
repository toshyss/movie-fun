package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;
import org.superbiz.moviefun.blobstore.S3Store;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;

    @Autowired
    private BlobStore s3Store;

    public AlbumsController(AlbumsBean albumsBean) {
        this.albumsBean = albumsBean;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }




    // Customized for S3Store
    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        //saveUploadToFile(uploadedFile, getCoverFile(albumId));

        String id = String.valueOf(albumId);
        InputStream inputStream = uploadedFile.getInputStream();
        String contentType = uploadedFile.getContentType();

        Blob blob = new Blob(id, inputStream, contentType);
        s3Store.put(blob);

        return format("redirect:/albums/%d", albumId);
    }



    // Customized for S3Store
    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
//        Path coverFilePath = getExistingCoverPath(albumId);
//        byte[] imageBytes = readAllBytes(coverFilePath);
//        HttpHeaders headers = createImageHttpHeaders(coverFilePath, imageBytes);

        Optional<Blob> blob = s3Store.get(String.valueOf(albumId));

        Path coverFilePath = null;
        byte[] imageBytes = new byte[0];
        HttpHeaders headers = new HttpHeaders();

        if (blob == null){
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
            imageBytes = readAllBytes(coverFilePath);
            headers = createImageHttpHeaders(coverFilePath, imageBytes);
        }
        else {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            InputStream in = blob.get().inputStream;
            int c;
            while((c = in.read()) != -1 ){
                bout.write(c);
            }

            //blob.get().name;
            String contentType = blob.get().contentType;
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageBytes.length);
            imageBytes = bout.toByteArray();
        }
        return new HttpEntity<>(imageBytes, headers);
    }




    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, File targetFile) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(uploadedFile.getBytes());
        }
    }

    private HttpHeaders createImageHttpHeaders(Path coverFilePath, byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    private File getCoverFile(@PathVariable long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }

    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }

}
