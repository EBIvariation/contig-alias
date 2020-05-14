package com.ebivariation.contigalias.entities;

import java.util.List;

public class AssemblyEntity {

    private long id;
    private String name;
    private String organism;
    private long taxid;
    private String genbank;
    private String refseq;

    private List<ScaffoldEntity> scaffolds;

}
