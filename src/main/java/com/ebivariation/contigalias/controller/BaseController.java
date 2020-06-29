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

package com.ebivariation.contigalias.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class BaseController {

    public static final int DEFAULT_PAGE_NUMBER = 0;

    public static final int DEFAULT_PAGE_SIZE = 10;

    public static final PageRequest DEFAULT_PAGE_REQUEST = BaseController.createPageRequest(DEFAULT_PAGE_NUMBER,
                                                                                            DEFAULT_PAGE_SIZE);

    public static PageRequest createPageRequest(Integer page, Integer size) {

        if (page == DEFAULT_PAGE_NUMBER && size == DEFAULT_PAGE_SIZE) {
            return DEFAULT_PAGE_REQUEST;
        }
        return PageRequest.of(page != null ? page : DEFAULT_PAGE_NUMBER, size != null ? size : DEFAULT_PAGE_SIZE);
    }

    public static <T> ResponseEntity<List<T>> createAppropriateResponseEntity(List<T> entities) {
        if (entities != null && !entities.isEmpty()) {
            return new ResponseEntity<>(entities, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
