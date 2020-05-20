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

package com.ebivariation.contigalias.service;

import com.ebivariation.contigalias.dao.AssemblyDao;
import com.ebivariation.contigalias.entities.AssemblyEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AssemblyService {

    private final AssemblyDao assemblyDao;

    @Autowired
    public AssemblyService(@Qualifier("ftpDao") AssemblyDao assemblyDao) {
        this.assemblyDao = assemblyDao;
    }

    public AssemblyEntity getAssemblyByAccession(String accession) throws IOException {
        AssemblyEntity assembly = assemblyDao.getAssemblyByAccession(accession);
        assembly.getChromosomes().forEach(it -> it.setAssembly(null));
        return assembly;
    }

}
