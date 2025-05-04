package loftily.gui.animation;

import lombok.Getter;

import java.util.function.Function;

import static java.lang.Math.*;

@Getter
public enum Easing {
    Linear(x -> x),
    
    EaseInSine(x -> 1 - cos((x * PI) / 2)),
    EaseOutSine(x -> sin((x * PI) / 2)),
    EaseInOutSine(x -> -(cos(PI * x) - 1) / 2),
    
    EaseInQuad(x -> x * x),
    EaseOutQuad(x -> 1 - (1 - x) * (1 - x)),
    EaseInOutQuad(x -> x < 0.5 ? 2 * x * x : 1 - pow(-2 * x + 2, 2) / 2),
    
    EaseInCubic(x -> x * x * x),
    EaseOutCubic(x -> 1 - pow(1 - x, 3)),
    EaseInOutCubic(x -> x < 0.5 ? 4 * x * x * x : 1 - pow(-2 * x + 2, 3) / 2),
    
    EaseInQuart(x -> x * x * x * x),
    EaseOutQuart(x -> 1 - pow(1 - x, 4)),
    EaseInOutQuart(x -> x < 0.5 ? 8 * x * x * x * x : 1 - pow(-2 * x + 2, 4) / 2),
    
    EaseInQuint(x -> x * x * x * x * x),
    EaseOutQuint(x -> x == 1 ? 1 : 1 - pow(2, -10 * x)),
    EaseInOutQuint(x -> x < 0.5 ? 16 * x * x * x * x * x : 1 - pow(-2 * x + 2, 5) / 2),
    
    EaseInExpo(x -> x == 0 ? 0 : pow(2, 10 * x - 10)),
    EaseOutExpo(x -> x == 1 ? 1 : 1 - pow(2, -10 * x)),
    EaseInOutExpo(x -> x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? pow(2, 20 * x - 10) / 2 : (2 - pow(2, -20 * x + 10)) / 2),
    
    EaseInCirc(x -> 1 - sqrt(1 - pow(x, 2))),
    EaseOutCirc(x -> sqrt(1 - pow(x - 1, 2))),
    EaseInOutCirc(x -> x < 0.5 ? (1 - sqrt(1 - pow(2 * x, 2))) / 2 : (sqrt(1 - pow(-2 * x + 2, 2)) + 1) / 2),
    
    EaseInBack(x -> 2.70158 * x * x * x - 1.70158 * x * x),
    EaseOutBack(x -> 1 + 2.70158 * pow(x - 1, 3) + 1.70158 * pow(x - 1, 2)),
    EaseInOutBack(x -> x < 0.5 ? (pow(2 * x, 2) * ((1.70158 * 1.525 + 1) * 2 * x - 1.70158 * 1.525)) / 2 : (pow(2 * x - 2, 2) * ((1.70158 * 1.525 + 1) * (x * 2 - 2) + 1.70158 * 1.525) + 2) / 2),
    
    EaseInElastic(x -> x == 0 ? 0 : x == 1 ? 1 : -pow(2, 10 * x - 10) * sin((x * 10 - 10.75) * (2 * PI) / 3)),
    EaseOutElastic(x -> x == 0 ? 0 : x == 1 ? 1 : pow(2, -10 * x) * sin((x * 10 - 0.75) * (2 * PI) / 3) + 1),
    EaseInOutElastic(x -> x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? -(pow(2, 20 * x - 10) * sin((20 * x - 11.125) * (2 * PI) / 4.5)) / 2 : (pow(2, -20 * x + 10) * sin((20 * x - 11.125) * (2 * PI) / 4.5)) / 2 + 1),
    
    EaseOutBounce(x -> x < 1 / 2.75 ? 7.5625 * x * x : x < 2 / 2.75 ? 7.5625 * (x -= 1.5 / 2.75) * x + 0.75 : x < 2.5 / 2.75 ? 7.5625 * (x -= 2.25 / 2.75) * x + 0.9375 : 7.5625 * (x -= 2.625 / 2.75) * x + 0.984375),
    EaseInBounce(x -> 1 - EaseOutBounce.getFunction().apply(1 - x)),
    EaseInOutBounce(x -> x < 0.5 ? (1 - EaseOutBounce.getFunction().apply(1 - 2 * x)) / 2 : (1 + EaseOutBounce.getFunction().apply(2 * x - 1)) / 2);
    
    private final Function<Double, Double> function;
    
    Easing(Function<Double, Double> function) {
        this.function = function;
    }
}