package core.settings;

public enum ApiEndpoints {
    PING("/ping"),
    BOOKING("/booking");

    private final String path;

    ApiEndpoints(String path) {

        this.path = path;
    }

    public String getPath() {
        return path;
    }

    // Метод для получения пути с ID
    public String getPathWithId(int id) {
        return path + "/" + id;
    }
}
