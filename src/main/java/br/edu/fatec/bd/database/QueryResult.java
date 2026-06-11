package br.edu.fatec.bd.database;

public class QueryResult<T> {

    private final String replica;
    private final T data;

    public QueryResult(String replica, T data) {
        this.replica = replica;
        this.data    = data;
    }

    public String getReplica() {
        return replica;
    }

    public T getData() {
        return data;
    }
}
