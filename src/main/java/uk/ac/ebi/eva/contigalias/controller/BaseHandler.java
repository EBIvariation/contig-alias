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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class BaseHandler {

    public static <T> Page<T> convertToPage(Optional<T> optional) {
        return new PageImpl<T>(convertToList(optional));
    }

    public static <T> List<T> convertToList(Optional<T> optional) {
        List<T> list;
        if (optional.isPresent()) {
            list = Collections.singletonList(optional.get());
        } else {
            list = Collections.emptyList();
        }
        return list;
    }

    public static <T> PagedModel<EntityModel<T>> assemblyPagedModelFromPage(
            Page<T> page,
            PagedResourcesAssembler<T> assembler) {
        return assembler.toModel(page);
    }


}
