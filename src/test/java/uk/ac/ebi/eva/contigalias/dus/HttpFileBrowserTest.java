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

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that {@link HttpFileBrowser#downloadFile} recovers from a connection dropped
 * mid-transfer by resuming (HTTP {@code Range}) rather than restarting a large download from
 * scratch, and falls back cleanly to a full restart when the server doesn't honour the range.
 */
public class HttpFileBrowserTest {

    private static final byte[] PAYLOAD = buildPayload(5000);

    private HttpServer server;

    private static byte[] buildPayload(int size) {
        byte[] payload = new byte[size];
        for (int i = 0; i < size; i++) {
            payload[i] = (byte) ('A' + (i % 26));
        }
        return payload;
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void resumesAfterDroppedConnection() throws Exception {
        int breakAt = 2000;
        AtomicInteger requestCount = new AtomicInteger();

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/file.txt", exchange -> {
            if (requestCount.incrementAndGet() == 1) {
                // First attempt: write part of the declared body then blow up, simulating a
                // connection dropped mid-transfer (the client sees a truncated fixed-length body).
                exchange.sendResponseHeaders(200, PAYLOAD.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(PAYLOAD, 0, breakAt);
                    os.flush();
                    throw new IOException("simulated broken connection");
                }
            }

            // Second attempt: honour the Range request for the remainder.
            String range = exchange.getRequestHeaders().getFirst("Range");
            assertEquals("bytes=" + breakAt + "-", range);
            byte[] remaining = Arrays.copyOfRange(PAYLOAD, breakAt, PAYLOAD.length);
            exchange.getResponseHeaders().set("ETag", "\"test-etag\"");
            exchange.getResponseHeaders().set("Content-Range",
                    "bytes " + breakAt + "-" + (PAYLOAD.length - 1) + "/" + PAYLOAD.length);
            exchange.sendResponseHeaders(206, remaining.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(remaining);
            }
        });
        server.start();

        Path downloadPath = Files.createTempFile("http-file-browser-test", ".txt");
        Files.delete(downloadPath);
        try {
            boolean success = new HttpFileBrowser().downloadFile(
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/file.txt",
                    downloadPath, PAYLOAD.length);

            assertTrue(success);
            assertEquals(2, requestCount.get());
            assertEquals(PAYLOAD.length, Files.size(downloadPath));
            assertArrayEquals(PAYLOAD, Files.readAllBytes(downloadPath));
        } finally {
            Files.deleteIfExists(downloadPath);
        }
    }

    @Test
    void restartsCleanlyWhenServerIgnoresRange() throws Exception {
        int breakAt = 2000;
        AtomicInteger requestCount = new AtomicInteger();

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/file.txt", exchange -> {
            if (requestCount.incrementAndGet() == 1) {
                exchange.sendResponseHeaders(200, PAYLOAD.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(PAYLOAD, 0, breakAt);
                    os.flush();
                    throw new IOException("simulated broken connection");
                }
            }

            // Second attempt: server doesn't support Range - re-sends the full body with 200.
            exchange.sendResponseHeaders(200, PAYLOAD.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(PAYLOAD);
            }
        });
        server.start();

        Path downloadPath = Files.createTempFile("http-file-browser-test", ".txt");
        Files.delete(downloadPath);
        try {
            boolean success = new HttpFileBrowser().downloadFile(
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/file.txt",
                    downloadPath, PAYLOAD.length);

            assertTrue(success);
            assertEquals(2, requestCount.get());
            assertEquals(PAYLOAD.length, Files.size(downloadPath));
            assertArrayEquals(PAYLOAD, Files.readAllBytes(downloadPath));
        } finally {
            Files.deleteIfExists(downloadPath);
        }
    }

}
