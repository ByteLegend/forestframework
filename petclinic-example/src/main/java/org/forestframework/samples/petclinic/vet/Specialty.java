package org.forestframework.samples.petclinic.vet;

import org.forestframework.samples.petclinic.model.NamedEntity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * Models a {@link Vet Vet's} specialty (for example, dentistry).
 *
 * @author Juergen Hoeller
 */
@Entity
@Table(name = "specialties")
public class Specialty extends NamedEntity implements Serializable {

}
