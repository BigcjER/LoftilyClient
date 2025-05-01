package loftily.value.impl.mode;

import loftily.core.AbstractModule;
import loftily.module.Module;
import loftily.value.Value;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Getter
public class ModeValue extends Value<Mode> {

    private final List<Mode> modes;
    private final AbstractModule parent;

    /**
     * @param parent the parent module (typically just 'this')
     */
    public ModeValue(String name, String value, AbstractModule parent, Mode... modes) {
        super(name, Arrays.stream(modes)
                .filter(mode -> mode.getName().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Mode '%s' not found!", value))));
        this.modes = Arrays.asList(modes);
        this.parent = parent;
    }


    public void update(Mode value) {
        if (getValue().getParent() != null && getValue().getParent().isToggled()) {
            getValue().unregister();
            setValue(value);
            getValue().register();
        } else {
            setValue(value);
        }
    }

    public String getValueByName() {
        return getValue().getName();
    }

    public boolean is(String mode) {
        return mode.equalsIgnoreCase(getValueByName());
    }


    public void initModes() {
        for (Mode mode : modes) {
            mode.setParent(parent instanceof Module ? (Module) parent : null);

            Field[] fields = mode.getClass().getDeclaredFields();

            /* Add the value of a sub mode */
            for (Field field : fields) {
                if (Value.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        mode.getValues().add((Value<?>) field.get(mode));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

            parent.getValues().addAll(mode.getValues());
        }

        /* Set the visible of a sub mode */
        modes.forEach(mode -> mode.getValues().forEach(value -> {
            Supplier<Boolean> visible = value.getVisible();

            value.setVisible(visible == null
                    ? () -> mode == this.getValue()
                    : () -> visible.get() && (mode == this.getValue()));
        }));
    }
}
