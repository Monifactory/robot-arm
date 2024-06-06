package screret.robotarm.pipenet.amhs.op;

import screret.robotarm.entity.FOUPCartEntity;

import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2023/8/13
 * @implNote OPFactory
 */
public enum OP {
    MOVE(MoveOp::new),
    AWAIT(AwaitOp::new);

    final Function<FOUPCartEntity, FOUPOp> factory;

    OP(Function<FOUPCartEntity, FOUPOp> factory) {
        this.factory = factory;
    }

    FOUPOp create(FOUPCartEntity cart) {
        return factory.apply(cart);
    }

}
