package io.github.wy.sql.api;

public enum Order {
        DESC, ASC;
    
    @Override
    public String toString() {
        return " "+this.name()+" ";
    }
}
