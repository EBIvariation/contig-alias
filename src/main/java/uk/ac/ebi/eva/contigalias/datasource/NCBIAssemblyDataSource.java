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

package uk.ac.ebi.eva.contigalias.datasource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.contigalias.dus.NCBIAssemblyReportReader;
import uk.ac.ebi.eva.contigalias.dus.NCBIAssemblyReportReaderFactory;
import uk.ac.ebi.eva.contigalias.dus.NCBIBrowser;
import uk.ac.ebi.eva.contigalias.dus.NCBIBrowserFactory;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Repository("NCBIDataSource")
public class NCBIAssemblyDataSource implements AssemblyDataSource {

    private final NCBIBrowserFactory factory;

    private final NCBIAssemblyReportReaderFactory readerFactory;

    @Autowired
    public NCBIAssemblyDataSource(NCBIBrowserFactory factory,
                                  NCBIAssemblyReportReaderFactory readerFactory) {
        this.factory = factory;
        this.readerFactory = readerFactory;
    }

    @Override
    public Optional<AssemblyEntity> getAssemblyByAccession(
            String accession) throws IOException, IllegalArgumentException {
        NCBIBrowser ncbiBrowser = factory.build();
        ncbiBrowser.connect();
        Optional<String> directory = ncbiBrowser.getGenomeReportDirectory(accession);
        if (!directory.isPresent()) {
            return Optional.empty();
        }
        AssemblyEntity assemblyEntity;
        try (InputStream stream = ncbiBrowser.getAssemblyReportInputStream(directory.get())) {
            NCBIAssemblyReportReader reader = readerFactory.build(stream);
            assemblyEntity = reader.getAssemblyEntity();
        } finally {
            ncbiBrowser.disconnect();
        }
        return Optional.of(assemblyEntity);
    }

}
