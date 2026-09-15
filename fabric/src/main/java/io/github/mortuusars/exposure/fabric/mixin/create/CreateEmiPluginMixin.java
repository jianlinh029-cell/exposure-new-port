package io.github.mortuusars.exposure.fabric.mixin.create;

/**
 * Yes, it's ugly.
 */
//@Mixin(value = CreateEmiPlugin.class, remap = false)
//public abstract class CreateEmiPluginMixin {
//    @Inject(method = "register(Ldev/emi/emi/api/EmiRegistry;)V", at = @At("RETURN"))
//    public void onRegister(EmiRegistry registry, CallbackInfo ci) {
//        registry.addRecipe(new SequencedAssemblyEmiRecipe(CreateFilmDeveloping.createSequencedDevelopingRecipe(FilmType.BLACK_AND_WHITE)));
//        registry.addRecipe(new SequencedAssemblyEmiRecipe(CreateFilmDeveloping.createSequencedDevelopingRecipe(FilmType.COLOR)));
//    }
//}
