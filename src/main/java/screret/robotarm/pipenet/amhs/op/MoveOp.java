package screret.robotarm.pipenet.amhs.op;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import screret.robotarm.entity.FOUPCartEntity;
import screret.robotarm.pipenet.amhs.AMHSRailNet;
import screret.robotarm.pipenet.amhs.AMHSRailNode;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/8/13
 * @implNote FOUPOp
 */
public class MoveOp extends FOUPOp {
    public BlockPos destination;
    // runtime
    protected List<AMHSRailNode> path;
    protected int index;
    protected float lerp;
    protected float gradientIn;
    protected float lastDegree;

    protected MoveOp(FOUPCartEntity cart) {
        super(cart);
    }

    public MoveOp(FOUPCartEntity cart, BlockPos destination) {
        super(cart);
        this.destination = destination;
    }

    @Override
    protected void onDone() {
        super.onDone();
        cart.syncCartPosAndRot();
    }

    public int getSpeedLerp() {
        return 5;
    }

    @Override
    public OP getType() {
        return OP.MOVE;
    }

    protected void setPath(List<AMHSRailNode> path) {
        this.path = path;
        this.index = 0;
        this.lastDegree = cart.getYRot();
        this.lerp = getSpeedLerp();
        this.gradientIn = this.lerp / 128;
    }

    protected boolean updateCart(AMHSRailNet net) {
        if (destination == null) return true;
        var cartPos = cart.getOnPos();
        if (path == null) {
            var fromNode = net.getNodeAt(cartPos);
            var toNode = net.getNodeAt(destination);
            if (fromNode != null && toNode != null) {
                setPath(net.routePath(fromNode, toNode));
            }
        }
        if (path == null || path.isEmpty()) return true;
        if (index + 1 < path.size()) {
            var from = path.get(index);
            var to = path.get(index + 1);

            // can move to next node
            if ((to.pos.equals(cartPos) || net.occupyNode(to.pos) && (from.pos.equals(cartPos) || net.occupyNode(from.pos)))) {
                float sl = getSpeedLerp();
                if (index == 0) {
                    if (this.gradientIn < 1) {
                        if (lerp == sl) {
                            lerp -= this.gradientIn;
                        }
                        lerp -= this.gradientIn;
                        this.gradientIn *= 1.5f;
                    } else {
                        lerp--;
                    }
                }  else {
                    lerp--;
                }


                float l = Mth.clamp((sl - lerp) / sl, 0, 1);

                var x = (from.pos.getX() + 0.5) * (1 - l) + (to.pos.getX() + 0.5) * l;
                var z = (from.pos.getZ() + 0.5) * (1 - l) + (to.pos.getZ() + 0.5) * l;

                var nextDegree = 0f;
                for (Direction side : AMHSRailNet.VALUES) {
                    if (from.pos.relative(side).equals(to.pos)) {
                        nextDegree = (side.getOpposite().get2DDataValue() * 90 + 180);
                        break;
                    }
                }

                cart.setPos(x, cart.getY(), z);
                float diff = (nextDegree - lastDegree) % 360;
                if (diff < -180.0F) {
                    diff += 360.0F;
                } else if (diff >= 180.0F) {
                    diff -= 360.0F;
                }
                cart.setYRot(lastDegree + diff * l);

                if (lerp <= 0) {
                    index++;
                    lerp = getSpeedLerp();
                    lastDegree += diff;
                }
            }
            return index + 1 >= path.size();
        } else {
            cart.setPos(destination.getX() + 0.5, cart.getY(), destination.getZ() + 0.5);
        }
        return true;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = super.serializeNBT();
        if (destination != null) {
            tag.put("dest", NbtUtils.writeBlockPos(destination));
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        super.deserializeNBT(compoundTag);
        if (compoundTag.contains("dest", Tag.TAG_COMPOUND)) {
            destination = NbtUtils.readBlockPos(compoundTag.getCompound("dest"));
        }
    }
}
