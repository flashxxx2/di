package tech.itpark.di;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class Container {
    private final Map<Class<?>, Object> objects = new HashMap<>();
    private final Set<Class<?>> definitions = new HashSet<>();

    public void register(Class<?>... definitions) {
        String badDefinitions = Arrays.stream(definitions)
                .filter(o -> o.getDeclaredConstructors().length != 1)
                .map(o -> o.getName())
                .collect(Collectors.joining(", "));
        if (!badDefinitions.isEmpty()) {
            throw new AmbiguousConstructorException(badDefinitions);
        }

        this.definitions.addAll(Arrays.asList(definitions));
    }

    public void wire() {
        HashSet<Class<?>> todo = new HashSet<>(definitions);
        if (todo.size() == 0) {
            return;
        }
        int size = todo.size();
        // -> .. -> .. -> .. ->
        for (int i = 0; i < size; i++) {

            final var generation = todo.stream() // lazy
                    .map(o -> o.getDeclaredConstructors()[0])
                    .filter(o -> objects.keySet()
                            .containsAll(Arrays.asList(o.getParameterTypes())))
                    .map(o -> {
                        try {
                            o.setAccessible(true);
                            return o.newInstance(Arrays.stream(o.getParameters())
                                    .map(p -> objects.get(p.getType()))
                                    .toArray()
                            );
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            throw new ObjectInstantiationException(e); // <- e //
                        }
                    })
                    .collect(Collectors.toMap(o -> o.getClass(), o -> o));
            objects.putAll(generation);
            todo.removeAll(generation.keySet());
            System.out.println(generation);
            if (todo.size() == 0) {
                return;
            }

            if (generation.size() == 0) {
                // sad path
                String unmet = todo.stream()
                        .map(o -> o.getName())
                        .collect(Collectors.joining(", "));
                throw new UnmetDependenciesException(unmet);
            }
        }
    }
}
