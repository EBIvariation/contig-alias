package uk.ac.ebi.eva.contigalias.entities;

import net.minidev.json.JSONObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SeqCol {
    @Id
    private String digest;
    @Column
    private JSONObject object;
}
