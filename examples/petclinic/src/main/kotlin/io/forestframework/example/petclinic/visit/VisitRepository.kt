package io.forestframework.example.petclinic.visit

import com.google.inject.ImplementedBy

/**
 * Repository class for `Visit` domain objects All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
@ImplementedBy(JdbcVisitRepository::class)
interface VisitRepository { // : Repository<Visit?, Int?> {
    /**
     * Save a `Visit` to the data store, either inserting or updating it.
     * @param visit the `Visit` to save
     * @see BaseEntity.isNew
     */
//    @Throws(DataAccessException::class)
    suspend fun save(visit: Visit?)
    suspend fun findByPetId(petId: Int?): List<Visit?>?
}

class JdbcVisitRepository : VisitRepository {
    override suspend fun save(visit: Visit?) {
        TODO("Not yet implemented")
    }

    override suspend fun findByPetId(petId: Int?): List<Visit?>? {
        TODO("Not yet implemented")
    }
}
