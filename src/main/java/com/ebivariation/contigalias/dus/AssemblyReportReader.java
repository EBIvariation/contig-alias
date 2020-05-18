package com.ebivariation.contigalias.dus;

import com.ebivariation.contigalias.entities.AssemblyEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class AssemblyReportReader extends BufferedReader {

    private final AssemblyEntity assemblyEntity = new AssemblyEntity();

    public AssemblyReportReader(Reader reader) {
        super(reader);
    }

    public AssemblyReportReader(InputStreamReader inputStreamReader) {
        super(new BufferedReader(inputStreamReader));
    }

    @Override
    public String readLine() throws IOException {
        String line = super.readLine();
        if (line == null) {
            return null;
        }

        if (line.startsWith("# ")) {
            parseAssemblyData(line);
        } else {
            parseScaffoldLine(line);
        }

        return line;
    }

    private void parseAssemblyData(String line) {
        int tagEnd = line.indexOf(':');
        if (tagEnd == -1) return;
        String tag = line.substring(2, tagEnd);
        String tagData = line.substring(tagEnd + 1).trim();
        switch (tag) {
            case "Assembly name": {
                assemblyEntity.setName(tagData);
                break;
            }
            case "Organism name": {
                assemblyEntity.setOrganism(tagData);
                break;
            }
            case "Taxid": {
                assemblyEntity.setTaxid(Long.parseLong(tagData));
                break;
            }
            case "GenBank assembly accession": {
                assemblyEntity.setGenbank(tagData);
                break;
            }
            case "RefSeq assembly accession": {
                assemblyEntity.setRefseq(tagData);
                break;
            }
        }
    }

    private void parseScaffoldLine(String line) {
        //TODO
    }
}
