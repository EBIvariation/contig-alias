/*
 * Copyright 2021 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.contigalias.datasource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.contigalias.dus.AssemblyReportReader;
import uk.ac.ebi.eva.contigalias.dus.AssemblyReportReaderFactory;
import uk.ac.ebi.eva.contigalias.dus.ENABrowser;
import uk.ac.ebi.eva.contigalias.dus.ENABrowserFactory;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Repository("ENADataSource")
public class ENAAssemblyDataSource implements AssemblyDataSource {

    private final ENABrowserFactory factory;

    private final AssemblyReportReaderFactory readerFactory;

    @Autowired
    public ENAAssemblyDataSource(ENABrowserFactory factory,
                                 AssemblyReportReaderFactory readerFactory) {
        this.factory = factory;
        this.readerFactory = readerFactory;
    }

    @Override
    public Optional<AssemblyEntity> getAssemblyByAccession(
            String accession) throws IOException, IllegalArgumentException {
        ENABrowser enaBrowser = factory.build();
        enaBrowser.connect();

        AssemblyEntity assemblyEntity;
        try (InputStream stream = enaBrowser.getAssemblyReportInputStream(accession)) {
            AssemblyReportReader reader = readerFactory.build(stream);
            assemblyEntity = reader.getAssemblyEntity();
        } finally {
            enaBrowser.disconnect();
        }
        return Optional.of(assemblyEntity);
    }

}
