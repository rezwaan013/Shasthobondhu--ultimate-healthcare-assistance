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
        divisions.add(new Area("Dhaka"));
        divisions.add(new Area("Chattogram"));
        divisions.add(new Area("Rajshahi"));
        divisions.add(new Area("Khulna"));
        divisions.add(new Area("Barishal"));
        divisions.add(new Area("Sylhet"));
        divisions.add(new Area("Rangpur"));
        divisions.add(new Area("Mymensingh"));
        return divisions;
    }
}
