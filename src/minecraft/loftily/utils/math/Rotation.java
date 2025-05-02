package loftily.utils.math;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Rotation {
    public float yaw, pitch;
    
    @Override
    public String toString() {
        return String.format("Yaw:%.2f, Pitch:%.2f", yaw, pitch);
    }
    
    public Rotation add(float yaw, float pitch) {
        this.yaw += yaw;
        this.pitch += pitch;
        return this;
    }
}