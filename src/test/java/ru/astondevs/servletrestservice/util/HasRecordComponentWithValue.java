package ru.astondevs.servletrestservice.util;

import org.hamcrest.*;
import org.testcontainers.shaded.org.hamcrest.beans.PropertyUtil;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

public class HasRecordComponentWithValue<T> extends TypeSafeDiagnosingMatcher<T> {
    private static final Condition.Step<RecordComponent, Method> WITH_READ_METHOD = withReadMethod();
    private final String componentName;
    private final Matcher<Object> valueMatcher;

    public HasRecordComponentWithValue(String componentName, Matcher<?> valueMatcher) {
        this.componentName = componentName;
        this.valueMatcher = nastyGenericsWorkaround(valueMatcher);
    }

    @Override
    public boolean matchesSafely(T bean, Description mismatch) {
        return recordComponentOn(bean, mismatch)
                .and(WITH_READ_METHOD)
                .and(withPropertyValue(bean))
                .matching(valueMatcher, "record component'" + componentName + "' ");
    }

    private Condition.Step<Method, Object> withPropertyValue(final T bean) {
        return new Condition.Step<Method, Object>() {
            @Override
            public Condition<Object> apply(Method readMethod, Description mismatch) {
                try {
                    return Condition.matched(readMethod.invoke(bean, PropertyUtil.NO_ARGUMENTS), mismatch);
                } catch (Exception e) {
                    mismatch.appendText(e.getMessage());
                    return Condition.notMatched();
                }
            }
        };
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("hasRecordComponent(").appendValue(componentName).appendText(", ")
                .appendDescriptionOf(valueMatcher).appendText(")");
    }

    private Condition<RecordComponent> recordComponentOn(T bean, Description mismatch) {
        RecordComponent[] recordComponents = bean.getClass().getRecordComponents();
        for (RecordComponent comp : recordComponents) {
            if (comp.getName().equals(componentName)) {
                return Condition.matched(comp, mismatch);
            }
        }
        mismatch.appendText("No record component \"" + componentName + "\"");
        return Condition.notMatched();
    }


    @SuppressWarnings("unchecked")
    private static Matcher<Object> nastyGenericsWorkaround(Matcher<?> valueMatcher) {
        return (Matcher<Object>) valueMatcher;
    }

    private static Condition.Step<RecordComponent, Method> withReadMethod() {
        return new Condition.Step<RecordComponent, Method>() {
            @Override
            public Condition<Method> apply(RecordComponent property, Description mismatch) {
                final Method readMethod = property.getAccessor();
                if (null == readMethod) {
                    mismatch.appendText("record component \"" + property.getName() + "\" is not readable");
                    return Condition.notMatched();
                }
                return Condition.matched(readMethod, mismatch);
            }
        };
    }

    @Factory
    public static <T> Matcher<T> hasRecordComponent(String componentName, Matcher<?> valueMatcher) {
        return new HasRecordComponentWithValue<T>(componentName, valueMatcher);
    }
}
