package com.ebivariation.contigalias.dus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class AssemblyReportReader extends BufferedReader {

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

        if (line.startsWith("#")) {
            parseAssemblyData(line);
        } else {
            parseScaffoldLine(line);
        }

        return line;
    }

    private void parseAssemblyData(String line) {
        //TODO
    }

    private void parseScaffoldLine(String line) {
        //TODO
    }
}
