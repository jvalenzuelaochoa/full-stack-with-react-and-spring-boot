package com.in28minutes.rest.webservices.restfulwebservices;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.List;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.in28minutes.rest.webservices.restfulwebservices.todo.Todo;


public class TodoTestSuite extends RestfulWebServicesApplicationTests {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
	
	@Test
	public void testAddTodo() throws Exception {

		System.out.println("Test Create");
		Todo sample = new Todo(1,"Javier", "First Post", new Date(), false);
		postTodo(sample);

		sample = new Todo(2,"Javier", "Second Post", new Date(), false);
		postTodo(sample);
		

		System.out.println("Test Readback");
		
		ArrayList<Todo> todoList = new ArrayList<>();
		todoList = getResponseObjects(getTodoString("Javier"));
		debugPrintTodos(todoList);
		
	}
	
	@Test
	public void testDeletion() throws Exception {

		ArrayList<Todo> todoList = new ArrayList<>();
		todoList = getResponseObjects(getTodoString("Javier"));
		
		deletePost(todoList.get(0));

		todoList = getResponseObjects(getTodoString("Javier"));
		debugPrintTodos(todoList);
	}
	
	public void deletePost(Todo todo) throws Exception {
		mockMvc.perform( MockMvcRequestBuilders
			      .delete("/jpa/users/{username}/todos/{id}", todo.getUsername(), todo.getId()));
	}
	
	public void postTodo(Todo todoIn) throws Exception {

		mockMvc.perform( MockMvcRequestBuilders
			      .post("/jpa/users/{username}/todos", todoIn.getUsername())
			      .content(asJsonString(todoIn))
			      .contentType(MediaType.APPLICATION_JSON)
			      .accept(MediaType.APPLICATION_JSON));
	}
	
	public void debugPrintTodos(ArrayList<Todo> todoList)
	{
		for(Todo x: todoList) {
			System.out.println(x);
		}
	}

	public String getTodoString(String username)
	{
		MvcResult result;
		try {
			result = mockMvc.perform( MockMvcRequestBuilders
				      .get("/jpa/users/{username}/todos",username)
				      .accept(MediaType.APPLICATION_JSON))
				      .andExpect(status().isOk()).andReturn();
			return result.getResponse().getContentAsString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void updateTodoItem(Todo todoIn,Todo todoOut) throws Exception 
	{
		mockMvc.perform( MockMvcRequestBuilders
	      .put("/jpa/users/{username}/todos/{id}", todoIn.getUsername(), todoIn.getId())
	      .content(asJsonString(new Todo(todoIn.getId(),
	    		  						 todoIn.getUsername(),
	    		  						 todoOut.getDescription(),
	    		  						 todoOut.getTargetDate(),
	    		  						 todoOut.isDone())))   
	      .contentType(MediaType.APPLICATION_JSON)
	      .accept(MediaType.APPLICATION_JSON))
	      .andExpect(status().isOk())
	      .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("firstName2"))
	      .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("lastName2"))
	      .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("email2@mail.com"));
	}

	
	public ArrayList<Todo> getResponseObjects(String jsonString)
	{	
		JSONArray jsonarray;
		ArrayList<Todo> todoList = new ArrayList<>();
		try {
			jsonarray = new JSONArray(jsonString);
			for (int i = 0; i < jsonarray.length(); i++)
			{
			    JSONObject jsonobject = jsonarray.getJSONObject(i);
				Gson gson = new Gson();
			    todoList.add(gson.fromJson(jsonobject.toString(), Todo.class));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return todoList;
	}
	
	public static String asJsonString(final Object obj) {
	    try {
	        return new ObjectMapper().writeValueAsString(obj);
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}

}