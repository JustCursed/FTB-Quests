package com.feed_the_beast.ftbquests.quest.theme.property;

import net.minecraft.util.math.MathHelper;

/**
 * @author LatvianModder
 */
public class DoubleProperty extends ThemeProperty<Double> {
    public final double min;
    public final double max;

    public DoubleProperty(String n, double mn, double mx) {
        super(n);
        min = mn;
        max = mx;
    }

    public DoubleProperty(String n) {
        this(n, 0D, 1D);
    }

    @Override
    public Double parse(String string) {
        try {
            double i = Double.parseDouble(string);
            return MathHelper.clamp(i, min, max);
        } catch (Exception ignored) {
        }

        return null;
    }
}