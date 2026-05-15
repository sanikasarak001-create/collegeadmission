package com.example.collegeadmission;

public class College {
    private String id;
    private String name;
    private String location;
    private String state;
    private String type;
    private String courses;
    private String description;
    private double cutoffGeneral;
    private double cutoffOBC;
    private double cutoffSC;
    private double cutoffST;

    public College() {
        // Required for Firebase deserialization
    }

    public College(String id, String name, String location, String state,
                   String type, String courses, String description,
                   double cutoffGeneral, double cutoffOBC,
                   double cutoffSC, double cutoffST) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.state = state;
        this.type = type;
        this.courses = courses;
        this.description = description;
        this.cutoffGeneral = cutoffGeneral;
        this.cutoffOBC = cutoffOBC;
        this.cutoffSC = cutoffSC;
        this.cutoffST = cutoffST;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getState() { return state; }
    public String getType() { return type; }
    public String getCourses() { return courses; }
    public String getDescription() { return description; }
    public double getCutoffGeneral() { return cutoffGeneral; }
    public double getCutoffOBC() { return cutoffOBC; }
    public double getCutoffSC() { return cutoffSC; }
    public double getCutoffST() { return cutoffST; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setState(String state) { this.state = state; }
    public void setType(String type) { this.type = type; }
    public void setCourses(String courses) { this.courses = courses; }
    public void setDescription(String description) { this.description = description; }
    public void setCutoffGeneral(double cutoffGeneral) { this.cutoffGeneral = cutoffGeneral; }
    public void setCutoffOBC(double cutoffOBC) { this.cutoffOBC = cutoffOBC; }
    public void setCutoffSC(double cutoffSC) { this.cutoffSC = cutoffSC; }
    public void setCutoffST(double cutoffST) { this.cutoffST = cutoffST; }

    public double getCutoffForCategory(String category) {
        if (category == null) return cutoffGeneral;
        switch (category) {
            case "OBC": return cutoffOBC;
            case "SC":  return cutoffSC;
            case "ST":  return cutoffST;
            default:    return cutoffGeneral;
        }
    }
}