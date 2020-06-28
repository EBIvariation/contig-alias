/*
 * Copyright 2020 EMBL - European Bioinformatics Institute
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

package com.ebivariation.contigalias.dus;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PassiveAnonymousFTPClient extends FTPClient {

    private static final int DEFAULT_FTP_PORT = 21;

    private final Logger logger = LoggerFactory.getLogger(PassiveAnonymousFTPClient.class);

    public void connect(String address) throws IOException {
        this.connect(address, DEFAULT_FTP_PORT);
    }

    @Override
    public void connect(String address, int port) throws IOException {

        try {
            logger.debug("Attempting to connect to {}:{}.", address, port);
            super.connect(address, port);
            // After connection attempt, you should check the reply code to verify
            // success.
            int reply = super.getReplyCode();
            logger.debug("Connected to {} with reply code {}.", address, reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new RuntimeException("FTP refused connection");
            }

            // Login as anonymous user
            super.enterLocalPassiveMode();
            boolean login = super.login("anonymous", "anonymous");
            logger.debug("Login {}.", (login ? "successful" : "unsuccessful"));
            if (!login) {
                throw new RuntimeException("FTP refused login");
            }

            String status = super.getStatus();
            logger.debug("FTP connection status: {}", status);
            logger.info("Connected successfully to {}", address);
        } catch (Exception e) {
            logger.error("Could not connect to FTP server '{}'. FTP status was: {}. Reply code: {}. Reply string: {}",
                         address, super.getStatus(), super.getReply(), super.getReplyString());
            this.disconnect();
            throw e;
        }
    }

    @Override
    public void disconnect() throws IOException {
        if (super.isConnected()) {
            super.logout();
            super.disconnect();
        }
    }

}
