package br.edu.fatec.bd.database;
public class QueryResult<T>{private final String replica;private final T data;public QueryResult(String r,T d){replica=r;data=d;}public String getReplica(){return replica;}public T getData(){return data;}}
