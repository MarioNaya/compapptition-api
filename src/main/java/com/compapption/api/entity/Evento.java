package com.compapption.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table
public class Evento {

    @Id
    private Long id;

    //TODO [Reverse Engineering] generate columns from DB
}