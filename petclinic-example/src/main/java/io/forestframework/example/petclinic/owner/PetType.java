package io.forestframework.example.petclinic.owner;

import io.forestframework.example.petclinic.model.NamedEntity;

import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * @author Juergen Hoeller Can be Cat, Dog, Hamster...
 */
@Entity
@Table(name = "types")
public class PetType extends NamedEntity {

}
