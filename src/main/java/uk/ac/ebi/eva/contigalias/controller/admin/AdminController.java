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

package uk.ac.ebi.eva.contigalias.controller.admin;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/v1/admin")
@RestController
public class AdminController {

    private final AdminHandler handler;

    public AdminController(AdminHandler handler) {
        this.handler = handler;
    }

    @ApiOperation(value = "Fetch an assembly from remote server using its INSDC or RefSeq accession and insert " +
            "into local database.",
            notes = "Given an assembly's accession, this endpoint will fetch and add the assembly that matches that " +
                    "accession into the local database. The accession can be either a INSDC or a RefSeq accession " +
                    "and the endpoint will automatically fetch the correct assembly from remote server. It will first" +
                    " search for the target assembly in the local database as trying to insert an assembly which " +
                    "already exists in the database is prohibited. If such an assembly is not found locally then it " +
                    "will look for it at a remote source (NCBI by default). If the desired assembly is found at the " +
                    "remote source, it will fetch it and add it to the local database. This endpoint does not return " +
                    "any data except an HTTP status code of 400 in case the user tries to insert an assembly that " +
                    "already exists in the local database.")
    @PutMapping(value = "assemblies/{accession}")
    public ResponseEntity<?> fetchAndInsertAssemblyByAccession(
            @PathVariable(name = "accession") @ApiParam(value = "INSDC or RefSeq assembly accession. Eg: " +
                    "GCA_000001405.10") String asmAccession) throws IOException {
        try {
            handler.fetchAndInsertAssemblyByAccession(asmAccession);
            // submit jobs for updating ena sequence name and md5 checksum for assembly
            handler.retrieveAndInsertENASequenceNameForAssembly(asmAccession);
            handler.retrieveAndInsertMd5ChecksumForAssembly(asmAccession);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Fetch assemblies from remote server using their INSDC or RefSeq accessions and insert " +
            "into local database.",
            notes = "Given a list of assembly accessions, for every accession in the list this endpoint will fetch " +
                    "and add the assembly that matches that accession into the local database. The accession can be " +
                    "either a INSDC or RefSeq accession and the endpoint will automatically fetch the correct " +
                    "assembly from remote server. It will first search for the target assembly in the local database " +
                    "as trying to insert an assembly which already exists in the database is prohibited. If such an " +
                    "assembly is not found locally then it will look for it at a remote source (NCBI by default). If " +
                    "desired assembly is found at remote source, it will fetch and add it to the local database. This" +
                    " endpoint does not return any data and processes elements in the given list in an asynchronous " +
                    "parallel manner.")
    @PutMapping(value = "assemblies")
    public ResponseEntity<?> fetchAndInsertAssemblyByAccession(
            @RequestBody @ApiParam(value = "A JSON array of INSDC or RefSeq assembly accessions. " +
                    "Eg: [\"GCA_000001405.10\",\"GCA_000001405.11\",\"GCA_000001405.12\"]") List<String> accessions) {
        if (accessions == null || accessions.size() <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Map<String, List<String>> accessionResult = handler.fetchAndInsertAssemblyByAccession(accessions);
        // submit jobs for updating ena sequence names and md5 checksum for all successfully inserted assemblies
        if (accessionResult.get("SUCCESS").size() > 0) {
            handler.retrieveAndInsertENASequenceNameForAssembly(accessionResult.get("SUCCESS"));
            handler.retrieveAndInsertMd5ChecksumForAssembly(accessionResult.get("SUCCESS"));
        }
        return new ResponseEntity<>("Accession Processing Result : " + accessionResult, HttpStatus.MULTI_STATUS);
    }

    @ApiOperation(value = "Given an assembly accession, retrieve MD5 checksum for all chromosomes belonging to assembly and update")
    @PutMapping(value = "assemblies/md5checksum/{accession}")
    public ResponseEntity<String> retrieveAndInsertMd5ChecksumForAssembly(@PathVariable(name = "accession")
                                                                          @ApiParam(value = "INSDC or RefSeq assembly accession. Eg: " +
                                                                                  "GCA_000001405.10") String asmAccession) {
        Optional<AssemblyEntity> assemblyOpt = handler.getAssemblyByAccession(asmAccession);
        if (assemblyOpt.isPresent()) {
            handler.retrieveAndInsertMd5ChecksumForAssembly(assemblyOpt.get().getInsdcAccession());
            return ResponseEntity.ok("A task has been submitted for updating md5checksum for assembly " + asmAccession
                    + "\nDepending upon the size of assembly and other scheduled jobs, this might take some time to complete");
        } else {
            return ResponseEntity.ok("Could not find assembly " + asmAccession +
                    ". Please insert the assembly first. MD5 checksum will be updated as part of the insertion process");
        }
    }

    @ApiOperation(value = "Given a list of assembly accessions, retrieve MD5 checksum for all chromosomes belonging to all the assemblies and update")
    @PutMapping(value = "assemblies/md5checksum")
    public ResponseEntity<String> retrieveAndInsertMd5ChecksumForAssembly(
            @RequestBody @ApiParam(value = "A JSON array of INSDC or RefSeq assembly accessions. " +
                    "Eg: [\"GCA_000001405.10\",\"GCA_000001405.11\",\"GCA_000001405.12\"]") List<String> accessions) {
        if (accessions == null || accessions.size() <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<String> asmInsdcAccessionsList = new ArrayList<>();
        List<String> asmNotPresent = new ArrayList<>();
        for (String accession : accessions) {
            Optional<AssemblyEntity> assemblyOpt = handler.getAssemblyByAccession(accession);
            if (assemblyOpt.isPresent()) {
                asmInsdcAccessionsList.add(assemblyOpt.get().getInsdcAccession());
            } else {
                asmNotPresent.add(accession);
            }
        }

        handler.retrieveAndInsertMd5ChecksumForAssembly(asmInsdcAccessionsList);

        accessions.removeAll(asmNotPresent);
        String responseText = "A task has been submitted for updating MD5 checksum for assemblies: " + accessions + "."
                + "\nDepending upon other scheduled jobs and the size of assembly, this might take some time to complete";
        if (!asmNotPresent.isEmpty()) {
            responseText = responseText + "\nThe following assemblies are not present: " + asmNotPresent + "."
                    + "\nPlease insert the assembly first, MD5 Checksum will be updated as part of the insertion process";
        }

        return ResponseEntity.ok(responseText);
    }

    @ApiOperation(value = "Given an assembly accession, retrieve ENA sequence name for all chromosomes belonging to assembly and update")
    @PutMapping(value = "assemblies/ena-sequence-name/{accession}")
    public ResponseEntity<String> retrieveAndInsertENASequenceNameForAssembly(@PathVariable(name = "accession")
                                                                              @ApiParam(value = "INSDC or RefSeq assembly accession. " +
                                                                                      "Eg: GCA_000001405.10") String asmAccession) {
        Optional<AssemblyEntity> assemblyOpt = handler.getAssemblyByAccession(asmAccession);
        if (assemblyOpt.isPresent()) {
            handler.retrieveAndInsertENASequenceNameForAssembly(assemblyOpt.get().getInsdcAccession());
            return ResponseEntity.ok("A task has been submitted for updating ENA Sequence Name for assembly " + asmAccession
                    + "\nDepending upon the size of assembly and other scheduled jobs, this might take some time to complete");
        } else {
            return ResponseEntity.ok("Could not find assembly " + asmAccession +
                    ". Please insert the assembly first. ENA sequence name will be updated as part of the insertion process");
        }
    }

    @ApiOperation(value = "Given a list of assembly accessions, retrieve ENA sequence name for all chromosomes belonging to all the assemblies and update")
    @PutMapping(value = "assemblies/ena-sequence-name")
    public ResponseEntity<String> retrieveAndInsertENASequenceNameForAssembly(
            @RequestBody @ApiParam(value = "A JSON array of INSDC or RefSeq assembly accessions. " +
                    "Eg: [\"GCA_000001405.10\",\"GCA_000001405.11\",\"GCA_000001405.12\"]") List<String> accessions) {
        if (accessions == null || accessions.size() <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<String> asmInsdcAccessionsList = new ArrayList<>();
        List<String> asmNotPresent = new ArrayList<>();
        for (String accession : accessions) {
            Optional<AssemblyEntity> assemblyOpt = handler.getAssemblyByAccession(accession);
            if (assemblyOpt.isPresent()) {
                asmInsdcAccessionsList.add(assemblyOpt.get().getInsdcAccession());
            } else {
                asmNotPresent.add(accession);
            }
        }

        handler.retrieveAndInsertENASequenceNameForAssembly(asmInsdcAccessionsList);

        accessions.removeAll(asmNotPresent);
        String responseText = "A task has been submitted for updating ENA Sequence Name for assemblies: " + accessions
                + "\nDepending upon other scheduled jobs and the size of assembly, this might take some time to complete";
        if (!asmNotPresent.isEmpty()) {
            responseText = responseText + "\nThe following assemblies are not present: " + asmNotPresent + "."
                    + "\nPlease insert the assembly first, ENA Sequence Name will be updated as part of the insertion process";
        }

        return ResponseEntity.ok(responseText);
    }


    @ApiOperation(value = "Retrieve list of Jobs that are running or scheduled to run")
    @GetMapping(value = "assemblies/scheduled-jobs")
    public ResponseEntity<List<String>> getMD5ChecksumUpdateTaskStatus() {
        List<String> scheduledJobStatus = handler.getScheduledJobStatus();
        return ResponseEntity.ok(scheduledJobStatus);
    }

//    This endpoint can be enabled in the future when checksums for assemblies are added to the project.
//    @ApiOperation(value = "Add MD5 and TRUNC512 checksums to an assembly by accession.",
//            notes = "Given an INSDC or RefSeq accession along with a MD5 or a TRUNC512 checksum, this endpoint will
//            " +
//                    "add the given checksums to the assembly that matches the given INSDC or RefSeq accession.")
//    @PutMapping(value = "assemblies/{accession}/checksum")
//    public void putAssemblyChecksumsByAccession(
//            @PathVariable @ApiParam(value = "INSDC or Refseq assembly accession. Eg: GCA_000001405.10") String
//            accession,
//            @RequestParam(required = false) @ApiParam("The MD5 checksum associated with the assembly.") String md5,
//            @RequestParam(required = false) @ApiParam("The TRUNC512 checksum associated with the assembly.") String
//                    trunc512) {
//        handler.putAssemblyChecksumsByAccession(accession, md5, trunc512);
//    }

    @ApiOperation(value = "Add MD5 and TRUNC512 checksums to all chromosomes by accession.",
            notes = "Given an INSDC or RefSeq accession along with a MD5 or a TRUNC512 checksum, this endpoint will " +
                    "add the given checksums to all chromosomes that match the given INSDC or RefSeq accession.")
    @PutMapping(value = "chromosomes/{accession}/checksum")
    public void putChromosomeChecksumsByAccession(
            @PathVariable @ApiParam(value = "INSDC or Refseq chromosome accession. Eg: NC_000001.11") String accession,
            @RequestParam(required = false) @ApiParam("The MD5 checksum associated with the chromosomes.") String md5,
            @RequestParam(required = false) @ApiParam("The TRUNC512 checksum associated with the chromosomes.") String trunc512) {
        handler.putChromosomeChecksumsByAccession(accession, md5, trunc512);
    }

    @ApiOperation(value = "Delete an assembly from local database using its INSDC or RefSeq accession.",
            notes = "Given an assembly's accession this endpoint will delete the assembly that matches that " +
                    "accession from the local database. The accession can be either a INSDC or RefSeq accession and" +
                    " the endpoint will automatically deletes the correct assembly from the database. Deleting an " +
                    "assembly also deletes all sequences that are associated with that assembly. This endpoint does" +
                    " not return any data.")
    @DeleteMapping(value = "assemblies/{accession}")
    public ResponseEntity<String> deleteAssemblyByAccession(
            @PathVariable(name = "accession") @ApiParam(value = "INSDC or RefSeq assembly accession. Eg: " +
                    "GCA_000001405.10") String asmAccession) {
        Optional<AssemblyEntity> assemblyOpt = handler.getAssemblyByAccession(asmAccession);
        if (assemblyOpt.isPresent()) {
            try {
                handler.deleteAssemblyByAccession(assemblyOpt.get().getInsdcAccession());
                return ResponseEntity.ok("Assembly Deleted Successfully");
            } catch (Exception e) {
                return new ResponseEntity<>("There was an error deleting assembly. " +
                        "The DB might be in inconsistent state, it is advised to retry deleting",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return ResponseEntity.ok("Could not find the requested assembly.");
        }
    }

}
