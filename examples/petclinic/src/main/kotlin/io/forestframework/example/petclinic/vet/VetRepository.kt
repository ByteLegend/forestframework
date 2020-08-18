package io.forestframework.example.petclinic.vet

import com.google.inject.ImplementedBy

@ImplementedBy(JdbcVetRepository::class)
interface VetRepository {
    /**
     * Retrieve all `Vet`s from the data store.
     * @return a `Collection` of `Vet`s
     */
//    @Transactional(readOnly = true)
//    @Cacheable("vets")
//    @Throws(DataAccessException::class)
    suspend fun findAll(): Collection<Vet>
}

class JdbcVetRepository : VetRepository {
    override suspend fun findAll(): Collection<Vet> {
        TODO("Not yet implemented")
    }
}
