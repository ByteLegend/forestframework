package io.forestframework.example.petclinic.owner

import com.google.inject.ImplementedBy

/**
 * Repository class for `Pet` domain objects All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
@ImplementedBy(JdbcPetRepository::class)
interface PetRepository { // : Repository<Pet?, Int?> {
    /**
     * Retrieve all [PetType]s from the data store.
     * @return a Collection of [PetType]s.
     */
//    @Query("SELECT ptype FROM PetType ptype ORDER BY ptype.name")
//    @Transactional(readOnly = true)
    suspend fun findPetTypes(): List<PetType>

    /**
     * Retrieve a [Pet] from the data store by id.
     * @param id the id to search for
     * @return the [Pet] if found
     */
//    @Transactional(readOnly = true)
    suspend fun findById(id: Int): Pet

    /**
     * Save a [Pet] to the data store, either inserting or updating it.
     * @param pet the [Pet] to save
     */
    suspend fun save(pet: Pet)
}

class JdbcPetRepository : PetRepository {
    override suspend fun findPetTypes(): List<PetType> {
        TODO("Not yet implemented")
    }

    override suspend fun findById(id: Int): Pet {
        TODO("Not yet implemented")
    }

    override suspend fun save(pet: Pet) {
        TODO("Not yet implemented")
    }
}
