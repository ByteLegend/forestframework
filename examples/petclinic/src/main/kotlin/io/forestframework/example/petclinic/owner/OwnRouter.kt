package io.forestframework.example.petclinic.owner

// import io.vertx.ext.web.RoutingContext
// import io.forestframework.core.http.param.Form
// import io.forestframework.core.http.routing.Get
// import io.forestframework.core.http.param.PathParam
// import io.forestframework.core.http.routing.Post
// import io.forestframework.example.petclinic.visit.VisitRepository
// import javax.inject.Inject
// import javax.validation.Valid
//
// class OwnRouter @Inject constructor(val owners: OwnerRepository, val visits: VisitRepository) {
//
//    private val VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm"
//
// //    @InitBinder
// //    fun setAllowedFields(dataBinder: WebDataBinder) {
// //        dataBinder.setDisallowedFields("id")
// //    }
//
//    @Get("/owners/new")
//    fun initCreationForm(routingContext: RoutingContext) = VIEWS_OWNER_CREATE_OR_UPDATE_FORM.also { routingContext.put("owner", Owner()) }
//
//    @Post("/owners/new")
//    suspend fun processCreationForm(owner: @Valid Form<Owner>): String? {
//        return if (owner.hasErrors()) {
//            VIEWS_OWNER_CREATE_OR_UPDATE_FORM
//        } else {
//            owners.save(owner.data)
//            "redirect:/owners/" + owner.data.id
//        }
//    }
//
//    @Get("/owners/find")
//    fun initFindForm(routingContext: RoutingContext) = "owners/findOwners".also { routingContext.put("owner", Owner()) }
//
//    @Get("/owners")
//    suspend fun processFindForm(owner: Owner, routingContext: RoutingContext): String? {
//
//        // allow parameterless GET request for /owners to return all records
//        var owner: Owner = owner
//        if (owner.getLastName() == null) {
//            owner.setLastName("") // empty string signifies broadest possible search
//        }
//
//        // find owners by last name
//        val results: Collection<Owner> = owners.findByLastName(owner.getLastName())
//        return if (results.isEmpty()) {
//            // no owners found
// //            result.rejectValue("lastName", "notFound", "not found")
//            "owners/findOwners"
//        } else if (results.size == 1) {
//            // 1 owner found
//            owner = results.iterator().next()
//            "redirect:/owners/" + owner.getId()
//        } else {
//            // multiple owners found
//            routingContext.put("selections", results)
//            "owners/ownersList"
//        }
//    }
//
//    @Get("/owners/{ownerId}/edit")
//    suspend fun initUpdateOwnerForm(@PathParam("ownerId") ownerId: Int, routingContext: RoutingContext) = VIEWS_OWNER_CREATE_OR_UPDATE_FORM.also {
//        routingContext.put("owner", owners.findById(ownerId))
//    }
//
//    @Post("/owners/{ownerId}/edit")
//    suspend fun processUpdateOwnerForm(owner: @Valid Form<Owner>,
//                                       @PathParam("ownerId") ownerId: Int): String {
//        return if (owner.hasErrors()) {
//            VIEWS_OWNER_CREATE_OR_UPDATE_FORM
//        } else {
//            owner.data.id = ownerId
//            owners.save(owner.data)
//            "redirect:/owners/{ownerId}"
//        }
//    }
//
//    /**
//     * Custom handler for displaying an owner.
//     * @param ownerId the ID of the owner to display
//     * @return a ModelMap with the model attributes for the view
//     */
//    @Get("/owners/{ownerId}")
//    suspend fun showOwner(@PathParam("ownerId") ownerId: Int, routingContext: RoutingContext) = "owners/ownerDetails".also {
//        val owner: Owner = owners.findById(ownerId)
//        owner.pets.forEach { pet -> pet.setVisitsInternal(visits.findByPetId(pet.id)) }
//        routingContext.put("owner", owner)
//    }
// }
//
//
