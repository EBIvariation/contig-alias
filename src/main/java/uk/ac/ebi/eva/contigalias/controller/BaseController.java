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

package uk.ac.ebi.eva.contigalias.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;

import java.util.List;
import java.util.Optional;

public class BaseController {

    public static final int DEFAULT_PAGE_NUMBER = 0;

    public static final int DEFAULT_PAGE_SIZE = 10;

    public static final String API_PARAM_VALUE_PAGE_NUMBER = "Page number of required page. Page numbers start from 0" +
            ". If not specified then default page number is " + DEFAULT_PAGE_NUMBER + ".";

    public static final String API_PARAM_VALUE_PAGE_SIZE = "Page size of required pages. Page size should be greater " +
            "than 0. If not specified then default page size is " + DEFAULT_PAGE_SIZE + " results per page.";

    public static final PageRequest DEFAULT_PAGE_REQUEST = BaseController.createPageRequest(DEFAULT_PAGE_NUMBER,
                                                                                            DEFAULT_PAGE_SIZE);

    public static final ResponseEntity BAD_REQUEST
            = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    public static PageRequest createPageRequest(Integer page, Integer size) {

        int pagex = DEFAULT_PAGE_NUMBER, sizex = DEFAULT_PAGE_SIZE;

        if (page != null) {
            pagex = page;
        }
        if (size != null) {
            sizex = size;
        }

        // Even though this is redundant it is required for some integration tests to pass.
        if (pagex == DEFAULT_PAGE_NUMBER && sizex == DEFAULT_PAGE_SIZE) {
            return DEFAULT_PAGE_REQUEST;
        } else {
            return PageRequest.of(pagex, sizex);
        }
    }

    public static <T> ResponseEntity<List<T>> createAppropriateResponseEntity(List<T> entities) {
        if (entities != null && !entities.isEmpty()) {
            return new ResponseEntity<>(entities, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public static <T> ResponseEntity<T> createAppropriateResponseEntity(Optional<T> entities) {
        if (entities.isPresent()) {
            return new ResponseEntity<T>(entities.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public static boolean paramsValidForSingleResponseQuery(Integer page, Integer size) {
        return (page == null || page == 0) && (size == null || size > 1);
    }
}
