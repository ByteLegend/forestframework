package org.forestframework.samples.petclinic

import org.forestframework.Forest
import org.forestframework.annotation.ForestApplication


@ForestApplication
class PetClinicApplication

fun main() {
    Forest.run(PetClinicApplication::class.java)
}