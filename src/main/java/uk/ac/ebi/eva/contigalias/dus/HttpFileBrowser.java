/*
 * Copyright 2026 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.eva.contigalias.dus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.eva.contigalias.exception.DownloadFailedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal HTTPS client for browsing and downloading files from NCBI's and EBI's public genome
 * archives, used in place of passive-mode FTP.
 */
public class HttpFileBrowser {

    private static final Logger logger = LoggerFactory.getLogger(HttpFileBrowser.class);

    // Matches href="..." attributes as emitted by both NCBI's <pre>-based and EBI's <table>-based
    // directory-index pages (both are standard Apache/nginx autoindex output).
    private static final Pattern HREF_PATTERN = Pattern.compile("href=\"([^\"]+)\"");

    // downloadFile() resumes from wherever it left off rather than restarting, so each attempt
    // only needs to carry the remaining bytes - this can be generous without risking a genuinely
    // large file being killed by the timeout while still transferring healthily.
    private static final Duration DOWNLOAD_TIMEOUT = Duration.ofMinutes(5);

    private static final int MAX_DOWNLOAD_ATTEMPTS = 3;

    private static final Duration DOWNLOAD_RETRY_BACKOFF = Duration.ofSeconds(2);

    private final HttpClient httpClient;

    public HttpFileBrowser() {
        this.httpClient = HttpClient.newBuilder()
                                     .connectTimeout(Duration.ofSeconds(30))
                                     .build();
    }

    /**
     * Lists the entry names (files and sub-directories, as they appear as link targets) of a
     * remote directory-index page. Sub-directories are returned with their trailing "/".
     *
     * @param url Full URL of the directory (should end in "/").
     */
    public List<String> listDirectory(String url) throws IOException {
        String body = get(url);
        List<String> entries = new ArrayList<>();
        Matcher matcher = HREF_PATTERN.matcher(body);
        while (matcher.find()) {
            String href = matcher.group(1);
            // Real entries are always relative names. Skip sort-order links ("?C=N;O=D"), the
            // parent-directory link and any other absolute link (icons, footer links).
            if (href.startsWith("?") || href.startsWith("/") || href.contains("://")) {
                continue;
            }
            entries.add(href);
        }
        return entries;
    }

    /**
     * Opens a stream to a remote file's content.
     */
    public InputStream openStream(String url) throws IOException {
        HttpRequest request = requestBuilder(url).GET().build();
        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            requireOk(response.statusCode(), url);
            return response.body();
        } catch (InterruptedException e) {
            throw interrupted(url);
        }
    }

    /**
     * Downloads a file over HTTPS to {@code downloadFilePath}, verifying its size against
     * {@code expectedSize} once downloaded.
     * <p>
     * Resumes from wherever a previous attempt left off using HTTP {@code Range} requests - the
     * direct analog of what {@code wget -c}/{@code curl -C -} do - instead of restarting a large
     * file from scratch on every dropped connection; both NCBI and EBI's static file servers
     * support this ({@code Accept-Ranges: bytes}). An {@code If-Range} validator (the ETag or
     * Last-Modified header the server offered on the previous attempt) guards against silently
     * appending onto a file that changed underneath us between attempts: if the server doesn't
     * honour the range, or the file changed, it responds with a fresh {@code 200} instead of
     * {@code 206}, and this falls back to a clean restart rather than producing a corrupt file.
     */
    public boolean downloadFile(String url, Path downloadFilePath, long expectedSize) throws IOException {
        long downloaded = Files.exists(downloadFilePath) ? Files.size(downloadFilePath) : 0;
        if (downloaded > expectedSize) {
            // Stale leftover, e.g. from a previous file at the same path - start clean.
            Files.deleteIfExists(downloadFilePath);
            downloaded = 0;
        }

        String validator = null;
        IOException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_DOWNLOAD_ATTEMPTS && downloaded < expectedSize; attempt++) {
            if (attempt > 1) {
                sleep(DOWNLOAD_RETRY_BACKOFF.multipliedBy(attempt - 1));
            }

            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
                                                       .timeout(DOWNLOAD_TIMEOUT)
                                                       .GET();
            if (downloaded > 0) {
                request.header("Range", "bytes=" + downloaded + "-");
                if (validator != null) {
                    request.header("If-Range", validator);
                }
            }

            try {
                HttpResponse<InputStream> response =
                        httpClient.send(request.build(), HttpResponse.BodyHandlers.ofInputStream());
                int status = response.statusCode();
                if (status != 200 && status != 206) {
                    throw new IOException("Unexpected HTTP status " + status + " for " + url);
                }
                boolean resumed = status == 206;
                validator = response.headers().firstValue("ETag")
                                     .or(() -> response.headers().firstValue("Last-Modified"))
                                     .orElse(null);

                OpenOption[] options = resumed
                        ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND}
                        : new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
                try (InputStream in = response.body();
                     OutputStream out = Files.newOutputStream(downloadFilePath, options)) {
                    in.transferTo(out);
                }
                downloaded = Files.size(downloadFilePath);
            } catch (IOException e) {
                lastFailure = e;
                downloaded = Files.exists(downloadFilePath) ? Files.size(downloadFilePath) : 0;
                logger.warn("Download attempt " + attempt + " for " + url + " failed after " + downloaded
                        + "/" + expectedSize + " bytes, will resume from there: " + e);
            } catch (InterruptedException e) {
                throw interrupted(url);
            }
        }

        if (downloaded != expectedSize) {
            String reason = lastFailure != null ? " Last error: " + lastFailure : "";
            throw new DownloadFailedException(
                    "File and downloaded file sizes does not match for " + url + ". Trying again." + reason);
        }
        logger.info(url + " downloaded successfully.");
        return true;
    }

    /**
     * Returns the {@code Content-Length} reported for a remote file, used as the "expected size"
     * for {@link #downloadFile} in place of FTP's {@code FTPFile.getSize()}.
     */
    public long headContentLength(String url) throws IOException {
        HttpRequest request = requestBuilder(url).method("HEAD", HttpRequest.BodyPublishers.noBody()).build();
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            requireOk(response.statusCode(), url);
            return response.headers().firstValueAsLong("Content-Length").orElse(-1);
        } catch (InterruptedException e) {
            throw interrupted(url);
        }
    }

    private String get(String url) throws IOException {
        HttpRequest request = requestBuilder(url).GET().build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            requireOk(response.statusCode(), url);
            return response.body();
        } catch (InterruptedException e) {
            throw interrupted(url);
        }
    }

    private HttpRequest.Builder requestBuilder(String url) {
        return HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(60));
    }

    private void requireOk(int statusCode, String url) throws IOException {
        if (statusCode != 200) {
            throw new IOException("Unexpected HTTP status " + statusCode + " for " + url);
        }
    }

    private InterruptedIOException interrupted(String url) {
        Thread.currentThread().interrupt();
        return new InterruptedIOException("Interrupted while requesting " + url);
    }

    private void sleep(Duration duration) throws InterruptedIOException {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Interrupted while backing off before retrying a download");
        }
    }

}
