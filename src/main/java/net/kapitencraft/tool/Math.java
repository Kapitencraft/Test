package net.kapitencraft.tool;

public class Math {

    public static Number mergeSub(Object a, Object b) {
        if (a instanceof Double aD && b instanceof Double bD) {
            return aD - bD;
        } else if (a instanceof Integer aI && b instanceof Double bD) {
            return aI - bD;
        } else if (a instanceof Double aD && b instanceof Integer bI) {
            return aD - bI;
        } else {
            return (int)a - (int) b;
        }
    }

    public static Number mergeMul(Object a, Object b) {
        if (a instanceof Double aD && b instanceof Double bD) {
            return aD * bD;
        } else if (a instanceof Integer aI && b instanceof Double bD) {
            return aI * bD;
        } else if (a instanceof Double aD && b instanceof Integer bI) {
            return aD * bI;
        } else {
            return (int)a * (int) b;
        }
    }

    public static Number mergeDiv(Object a, Object b) {
        if (a instanceof Double aD && b instanceof Double bD) {
            return aD / bD;
        } else if (a instanceof Integer aI && b instanceof Double bD) {
            return aI / bD;
        } else if (a instanceof Double aD && b instanceof Integer bI) {
            return aD + bI;
        } else {
            return (int)a / (int) b;
        }
    }

    public static Number mergeMod(Object a, Object b) {
        if (a instanceof Double aD && b instanceof Double bD) {
            return aD % bD;
        } else if (a instanceof Integer aI && b instanceof Double bD) {
            return aI % bD;
        } else if (a instanceof Double aD && b instanceof Integer bI) {
            return aD % bI;
        } else {
            return (int)a % (int) b;
        }
    }

    public static Number mergeAdd(Object a, Object b) {
        if (a instanceof Double aD && b instanceof Double bD) {
            return aD + bD;
        } else if (a instanceof Integer aI && b instanceof Double bD) {
            return aI + bD;
        } else if (a instanceof Double aD && b instanceof Integer bI) {
            return aD + bI;
        } else {
            return (int)a + (int)b;
        }
    }
}
