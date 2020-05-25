package org.forestframework.samples.petclinic.owner

import com.google.inject.Inject
import io.vertx.ext.web.RoutingContext
import org.forestframework.Form
import org.forestframework.annotation.ContextData
import org.forestframework.annotation.Get
import org.forestframework.annotation.Intercept
import org.forestframework.annotation.PathParam
import org.forestframework.annotation.Post
import org.forestframework.annotation.RequestBody
import org.forestframework.annotation.Router
import javax.validation.Valid

@Router("/owners/:ownerId")
class PetRouter @Inject constructor(private val pets: PetRepository, private val owners: OwnerRepository) {
    private val VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm"


    @Intercept("/*")
    suspend fun intercept(@PathParam("ownerId") ownerId: Int, routingContext: RoutingContext) {
        routingContext.put("owner", owners.findById(ownerId))
    }

//    @ModelAttribute("types")
//    fun populatePetTypes(): Collection<PetType> {
//        return pets.findPetTypes()
//    }

//    @ModelAttribute("owner")
//    fun findOwner(@PathVariable("ownerId") ownerId: Int): Owner {
//        return owners.findById(ownerId)
//    }

    //    @InitBinder("owner")
//    fun initOwnerBinder(dataBinder: WebDataBinder) {
//        dataBinder.setDisallowedFields("id")
//    }
//
//    @InitBinder("pet")
//    fun initPetBinder(dataBinder: WebDataBinder) {
//        dataBinder.setValidator(PetValidator())
//    }
//
    @Get("/pets/new")
    fun initCreationForm(owner: Owner, routingContext: RoutingContext) = VIEWS_PETS_CREATE_OR_UPDATE_FORM.also {
        Pet().also { pet ->
            owner.addPet(pet)
            routingContext.put("pet", pet)
        }
    }

    @Post("/pets/new")
    suspend fun processCreationForm(@ContextData("owner") owner: Owner,
                                    pet: @Valid Form<Pet>,
                                    routingContext: RoutingContext): String {
//        if (StringUtils.hasLength(pet!!.name) && pet.isNew && owner.getPet(pet.name, true) != null) {
//            result.rejectValue("name", "duplicate", "already exists")
//        }
        owner.addPet(pet.data)
        return if (pet.hasErrors()) {
            routingContext.put("pet", pet)
            VIEWS_PETS_CREATE_OR_UPDATE_FORM
        } else {
            pets.save(pet.data)
            routingContext.reroute("/owners/${owner.id}")
            return "REROUTED"
        }
    }

    @Get("/pets/:petId/edit")
    suspend fun initUpdateForm(@PathParam("petId") petId: Int, routingContext: RoutingContext) = VIEWS_PETS_CREATE_OR_UPDATE_FORM.also {
        routingContext.put("pet", pets.findById(petId))
    }

    @Post("/pets/:petId/edit")
    suspend fun processUpdateForm(pet: @RequestBody @Valid Form<Pet>, @ContextData("owner") owner: Owner, routingContext: RoutingContext): String {
        return if (pet.hasErrors()) {
            pet.data.owner = owner
            routingContext.put("pet", pet)
            VIEWS_PETS_CREATE_OR_UPDATE_FORM
        } else {
            owner.addPet(pet.data)
            pets.save(pet.data)
            routingContext.reroute("/owners/${owner.id}")
            "REROUTED"
        }
    }
}
