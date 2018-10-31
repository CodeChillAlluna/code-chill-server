package fr.codechill.spring.utils.docker;

public enum DockerActions {

    START ("start"),
    STOP ("stop"),
    PAUSE ("pause"),
    RESUME ("unpause"),
    STATS ("stats"),
    INSPECT ("json"),
    RESTART ("restart");
        
    private String action;
        
    DockerActions(String action) {
        this.action = action;
    }
        
    public String toString() {
        return action;
    }
}