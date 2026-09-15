package io.github.mortuusars.exposure.fabric.mixin.create;

//@Pseudo
//@Mixin(value = AllRecipeTypes.class, remap = false)
//public class RecipeTypesMixin {
//    @Inject(method = "shouldIgnoreInAutomation", at = @At("HEAD"), cancellable = true)
//    private static void onShouldIgnoreInAutomation(Recipe<?> recipe, CallbackInfoReturnable<Boolean> cir) {
//        if (recipe.getSerializer().equals(Exposure.RecipeSerializers.FILM_DEVELOPING.get()) ||
//                recipe.getSerializer().equals(Exposure.RecipeSerializers.PHOTOGRAPH_CLONING.get()) ||
//                recipe.getSerializer().equals(Exposure.RecipeSerializers.PHOTOGRAPH_AGING.get()))
//            cir.setReturnValue(true);
//    }
//}
