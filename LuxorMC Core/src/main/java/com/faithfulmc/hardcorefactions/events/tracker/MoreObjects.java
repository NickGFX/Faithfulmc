package com.faithfulmc.hardcorefactions.events.tracker;

import com.google.common.base.Preconditions;
import net.minecraft.util.com.google.common.annotations.GwtCompatible;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.Arrays;

@GwtCompatible
public final class MoreObjects {
    @CheckReturnValue
    public static <T> T firstNonNull(@Nullable final T first, @Nullable final T second) {
        return (T) ((first != null) ? first : Preconditions.checkNotNull((Object) second));
    }

    @CheckReturnValue
    public static ToStringHelper toStringHelper(final Object self) {
        return new ToStringHelper(self.getClass().getSimpleName());
    }

    @CheckReturnValue
    public static ToStringHelper toStringHelper(final Class<?> clazz) {
        return new ToStringHelper(clazz.getSimpleName());
    }

    @CheckReturnValue
    public static ToStringHelper toStringHelper(final String className) {
        return new ToStringHelper(className);
    }

    public static final class ToStringHelper {
        private final String className;
        private ValueHolder holderHead;
        private ValueHolder holderTail;
        private boolean omitNullValues;

        private ToStringHelper(final String className) {
            this.holderHead = new ValueHolder();
            this.holderTail = this.holderHead;
            this.omitNullValues = false;
            this.className = (String) Preconditions.checkNotNull((Object) className);
        }

        public ToStringHelper omitNullValues() {
            this.omitNullValues = true;
            return this;
        }

        public ToStringHelper add(final String name, @Nullable final Object value) {
            return this.addHolder(name, value);
        }

        public ToStringHelper add(final String name, final boolean value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper add(final String name, final char value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper add(final String name, final double value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper add(final String name, final float value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper add(final String name, final int value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper add(final String name, final long value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper addValue(@Nullable final Object value) {
            return this.addHolder(value);
        }

        public ToStringHelper addValue(final boolean value) {
            return this.addHolder(String.valueOf(value));
        }

        public ToStringHelper addValue(final char value) {
            return this.addHolder(String.valueOf(value));
        }

        public ToStringHelper addValue(final double value) {
            return this.addHolder(String.valueOf(value));
        }

        public ToStringHelper addValue(final float value) {
            return this.addHolder(String.valueOf(value));
        }

        public ToStringHelper addValue(final int value) {
            return this.addHolder(String.valueOf(value));
        }

        public ToStringHelper addValue(final long value) {
            return this.addHolder(String.valueOf(value));
        }

        @CheckReturnValue
        @Override
        public String toString() {
            final boolean omitNullValuesSnapshot = this.omitNullValues;
            String nextSeparator = "";
            final StringBuilder builder = new StringBuilder(32).append(this.className).append('{');
            for (ValueHolder valueHolder = this.holderHead.next; valueHolder != null; valueHolder = valueHolder.next) {
                final Object value = valueHolder.value;
                if (!omitNullValuesSnapshot || value != null) {
                    builder.append(nextSeparator);
                    nextSeparator = ", ";
                    if (valueHolder.name != null) {
                        builder.append(valueHolder.name).append('=');
                    }
                    if (value != null && value.getClass().isArray()) {
                        final Object[] objectArray = {value};
                        final String arrayString = Arrays.deepToString(objectArray);
                        builder.append(arrayString.substring(1, arrayString.length() - 1));
                    } else {
                        builder.append(value);
                    }
                }
            }
            return builder.append('}').toString();
        }

        private ValueHolder addHolder() {
            final ValueHolder valueHolder = new ValueHolder();
            final ValueHolder holderTail = this.holderTail;
            final ValueHolder valueHolder2 = valueHolder;
            holderTail.next = valueHolder2;
            this.holderTail = valueHolder2;
            return valueHolder;
        }

        private ToStringHelper addHolder(@Nullable final Object value) {
            final ValueHolder valueHolder = this.addHolder();
            valueHolder.value = value;
            return this;
        }

        private ToStringHelper addHolder(final String name, @Nullable final Object value) {
            final ValueHolder valueHolder = this.addHolder();
            valueHolder.value = value;
            valueHolder.name = (String) Preconditions.checkNotNull((Object) name);
            return this;
        }

        private static final class ValueHolder {
            String name;
            Object value;
            ValueHolder next;
        }
    }
}