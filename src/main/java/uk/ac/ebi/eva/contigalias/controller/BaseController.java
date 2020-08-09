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
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public class BaseController {

    public static final int DEFAULT_PAGE_NUMBER = 0;

    public static final int DEFAULT_PAGE_SIZE = 10;

    public static final String PAGE_NUMBER_DESCRIPTION = "You can provide a page index to return only a subset of" +
            " the data. Page numbers start from 0 and if not specified then default page number is 0.";

    public static final String PAGE_SIZE_DESCRIPTION = "You can provide a page size to return only a subset of"
            + " the data. Page size should be greater than 0 and if not specified then default page size is 10 " +
            "results per page.";

    public static final String PAGINATION_EXPLANATION = "This endpoint is paginated which means that all results are " +
            "not returned at once, instead a small subset is returned in the form of a page and the result needs to " +
            "be traversed through this set of pages. You can control pagination by specifying the index and size of " +
            "the page you want using the two optional parameters \"page\" and \"size\".";

    public static final String HATEOAS_EXPLANATION = "This endpoint also provided hyperlinks to other relevant " +
            "endpoints to help the user navigate the API with ease. These links are embedded inside an object called " +
            "\"_links\" which is present at the root level. Some information about pagination is also included in a " +
            "root level object called \"page\". Due to this, the actual result is not available at the root level but" +
            " is actually embedded in another root level element.";

    public static final PageRequest DEFAULT_PAGE_REQUEST = PageRequest.of(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);

    public static final String REL_CHROMOSOMES = "chromosomes";

    public static final String REL_ASSEMBLY = "assembly";

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
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    public static <T> ResponseEntity<T> createAppropriateResponseEntity(Optional<T> entities) {
        return entities.map(t -> new ResponseEntity<>(t, HttpStatus.OK)).orElseGet(
                () -> new ResponseEntity<>(HttpStatus.OK));
    }

    public static <T> ResponseEntity<PagedModel<EntityModel<T>>> createAppropriateResponseEntity(
            PagedModel<EntityModel<T>> entityModels) {
        return new ResponseEntity<>(entityModels, HttpStatus.OK);
    }

    public static boolean paramsValidForSingleResponseQuery(Integer page, Integer size) {
        return (page == null || page == 0) && (size == null || size > 1);
    }

}
