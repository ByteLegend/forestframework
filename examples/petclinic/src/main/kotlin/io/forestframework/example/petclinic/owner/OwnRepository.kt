package io.forestframework.example.petclinic.owner

import com.google.inject.ImplementedBy

annotation class Param(val value: String)

@ImplementedBy(JdbcOwnerRepository::class)
interface OwnerRepository { // : Repository<Owner?, Int?> {
    /**
     * Retrieve [Owner]s from the data store by last name, returning all owners
     * whose last name *starts* with the given name.
     * @param lastName Value to search for
     * @return a Collection of matching [Owner]s (or an empty Collection if none
     * found)
     */
//    @Query("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName%")
//    @Transactional(readOnly = true)
    suspend fun findByLastName(@Param("lastName") lastName: String): Collection<Owner>

    /**
     * Retrieve an [Owner] from the data store by id.
     * @param id the id to search for
     * @return the [Owner] if found
     */
//    @Query("SELECT owner FROM Owner owner left join fetch owner.pets WHERE owner.id =:id")
//    @Transactional(readOnly = true)
    suspend fun findById(@Param("id") id: Int): Owner

    /**
     * Save an [Owner] to the data store, either inserting or updating it.
     * @param owner the [Owner] to save
     */
    suspend fun save(owner: Owner)
}

class JdbcOwnerRepository : OwnerRepository {
    override suspend fun findByLastName(lastName: String): Collection<Owner> {
        TODO("Not yet implemented")
    }

    override suspend fun findById(id: Int): Owner {
        TODO("Not yet implemented")
    }

    override suspend fun save(owner: Owner) {
        TODO("Not yet implemented")
    }
}
