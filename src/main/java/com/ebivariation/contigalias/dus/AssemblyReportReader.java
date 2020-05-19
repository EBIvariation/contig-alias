package com.ebivariation.contigalias.dus;

import com.ebivariation.contigalias.entities.AssemblyEntity;
import com.ebivariation.contigalias.entities.ChromosomeEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class AssemblyReportReader {

    private final BufferedReader reader;

    private AssemblyEntity assemblyEntity;

    private boolean reportParsed = false;

    public AssemblyReportReader(InputStreamReader inputStreamReader) {
        reader = new BufferedReader(inputStreamReader);
    }

    public AssemblyEntity getAssemblyEntity() throws IOException {
        if (!reportParsed || assemblyEntity == null) {
            parseReport();
        }
        return assemblyEntity;
    }

    private void parseReport() throws IOException {
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith("# ")) {
                if (assemblyEntity == null) {
                    assemblyEntity = new AssemblyEntity();
                }
                parseAssemblyData(line);
            } else if (!line.startsWith("#")) {
                parseChromosomeLine(line);
            }
            line = reader.readLine();
        }
        reportParsed = true;
    }

    private void parseAssemblyData(String line) {
        int tagEnd = line.indexOf(':');
        if (tagEnd == -1) {
            return;
        }
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
            case "RefSeq assembly and GenBank assemblies identical": {
                assemblyEntity.setGenbankRefseqIdentical(!tagData.equals("no"));
                break;
            }
        }
    }

    private void parseChromosomeLine(String line) {

        String[] columns = line.split("\t", -1);

        if (columns.length < 6 || !columns[3].equals("Chromosome")) {
            return;
        }

        ChromosomeEntity chromosomeEntity = new ChromosomeEntity();

        chromosomeEntity.setName(columns[0]);
        chromosomeEntity.setGenbank(columns[4]);
        chromosomeEntity.setRefseq(columns[6]);

        if (assemblyEntity == null) {
            assemblyEntity = new AssemblyEntity();
        }
        chromosomeEntity.setAssembly(this.assemblyEntity);

        List<ChromosomeEntity> chromosomes = this.assemblyEntity.getChromosomes();
        if (chromosomes == null) {
            chromosomes = new LinkedList<>();
            assemblyEntity.setChromosomes(chromosomes);
        }
        chromosomes.add(chromosomeEntity);
    }

    public long getLineCount() {
        return reader.lines().count();
    }
}
