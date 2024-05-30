package screret.robotarm.block;

import net.minecraft.MethodsReturnNonnullByDefault;
import screret.robotarm.pipenet.amhs.AMHSRailType;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/8/9
 * @implNote NormalRailBlock
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NormalRailBlock extends AMHSRailBlock {

    public NormalRailBlock(Properties properties) {
        super(properties, AMHSRailType.NORMAL);
    }

}
