package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private final BlobStore blobStore;
    private final Tika tika = new Tika();

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore){
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
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

    @GetMapping("/deleteAll")
    public String deleteAll() {
        blobStore.deleteAll();
        //   model.put("album", albumsBean.find(albumId));
        return "albums";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        byte[] byteArray = new byte[(int)uploadedFile.getSize()];
        uploadedFile.getInputStream().read(byteArray);

        Blob coverBlob = new Blob(
                getCoverBlobName(albumId),
                byteArray,
                uploadedFile.getContentType()
        );
        blobStore.put(coverBlob);

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        Optional<Blob> maybeCoverBlob = blobStore.get(getCoverBlobName(albumId));
        Blob coverBlob = maybeCoverBlob.orElseGet(this::buildDefaultCoverBlob);

        Path coverFilePath = getExistingCoverPath(albumId);
        HttpHeaders headers = createImageHttpHeaders(coverFilePath, coverBlob.byteArray);

        return new HttpEntity<>(coverBlob.byteArray, headers);
    }

    private String getCoverBlobName(long albumId) {
        return format("covers/%d", albumId);
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
            coverFilePath = Paths.get(getClass().getClassLoader().getResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }

    private Blob buildDefaultCoverBlob() {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream input = classLoader.getResourceAsStream("default-cover.jpg");
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();){
            int reads = input.read();
            while (reads != -1) {
                byteArrayOutputStream.write(reads);
                reads = input.read();
            }
            return new Blob(
                    "default-cover",
                    byteArrayOutputStream.toByteArray(),
                    tika.detect(input)
            );
        }catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

    }
}