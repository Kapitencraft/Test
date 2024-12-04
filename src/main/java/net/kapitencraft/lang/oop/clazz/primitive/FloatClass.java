package net.kapitencraft.lang.oop.clazz.primitive;

import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.lang.run.algebra.OperationType;

public class FloatClass extends PrimitiveClass {
    public FloatClass() {
        super(VarTypeManager.NUMBER, "float", 0f);
    }

    @Override
    public LoxClass checkOperation(OperationType type, Operand operand, LoxClass other) {
        return other.is(VarTypeManager.NUMBER) ? other : null;
    }

    @Override
    public Object doOperation(OperationType type, Operand operand, Object self, Object other) {
        float val = (float) self;
        if (type == OperationType.ADDITION) {
            if (other instanceof Float f) {
                return val + f;
            }
            if (other instanceof Double d) {
                return val + d;
            }
            if (other instanceof Integer i) {
                return val + i;
            }
        }
        if (type == OperationType.SUBTRACTION) {
            if (other instanceof Float f) {
                return val - f;
            }
            if (other instanceof Double d) {
                return val - d;
            }
            if (other instanceof Integer i) {
                return val - i;
            }
        }
        if (type == OperationType.MULTIPLICATION) {
            if (other instanceof Float f) {
                return val * f;
            }
            if (other instanceof Double d) {
                return val * d;
            }
            if (other instanceof Integer i) {
                return val * i;
            }
        }
        if (type == OperationType.DIVISION) {
            if (other instanceof Float f) {
                return val / f;
            }
            if (other instanceof Double d) {
                return val / d;
            }
            if (other instanceof Integer i) {
                return val / i;
            }
        }
        if (type == OperationType.MODULUS) {
            if (other instanceof Float f) {
                return val % f;
            }
            if (other instanceof Double d) {
                return val % d;
            }
            if (other instanceof Integer i) {
                return val % i;
            }
        }
        if (type == OperationType.POTENCY) {
            return Math.pow(val, ((Number) other).doubleValue());
        }
        if (type == OperationType.LEQUAL) {
            if (other instanceof Float f) {
                return val <= f;
            }
            if (other instanceof Double d) {
                return val <= d;
            }
            if (other instanceof Integer i) {
                return val <= i;
            }
        }
        if (type == OperationType.NEQUAL) {
            if (other instanceof Float f) {
                return val != f;
            }
            if (other instanceof Double d) {
                return val != d;
            }
            if (other instanceof Integer i) {
                return val != i;
            }
        }
        if (type == OperationType.GEQUAL) {
            if (other instanceof Float f) {
                return val >= f;
            }
            if (other instanceof Double d) {
                return val >= d;
            }
            if (other instanceof Integer i) {
                return val >= i;
            }
        }
        if (type == OperationType.LESS) {
            if (other instanceof Float f) {
                return val < f;
            }
            if (other instanceof Double d) {
                return val < d;
            }
            if (other instanceof Integer i) {
                return val < i;
            }
        }
        if (type == OperationType.MORE) {
            if (other instanceof Float f) {
                return val > f;
            }
            if (other instanceof Double d) {
                return val > d;
            }
            if (other instanceof Integer i) {
                return val > i;
            }
        }
        if (type == OperationType.EQUAL) {
            if (other instanceof Float f) {
                return val == f;
            }
            if (other instanceof Double d) {
                return val == d;
            }
            if (other instanceof Integer i) {
                return val == i;
            }
        }
        return null;
    }
}
