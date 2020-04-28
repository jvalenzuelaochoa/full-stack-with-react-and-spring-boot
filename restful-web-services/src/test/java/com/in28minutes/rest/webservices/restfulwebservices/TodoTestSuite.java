package com.in28minutes.rest.webservices.restfulwebservices;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.List;
import java.util.ArrayList;
import java.util.Date;

import static org.mockito.Mockito.*;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.in28minutes.rest.webservices.restfulwebservices.todo.Todo;

import org.junit.Assert;


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

		// Simple hand-made Insertion test
		System.out.println("Test Create");

		Todo sample = new Todo(1,"Jesus", "First Post", new Date(), false);
		postTodo(sample);

		ArrayList<Todo> todoList = new ArrayList<>();
		todoList = getResponseObjects(getTodoString("Jesus"));
		debugPrintTodos(todoList);
		
		sample = new Todo(1,"Javier", "First Post", new Date(), false);
		postTodo(sample);

		sample = new Todo(2,"Javier", "Second Post", new Date(), false);
		postTodo(sample);

		sample.setId((long)4);
		postTodo(sample);

		System.out.println("Test Readback");
		todoList = getResponseObjects(getTodoString("Javier"));
		debugPrintTodos(todoList);
		
	}
	
	@Test
	public void testAddMultiple() throws Exception {

		System.out.println("Test Create");
		

		int numberOfUsers=5;

	    for(int u = 0 ; u < numberOfUsers; u++)
	    {
			
			String newUser = RandomStringUtils.randomAlphanumeric(25);
			    
			int numberOfTasks = 50;
	
			createRandomPosts(newUser, numberOfTasks);
	
			ArrayList<Todo> todoList = new ArrayList<>();
			todoList = getResponseObjects(getTodoString(newUser));
			
			Assert.assertEquals(numberOfTasks, todoList.size());
	    
	    }
		
	}
	
	@Test
	public void testAddInterleaved() throws Exception {

		System.out.println("Test Create");
		

		int numberOfUsers=5;

		int numberOfTasks = 50;

		// Create random users
		ArrayList<String> users = new ArrayList<>();

	    for(int u = 0 ; u < numberOfUsers; u++)
	    {
			users.add(RandomStringUtils.randomAlphanumeric(25));
			
	    }
	    
	    // Start execution
		for(int i = 0; i < numberOfTasks*numberOfUsers; i++)
		{
			// Create one random task and assign it to the interleaved user.
			createRandomPosts(users.get(i%numberOfUsers), 1);
	
			// Check that the list of items for all users stays consistent.
		    for(int u = 0 ; u < numberOfUsers; u++)
		    {
		    	ArrayList<Todo> todoList = new ArrayList<>();
		    	todoList = getResponseObjects(getTodoString(users.get(u)));
			
		    	Assert.assertEquals( i/numberOfUsers + ( i % numberOfUsers >= u? 1:0), todoList.size());
		    }
	    
	    }
		
	}
	
	@Test
	public void testDeletion() throws Exception {

		// Simple hand-made deletion test
		ArrayList<Todo> todoList = new ArrayList<>();
		todoList = getResponseObjects(getTodoString("Javier"));
		
		deletePost(todoList.get(0));

		todoList = getResponseObjects(getTodoString("Javier"));
		debugPrintTodos(todoList);
	}
	
	@Test
	public void deleteMultiple() throws Exception {

		int numberOfTasks = 50;

		createRandomPosts("deleteMultipleUser", numberOfTasks);
		
		int numberOfDeletes = numberOfTasks/3;
		
		ArrayList<Todo> todoList = new ArrayList<>();
		todoList = getResponseObjects(getTodoString("deleteMultipleUser"));

	    for(int i = 0 ; i < numberOfDeletes; i++)
	    {
			deletePost(todoList.get(i));
	    
	    }
	    
	    //Refresh with the remaining To-Do's
		todoList = getResponseObjects(getTodoString("deleteMultipleUser"));
		
		Assert.assertEquals(numberOfTasks-numberOfDeletes, todoList.size());
		
	}
	

	@Test
	public void testModify() throws Exception {
		// Simple hand-made Modify test
		ArrayList<Todo> todoList = new ArrayList<>();
		todoList = getResponseObjects(getTodoString("Javier"));
		
		Todo modified = new Todo(todoList.get(0));
		System.out.println(modified);
		modified.setDone(true);
		modified.setDescription("Modified Todo");
		modified.setTargetDate(new Date(690349616000L));
		System.out.println(modified);

		updateTodoItem(todoList.get(0), modified);
		
		todoList = getResponseObjects(getTodoString("Javier"));
		debugPrintTodos(todoList);
	}
	
	
	@Test
	public void testModifyMultiple() throws Exception {

		int numberOfTasks = 50;

		createRandomPosts("ModifyMultipleUser", numberOfTasks);
		
		int numberOfMods = numberOfTasks/3;
		
		ArrayList<Todo> todoList = new ArrayList<>();
		todoList = getResponseObjects(getTodoString("deleteMultipleUser"));

	    for(int i = 0 ; i < numberOfMods; i++)
	    {
	    	//Determine new values to be used in current To-Do
			String newDescription = RandomStringUtils.randomAlphanumeric(75);
			Date newDate = new Date(349616000L*i);
			
			Todo modified = new Todo(todoList.get(i));
			modified.setDescription(newDescription);
			modified.setTargetDate(newDate);
			modified.setDone(true);
			updateTodoItem(todoList.get(i), modified);

			// Re-fresh with any modified To-Dos
			ArrayList<Todo> updated = new ArrayList<>();
			updated = getResponseObjects(getTodoString("ModifyMultipleUser"));
			
			// Make sure that a modified item for the selected ID can still be found.
			Todo updatedItem = getTodo(todoList.get(i));
			Assert.assertNotEquals(null, updatedItem);

			// Check that the updated properties took effect.
			Assert.assertEquals(todoList.get(i).getId(), updatedItem.getId());
			Assert.assertEquals(newDescription, updatedItem.getDescription());
			Assert.assertEquals(newDate, updatedItem.getTargetDate());
			
			// Check that the amount of To-Do items for the user is unmodified
			Assert.assertEquals(numberOfTasks, updated.size());
			
	    }
		
	}
	
	@Test
	public void testGetSimple() throws Exception {

		String newDescription = RandomStringUtils.randomAlphanumeric(75);
		Date newDate = new Date(349616000L);
		Todo sample = new Todo(0, "SimpleTestUser", newDescription, newDate, false);
		postTodo(sample);
		
		ArrayList<Todo> todoList = new ArrayList<>();
		todoList = getResponseObjects(getTodoString("SimpleTestUser"));
		
		Assert.assertEquals(1, todoList.size());
		Assert.assertEquals(newDescription, todoList.get(0).getDescription());
		Assert.assertEquals(newDate, todoList.get(0).getTargetDate());
				
	}
	
	////////////////////////////////////////
	///	HELPER METHODS
	////////////////////////////////////////
	
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
	
	public void createRandomPosts(String username, int numberOfTasks) throws Exception {
		Todo sample = new Todo();
	    for(int i = 0 ; i < numberOfTasks; i++)
	    {
	    	String newDescription = RandomStringUtils.randomAlphanumeric(85);
			sample = new Todo(0, username, newDescription, new Date(), false);
			postTodo(sample);
	    }
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
	
	public Todo getTodo(Todo todo)
	{
		MvcResult result;
		try {
			result = mockMvc.perform( MockMvcRequestBuilders
				      .get("/jpa/users/{username}/todos/{id}",todo.getUsername(), todo.getId())
				      .accept(MediaType.APPLICATION_JSON))
				      .andExpect(status().isOk()).andReturn();

			Gson gson = new Gson();
			return gson.fromJson( result.getResponse().getContentAsString(), Todo.class);
			
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
	      .andExpect(status().isOk());
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