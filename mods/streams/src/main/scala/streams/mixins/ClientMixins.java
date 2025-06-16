package streams.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.*;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.neoforged.neoforge.client.extensions.common.*;
import net.neoforged.neoforge.fluids.*;
import org.spongepowered.asm.mixin.*;
import streams.*;

@Mixin(RegisterClientExtensionsEvent.class)
abstract class ClientExtensionsEventMixins {
    @WrapMethod(method = "registerFluidType(" +
        "Lnet/neoforged/neoforge/client/extensions/common/IClientFluidTypeExtensions;" +
        "[Lnet/neoforged/neoforge/fluids/FluidType;)V")
    private void registerFluidType(IClientFluidTypeExtensions extensions, FluidType[] types, Operation<Void> original) {
        original.call(StreamsMod.fluidExtensions(extensions, types), types);
    }
}
