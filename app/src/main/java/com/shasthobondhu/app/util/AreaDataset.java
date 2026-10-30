package com.shasthobondhu.app.util;

import java.util.ArrayList;
import java.util.List;

public class AreaDataset {
    public static class Area {
        public String name;
        public List<Area> children;

        public Area(String name) { this.name = name; this.children = new ArrayList<>(); }
        public Area(String name, List<Area> children) { this.name = name; this.children = children; }
    }

    public static List<Area> getDivisions() {
        List<Area> divisions = new ArrayList<>();
        
        // Dhaka Division
        Area dhakaDiv = new Area("Dhaka");
        List<Area> dhakaDistricts = new ArrayList<>();
        Area dhakaDist = new Area("Dhaka");
        dhakaDist.children.add(new Area("Mirpur"));
        dhakaDist.children.add(new Area("Uttara"));
        dhakaDist.children.add(new Area("Gulshan"));
        dhakaDistricts.add(dhakaDist);
        
        Area gazipurDist = new Area("Gazipur");
        gazipurDist.children.add(new Area("Sreepur"));
        gazipurDist.children.add(new Area("Kapasia"));
        dhakaDistricts.add(gazipurDist);
        
        dhakaDiv.children = dhakaDistricts;
        divisions.add(dhakaDiv);
        
        // Chittagong Division
        Area ctgDiv = new Area("Chattogram");
        List<Area> ctgDistricts = new ArrayList<>();
        Area ctgDist = new Area("Chattogram");
        ctgDist.children.add(new Area("Pahartali"));
        ctgDist.children.add(new Area("Kotwali"));
        ctgDistricts.add(ctgDist);
        ctgDiv.children = ctgDistricts;
        divisions.add(ctgDiv);
        
        return divisions;
    }
}
