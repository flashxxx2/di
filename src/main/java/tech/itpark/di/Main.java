package tech.itpark.di;

import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            final var container = new Container();
            container.register(Service.class, Repository.class);
            container.wire();
            System.out.println("finish");
        } catch (DIException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}

class Repository {
    public List<Object> getAll() {
        return Collections.emptyList();
    }
}

class Service {
    private final Repository repository;

    public Service(Repository repository) {
        this.repository = repository;
    }
}
