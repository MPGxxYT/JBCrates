package me.mortaldev.jbcrates.modules.crate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CrateManager {
    private static List<Crate> crateList;

    public static void updateCratesList(){
        crateList = new ArrayList<>();
        File mainPath = new File(CrateCRUD.getMainFilePath());
        if (!mainPath.exists()) {
            return;
        }
        for (File file : mainPath.listFiles()) {
            Crate crate = CrateCRUD.getCrate(file.getName().replace(".json", ""));
            crateList.add(crate);
        }
    }

    public static List<Crate> getCrates() {
        if (crateList == null) {
            updateCratesList();
        }
        return crateList;
    }

    public static List<Crate> addCrate(Crate crate){
        crateList.add(crate);
        return crateList;
    }
}
