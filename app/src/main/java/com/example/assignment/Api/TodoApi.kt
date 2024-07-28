package com.example.assignment.Api

import com.example.assignment.Model.Todo
import com.example.assignment.Model.TodoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TodoApi {
    @GET("todos")
    suspend fun getTodos(
        @Query("limit") limit: Int,
        @Query("skip") skip: Int
    ): TodoResponse

    @POST("todos/add")
    suspend fun addTodo(
        @Body newTodo: Todo
    ): Response<Todo>

    @PUT("todos/{id}")
    suspend fun updateTodo(
        @Path("id") id: Int,
        @Body updatedTodo: Todo
    ): Response<Todo>

    @DELETE("todos/{id}")
    suspend fun deleteTodo(
        @Path("id") id: Int
    ): Response<Unit>
}
