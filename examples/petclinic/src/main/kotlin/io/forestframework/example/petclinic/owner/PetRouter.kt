package io.forestframework.example.petclinic.owner
//
// //import io.forestframework.core.http.param.RequestBody
// //import io.forestframework.core.http.routing.Route
// import io.forestframework.core.http.Router
// import io.forestframework.core.http.param.ContextData
// import io.forestframework.core.http.param.Form
// import io.forestframework.core.http.param.PathParam
// import io.forestframework.core.http.routing.Get
// import io.forestframework.core.http.routing.Intercept
// import io.forestframework.core.http.routing.Post
// import io.vertx.ext.web.RoutingContext
// import javax.inject.Inject
// import javax.validation.Valid
//
// @Router("/owners/:ownerId")
// class PetRouter @Inject constructor(private val pets: PetRepository, private val owners: OwnerRepository) {
//    private val VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm"
//
//
//    @Intercept("/*")
//    suspend fun intercept(@PathParam("ownerId") ownerId: Int, routingContext: RoutingContext) {
//        routingContext.put("owner", owners.findById(ownerId))
//    }
//
// //    @ModelAttribute("types")
// //    fun populatePetTypes(): Collection<PetType> {
// //        return pets.findPetTypes()
// //    }
//
// //    @ModelAttribute("owner")
// //    fun findOwner(@PathVariable("ownerId") ownerId: Int): Owner {
// //        return owners.findById(ownerId)
// //    }
//
//    //    @InitBinder("owner")
// //    fun initOwnerBinder(dataBinder: WebDataBinder) {
// //        dataBinder.setDisallowedFields("id")
// //    }
// //
// //    @InitBinder("pet")
// //    fun initPetBinder(dataBinder: WebDataBinder) {
// //        dataBinder.setValidator(PetValidator())
// //    }
// //
//    @Get("/pets/new")
//    fun initCreationForm(owner: Owner, routingContext: RoutingContext) = VIEWS_PETS_CREATE_OR_UPDATE_FORM.also {
//        Pet().also { pet ->
//            owner.addPet(pet)
//            routingContext.put("pet", pet)
//        }
//    }
//
//    @Post("/pets/new")
//    suspend fun processCreationForm(@ContextData("owner") owner: Owner,
//                                    pet: @Valid Form<Pet>,
//                                    routingContext: RoutingContext): String {
// //        if (StringUtils.hasLength(pet!!.name) && pet.isNew && owner.getPet(pet.name, true) != null) {
// //            result.rejectValue("name", "duplicate", "already exists")
// //        }
//        owner.addPet(pet.data)
//        return if (pet.hasErrors()) {
//            routingContext.put("pet", pet)
//            VIEWS_PETS_CREATE_OR_UPDATE_FORM
//        } else {
//            pets.save(pet.data)
//            routingContext.reroute("/owners/${owner.id}")
//            return "REROUTED"
//        }
//    }
//
//    @Get("/pets/:petId/edit")
//    suspend fun initUpdateForm(@PathParam("petId") petId: Int, routingContext: RoutingContext) = VIEWS_PETS_CREATE_OR_UPDATE_FORM.also {
//        routingContext.put("pet", pets.findById(petId))
//    }
//
//    @Post("/pets/:petId/edit")
//    suspend fun processUpdateForm(pet: @Valid Form<Pet>, @ContextData("owner") owner: Owner, routingContext: RoutingContext): String {
//        return if (pet.hasErrors()) {
//            pet.data.owner = owner
//            routingContext.put("pet", pet)
//            VIEWS_PETS_CREATE_OR_UPDATE_FORM
//        } else {
//            owner.addPet(pet.data)
//            pets.save(pet.data)
//            routingContext.reroute("/owners/${owner.id}")
//            "REROUTED"
//        }
//    }
// }
